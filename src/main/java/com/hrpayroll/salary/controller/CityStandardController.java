package com.hrpayroll.salary.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.salary.entity.CityStandard;
import com.hrpayroll.salary.service.CityStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary/city-standards")
public class CityStandardController {

    @Autowired
    private CityStandardService cityStandardService;

    @PostMapping
    public Result<CityStandard> create(@RequestBody CityStandard standard) {
        return Result.success(cityStandardService.create(standard));
    }

    @PutMapping("/{id}")
    public Result<CityStandard> update(@PathVariable String id, @RequestBody CityStandard standard) {
        return Result.success(cityStandardService.update(id, standard));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        cityStandardService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CityStandard> getById(@PathVariable String id) {
        return Result.success(cityStandardService.getById(id));
    }

    @GetMapping("/city/{cityCode}/{year}")
    public Result<CityStandard> getByCityAndYear(@PathVariable String cityCode, @PathVariable int year) {
        return Result.success(cityStandardService.getByCityAndYear(cityCode, year));
    }

    @GetMapping
    public Result<List<CityStandard>> list(@RequestParam(required = false) Integer year) {
        if (year != null) {
            return Result.success(cityStandardService.listByYear(year));
        }
        return Result.success(cityStandardService.listAll());
    }
}
