package com.hrpayroll.salary.controller;

import com.hrpayroll.common.Result;
import com.hrpayroll.salary.entity.PositionGrade;
import com.hrpayroll.salary.service.PositionGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary/position-grades")
public class PositionGradeController {

    @Autowired
    private PositionGradeService positionGradeService;

    @PostMapping
    public Result<PositionGrade> create(@RequestBody PositionGrade grade) {
        return Result.success(positionGradeService.create(grade));
    }

    @PutMapping("/{id}")
    public Result<PositionGrade> update(@PathVariable String id, @RequestBody PositionGrade grade) {
        return Result.success(positionGradeService.update(id, grade));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        positionGradeService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<PositionGrade> getById(@PathVariable String id) {
        return Result.success(positionGradeService.getById(id));
    }

    @GetMapping("/code/{gradeCode}")
    public Result<PositionGrade> getByCode(@PathVariable String gradeCode) {
        return Result.success(positionGradeService.getByCode(gradeCode));
    }

    @GetMapping
    public Result<List<PositionGrade>> list() {
        return Result.success(positionGradeService.listAll());
    }
}
