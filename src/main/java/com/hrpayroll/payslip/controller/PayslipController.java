package com.hrpayroll.payslip.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.payslip.entity.Payslip;
import com.hrpayroll.payslip.service.PayslipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payslip")
public class PayslipController {

    @Autowired
    private PayslipService payslipService;

    @PostMapping("/draft/{employeeId}/{yearMonth}")
    public Result<Payslip> createDraft(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payslipService.createDraft(employeeId, yearMonth));
    }

    @PostMapping("/draft/batch/{yearMonth}")
    public Result<List<Payslip>> createDraftsBatch(@PathVariable String yearMonth) {
        return Result.success(payslipService.createDraftsBatch(yearMonth));
    }

    @PostMapping("/confirm/{employeeId}/{yearMonth}")
    public Result<Payslip> confirm(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payslipService.confirm(employeeId, yearMonth));
    }

    @PostMapping("/confirm/batch/{yearMonth}")
    public Result<Void> confirmBatch(@PathVariable String yearMonth) {
        payslipService.confirmBatch(yearMonth);
        return Result.success();
    }

    @PostMapping("/revoke/{employeeId}/{yearMonth}")
    public Result<Payslip> revokeConfirm(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payslipService.revokeConfirm(employeeId, yearMonth));
    }

    @PostMapping("/send/{employeeId}/{yearMonth}")
    public Result<Payslip> send(
            @PathVariable String employeeId,
            @PathVariable String yearMonth,
            @RequestParam(defaultValue = "EMAIL") String channel) {
        return Result.success(payslipService.send(employeeId, yearMonth, channel));
    }

    @PostMapping("/send/batch/{yearMonth}")
    public Result<Integer> sendBatch(
            @PathVariable String yearMonth,
            @RequestParam(defaultValue = "EMAIL") String channel) {
        return Result.success(payslipService.sendBatch(yearMonth, channel));
    }

    @GetMapping("/{employeeId}/{yearMonth}")
    public Result<Payslip> getPayslip(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payslipService.getPayslip(employeeId, yearMonth));
    }

    @GetMapping("/month/{yearMonth}")
    public Result<List<Payslip>> listByMonth(@PathVariable String yearMonth) {
        return Result.success(payslipService.listByMonth(yearMonth));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<Payslip>> listByEmployee(@PathVariable String employeeId) {
        return Result.success(payslipService.listByEmployee(employeeId));
    }

    @GetMapping("/pdf/{employeeId}/{yearMonth}")
    public ResponseEntity<byte[]> getPdf(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        byte[] pdfData = payslipService.getPdf(employeeId, yearMonth);
        Payslip payslip = payslipService.getPayslip(employeeId, yearMonth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                payslip.getEmployeeName() + "_" + yearMonth + "_工资条.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }

    @GetMapping("/channels")
    public Result<List<String>> getChannels() {
        return Result.success(payslipService.getAvailableChannels());
    }
}
