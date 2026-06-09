package com.hrpayroll.payslip.service;

import com.hrpayroll.payslip.entity.Payslip;

public interface PayslipSender {

    String getChannelName();

    boolean send(Payslip payslip, byte[] pdfData);
}
