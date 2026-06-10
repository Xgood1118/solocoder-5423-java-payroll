package com.hrpayroll.bank.service;

import com.hrpayroll.bank.entity.BankPaymentDetail;
import com.hrpayroll.bank.entity.BankPaymentFile;
import com.hrpayroll.bank.repository.BankPaymentFileRepository;
import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.service.PayrollCalculationService;
import com.hrpayroll.common.BusinessException;
import com.hrpayroll.common.PayrollUtils;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BankPaymentService {

    @Autowired
    private BankPaymentFileRepository bankPaymentFileRepository;

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    @Autowired
    private EmployeeService employeeService;

    private final ConcurrentHashMap<String, Boolean> generationLocks = new ConcurrentHashMap<>();

    public BankPaymentFile generatePaymentFile(String yearMonth, String bankType, String format,
                                               String operator) {
        String lockKey = buildLockKey(yearMonth, bankType, format);

        Boolean previous = generationLocks.putIfAbsent(lockKey, Boolean.TRUE);
        if (previous != null && previous) {
            throw new BusinessException("该月份银行发放文件正在生成中，请稍后再试");
        }

        try {
            List<PayrollCalculation> approvedCalcs = payrollCalculationService.listByMonth(yearMonth)
                    .stream()
                    .filter(c -> "APPROVED".equals(c.getReviewStatus()))
                    .collect(Collectors.toList());

            if (approvedCalcs.isEmpty()) {
                throw new BusinessException("该月份没有已通过复核的薪资记录");
            }

            List<BankPaymentDetail> details = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (PayrollCalculation calc : approvedCalcs) {
                Employee employee = employeeService.getById(calc.getEmployeeId());
                BankPaymentDetail detail = new BankPaymentDetail();
                detail.setEmployeeNo(calc.getEmployeeNo());
                detail.setEmployeeName(calc.getEmployeeName());
                detail.setBankAccount(employee.getBankAccount());
                detail.setBankName(employee.getBankName());
                detail.setAmount(PayrollUtils.setScale(calc.getNetPay()));
                detail.setRemark(yearMonth + "工资");
                details.add(detail);
                totalAmount = totalAmount.add(calc.getNetPay());
            }

            details.sort(Comparator.comparing(BankPaymentDetail::getEmployeeNo));

            BankPaymentFile fileRecord = new BankPaymentFile();
            String fileId = PayrollUtils.generateId();
            fileRecord.setFileId(fileId);
            fileRecord.setYearMonth(yearMonth);
            fileRecord.setBankType(bankType);
            fileRecord.setFileFormat(format);
            fileRecord.setFileName(generateFileName(yearMonth, bankType, format));
            fileRecord.setTotalCount(details.size());
            fileRecord.setTotalAmount(PayrollUtils.setScale(totalAmount));
            fileRecord.setStatus("GENERATED");
            fileRecord.setGenerateTime(LocalDateTime.now());
            fileRecord.setGeneratedBy(operator);

            bankPaymentFileRepository.save(fileRecord);

            return fileRecord;
        } finally {
            generationLocks.remove(lockKey);
        }
    }

    public byte[] downloadPaymentFile(String fileId) {
        BankPaymentFile fileRecord = bankPaymentFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("银行发放文件不存在"));

        List<PayrollCalculation> approvedCalcs = payrollCalculationService
                .listByMonth(fileRecord.getYearMonth())
                .stream()
                .filter(c -> "APPROVED".equals(c.getReviewStatus()))
                .collect(Collectors.toList());

        List<BankPaymentDetail> details = new ArrayList<>();
        for (PayrollCalculation calc : approvedCalcs) {
            Employee employee = employeeService.getById(calc.getEmployeeId());
            BankPaymentDetail detail = new BankPaymentDetail();
            detail.setEmployeeNo(calc.getEmployeeNo());
            detail.setEmployeeName(calc.getEmployeeName());
            detail.setBankAccount(employee.getBankAccount());
            detail.setBankName(employee.getBankName());
            detail.setAmount(PayrollUtils.setScale(calc.getNetPay()));
            detail.setRemark(fileRecord.getYearMonth() + "工资");
            details.add(detail);
        }

        details.sort(Comparator.comparing(BankPaymentDetail::getEmployeeNo));

        if ("csv".equalsIgnoreCase(fileRecord.getFileFormat())) {
            return generateCsvContent(details, fileRecord.getTotalAmount());
        } else {
            return generateTxtContent(details, fileRecord.getTotalAmount());
        }
    }

    private byte[] generateCsvContent(List<BankPaymentDetail> details, BigDecimal totalAmount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            writer.write("序号,工号,姓名,银行账号,开户行,金额,备注\n");

            int seq = 1;
            for (BankPaymentDetail detail : details) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                        seq++,
                        detail.getEmployeeNo(),
                        detail.getEmployeeName(),
                        detail.getBankAccount(),
                        detail.getBankName(),
                        detail.getAmount(),
                        detail.getRemark()));
            }

            writer.write(String.format("\n合计,%d笔,,,,%s,\n",
                    details.size(), totalAmount.setScale(2)));

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成 CSV 文件失败: " + e.getMessage(), e);
        }
    }

    private byte[] generateTxtContent(List<BankPaymentDetail> details, BigDecimal totalAmount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            for (BankPaymentDetail detail : details) {
                writer.write(String.format("%-10s %-10s %-20s %-20s %15.2f %s\n",
                        detail.getEmployeeNo(),
                        detail.getEmployeeName(),
                        detail.getBankAccount(),
                        detail.getBankName(),
                        detail.getAmount(),
                        detail.getRemark()));
            }

            writer.write(String.format("\n合计: %d 笔, 总金额: %.2f 元\n",
                    details.size(), totalAmount.setScale(2)));

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成 TXT 文件失败: " + e.getMessage(), e);
        }
    }

    public BankPaymentFile getFile(String fileId) {
        return bankPaymentFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("银行发放文件不存在"));
    }

    public List<BankPaymentFile> listByMonth(String yearMonth) {
        return bankPaymentFileRepository.findByYearMonth(yearMonth);
    }

    public List<BankPaymentFile> listByMonthAndBank(String yearMonth, String bankType) {
        return bankPaymentFileRepository.findByYearMonthAndBankType(yearMonth, bankType);
    }

    public List<BankPaymentFile> listAll() {
        return bankPaymentFileRepository.findAll();
    }

    public void confirmFile(String fileId) {
        BankPaymentFile file = getFile(fileId);
        file.setStatus("CONFIRMED");
        bankPaymentFileRepository.save(file);
    }

    private String buildLockKey(String yearMonth, String bankType, String format) {
        return yearMonth + "_" + bankType + "_" + format;
    }

    private String generateFileName(String yearMonth, String bankType, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("工资发放_%s_%s_%s.%s",
                yearMonth.replace("-", ""), bankType, timestamp, format.toLowerCase());
    }

    public boolean verifyBalance(String fileId) {
        BankPaymentFile file = getFile(fileId);

        List<PayrollCalculation> approvedCalcs = payrollCalculationService
                .listByMonth(file.getYearMonth())
                .stream()
                .filter(c -> "APPROVED".equals(c.getReviewStatus()))
                .collect(Collectors.toList());

        BigDecimal calcTotal = approvedCalcs.stream()
                .map(PayrollCalculation::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return file.getTotalAmount().compareTo(PayrollUtils.setScale(calcTotal)) == 0
                && file.getTotalCount() == approvedCalcs.size();
    }
}
