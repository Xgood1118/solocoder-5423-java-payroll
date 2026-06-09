package com.hrpayroll.bank.entity;

import com.hrpayroll.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankPaymentFile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String fileId;
    private String yearMonth;
    private String bankType;
    private String fileFormat;
    private String fileName;
    private int totalCount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime generateTime;
    private String generatedBy;
    private String remark;
}
