package com.hrpayroll.tax.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaxCumulativeState extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String yearMonth;
    private int taxYear;
    private int monthIndex;

    private BigDecimal cumulativeIncome;
    private BigDecimal cumulativeDeduction;
    private BigDecimal cumulativeSpecialDeduction;
    private BigDecimal cumulativeSpecialAdditionalDeduction;
    private BigDecimal cumulativeTaxableIncome;
    private BigDecimal cumulativeTax;
    private BigDecimal monthlyTax;

    private BigDecimal socialSecurity;
    private BigDecimal housingFund;
    private BigDecimal specialAdditionalTotal;
}
