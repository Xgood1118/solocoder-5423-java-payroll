package com.hrpayroll.bonus.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class YearEndBonus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String employeeName;
    private String employeeNo;
    private int taxYear;
    private BigDecimal totalAnnualIncome;
    private BigDecimal monthlyAverageIncome;
    private int workMonths;
    private BigDecimal bonusCoefficient;
    private BigDecimal bonusAmount;
    private BigDecimal bonusTax;
    private BigDecimal bonusNetAmount;
    private String taxMethod;
    private String status;
    private String remark;
}
