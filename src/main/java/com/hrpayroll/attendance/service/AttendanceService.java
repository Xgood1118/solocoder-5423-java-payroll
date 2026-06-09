package com.hrpayroll.attendance.service;

import com.hrpayroll.attendance.entity.MonthlyAttendance;
import com.hrpayroll.attendance.repository.MonthlyAttendanceRepository;
import com.hrpayroll.common.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private MonthlyAttendanceRepository attendanceRepository;

    public MonthlyAttendance save(MonthlyAttendance attendance) {
        if (attendance.getEmployeeId() == null || attendance.getYearMonth() == null) {
            throw new BusinessException("员工ID和年月不能为空");
        }
        attendanceRepository.findByEmployeeIdAndYearMonth(
                attendance.getEmployeeId(), attendance.getYearMonth())
                .ifPresent(existing -> {
                    attendance.setId(existing.getId());
                    attendance.setCreateTime(existing.getCreateTime());
                });
        return attendanceRepository.save(attendance);
    }

    public void delete(String id) {
        if (!attendanceRepository.existsById(id)) {
            throw new BusinessException("考勤记录不存在");
        }
        attendanceRepository.deleteById(id);
    }

    public MonthlyAttendance getById(String id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
    }

    public MonthlyAttendance getByEmployeeAndMonth(String employeeId, String yearMonth) {
        return attendanceRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElse(null);
    }

    public List<MonthlyAttendance> listByMonth(String yearMonth) {
        return attendanceRepository.findByYearMonth(yearMonth);
    }

    public List<MonthlyAttendance> listByEmployee(String employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public BigDecimal getTotalOvertimeHours(MonthlyAttendance attendance) {
        BigDecimal total = BigDecimal.ZERO;
        if (attendance.getWorkdayOvertimeHours() != null) {
            total = total.add(attendance.getWorkdayOvertimeHours());
        }
        if (attendance.getWeekendOvertimeHours() != null) {
            total = total.add(attendance.getWeekendOvertimeHours());
        }
        if (attendance.getHolidayOvertimeHours() != null) {
            total = total.add(attendance.getHolidayOvertimeHours());
        }
        return total;
    }

    public BigDecimal getTotalLeaveDays(MonthlyAttendance attendance) {
        BigDecimal total = BigDecimal.ZERO;
        if (attendance.getLeaveDays() != null) {
            total = total.add(attendance.getLeaveDays());
        }
        if (attendance.getSickLeaveDays() != null) {
            total = total.add(attendance.getSickLeaveDays());
        }
        if (attendance.getPersonalLeaveDays() != null) {
            total = total.add(attendance.getPersonalLeaveDays());
        }
        return total;
    }
}
