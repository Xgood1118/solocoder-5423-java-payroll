package com.hrpayroll.salary.service;

import com.hrpayroll.common.BusinessException;
import com.hrpayroll.salary.entity.SpecialDeduction;
import com.hrpayroll.salary.repository.SpecialDeductionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialDeductionService {

    @Autowired
    private SpecialDeductionRepository specialDeductionRepository;

    public SpecialDeduction save(SpecialDeduction deduction) {
        if (deduction.getEmployeeId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (deduction.getEffectYearMonth() == null) {
            throw new BusinessException("生效年月不能为空");
        }
        return specialDeductionRepository.save(deduction);
    }

    public void delete(String id) {
        if (!specialDeductionRepository.existsById(id)) {
            throw new BusinessException("专项附加扣除记录不存在");
        }
        specialDeductionRepository.deleteById(id);
    }

    public SpecialDeduction getById(String id) {
        return specialDeductionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("专项附加扣除记录不存在"));
    }

    public List<SpecialDeduction> listByEmployee(String employeeId) {
        return specialDeductionRepository.findByEmployeeId(employeeId);
    }

    public SpecialDeduction getByEmployeeAndMonth(String employeeId, String yearMonth) {
        return specialDeductionRepository.findByEmployeeIdAndEffectYearMonth(employeeId, yearMonth)
                .orElse(null);
    }
}
