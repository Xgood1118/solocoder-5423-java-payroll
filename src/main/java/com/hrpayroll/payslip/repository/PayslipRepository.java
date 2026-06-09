package com.hrpayroll.payslip.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.payslip.entity.Payslip;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class PayslipRepository extends InMemoryRepository<Payslip> {

    public Optional<Payslip> findByEmployeeIdAndYearMonth(String employeeId, String yearMonth) {
        return storage.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()) && yearMonth.equals(p.getYearMonth()))
                .findFirst();
    }

    public List<Payslip> findByYearMonth(String yearMonth) {
        return storage.values().stream()
                .filter(p -> yearMonth.equals(p.getYearMonth()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Payslip> findByYearMonthAndStatus(String yearMonth, String status) {
        return storage.values().stream()
                .filter(p -> yearMonth.equals(p.getYearMonth()) && status.equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Payslip> findByEmployeeId(String employeeId) {
        return storage.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()))
                .sorted(Comparator.comparing(Payslip::getYearMonth).reversed())
                .collect(java.util.stream.Collectors.toList());
    }
}
