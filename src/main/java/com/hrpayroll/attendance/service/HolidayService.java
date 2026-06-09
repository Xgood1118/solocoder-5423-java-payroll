package com.hrpayroll.attendance.service;

import com.hrpayroll.attendance.entity.Holiday;
import com.hrpayroll.attendance.repository.HolidayRepository;
import com.hrpayroll.common.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    public Holiday create(Holiday holiday) {
        if (holidayRepository.existsByHolidayDate(holiday.getHolidayDate())) {
            throw new BusinessException("该日期节假日已存在");
        }
        if (holiday.getYear() == 0 && holiday.getHolidayDate() != null) {
            holiday.setYear(holiday.getHolidayDate().getYear());
        }
        return holidayRepository.save(holiday);
    }

    public Holiday update(String id, Holiday holiday) {
        Holiday existing = holidayRepository.findById(id)
                .orElseThrow(() -> new BusinessException("节假日不存在"));
        existing.setHolidayName(holiday.getHolidayName());
        existing.setHolidayDate(holiday.getHolidayDate());
        existing.setYear(holiday.getHolidayDate().getYear());
        existing.setType(holiday.getType());
        return holidayRepository.save(existing);
    }

    public void delete(String id) {
        if (!holidayRepository.existsById(id)) {
            throw new BusinessException("节假日不存在");
        }
        holidayRepository.deleteById(id);
    }

    public Holiday getById(String id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new BusinessException("节假日不存在"));
    }

    public List<Holiday> listByYear(int year) {
        return holidayRepository.findByYear(year);
    }

    public List<Holiday> listByDateRange(LocalDate start, LocalDate end) {
        return holidayRepository.findByDateRange(start, end);
    }

    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByHolidayDate(date);
    }

    public int batchCreate(List<Holiday> holidays) {
        int count = 0;
        for (Holiday holiday : holidays) {
            if (!holidayRepository.existsByHolidayDate(holiday.getHolidayDate())) {
                if (holiday.getYear() == 0 && holiday.getHolidayDate() != null) {
                    holiday.setYear(holiday.getHolidayDate().getYear());
                }
                holidayRepository.save(holiday);
                count++;
            }
        }
        return count;
    }
}
