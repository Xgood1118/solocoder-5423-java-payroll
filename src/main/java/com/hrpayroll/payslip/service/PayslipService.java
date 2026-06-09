package com.hrpayroll.payslip.service;

import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.service.PayrollCalculationService;
import com.hrpayroll.common.BusinessException;
import com.hrpayroll.common.PayrollUtils;
import com.hrpayroll.payslip.entity.Payslip;
import com.hrpayroll.payslip.repository.PayslipRepository;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PayslipService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayslipPdfService payslipPdfService;

    @Autowired
    private List<PayslipSender> payslipSenders;

    public Payslip createDraft(String employeeId, String yearMonth) {
        PayrollCalculation calc = payrollCalculationService.getCalculation(employeeId, yearMonth);
        if (!"APPROVED".equals(calc.getReviewStatus())) {
            throw new BusinessException("薪资计算未通过复核，不能生成工资条");
        }

        Employee employee = employeeService.getById(employeeId);
        String password = PayrollUtils.maskIdCardLast6(employee.getIdCard());

        Payslip payslip = new Payslip();
        payslip.setEmployeeId(employeeId);
        payslip.setEmployeeName(employee.getName());
        payslip.setEmployeeNo(employee.getEmployeeNo());
        payslip.setYearMonth(yearMonth);
        payslip.setStatus("DRAFT");
        payslip.setSendStatus("NOT_SENT");

        payslip.setBaseSalary(calc.getBaseSalary());
        payslip.setPerformanceSalary(calc.getPerformanceSalary());
        payslip.setPostSalary(calc.getPostSalary());
        payslip.setSenioritySalary(calc.getSenioritySalary());
        payslip.setOvertimePay(calc.getOvertimePay());
        payslip.setMealAllowance(calc.getMealAllowance());
        payslip.setTransportAllowance(calc.getTransportAllowance());
        payslip.setCommunicationAllowance(calc.getCommunicationAllowance());
        payslip.setAttendanceDeduction(calc.getAttendanceDeduction());
        payslip.setSocialSecurity(calc.getSocialSecurity());
        payslip.setHousingFund(calc.getHousingFund());
        payslip.setIndividualTax(calc.getIndividualTax());
        payslip.setGrossPay(calc.getGrossPay());
        payslip.setNetPay(calc.getNetPay());

        payslip.setPdfPassword(password);

        return payslipRepository.save(payslip);
    }

    public List<Payslip> createDraftsBatch(String yearMonth) {
        List<PayrollCalculation> approvedCalcs = payrollCalculationService.listByMonth(yearMonth)
                .stream()
                .filter(c -> "APPROVED".equals(c.getReviewStatus()))
                .collect(Collectors.toList());

        List<Payslip> results = new ArrayList<>();
        for (PayrollCalculation calc : approvedCalcs) {
            try {
                Payslip payslip = createDraft(calc.getEmployeeId(), yearMonth);
                results.add(payslip);
            } catch (Exception e) {
                // skip
            }
        }
        return results;
    }

    public Payslip confirm(String employeeId, String yearMonth) {
        Payslip payslip = getPayslip(employeeId, yearMonth);
        if (!"DRAFT".equals(payslip.getStatus())) {
            throw new BusinessException("只有草稿状态的工资条才能确认");
        }
        payslip.setStatus("CONFIRMED");
        return payslipRepository.save(payslip);
    }

    public void confirmBatch(String yearMonth) {
        List<Payslip> drafts = payslipRepository.findByYearMonthAndStatus(yearMonth, "DRAFT");
        for (Payslip payslip : drafts) {
            payslip.setStatus("CONFIRMED");
            payslipRepository.save(payslip);
        }
    }

    public Payslip send(String employeeId, String yearMonth, String channel) {
        Payslip payslip = getPayslip(employeeId, yearMonth);
        if (!"CONFIRMED".equals(payslip.getStatus())) {
            throw new BusinessException("只有已确认的工资条才能发送");
        }

        PayslipSender sender = getSender(channel);

        byte[] pdfData = payslipPdfService.generatePdf(payslip, payslip.getPdfPassword());

        boolean success = sender.send(payslip, pdfData);

        payslip.setSendChannel(channel);
        payslip.setSendTime(LocalDateTime.now());
        payslip.setSendStatus(success ? "SENT" : "FAILED");
        if (success) {
            payslip.setStatus("SENT");
        }

        return payslipRepository.save(payslip);
    }

    public int sendBatch(String yearMonth, String channel) {
        List<Payslip> confirmed = payslipRepository.findByYearMonthAndStatus(yearMonth, "CONFIRMED");
        int sentCount = 0;
        for (Payslip payslip : confirmed) {
            try {
                send(payslip.getEmployeeId(), yearMonth, channel);
                sentCount++;
            } catch (Exception e) {
                // skip failed
            }
        }
        return sentCount;
    }

    public byte[] getPdf(String employeeId, String yearMonth) {
        Payslip payslip = getPayslip(employeeId, yearMonth);
        return payslipPdfService.generatePdf(payslip, payslip.getPdfPassword());
    }

    public Payslip getPayslip(String employeeId, String yearMonth) {
        return payslipRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElseThrow(() -> new BusinessException("工资条不存在"));
    }

    public List<Payslip> listByMonth(String yearMonth) {
        return payslipRepository.findByYearMonth(yearMonth);
    }

    public List<Payslip> listByEmployee(String employeeId) {
        return payslipRepository.findByEmployeeId(employeeId);
    }

    public Payslip revokeConfirm(String employeeId, String yearMonth) {
        Payslip payslip = getPayslip(employeeId, yearMonth);
        if (!"CONFIRMED".equals(payslip.getStatus())) {
            throw new BusinessException("只有已确认状态的工资条才能撤销确认");
        }
        payslip.setStatus("DRAFT");
        return payslipRepository.save(payslip);
    }

    private PayslipSender getSender(String channel) {
        if (channel == null) {
            channel = "EMAIL";
        }
        for (PayslipSender sender : payslipSenders) {
            if (channel.equalsIgnoreCase(sender.getChannelName())) {
                return sender;
            }
        }
        throw new BusinessException("不支持的发送通道: " + channel);
    }

    public List<String> getAvailableChannels() {
        return payslipSenders.stream()
                .map(PayslipSender::getChannelName)
                .collect(Collectors.toList());
    }
}
