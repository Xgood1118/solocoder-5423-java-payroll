package com.hrpayroll.config;

import com.hrpayroll.attendance.entity.Holiday;
import com.hrpayroll.attendance.entity.MonthlyAttendance;
import com.hrpayroll.attendance.service.HolidayService;
import com.hrpayroll.attendance.service.AttendanceService;
import com.hrpayroll.salary.entity.CityStandard;
import com.hrpayroll.salary.entity.Employee;
import com.hrpayroll.salary.entity.PositionGrade;
import com.hrpayroll.salary.entity.SpecialDeduction;
import com.hrpayroll.salary.service.CityStandardService;
import com.hrpayroll.salary.service.EmployeeService;
import com.hrpayroll.salary.service.PositionGradeService;
import com.hrpayroll.salary.service.SpecialDeductionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PositionGradeService positionGradeService;

    @Autowired
    private CityStandardService cityStandardService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private SpecialDeductionService specialDeductionService;

    @Override
    public void run(String... args) {
        initPositionGrades();
        initCityStandards();
        initHolidays();
        initEmployees();
        initAttendanceData();
        initSpecialDeductions();
        log.info("数据初始化完成");
    }

    private void initPositionGrades() {
        if (!positionGradeService.listAll().isEmpty()) {
            return;
        }

        PositionGrade executive = new PositionGrade();
        executive.setGradeCode("EXECUTIVE");
        executive.setGradeName("高管岗");
        executive.setBaseSalaryRatio(new BigDecimal("0.70"));
        executive.setPerformanceRatio(new BigDecimal("0.30"));
        executive.setDescription("高级管理岗位，基本70%绩效30%");
        executive.setEnabled(true);
        positionGradeService.create(executive);

        PositionGrade management = new PositionGrade();
        management.setGradeCode("MANAGEMENT");
        management.setGradeName("管理岗");
        management.setBaseSalaryRatio(new BigDecimal("0.60"));
        management.setPerformanceRatio(new BigDecimal("0.40"));
        management.setDescription("管理岗位，基本60%绩效40%");
        management.setEnabled(true);
        positionGradeService.create(management);

        PositionGrade professional = new PositionGrade();
        professional.setGradeCode("PROFESSIONAL");
        professional.setGradeName("专技岗");
        professional.setBaseSalaryRatio(new BigDecimal("0.50"));
        professional.setPerformanceRatio(new BigDecimal("0.50"));
        professional.setDescription("专业技术岗位，基本50%绩效50%");
        professional.setEnabled(true);
        positionGradeService.create(professional);

        PositionGrade operation = new PositionGrade();
        operation.setGradeCode("OPERATION");
        operation.setGradeName("操作岗");
        operation.setBaseSalaryRatio(new BigDecimal("0.80"));
        operation.setPerformanceRatio(new BigDecimal("0.20"));
        operation.setDescription("操作岗位，基本80%绩效20%");
        operation.setEnabled(true);
        positionGradeService.create(operation);

        log.info("初始化4个职级配置完成");
    }

    private void initCityStandards() {
        if (!cityStandardService.listAll().isEmpty()) {
            return;
        }

        int year = LocalDate.now().getYear();

        CityStandard shenzhen = new CityStandard();
        shenzhen.setCityCode("SZ");
        shenzhen.setCityName("深圳");
        shenzhen.setSocialSecurityMinBase(new BigDecimal("2360"));
        shenzhen.setSocialSecurityMaxBase(new BigDecimal("12960"));
        shenzhen.setSocialSecurityPersonalRate(new BigDecimal("0.085"));
        shenzhen.setSocialSecurityCompanyRate(new BigDecimal("0.155"));
        shenzhen.setYear(year);
        shenzhen.setEnabled(true);
        cityStandardService.create(shenzhen);

        CityStandard shanghai = new CityStandard();
        shanghai.setCityCode("SH");
        shanghai.setCityName("上海");
        shanghai.setSocialSecurityMinBase(new BigDecimal("7310"));
        shanghai.setSocialSecurityMaxBase(new BigDecimal("36549"));
        shanghai.setSocialSecurityPersonalRate(new BigDecimal("0.105"));
        shanghai.setSocialSecurityCompanyRate(new BigDecimal("0.275"));
        shanghai.setYear(year);
        shanghai.setEnabled(true);
        cityStandardService.create(shanghai);

        CityStandard beijing = new CityStandard();
        beijing.setCityCode("BJ");
        beijing.setCityName("北京");
        beijing.setSocialSecurityMinBase(new BigDecimal("6326"));
        beijing.setSocialSecurityMaxBase(new BigDecimal("33891"));
        beijing.setSocialSecurityPersonalRate(new BigDecimal("0.102"));
        beijing.setSocialSecurityCompanyRate(new BigDecimal("0.252"));
        beijing.setYear(year);
        beijing.setEnabled(true);
        cityStandardService.create(beijing);

        log.info("初始化3个城市社保标准完成");
    }

    private void initHolidays() {
        int year = LocalDate.now().getYear();
        if (!holidayService.listByYear(year).isEmpty()) {
            return;
        }

        Holiday newYear = new Holiday();
        newYear.setHolidayName("元旦");
        newYear.setHolidayDate(LocalDate.of(year, 1, 1));
        newYear.setType("法定节假日");
        holidayService.create(newYear);

        Holiday springFestival1 = new Holiday();
        springFestival1.setHolidayName("春节");
        springFestival1.setHolidayDate(LocalDate.of(year, 2, 10));
        springFestival1.setType("法定节假日");
        holidayService.create(springFestival1);

        Holiday springFestival2 = new Holiday();
        springFestival2.setHolidayName("春节");
        springFestival2.setHolidayDate(LocalDate.of(year, 2, 11));
        springFestival2.setType("法定节假日");
        holidayService.create(springFestival2);

        Holiday qingming = new Holiday();
        qingming.setHolidayName("清明节");
        qingming.setHolidayDate(LocalDate.of(year, 4, 4));
        qingming.setType("法定节假日");
        holidayService.create(qingming);

        Holiday laborDay = new Holiday();
        laborDay.setHolidayName("劳动节");
        laborDay.setHolidayDate(LocalDate.of(year, 5, 1));
        laborDay.setType("法定节假日");
        holidayService.create(laborDay);

        Holiday dragonBoat = new Holiday();
        dragonBoat.setHolidayName("端午节");
        dragonBoat.setHolidayDate(LocalDate.of(year, 6, 10));
        dragonBoat.setType("法定节假日");
        holidayService.create(dragonBoat);

        Holiday midAutumn = new Holiday();
        midAutumn.setHolidayName("中秋节");
        midAutumn.setHolidayDate(LocalDate.of(year, 9, 17));
        midAutumn.setType("法定节假日");
        holidayService.create(midAutumn);

        Holiday nationalDay1 = new Holiday();
        nationalDay1.setHolidayName("国庆节");
        nationalDay1.setHolidayDate(LocalDate.of(year, 10, 1));
        nationalDay1.setType("法定节假日");
        holidayService.create(nationalDay1);

        Holiday nationalDay2 = new Holiday();
        nationalDay2.setHolidayName("国庆节");
        nationalDay2.setHolidayDate(LocalDate.of(year, 10, 2));
        nationalDay2.setType("法定节假日");
        holidayService.create(nationalDay2);

        Holiday nationalDay3 = new Holiday();
        nationalDay3.setHolidayName("国庆节");
        nationalDay3.setHolidayDate(LocalDate.of(year, 10, 3));
        nationalDay3.setType("法定节假日");
        holidayService.create(nationalDay3);

        log.info("初始化节假日数据完成");
    }

    private void initEmployees() {
        if (!employeeService.listAll().isEmpty()) {
            return;
        }

        Employee e1 = new Employee();
        e1.setEmployeeNo("E001");
        e1.setName("张三");
        e1.setIdCard("440301199001011234");
        e1.setGender("男");
        e1.setPhone("13800138001");
        e1.setEmail("zhangsan@example.com");
        e1.setDepartment("技术部");
        e1.setPosition("技术总监");
        e1.setGradeCode("EXECUTIVE");
        e1.setCityCode("SZ");
        e1.setHireDate(LocalDate.of(2018, 3, 15));
        e1.setBaseSalary(new BigDecimal("30000"));
        e1.setPostSalary(new BigDecimal("5000"));
        e1.setHousingFundRate(new BigDecimal("0.12"));
        e1.setBankAccount("6222021234567890123");
        e1.setBankName("工商银行深圳分行");
        e1.setActive(true);
        employeeService.create(e1);

        Employee e2 = new Employee();
        e2.setEmployeeNo("E002");
        e2.setName("李四");
        e2.setIdCard("310101198805054321");
        e2.setGender("女");
        e2.setPhone("13900139002");
        e2.setEmail("lisi@example.com");
        e2.setDepartment("人力资源部");
        e2.setPosition("HR经理");
        e2.setGradeCode("MANAGEMENT");
        e2.setCityCode("SH");
        e2.setHireDate(LocalDate.of(2020, 1, 10));
        e2.setBaseSalary(new BigDecimal("18000"));
        e2.setPostSalary(new BigDecimal("2000"));
        e2.setHousingFundRate(new BigDecimal("0.10"));
        e2.setBankAccount("6228489876543210987");
        e2.setBankName("招商银行上海分行");
        e2.setActive(true);
        employeeService.create(e2);

        Employee e3 = new Employee();
        e3.setEmployeeNo("E003");
        e3.setName("王五");
        e3.setIdCard("110101199512125678");
        e3.setGender("男");
        e3.setPhone("13700137003");
        e3.setEmail("wangwu@example.com");
        e3.setDepartment("研发部");
        e3.setPosition("高级工程师");
        e3.setGradeCode("PROFESSIONAL");
        e3.setCityCode("BJ");
        e3.setHireDate(LocalDate.of(2021, 7, 1));
        e3.setBaseSalary(new BigDecimal("20000"));
        e3.setPostSalary(new BigDecimal("1500"));
        e3.setHousingFundRate(new BigDecimal("0.12"));
        e3.setBankAccount("6217001122334455667");
        e3.setBankName("建设银行北京分行");
        e3.setActive(true);
        employeeService.create(e3);

        Employee e4 = new Employee();
        e4.setEmployeeNo("E004");
        e4.setName("赵六");
        e4.setIdCard("440301199903038765");
        e4.setGender("男");
        e4.setPhone("13600136004");
        e4.setEmail("zhaoliu@example.com");
        e4.setDepartment("运营部");
        e4.setPosition("运营专员");
        e4.setGradeCode("OPERATION");
        e4.setCityCode("SZ");
        e4.setHireDate(LocalDate.of(2023, 5, 20));
        e4.setBaseSalary(new BigDecimal("8000"));
        e4.setPostSalary(new BigDecimal("500"));
        e4.setHousingFundRate(new BigDecimal("0.05"));
        e4.setBankAccount("6225886677889900112");
        e4.setBankName("招商银行深圳分行");
        e4.setActive(true);
        employeeService.create(e4);

        Employee e5 = new Employee();
        e5.setEmployeeNo("E005");
        e5.setName("钱七");
        e5.setIdCard("440301199202026543");
        e5.setGender("女");
        e5.setPhone("13500135005");
        e5.setEmail("qianqi@example.com");
        e5.setDepartment("财务部");
        e5.setPosition("会计");
        e5.setGradeCode("PROFESSIONAL");
        e5.setCityCode("SZ");
        e5.setHireDate(LocalDate.of(2019, 9, 1));
        e5.setBaseSalary(new BigDecimal("12000"));
        e5.setPostSalary(new BigDecimal("1000"));
        e5.setHousingFundRate(new BigDecimal("0.08"));
        e5.setBankAccount("6222021234567890555");
        e5.setBankName("工商银行深圳分行");
        e5.setActive(true);
        employeeService.create(e5);

        log.info("初始化5个测试员工完成");
    }

    private void initAttendanceData() {
        if (!attendanceService.listByMonth("2026-05").isEmpty()) {
            return;
        }

        Employee e1 = employeeService.getByEmployeeNo("E001");
        Employee e2 = employeeService.getByEmployeeNo("E002");
        Employee e3 = employeeService.getByEmployeeNo("E003");
        Employee e4 = employeeService.getByEmployeeNo("E004");
        Employee e5 = employeeService.getByEmployeeNo("E005");

        MonthlyAttendance a1 = new MonthlyAttendance();
        a1.setEmployeeId(e1.getId());
        a1.setYearMonth("2026-05");
        a1.setWorkDays(new BigDecimal("21.75"));
        a1.setLeaveDays(BigDecimal.ZERO);
        a1.setSickLeaveDays(BigDecimal.ZERO);
        a1.setPersonalLeaveDays(BigDecimal.ZERO);
        a1.setWorkdayOvertimeHours(new BigDecimal("8"));
        a1.setWeekendOvertimeHours(new BigDecimal("4"));
        a1.setHolidayOvertimeHours(BigDecimal.ZERO);
        attendanceService.save(a1);

        MonthlyAttendance a2 = new MonthlyAttendance();
        a2.setEmployeeId(e2.getId());
        a2.setYearMonth("2026-05");
        a2.setWorkDays(new BigDecimal("21.75"));
        a2.setLeaveDays(new BigDecimal("3"));
        a2.setSickLeaveDays(new BigDecimal("1"));
        a2.setPersonalLeaveDays(new BigDecimal("2"));
        a2.setWorkdayOvertimeHours(new BigDecimal("10"));
        a2.setWeekendOvertimeHours(BigDecimal.ZERO);
        a2.setHolidayOvertimeHours(BigDecimal.ZERO);
        attendanceService.save(a2);

        MonthlyAttendance a3 = new MonthlyAttendance();
        a3.setEmployeeId(e3.getId());
        a3.setYearMonth("2026-05");
        a3.setWorkDays(new BigDecimal("21.75"));
        a3.setLeaveDays(BigDecimal.ZERO);
        a3.setSickLeaveDays(BigDecimal.ZERO);
        a3.setPersonalLeaveDays(BigDecimal.ZERO);
        a3.setWorkdayOvertimeHours(new BigDecimal("20"));
        a3.setWeekendOvertimeHours(new BigDecimal("16"));
        a3.setHolidayOvertimeHours(BigDecimal.ZERO);
        attendanceService.save(a3);

        MonthlyAttendance a4 = new MonthlyAttendance();
        a4.setEmployeeId(e4.getId());
        a4.setYearMonth("2026-05");
        a4.setWorkDays(new BigDecimal("21.75"));
        a4.setLeaveDays(BigDecimal.ZERO);
        a4.setSickLeaveDays(BigDecimal.ZERO);
        a4.setPersonalLeaveDays(BigDecimal.ZERO);
        a4.setWorkdayOvertimeHours(new BigDecimal("5"));
        a4.setWeekendOvertimeHours(BigDecimal.ZERO);
        a4.setHolidayOvertimeHours(BigDecimal.ZERO);
        attendanceService.save(a4);

        MonthlyAttendance a5 = new MonthlyAttendance();
        a5.setEmployeeId(e5.getId());
        a5.setYearMonth("2026-05");
        a5.setWorkDays(new BigDecimal("21.75"));
        a5.setLeaveDays(new BigDecimal("2"));
        a5.setSickLeaveDays(BigDecimal.ZERO);
        a5.setPersonalLeaveDays(new BigDecimal("2"));
        a5.setWorkdayOvertimeHours(BigDecimal.ZERO);
        a5.setWeekendOvertimeHours(new BigDecimal("8"));
        a5.setHolidayOvertimeHours(BigDecimal.ZERO);
        attendanceService.save(a5);

        log.info("初始化考勤数据完成");
    }

    private void initSpecialDeductions() {
        Employee e1 = employeeService.getByEmployeeNo("E001");
        Employee e3 = employeeService.getByEmployeeNo("E003");
        Employee e5 = employeeService.getByEmployeeNo("E005");

        SpecialDeduction sd1 = new SpecialDeduction();
        sd1.setEmployeeId(e1.getId());
        sd1.setEffectYearMonth("2026-05");
        sd1.setChildrenEducation(new BigDecimal("1000"));
        sd1.setHousingLoanInterest(new BigDecimal("1000"));
        sd1.setElderlySupport(new BigDecimal("2000"));
        specialDeductionService.save(sd1);

        SpecialDeduction sd3 = new SpecialDeduction();
        sd3.setEmployeeId(e3.getId());
        sd3.setEffectYearMonth("2026-05");
        sd3.setHousingRent(new BigDecimal("1500"));
        specialDeductionService.save(sd3);

        SpecialDeduction sd5 = new SpecialDeduction();
        sd5.setEmployeeId(e5.getId());
        sd5.setEffectYearMonth("2026-05");
        sd5.setChildrenEducation(new BigDecimal("2000"));
        sd5.setElderlySupport(new BigDecimal("1000"));
        specialDeductionService.save(sd5);

        log.info("初始化专项附加扣除数据完成");
    }
}
