package com.hrpayroll.bank.controller;

import com.hrpayroll.bank.entity.BankPaymentFile;
import com.hrpayroll.bank.service.BankPaymentService;
import com.hrpayroll.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankPaymentController {

    @Autowired
    private BankPaymentService bankPaymentService;

    @PostMapping("/generate")
    public Result<BankPaymentFile> generate(
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "DEFAULT") String bankType,
            @RequestParam(defaultValue = "csv") String format,
            @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.get("operator") : "system";
        return Result.success(bankPaymentService.generatePaymentFile(yearMonth, bankType, format, operator));
    }

    @GetMapping("/{fileId}")
    public Result<BankPaymentFile> getFile(@PathVariable String fileId) {
        return Result.success(bankPaymentService.getFile(fileId));
    }

    @GetMapping("/month/{yearMonth}")
    public Result<List<BankPaymentFile>> listByMonth(
            @PathVariable String yearMonth,
            @RequestParam(required = false) String bankType) {
        if (bankType != null) {
            return Result.success(bankPaymentService.listByMonthAndBank(yearMonth, bankType));
        }
        return Result.success(bankPaymentService.listByMonth(yearMonth));
    }

    @GetMapping
    public Result<List<BankPaymentFile>> listAll() {
        return Result.success(bankPaymentService.listAll());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable String fileId) {
        byte[] content = bankPaymentService.downloadPaymentFile(fileId);
        BankPaymentFile file = bankPaymentService.getFile(fileId);

        HttpHeaders headers = new HttpHeaders();
        if ("csv".equalsIgnoreCase(file.getFileFormat())) {
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        } else {
            headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
        }
        headers.setContentDispositionFormData("attachment", file.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    @PostMapping("/confirm/{fileId}")
    public Result<Void> confirm(@PathVariable String fileId) {
        bankPaymentService.confirmFile(fileId);
        return Result.success();
    }

    @GetMapping("/verify/{fileId}")
    public Result<Boolean> verifyBalance(@PathVariable String fileId) {
        return Result.success(bankPaymentService.verifyBalance(fileId));
    }
}
