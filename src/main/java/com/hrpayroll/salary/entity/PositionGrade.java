package com.hrpayroll.salary.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionGrade extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String gradeCode;
    private String gradeName;
    private BigDecimal baseSalaryRatio;
    private BigDecimal performanceRatio;
    private String description;
    private boolean enabled;
}
