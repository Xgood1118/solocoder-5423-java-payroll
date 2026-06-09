package com.hrpayroll.payslip.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Payslip extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String employeeName;
    private String employeeNo;
    private String yearMonth;
    private String status;

    private BigDecimal baseSalary;
    private BigDecimal performanceSalary;
    private BigDecimal postSalary;
    private BigDecimal senioritySalary;
    private BigDecimal overtimePay;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal communicationAllowance;

    private BigDecimal attendanceDeduction;
    private BigDecimal socialSecurity;
    private BigDecimal housingFund;
    private BigDecimal individualTax;

    private BigDecimal grossPay;
    private BigDecimal netPay;

    private String pdfPassword;
    private String pdfPath;
    private String sendChannel;
    private LocalDateTime sendTime;
    private String sendStatus;

    private String remark;
}
