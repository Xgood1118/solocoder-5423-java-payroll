package com.hrpayroll.salary.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.salary.entity.SpecialDeduction;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class SpecialDeductionRepository extends InMemoryRepository<SpecialDeduction> {

    public List<SpecialDeduction> findByEmployeeId(String employeeId) {
        return storage.values().stream()
                .filter(d -> employeeId.equals(d.getEmployeeId()))
                .sorted(Comparator.comparing(SpecialDeduction::getEffectYearMonth))
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<SpecialDeduction> findByEmployeeIdAndEffectYearMonth(String employeeId, String yearMonth) {
        return storage.values().stream()
                .filter(d -> employeeId.equals(d.getEmployeeId()) && yearMonth.equals(d.getEffectYearMonth()))
                .findFirst();
    }
}
