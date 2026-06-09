package com.hrpayroll.salary.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CityStandard extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String cityCode;
    private String cityName;
    private BigDecimal socialSecurityMinBase;
    private BigDecimal socialSecurityMaxBase;
    private BigDecimal socialSecurityPersonalRate;
    private BigDecimal socialSecurityCompanyRate;
    private int year;
    private boolean enabled;
}
