package com.hrpayroll.calc.service;

import com.hrpayroll.attendance.entity.MonthlyAttendance;
import com.hrpayroll.attendance.service.AttendanceService;
import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.repository.PayrollCalculationRepository;
import com.hrpayroll.common.BusinessException;
import com.hrpayroll.common.PayrollUtils;
import com.hrpayroll.config.PayrollProperties;
import com.hrpayroll.salary.entity.CityStandard;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.entity.PositionGrade;
import com.hrpayroll.salary.entity.SpecialDeduction;
import com.hrpayroll.salary.service.CityStandardService;
import com.hrpayroll.salary.service.EmployeeService;
import com.hrpayroll.salary.service.PositionGradeService;
import com.hrpayroll.salary.service.SpecialDeductionService;
import com.hrpayroll.tax.entity.TaxCumulativeState;
import com.hrpayroll.tax.service.TaxCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayrollCalculationService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PositionGradeService positionGradeService;

    @Autowired
    private CityStandardService cityStandardService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private SpecialDeductionService specialDeductionService;

    @Autowired
    private TaxCalculationService taxCalculationService;

    @Autowired
    private PayrollCalculationRepository calculationRepository;

    @Autowired
    private PayrollProperties payrollProperties;

    public PayrollCalculation calculate(String employeeId, String yearMonth) {
        Employee employee = employeeService.getById(employeeId);
        if (!employee.isActive()) {
            throw new BusinessException("员工已离职，不能计算工资: " + employee.getName());
        }

        MonthlyAttendance attendance = attendanceService.getByEmployeeAndMonth(employeeId, yearMonth);
        if (attendance == null) {
            attendance = createDefaultAttendance(employeeId, yearMonth);
        }

        PositionGrade grade = positionGradeService.getByCode(employee.getGradeCode());
        int taxYear = Integer.parseInt(yearMonth.substring(0, 4));
        int monthIndex = Integer.parseInt(yearMonth.substring(5, 7));

        PayrollCalculation calc = new PayrollCalculation();
        calc.setEmployeeId(employeeId);
        calc.setEmployeeName(employee.getName());
        calc.setEmployeeNo(employee.getEmployeeNo());
        calc.setYearMonth(yearMonth);
        calc.setCalcStatus("CALCULATED");
        calc.setReviewStatus("PENDING");
        calc.setAnomalies(new ArrayList<>());

        calcStep1_BaseItems(calc, employee, grade);

        calcStep2_Seniority(calc, employee, yearMonth);

        calcStep3_Overtime(calc, employee, attendance);

        calcStep4_Allowances(calc);

        calcStep5_AttendanceDeduction(calc, employee, attendance);

        calcStep6_SocialSecurity(calc, employee, yearMonth);

        calcStep7_HousingFund(calc, employee);

        BigDecimal specialAdditionalTotal = getSpecialAdditionalTotal(employeeId, yearMonth);
        calcStep8_Tax(calc, employee, yearMonth, taxYear, monthIndex, specialAdditionalTotal);

        calcSummary(calc);

        detectAnomalies(calc, attendance, employeeId, yearMonth);

        return saveCalculation(calc);
    }

    public List<PayrollCalculation> calculateBatch(String yearMonth) {
        List<Employee> activeEmployees = employeeService.listActive();
        List<PayrollCalculation> results = new ArrayList<>();
        for (Employee employee : activeEmployees) {
            try {
                PayrollCalculation calc = calculate(employee.getId(), yearMonth);
                results.add(calc);
            } catch (Exception e) {
                PayrollCalculation errorCalc = new PayrollCalculation();
                errorCalc.setEmployeeId(employee.getId());
                errorCalc.setEmployeeName(employee.getName());
                errorCalc.setEmployeeNo(employee.getEmployeeNo());
                errorCalc.setYearMonth(yearMonth);
                errorCalc.setCalcStatus("ERROR");
                errorCalc.setReviewStatus("PENDING");
                errorCalc.setRemark("计算失败: " + e.getMessage());
                results.add(errorCalc);
            }
        }
        return results;
    }

    public PayrollCalculation recalculate(String employeeId, String yearMonth) {
        PayrollCalculation existing = calculationRepository
                .findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElse(null);

        if (existing != null && "APPROVED".equals(existing.getReviewStatus())) {
            throw new BusinessException("已通过复核的薪资记录不能重算，请先撤销复核");
        }

        PayrollCalculation newCalc = calculate(employeeId, yearMonth);

        recalculateSubsequentTax(employeeId, yearMonth);

        return newCalc;
    }

    public void recalculateSubsequentTax(String employeeId, String fromYearMonth) {
        List<PayrollCalculation> subsequent = calculationRepository
                .findByEmployeeIdFromMonth(employeeId, fromYearMonth);

        if (subsequent.size() <= 1) {
            return;
        }

        String firstMonth = subsequent.get(0).getYearMonth();
        int taxYear = Integer.parseInt(firstMonth.substring(0, 4));
        int fromMonthIndex = Integer.parseInt(firstMonth.substring(5, 7));

        List<TaxCalculationService.MonthlyTaxInput> inputs = new ArrayList<>();
        for (PayrollCalculation calc : subsequent) {
            int monthIdx = Integer.parseInt(calc.getYearMonth().substring(5, 7));
            inputs.add(new TaxCalculationService.MonthlyTaxInput(
                    calc.getYearMonth(),
                    monthIdx,
                    calcGrossPay(calc),
                    calc.getSocialSecurity(),
                    calc.getHousingFund(),
                    calc.getSpecialAdditionalTotal() != null ? calc.getSpecialAdditionalTotal() : BigDecimal.ZERO
            ));
        }

        taxCalculationService.recalculateFromMonth(employeeId, taxYear, fromMonthIndex, inputs);

        for (PayrollCalculation calc : subsequent) {
            TaxCumulativeState state = taxCalculationService.getState(employeeId, calc.getYearMonth());
            if (state != null) {
                calc.setIndividualTax(state.getMonthlyTax());
                calcSummary(calc);
                calculationRepository.save(calc);
            }
        }
    }

    private void calcStep1_BaseItems(PayrollCalculation calc, Employee employee, PositionGrade grade) {
        BigDecimal baseSalary = employee.getBaseSalary();
        if (baseSalary == null) baseSalary = BigDecimal.ZERO;

        BigDecimal basePortion = PayrollUtils.setScale(baseSalary.multiply(grade.getBaseSalaryRatio()));
        BigDecimal performancePortion = PayrollUtils.setScale(baseSalary.multiply(grade.getPerformanceRatio()));

        calc.setBaseSalary(basePortion);
        calc.setPerformanceSalary(performancePortion);
        calc.setTotalBaseSalary(PayrollUtils.setScale(baseSalary));

        BigDecimal postSalary = employee.getPostSalary();
        if (postSalary == null) postSalary = BigDecimal.ZERO;
        calc.setPostSalary(postSalary);
    }

    private void calcStep2_Seniority(PayrollCalculation calc, Employee employee, String yearMonth) {
        if (employee.getHireDate() == null) {
            calc.setSenioritySalary(BigDecimal.ZERO);
            calc.setSeniorityYears(0);
            calc.setSeniorityMonths(0);
            return;
        }

        YearMonth ym = YearMonth.parse(yearMonth.replace(".", "-"));
        LocalDate calcDate = ym.atEndOfMonth();

        int months = PayrollUtils.monthsBetween(employee.getHireDate(), calcDate);
        int years = months / 12;

        BigDecimal seniority = payrollProperties.getSeniorityIncrementPerYear()
                .multiply(BigDecimal.valueOf(years));

        if (seniority.compareTo(payrollProperties.getSeniorityMax()) > 0) {
            seniority = payrollProperties.getSeniorityMax();
        }

        calc.setSenioritySalary(PayrollUtils.setScale(seniority));
        calc.setSeniorityYears(years);
        calc.setSeniorityMonths(months);
    }

    private void calcStep3_Overtime(PayrollCalculation calc, Employee employee,
                                    MonthlyAttendance attendance) {
        BigDecimal baseSalary = calc.getBaseSalary();
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) == 0) {
            baseSalary = BigDecimal.ONE;
        }

        BigDecimal hourBase = baseSalary
                .divide(payrollProperties.getWorkDaysPerMonth(), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(payrollProperties.getWorkHoursPerDay()), 4, RoundingMode.HALF_UP);
        calc.setOvertimeHourBase(PayrollUtils.setScale(hourBase));

        BigDecimal workdayHours = attendance.getWorkdayOvertimeHours() != null
                ? attendance.getWorkdayOvertimeHours() : BigDecimal.ZERO;
        BigDecimal weekendHours = attendance.getWeekendOvertimeHours() != null
                ? attendance.getWeekendOvertimeHours() : BigDecimal.ZERO;
        BigDecimal holidayHours = attendance.getHolidayOvertimeHours() != null
                ? attendance.getHolidayOvertimeHours() : BigDecimal.ZERO;

        BigDecimal workdayPay = PayrollUtils.setScale(
                hourBase.multiply(new BigDecimal("1.5")).multiply(workdayHours));
        BigDecimal weekendPay = PayrollUtils.setScale(
                hourBase.multiply(new BigDecimal("2.0")).multiply(weekendHours));
        BigDecimal holidayPay = PayrollUtils.setScale(
                hourBase.multiply(new BigDecimal("3.0")).multiply(holidayHours));

        calc.setWorkdayOvertimePay(workdayPay);
        calc.setWeekendOvertimePay(weekendPay);
        calc.setHolidayOvertimePay(holidayPay);
        calc.setOvertimePay(PayrollUtils.setScale(workdayPay.add(weekendPay).add(holidayPay)));
    }

    private void calcStep4_Allowances(PayrollCalculation calc) {
        calc.setMealAllowance(payrollProperties.getMealAllowance());
        calc.setTransportAllowance(payrollProperties.getTransportAllowance());
        calc.setCommunicationAllowance(payrollProperties.getCommunicationAllowance());
    }

    private void calcStep5_AttendanceDeduction(PayrollCalculation calc, Employee employee,
                                               MonthlyAttendance attendance) {
        BigDecimal totalLeaveDays = attendanceService.getTotalLeaveDays(attendance);
        calc.setLeaveDeductionDays(totalLeaveDays);

        BigDecimal baseSalary = calc.getTotalBaseSalary();
        BigDecimal dailyRate = baseSalary.divide(payrollProperties.getWorkDaysPerMonth(), 4, RoundingMode.HALF_UP);

        BigDecimal deduction = PayrollUtils.setScale(dailyRate.multiply(totalLeaveDays));
        calc.setAttendanceDeduction(deduction);
    }

    private void calcStep6_SocialSecurity(PayrollCalculation calc, Employee employee, String yearMonth) {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        CityStandard standard;
        try {
            standard = cityStandardService.getByCityAndYear(employee.getCityCode(), year);
        } catch (BusinessException e) {
            calc.setSocialSecurity(BigDecimal.ZERO);
            calc.setSocialSecurityBase(BigDecimal.ZERO);
            calc.getAnomalies().add("未找到城市社保标准: " + employee.getCityCode());
            return;
        }

        BigDecimal baseSalary = calc.getTotalBaseSalary();
        BigDecimal ssBase = baseSalary;

        if (ssBase.compareTo(standard.getSocialSecurityMinBase()) < 0) {
            ssBase = standard.getSocialSecurityMinBase();
        }
        if (ssBase.compareTo(standard.getSocialSecurityMaxBase()) > 0) {
            ssBase = standard.getSocialSecurityMaxBase();
        }

        BigDecimal ss = PayrollUtils.setScale(ssBase.multiply(standard.getSocialSecurityPersonalRate()));
        calc.setSocialSecurityBase(PayrollUtils.setScale(ssBase));
        calc.setSocialSecurity(ss);
    }

    private void calcStep7_HousingFund(PayrollCalculation calc, Employee employee) {
        BigDecimal baseSalary = calc.getTotalBaseSalary();
        BigDecimal rate = employee.getHousingFundRate();
        if (rate == null) {
            rate = BigDecimal.ZERO;
        }

        BigDecimal hf = PayrollUtils.setScale(baseSalary.multiply(rate));
        calc.setHousingFundBase(PayrollUtils.setScale(baseSalary));
        calc.setHousingFund(hf);
    }

    private void calcStep8_Tax(PayrollCalculation calc, Employee employee, String yearMonth,
                               int taxYear, int monthIndex, BigDecimal specialAdditionalTotal) {
        calc.setSpecialAdditionalTotal(specialAdditionalTotal);

        BigDecimal incomeForTax = calcGrossPay(calc);

        TaxCumulativeState taxState = taxCalculationService.calculateMonthlyTax(
                employee.getId(), yearMonth, incomeForTax,
                calc.getSocialSecurity(), calc.getHousingFund(),
                specialAdditionalTotal, monthIndex);

        calc.setIndividualTax(taxState.getMonthlyTax());
        calc.setTaxableIncome(taxState.getCumulativeTaxableIncome());

        taxCalculationService.saveState(taxState);
    }

    private BigDecimal calcGrossPay(PayrollCalculation calc) {
        BigDecimal gross = calc.getBaseSalary()
                .add(calc.getPerformanceSalary())
                .add(calc.getPostSalary())
                .add(calc.getSenioritySalary())
                .add(calc.getOvertimePay())
                .add(calc.getMealAllowance())
                .add(calc.getTransportAllowance())
                .add(calc.getCommunicationAllowance())
                .subtract(calc.getAttendanceDeduction());
        return PayrollUtils.setScale(gross);
    }

    private BigDecimal getSpecialAdditionalTotal(String employeeId, String yearMonth) {
        SpecialDeduction deduction = specialDeductionService.getByEmployeeAndMonth(employeeId, yearMonth);
        if (deduction == null) {
            return BigDecimal.ZERO;
        }
        return deduction.getTotal();
    }

    private void calcSummary(PayrollCalculation calc) {
        BigDecimal gross = calc.getBaseSalary()
                .add(calc.getPerformanceSalary())
                .add(calc.getPostSalary())
                .add(calc.getSenioritySalary())
                .add(calc.getOvertimePay())
                .add(calc.getMealAllowance())
                .add(calc.getTransportAllowance())
                .add(calc.getCommunicationAllowance())
                .subtract(calc.getAttendanceDeduction());

        BigDecimal net = gross
                .subtract(calc.getSocialSecurity())
                .subtract(calc.getHousingFund())
                .subtract(calc.getIndividualTax());

        calc.setGrossPay(PayrollUtils.setScale(gross));
        calc.setNetPay(PayrollUtils.setScale(net));
    }

    private void detectAnomalies(PayrollCalculation calc, MonthlyAttendance attendance,
                                 String employeeId, String yearMonth) {
        BigDecimal totalLeave = attendanceService.getTotalLeaveDays(attendance);
        if (totalLeave.compareTo(BigDecimal.valueOf(payrollProperties.getLeaveMaxDaysWarning())) > 0) {
            calc.getAnomalies().add("请假天数超过 " + payrollProperties.getLeaveMaxDaysWarning() + " 天: " + totalLeave + " 天");
        }

        BigDecimal totalOvertime = attendanceService.getTotalOvertimeHours(attendance);
        if (totalOvertime.compareTo(BigDecimal.valueOf(payrollProperties.getOvertimeMaxHoursWarning())) > 0) {
            calc.getAnomalies().add("加班时长超过 " + payrollProperties.getOvertimeMaxHoursWarning() + " 小时: " + totalOvertime + " 小时");
        }

        YearMonth currentYm = YearMonth.parse(yearMonth.replace(".", "-"));
        YearMonth prevYm = currentYm.minusMonths(1);
        String prevMonth = String.format("%04d-%02d", prevYm.getYear(), prevYm.getMonthValue());

        PayrollCalculation prevCalc = calculationRepository
                .findByEmployeeIdAndYearMonth(employeeId, prevMonth).orElse(null);

        if (prevCalc != null && prevCalc.getPerformanceSalary().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = calc.getPerformanceSalary()
                    .subtract(prevCalc.getPerformanceSalary())
                    .divide(prevCalc.getPerformanceSalary(), 4, RoundingMode.HALF_UP)
                    .abs();

            if (change.compareTo(payrollProperties.getPerformanceChangeThreshold()) > 0) {
                calc.getAnomalies().add("绩效工资较上月变动超过 "
                        + payrollProperties.getPerformanceChangeThreshold().multiply(BigDecimal.valueOf(100))
                        + "%");
            }
        }
    }

    private MonthlyAttendance createDefaultAttendance(String employeeId, String yearMonth) {
        MonthlyAttendance attendance = new MonthlyAttendance();
        attendance.setEmployeeId(employeeId);
        attendance.setYearMonth(yearMonth);
        attendance.setWorkDays(payrollProperties.getWorkDaysPerMonth());
        attendance.setLeaveDays(BigDecimal.ZERO);
        attendance.setSickLeaveDays(BigDecimal.ZERO);
        attendance.setPersonalLeaveDays(BigDecimal.ZERO);
        attendance.setWorkdayOvertimeHours(BigDecimal.ZERO);
        attendance.setWeekendOvertimeHours(BigDecimal.ZERO);
        attendance.setHolidayOvertimeHours(BigDecimal.ZERO);
        return attendance;
    }

    private PayrollCalculation saveCalculation(PayrollCalculation calc) {
        calculationRepository.findByEmployeeIdAndYearMonth(calc.getEmployeeId(), calc.getYearMonth())
                .ifPresent(existing -> {
                    calc.setId(existing.getId());
                    calc.setCreateTime(existing.getCreateTime());
                    calc.setReviewStatus(existing.getReviewStatus());
                });
        return calculationRepository.save(calc);
    }

    public PayrollCalculation getCalculation(String employeeId, String yearMonth) {
        return calculationRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElseThrow(() -> new BusinessException("薪资计算记录不存在"));
    }

    public List<PayrollCalculation> listByMonth(String yearMonth) {
        return calculationRepository.findByYearMonth(yearMonth);
    }

    public List<PayrollCalculation> listByEmployee(String employeeId) {
        return calculationRepository.findByEmployeeId(employeeId);
    }

    public PayrollCalculation approve(String employeeId, String yearMonth) {
        PayrollCalculation calc = getCalculation(employeeId, yearMonth);
        if (!"CALCULATED".equals(calc.getCalcStatus())) {
            throw new BusinessException("薪资计算未完成，不能通过复核");
        }
        calc.setReviewStatus("APPROVED");
        return calculationRepository.save(calc);
    }

    public PayrollCalculation reject(String employeeId, String yearMonth, String reason) {
        PayrollCalculation calc = getCalculation(employeeId, yearMonth);
        calc.setReviewStatus("REJECTED");
        calc.setRemark(reason);
        return calculationRepository.save(calc);
    }

    public PayrollCalculation revokeApproval(String employeeId, String yearMonth) {
        PayrollCalculation calc = getCalculation(employeeId, yearMonth);
        if (!"APPROVED".equals(calc.getReviewStatus())) {
            throw new BusinessException("只有已通过的记录才能撤销");
        }
        calc.setReviewStatus("PENDING");
        return calculationRepository.save(calc);
    }

    public void approveBatch(String yearMonth) {
        List<PayrollCalculation> calcs = calculationRepository.findByYearMonth(yearMonth);
        for (PayrollCalculation calc : calcs) {
            if ("CALCULATED".equals(calc.getCalcStatus())) {
                calc.setReviewStatus("APPROVED");
                calculationRepository.save(calc);
            }
        }
    }
}
