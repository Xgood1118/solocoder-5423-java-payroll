package com.hrpayroll.attendance.controller;

import com.hrpayroll.attendance.entity.Holiday;
import com.hrpayroll.attendance.service.HolidayService;
import com.hrpayroll.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @PostMapping
    public Result<Holiday> create(@RequestBody Holiday holiday) {
        return Result.success(holidayService.create(holiday));
    }

    @PostMapping("/batch")
    public Result<Integer> batchCreate(@RequestBody List<Holiday> holidays) {
        return Result.success(holidayService.batchCreate(holidays));
    }

    @PutMapping("/{id}")
    public Result<Holiday> update(@PathVariable String id, @RequestBody Holiday holiday) {
        return Result.success(holidayService.update(id, holiday));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        holidayService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Holiday> getById(@PathVariable String id) {
        return Result.success(holidayService.getById(id));
    }

    @GetMapping("/year/{year}")
    public Result<List<Holiday>> listByYear(@PathVariable int year) {
        return Result.success(holidayService.listByYear(year));
    }

    @GetMapping("/range")
    public Result<List<Holiday>> listByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(holidayService.listByDateRange(start, end));
    }

    @GetMapping("/check/{date}")
    public Result<Boolean> isHoliday(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(holidayService.isHoliday(date));
    }
}
