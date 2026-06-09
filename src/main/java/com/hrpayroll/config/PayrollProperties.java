package com.hrpayroll.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "payroll")
public class PayrollProperties {

    private BigDecimal workDaysPerMonth = new BigDecimal("21.75");
    private int workHoursPerDay = 8;
    private BigDecimal seniorityIncrementPerYear = new BigDecimal("100");
    private BigDecimal seniorityMax = new BigDecimal("2000");
    private BigDecimal taxThreshold = new BigDecimal("5000");
    private BigDecimal mealAllowance = new BigDecimal("300");
    private BigDecimal transportAllowance = new BigDecimal("200");
    private BigDecimal communicationAllowance = new BigDecimal("100");
    private int overtimeMaxHoursWarning = 36;
    private int leaveMaxDaysWarning = 10;
    private BigDecimal performanceChangeThreshold = new BigDecimal("0.3");
}
