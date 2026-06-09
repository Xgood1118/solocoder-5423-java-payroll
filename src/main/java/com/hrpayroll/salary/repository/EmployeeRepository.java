package com.hrpayroll.salary.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.salary.entity.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository extends InMemoryRepository<Employee> {

    public Optional<Employee> findByEmployeeNo(String employeeNo) {
        return storage.values().stream()
                .filter(e -> employeeNo.equals(e.getEmployeeNo()))
                .findFirst();
    }

    public List<Employee> findByActive(boolean active) {
        return storage.values().stream()
                .filter(e -> e.isActive() == active)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Employee> findByDepartment(String department) {
        return storage.values().stream()
                .filter(e -> department.equals(e.getDepartment()))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean existsByEmployeeNo(String employeeNo) {
        return storage.values().stream()
                .anyMatch(e -> employeeNo.equals(e.getEmployeeNo()));
    }
}
