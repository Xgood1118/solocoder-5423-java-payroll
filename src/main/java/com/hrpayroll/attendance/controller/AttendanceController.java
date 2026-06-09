package com.hrpayroll.attendance.controller;

import com.hrpayroll.attendance.entity.MonthlyAttendance;
import com.hrpayroll.attendance.service.AttendanceService;
import com.hrpayroll.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/monthly")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping
    public Result<MonthlyAttendance> save(@RequestBody MonthlyAttendance attendance) {
        return Result.success(attendanceService.save(attendance));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        attendanceService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<MonthlyAttendance> getById(@PathVariable String id) {
        return Result.success(attendanceService.getById(id));
    }

    @GetMapping("/employee/{employeeId}/{yearMonth}")
    public Result<MonthlyAttendance> getByEmployeeAndMonth(
            @PathVariable String employeeId,
            @PathVariable String yearMonth) {
        return Result.success(attendanceService.getByEmployeeAndMonth(employeeId, yearMonth));
    }

    @GetMapping("/month/{yearMonth}")
    public Result<List<MonthlyAttendance>> listByMonth(@PathVariable String yearMonth) {
        return Result.success(attendanceService.listByMonth(yearMonth));
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<MonthlyAttendance>> listByEmployee(@PathVariable String employeeId) {
        return Result.success(attendanceService.listByEmployee(employeeId));
    }
}
