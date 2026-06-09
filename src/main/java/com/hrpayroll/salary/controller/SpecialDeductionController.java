package com.hrpayroll.salary.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.salary.entity.SpecialDeduction;
import com.hrpayroll.salary.service.SpecialDeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary/special-deductions")
public class SpecialDeductionController {

    @Autowired
    private SpecialDeductionService specialDeductionService;

    @PostMapping
    public Result<SpecialDeduction> save(@RequestBody SpecialDeduction deduction) {
        return Result.success(specialDeductionService.save(deduction));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        specialDeductionService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SpecialDeduction> getById(@PathVariable String id) {
        return Result.success(specialDeductionService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<SpecialDeduction>> listByEmployee(@PathVariable String employeeId) {
        return Result.success(specialDeductionService.listByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/{yearMonth}")
    public Result<SpecialDeduction> getByMonth(@PathVariable String employeeId, @PathVariable String yearMonth) {
        return Result.success(specialDeductionService.getByEmployeeAndMonth(employeeId, yearMonth));
    }
}
