package com.hrpayroll.attendance.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.attendance.entity.MonthlyAttendance;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MonthlyAttendanceRepository extends InMemoryRepository<MonthlyAttendance> {

    public Optional<MonthlyAttendance> findByEmployeeIdAndYearMonth(String employeeId, String yearMonth) {
        return storage.values().stream()
                .filter(a -> employeeId.equals(a.getEmployeeId()) && yearMonth.equals(a.getYearMonth()))
                .findFirst();
    }

    public List<MonthlyAttendance> findByYearMonth(String yearMonth) {
        return storage.values().stream()
                .filter(a -> yearMonth.equals(a.getYearMonth()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<MonthlyAttendance> findByEmployeeId(String employeeId) {
        return storage.values().stream()
                .filter(a -> employeeId.equals(a.getEmployeeId()))
                .sorted(java.util.Comparator.comparing(MonthlyAttendance::getYearMonth).reversed())
                .collect(java.util.stream.Collectors.toList());
    }
}
