package com.hrpayroll.payslip.service;

import com.hrpayroll.payslip.entity.Payslip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailPayslipSender implements PayslipSender {

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean send(Payslip payslip, byte[] pdfData) {
        log.info("发送工资条邮件: 员工={}, 月份={}, 附件大小={} bytes",
                payslip.getEmployeeName(), payslip.getYearMonth(), pdfData.length);
        return true;
    }
}
