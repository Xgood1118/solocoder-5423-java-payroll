package com.hrpayroll.calc.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.calc.entity.PayrollCalculation;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class PayrollCalculationRepository extends InMemoryRepository<PayrollCalculation> {

    public Optional<PayrollCalculation> findByEmployeeIdAndYearMonth(String employeeId, String yearMonth) {
        return storage.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()) && yearMonth.equals(p.getYearMonth()))
                .findFirst();
    }

    public List<PayrollCalculation> findByYearMonth(String yearMonth) {
        return storage.values().stream()
                .filter(p -> yearMonth.equals(p.getYearMonth()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<PayrollCalculation> findByEmployeeId(String employeeId) {
        return storage.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()))
                .sorted(Comparator.comparing(PayrollCalculation::getYearMonth).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<PayrollCalculation> findByYearMonthAndReviewStatus(String yearMonth, String reviewStatus) {
        return storage.values().stream()
                .filter(p -> yearMonth.equals(p.getYearMonth()) && reviewStatus.equals(p.getReviewStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<PayrollCalculation> findByEmployeeIdFromMonth(String employeeId, String startYearMonth) {
        return storage.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId())
                        && p.getYearMonth().compareTo(startYearMonth) >= 0)
                .sorted(Comparator.comparing(PayrollCalculation::getYearMonth))
                .collect(java.util.stream.Collectors.toList());
    }
}
