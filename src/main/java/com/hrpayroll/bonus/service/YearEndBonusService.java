package com.hrpayroll.bonus.service;

import com.hrpayroll.bonus.entity.YearEndBonus;
import com.hrpayroll.bonus.repository.YearEndBonusRepository;
import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.service.PayrollCalculationService;
import com.hrpayroll.common.BusinessException;
import com.hrpayroll.common.PayrollUtils;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.service.EmployeeService;
import com.hrpayroll.tax.entity.TaxBracket;
import com.hrpayroll.tax.service.TaxCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class YearEndBonusService {

    @Autowired
    private YearEndBonusRepository yearEndBonusRepository;

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    @Autowired
    private EmployeeService employeeService;

    private static final List<TaxBracket> MONTHLY_TAX_BRACKETS = Arrays.asList(
            new TaxBracket(BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("0.03"), BigDecimal.ZERO),
            new TaxBracket(new BigDecimal("3000"), new BigDecimal("12000"), new BigDecimal("0.10"), new BigDecimal("210")),
            new TaxBracket(new BigDecimal("12000"), new BigDecimal("25000"), new BigDecimal("0.20"), new BigDecimal("1410")),
            new TaxBracket(new BigDecimal("25000"), new BigDecimal("35000"), new BigDecimal("0.25"), new BigDecimal("2660")),
            new TaxBracket(new BigDecimal("35000"), new BigDecimal("55000"), new BigDecimal("0.30"), new BigDecimal("4410")),
            new TaxBracket(new BigDecimal("55000"), new BigDecimal("80000"), new BigDecimal("0.35"), new BigDecimal("7160")),
            new TaxBracket(new BigDecimal("80000"), new BigDecimal("9999999"), new BigDecimal("0.45"), new BigDecimal("15160"))
    );

    public YearEndBonus calculate(String employeeId, int taxYear, BigDecimal bonusCoefficient, String taxMethod) {
        Employee employee = employeeService.getById(employeeId);

        List<PayrollCalculation> yearCalcs = new ArrayList<>();
        BigDecimal totalAnnualIncome = BigDecimal.ZERO;
        int workMonths = 0;

        for (int month = 1; month <= 12; month++) {
            String yearMonth = String.format("%04d-%02d", taxYear, month);
            try {
                PayrollCalculation calc = payrollCalculationService.getCalculation(employeeId, yearMonth);
                if ("CALCULATED".equals(calc.getCalcStatus()) || "APPROVED".equals(calc.getReviewStatus())) {
                    yearCalcs.add(calc);
                    totalAnnualIncome = totalAnnualIncome.add(calc.getGrossPay());
                    workMonths++;
                }
            } catch (Exception e) {
                // skip
            }
        }

        if (workMonths == 0) {
            throw new BusinessException("该年度没有薪资数据，无法计算年终奖");
        }

        BigDecimal monthlyAverage = totalAnnualIncome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal bonusBase = monthlyAverage.multiply(BigDecimal.valueOf(workMonths));
        BigDecimal bonusAmount = PayrollUtils.setScale(bonusBase.multiply(bonusCoefficient));

        BigDecimal bonusTax;
        if ("SEPARATE".equalsIgnoreCase(taxMethod)) {
            bonusTax = calculateSeparateTax(bonusAmount);
        } else {
            bonusTax = calculateIntegratedTax(employeeId, taxYear, bonusAmount, yearCalcs);
        }

        YearEndBonus bonus = new YearEndBonus();
        bonus.setEmployeeId(employeeId);
        bonus.setEmployeeName(employee.getName());
        bonus.setEmployeeNo(employee.getEmployeeNo());
        bonus.setTaxYear(taxYear);
        bonus.setTotalAnnualIncome(PayrollUtils.setScale(totalAnnualIncome));
        bonus.setMonthlyAverageIncome(monthlyAverage);
        bonus.setWorkMonths(workMonths);
        bonus.setBonusCoefficient(bonusCoefficient);
        bonus.setBonusAmount(bonusAmount);
        bonus.setBonusTax(PayrollUtils.setScale(bonusTax));
        bonus.setBonusNetAmount(PayrollUtils.setScale(bonusAmount.subtract(bonusTax)));
        bonus.setTaxMethod(taxMethod != null ? taxMethod : "INTEGRATED");
        bonus.setStatus("CALCULATED");

        return saveBonus(bonus);
    }

    public List<YearEndBonus> calculateBatch(int taxYear, BigDecimal defaultCoefficient, String taxMethod) {
        List<Employee> activeEmployees = employeeService.listActive();
        List<YearEndBonus> results = new ArrayList<>();
        for (Employee employee : activeEmployees) {
            try {
                YearEndBonus bonus = calculate(employee.getId(), taxYear, defaultCoefficient, taxMethod);
                results.add(bonus);
            } catch (Exception e) {
                // skip
            }
        }
        return results;
    }

    private BigDecimal calculateSeparateTax(BigDecimal bonusAmount) {
        BigDecimal monthlyAmount = bonusAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        for (TaxBracket bracket : MONTHLY_TAX_BRACKETS) {
            if (monthlyAmount.compareTo(bracket.getLowerBound()) > 0
                    && monthlyAmount.compareTo(bracket.getUpperBound()) <= 0) {
                return PayrollUtils.setScale(
                        bonusAmount.multiply(bracket.getRate()).subtract(bracket.getQuickDeduction()));
            }
        }
        TaxBracket last = MONTHLY_TAX_BRACKETS.get(MONTHLY_TAX_BRACKETS.size() - 1);
        return PayrollUtils.setScale(
                bonusAmount.multiply(last.getRate()).subtract(last.getQuickDeduction()));
    }

    private BigDecimal calculateIntegratedTax(String employeeId, int taxYear,
                                              BigDecimal bonusAmount, List<PayrollCalculation> yearCalcs) {
        BigDecimal totalIncome = yearCalcs.stream()
                .map(PayrollCalculation::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(bonusAmount);

        BigDecimal totalDeduction = new BigDecimal("5000").multiply(BigDecimal.valueOf(yearCalcs.size()));

        BigDecimal totalSocialSecurity = yearCalcs.stream()
                .map(PayrollCalculation::getSocialSecurity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHousingFund = yearCalcs.stream()
                .map(PayrollCalculation::getHousingFund)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpecialAdditional = yearCalcs.stream()
                .map(c -> c.getSpecialAdditionalTotal() != null
                        ? c.getSpecialAdditionalTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxableIncome = totalIncome
                .subtract(totalDeduction)
                .subtract(totalSocialSecurity)
                .subtract(totalHousingFund)
                .subtract(totalSpecialAdditional);

        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        BigDecimal totalTaxWithBonus = TaxCalculationService.calculateTax(taxableIncome);

        BigDecimal taxableIncomeWithoutBonus = totalIncome.subtract(bonusAmount)
                .subtract(totalDeduction)
                .subtract(totalSocialSecurity)
                .subtract(totalHousingFund)
                .subtract(totalSpecialAdditional);
        if (taxableIncomeWithoutBonus.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncomeWithoutBonus = BigDecimal.ZERO;
        }
        BigDecimal totalTaxWithoutBonus = TaxCalculationService.calculateTax(taxableIncomeWithoutBonus);

        return totalTaxWithBonus.subtract(totalTaxWithoutBonus);
    }

    private YearEndBonus saveBonus(YearEndBonus bonus) {
        yearEndBonusRepository.findByEmployeeIdAndTaxYear(bonus.getEmployeeId(), bonus.getTaxYear())
                .ifPresent(existing -> {
                    bonus.setId(existing.getId());
                    bonus.setCreateTime(existing.getCreateTime());
                    bonus.setStatus(existing.getStatus());
                });
        return yearEndBonusRepository.save(bonus);
    }

    public YearEndBonus getBonus(String employeeId, int taxYear) {
        return yearEndBonusRepository.findByEmployeeIdAndTaxYear(employeeId, taxYear)
                .orElseThrow(() -> new BusinessException("年终奖记录不存在"));
    }

    public List<YearEndBonus> listByYear(int taxYear) {
        return yearEndBonusRepository.findByTaxYear(taxYear);
    }

    public List<YearEndBonus> listByEmployee(String employeeId) {
        return yearEndBonusRepository.findByEmployeeId(employeeId);
    }

    public YearEndBonus approve(String employeeId, int taxYear) {
        YearEndBonus bonus = getBonus(employeeId, taxYear);
        bonus.setStatus("APPROVED");
        return yearEndBonusRepository.save(bonus);
    }

    public void approveBatch(int taxYear) {
        List<YearEndBonus> bonuses = yearEndBonusRepository.findByTaxYear(taxYear);
        for (YearEndBonus bonus : bonuses) {
            if ("CALCULATED".equals(bonus.getStatus())) {
                bonus.setStatus("APPROVED");
                yearEndBonusRepository.save(bonus);
            }
        }
    }
}
