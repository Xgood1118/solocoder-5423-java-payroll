package com.hrpayroll.salary.service;

import com.hrpayroll.common.BusinessException;
import com.hrpayroll.salary.entity.PositionGrade;
import com.hrpayroll.salary.repository.PositionGradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PositionGradeService {

    @Autowired
    private PositionGradeRepository positionGradeRepository;

    public PositionGrade create(PositionGrade grade) {
        if (positionGradeRepository.existsByGradeCode(grade.getGradeCode())) {
            throw new BusinessException("职级编码已存在");
        }
        validateRatios(grade.getBaseSalaryRatio(), grade.getPerformanceRatio());
        grade.setEnabled(true);
        return positionGradeRepository.save(grade);
    }

    public PositionGrade update(String id, PositionGrade grade) {
        PositionGrade existing = positionGradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职级不存在"));
        if (!existing.getGradeCode().equals(grade.getGradeCode())
                && positionGradeRepository.existsByGradeCode(grade.getGradeCode())) {
            throw new BusinessException("职级编码已存在");
        }
        validateRatios(grade.getBaseSalaryRatio(), grade.getPerformanceRatio());
        existing.setGradeName(grade.getGradeName());
        existing.setBaseSalaryRatio(grade.getBaseSalaryRatio());
        existing.setPerformanceRatio(grade.getPerformanceRatio());
        existing.setDescription(grade.getDescription());
        existing.setEnabled(grade.isEnabled());
        return positionGradeRepository.save(existing);
    }

    public void delete(String id) {
        if (!positionGradeRepository.existsById(id)) {
            throw new BusinessException("职级不存在");
        }
        positionGradeRepository.deleteById(id);
    }

    public PositionGrade getById(String id) {
        return positionGradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职级不存在"));
    }

    public PositionGrade getByCode(String gradeCode) {
        return positionGradeRepository.findByGradeCode(gradeCode)
                .orElseThrow(() -> new BusinessException("职级不存在: " + gradeCode));
    }

    public List<PositionGrade> listAll() {
        return positionGradeRepository.findAll();
    }

    private void validateRatios(BigDecimal base, BigDecimal performance) {
        if (base == null || performance == null) {
            throw new BusinessException("薪资比例不能为空");
        }
        if (base.add(performance).compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException("基本工资比例 + 绩效工资比例必须等于 100%");
        }
    }
}
