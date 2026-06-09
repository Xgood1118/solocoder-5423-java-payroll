package com.hrpayroll.attendance.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Holiday extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String holidayName;
    private LocalDate holidayDate;
    private int year;
    private String type;
}
