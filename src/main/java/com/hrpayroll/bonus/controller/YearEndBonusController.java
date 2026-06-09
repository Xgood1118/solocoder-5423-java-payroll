package com.hrpayroll.bonus.controller;

import com.hrpayroll.bonus.entity.YearEndBonus;
import com.hrpayroll.bonus.service.YearEndBonusService;
import com.hrpayroll.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bonus/year-end")
public class YearEndBonusController {

    @Autowired
    private YearEndBonusService yearEndBonusService;

    @PostMapping("/calculate/{employeeId}/{taxYear}")
    public Result<YearEndBonus> calculate(
            @PathVariable String employeeId,
            @PathVariable int taxYear,
            @RequestParam(defaultValue = "1.0") BigDecimal coefficient,
            @RequestParam(defaultValue = "INTEGRATED") String taxMethod) {
        return Result.success(yearEndBonusService.calculate(employeeId, taxYear, coefficient, taxMethod));
    }

    @PostMapping("/calculate/batch/{taxYear}")
    public Result<List<YearEndBonus>> calculateBatch(
            @PathVariable int taxYear,
            @RequestParam(defaultValue = "1.0") BigDecimal coefficient,
            @RequestParam(defaultValue = "INTEGRATED") String taxMethod) {
        return Result.success(yearEndBonusService.calculateBatch(taxYear, coefficient, taxMethod));
    }

    @GetMapping("/{employeeId}/{taxYear}")
    public Result<YearEndBonus> getBonus(
            @PathVariable String employeeId,
            @PathVariable int taxYear) {
        return Result.success(yearEndBonusService.getBonus(employeeId, taxYear));
    }

    @GetMapping("/year/{taxYear}")
    public Result<List<YearEndBonus>> listByYear(@PathVariable int taxYear) {
        return Result.success(yearEndBonusService.listByYear(taxYear));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<YearEndBonus>> listByEmployee(@PathVariable String employeeId) {
        return Result.success(yearEndBonusService.listByEmployee(employeeId));
    }

    @PostMapping("/approve/{employeeId}/{taxYear}")
    public Result<YearEndBonus> approve(
            @PathVariable String employeeId,
            @PathVariable int taxYear) {
        return Result.success(yearEndBonusService.approve(employeeId, taxYear));
    }

    @PostMapping("/approve/batch/{taxYear}")
    public Result<Void> approveBatch(@PathVariable int taxYear) {
        yearEndBonusService.approveBatch(taxYear);
        return Result.success();
    }
}
