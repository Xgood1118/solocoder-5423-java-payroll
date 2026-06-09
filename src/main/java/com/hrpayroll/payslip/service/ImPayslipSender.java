package com.hrpayroll.payslip.service;

import com.hrpayroll.payslip.entity.Payslip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImPayslipSender implements PayslipSender {

    @Override
    public String getChannelName() {
        return "IM";
    }

    @Override
    public boolean send(Payslip payslip, byte[] pdfData) {
        log.warn("IM 发送通道暂未实现: 员工={}, 月份={}",
                payslip.getEmployeeName(), payslip.getYearMonth());
        return false;
    }
}
