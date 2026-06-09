package com.hrpayroll.calc.controller;

import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.service.PayrollCalculationService;
import com.hrpayroll.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calc")
public class PayrollCalculationController {

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    @PostMapping("/employee/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> calculate(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.calculate(employeeId, yearMonth));
    }

    @PostMapping("/batch/{yearMonth}")
    public Result<List<PayrollCalculation>> calculateBatch(@PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.calculateBatch(yearMonth));
    }

    @PostMapping("/recalculate/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> recalculate(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.recalculate(employeeId, yearMonth));
    }

    @GetMapping("/employee/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> getCalculation(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.getCalculation(employeeId, yearMonth));
    }

    @GetMapping("/month/{yearMonth}")
    public Result<List<PayrollCalculation>> listByMonth(@PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.listByMonth(yearMonth));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<PayrollCalculation>> listByEmployee(@PathVariable String employeeId) {
        return Result.success(payrollCalculationService.listByEmployee(employeeId));
    }

    @PostMapping("/approve/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> approve(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.approve(employeeId, yearMonth));
    }

    @PostMapping("/reject/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> reject(
            @PathVariable String employeeId,
            @PathVariable String yearMonth,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return Result.success(payrollCalculationService.reject(employeeId, yearMonth, reason));
    }

    @PostMapping("/revoke/{employeeId}/{yearMonth}")
    public Result<PayrollCalculation> revokeApproval(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(payrollCalculationService.revokeApproval(employeeId, yearMonth));
    }

    @PostMapping("/approve/batch/{yearMonth}")
    public Result<Void> approveBatch(@PathVariable String yearMonth) {
        payrollCalculationService.approveBatch(yearMonth);
        return Result.success();
    }
}
