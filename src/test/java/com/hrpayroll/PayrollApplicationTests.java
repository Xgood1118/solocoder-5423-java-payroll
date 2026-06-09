package com.hrpayroll;

import com.hrpayroll.calc.entity.PayrollCalculation;
import com.hrpayroll.calc.service.PayrollCalculationService;
import com.hrpayroll.tax.entity.TaxBracket;
import com.hrpayroll.tax.service.TaxCalculationService;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.service.EmployeeService;
import com.hrpayroll.common.PayrollUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PayrollApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    @Test
    void contextLoads() {
    }

    @Test
    void testTaxBrackets() {
        List<TaxBracket> brackets = TaxCalculationService.getTaxBrackets();
        assertEquals(7, brackets.size());
        assertEquals(new BigDecimal("0.03"), brackets.get(0).getRate());
        assertEquals(new BigDecimal("0.45"), brackets.get(6).getRate());
    }

    @Test
    void testTaxCalculation() {
        BigDecimal tax = TaxCalculationService.calculateTax(new BigDecimal("36000"));
        assertEquals(new BigDecimal("1080.00"), tax);

        BigDecimal tax2 = TaxCalculationService.calculateTax(new BigDecimal("144000"));
        assertEquals(new BigDecimal("11880.00"), tax2);

        BigDecimal tax3 = TaxCalculationService.calculateTax(new BigDecimal("300000"));
        assertEquals(new BigDecimal("43080.00"), tax3);
    }

    @Test
    void testUtils() {
        int months = PayrollUtils.monthsBetween(
                java.time.LocalDate.of(2020, 1, 1),
                java.time.LocalDate.of(2023, 1, 1));
        assertEquals(36, months);

        String password = PayrollUtils.maskIdCardLast6("440301199001011234");
        assertEquals("011234", password);
    }

    @Test
    void testEmployeeList() {
        List<Employee> employees = employeeService.listActive();
        assertTrue(employees.size() >= 5);
    }

    @Test
    void testPayrollCalculation() {
        Employee employee = employeeService.getByEmployeeNo("E001");
        assertNotNull(employee);

        PayrollCalculation calc = payrollCalculationService.calculate(employee.getId(), "2026-05");
        assertNotNull(calc);
        assertNotNull(calc.getBaseSalary());
        assertNotNull(calc.getPerformanceSalary());
        assertNotNull(calc.getNetPay());

        assertTrue(calc.getGrossPay().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(calc.getNetPay().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(calc.getGrossPay().compareTo(calc.getNetPay()) > 0);

        assertEquals("CALCULATED", calc.getCalcStatus());
        assertEquals("PENDING", calc.getReviewStatus());
    }

    @Test
    void testSeniorityCalculation() {
        Employee employee = employeeService.getByEmployeeNo("E004");
        PayrollCalculation calc = payrollCalculationService.calculate(employee.getId(), "2026-05");
        assertTrue(calc.getSeniorityYears() >= 3);
        assertTrue(calc.getSenioritySalary().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testOvertimeCalculation() {
        Employee employee = employeeService.getByEmployeeNo("E003");
        PayrollCalculation calc = payrollCalculationService.calculate(employee.getId(), "2026-05");
        assertTrue(calc.getOvertimePay().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(calc.getOvertimeHourBase());
    }

    @Test
    void testReviewProcess() {
        Employee employee = employeeService.getByEmployeeNo("E002");
        String employeeId = employee.getId();

        PayrollCalculation calc = payrollCalculationService.calculate(employeeId, "2026-05");
        assertEquals("PENDING", calc.getReviewStatus());

        PayrollCalculation approved = payrollCalculationService.approve(employeeId, "2026-05");
        assertEquals("APPROVED", approved.getReviewStatus());

        PayrollCalculation revoked = payrollCalculationService.revokeApproval(employeeId, "2026-05");
        assertEquals("PENDING", revoked.getReviewStatus());
    }

    @Test
    void testAnomalyDetection() {
        Employee employee = employeeService.getByEmployeeNo("E003");
        PayrollCalculation calc = payrollCalculationService.calculate(employee.getId(), "2026-05");
        assertNotNull(calc.getAnomalies());
    }
}
