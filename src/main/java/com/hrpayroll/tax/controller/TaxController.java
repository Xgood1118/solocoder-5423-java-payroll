package com.hrpayroll.tax.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.tax.entity.TaxBracket;
import com.hrpayroll.tax.entity.TaxCumulativeState;
import com.hrpayroll.tax.service.TaxCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
public class TaxController {

    @Autowired
    private TaxCalculationService taxCalculationService;

    @GetMapping("/brackets")
    public Result<List<TaxBracket>> getTaxBrackets() {
        return Result.success(TaxCalculationService.getTaxBrackets());
    }

    @GetMapping("/state/{employeeId}/{yearMonth}")
    public Result<TaxCumulativeState> getState(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(taxCalculationService.getState(employeeId, yearMonth));
    }

    @GetMapping("/states/{employeeId}/{taxYear}")
    public Result<List<TaxCumulativeState>> listStates(
            @PathVariable String employeeId,
            @PathVariable int taxYear) {
        return Result.success(taxCalculationService.listStates(employeeId, taxYear));
    }

    @PostMapping("/reset/{employeeId}/{taxYear}")
    public Result<Void> resetYear(@PathVariable String employeeId, @PathVariable int taxYear) {
        taxCalculationService.resetYear(employeeId, taxYear);
        return Result.success();
    }
}
