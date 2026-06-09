package com.hrpayroll.salary.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public Result<Employee> create(@RequestBody Employee employee) {
        return Result.success(employeeService.create(employee));
    }

    @PutMapping("/{id}")
    public Result<Employee> update(@PathVariable String id, @RequestBody Employee employee) {
        return Result.success(employeeService.update(id, employee));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        employeeService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable String id) {
        return Result.success(employeeService.getById(id));
    }

    @GetMapping("/no/{employeeNo}")
    public Result<Employee> getByNo(@PathVariable String employeeNo) {
        return Result.success(employeeService.getByEmployeeNo(employeeNo));
    }

    @GetMapping
    public Result<List<Employee>> list(
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        if (department != null) {
            return Result.success(employeeService.listByDepartment(department));
        }
        if (activeOnly) {
            return Result.success(employeeService.listActive());
        }
        return Result.success(employeeService.listAll());
    }
}
