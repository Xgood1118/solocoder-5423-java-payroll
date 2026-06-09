package com.hrpayroll.salary.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialDeduction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String effectYearMonth;
    private BigDecimal childrenEducation;
    private BigDecimal continuingEducation;
    private BigDecimal housingLoanInterest;
    private BigDecimal housingRent;
    private BigDecimal elderlySupport;
    private BigDecimal seriousIllness;

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (childrenEducation != null) total = total.add(childrenEducation);
        if (continuingEducation != null) total = total.add(continuingEducation);
        if (housingLoanInterest != null) total = total.add(housingLoanInterest);
        if (housingRent != null) total = total.add(housingRent);
        if (elderlySupport != null) total = total.add(elderlySupport);
        if (seriousIllness != null) total = total.add(seriousIllness);
        return total;
    }
}
