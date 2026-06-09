package com.hrpayroll.salary.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeNo;
    private String name;
    private String idCard;
    private String gender;
    private String phone;
    private String email;
    private String department;
    private String position;
    private String gradeCode;
    private String cityCode;
    private LocalDate hireDate;
    private BigDecimal baseSalary;
    private BigDecimal postSalary;
    private BigDecimal housingFundRate;
    private String bankAccount;
    private String bankName;
    private boolean active;
}
