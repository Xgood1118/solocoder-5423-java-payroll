package com.hrpayroll.attendance.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonthlyAttendance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String yearMonth;
    private BigDecimal workDays;
    private BigDecimal leaveDays;
    private BigDecimal sickLeaveDays;
    private BigDecimal personalLeaveDays;
    private BigDecimal workdayOvertimeHours;
    private BigDecimal weekendOvertimeHours;
    private BigDecimal holidayOvertimeHours;
    private BigDecimal lateTimes;
    private BigDecimal earlyLeaveTimes;
    private String remark;
}
