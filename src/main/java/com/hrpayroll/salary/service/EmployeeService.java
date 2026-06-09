package com.hrpayroll.salary.service;

import com.hrpayroll.common.BusinessException;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee create(Employee employee) {
        if (employeeRepository.existsByEmployeeNo(employee.getEmployeeNo())) {
            throw new BusinessException("员工工号已存在");
        }
        validateEmployee(employee);
        employee.setActive(true);
        return employeeRepository.save(employee);
    }

    public Employee update(String id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        if (!existing.getEmployeeNo().equals(employee.getEmployeeNo())
                && employeeRepository.existsByEmployeeNo(employee.getEmployeeNo())) {
            throw new BusinessException("员工工号已存在");
        }
        validateEmployee(employee);
        existing.setName(employee.getName());
        existing.setIdCard(employee.getIdCard());
        existing.setGender(employee.getGender());
        existing.setPhone(employee.getPhone());
        existing.setEmail(employee.getEmail());
        existing.setDepartment(employee.getDepartment());
        existing.setPosition(employee.getPosition());
        existing.setGradeCode(employee.getGradeCode());
        existing.setCityCode(employee.getCityCode());
        existing.setHireDate(employee.getHireDate());
        existing.setBaseSalary(employee.getBaseSalary());
        existing.setPostSalary(employee.getPostSalary());
        existing.setHousingFundRate(employee.getHousingFundRate());
        existing.setBankAccount(employee.getBankAccount());
        existing.setBankName(employee.getBankName());
        existing.setActive(employee.isActive());
        return employeeRepository.save(existing);
    }

    public void delete(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new BusinessException("员工不存在");
        }
        employeeRepository.deleteById(id);
    }

    public Employee getById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
    }

    public Employee getByEmployeeNo(String employeeNo) {
        return employeeRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new BusinessException("员工不存在: " + employeeNo));
    }

    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> listActive() {
        return employeeRepository.findByActive(true);
    }

    public List<Employee> listByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    private void validateEmployee(Employee employee) {
        if (employee.getHousingFundRate() != null) {
            BigDecimal minRate = new BigDecimal("0.05");
            BigDecimal maxRate = new BigDecimal("0.12");
            if (employee.getHousingFundRate().compareTo(minRate) < 0
                    || employee.getHousingFundRate().compareTo(maxRate) > 0) {
                throw new BusinessException("公积金比例必须在 5% 到 12% 之间");
            }
        }
    }
}
