package com.hrpayroll.calc.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PayrollCalculation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String employeeName;
    private String employeeNo;
    private String yearMonth;
    private String calcStatus;
    private String reviewStatus;

    private BigDecimal totalBaseSalary;
    private BigDecimal baseSalary;
    private BigDecimal performanceSalary;
    private BigDecimal postSalary;

    private BigDecimal senioritySalary;
    private int seniorityYears;
    private int seniorityMonths;

    private BigDecimal overtimePay;
    private BigDecimal workdayOvertimePay;
    private BigDecimal weekendOvertimePay;
    private BigDecimal holidayOvertimePay;
    private BigDecimal overtimeHourBase;

    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal communicationAllowance;

    private BigDecimal attendanceDeduction;
    private BigDecimal leaveDeductionDays;

    private BigDecimal socialSecurity;
    private BigDecimal socialSecurityBase;

    private BigDecimal housingFund;
    private BigDecimal housingFundBase;

    private BigDecimal specialAdditionalTotal;
    private BigDecimal taxableIncome;
    private BigDecimal individualTax;

    private BigDecimal grossPay;
    private BigDecimal netPay;

    private List<String> anomalies = new ArrayList<>();

    private String remark;
}
