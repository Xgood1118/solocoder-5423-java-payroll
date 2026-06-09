package com.hrpayroll.bonus.repository;

import com.hrpayroll.bonus.entity.YearEndBonus;
import com.hrpayroll.common.InMemoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class YearEndBonusRepository extends InMemoryRepository<YearEndBonus> {

    public Optional<YearEndBonus> findByEmployeeIdAndTaxYear(String employeeId, int taxYear) {
        return storage.values().stream()
                .filter(b -> employeeId.equals(b.getEmployeeId()) && b.getTaxYear() == taxYear)
                .findFirst();
    }

    public List<YearEndBonus> findByTaxYear(int taxYear) {
        return storage.values().stream()
                .filter(b -> b.getTaxYear() == taxYear)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<YearEndBonus> findByEmployeeId(String employeeId) {
        return storage.values().stream()
                .filter(b -> employeeId.equals(b.getEmployeeId()))
                .sorted(java.util.Comparator.comparing(YearEndBonus::getTaxYear).reversed())
                .collect(java.util.stream.Collectors.toList());
    }
}
