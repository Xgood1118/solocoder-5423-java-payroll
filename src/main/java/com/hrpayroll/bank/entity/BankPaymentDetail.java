package com.hrpayroll.bank.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BankPaymentDetail {

    private String employeeNo;
    private String employeeName;
    private String bankAccount;
    private String bankName;
    private BigDecimal amount;
    private String remark;
}
