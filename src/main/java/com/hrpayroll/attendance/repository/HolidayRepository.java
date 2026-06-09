package com.hrpayroll.attendance.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.attendance.entity.Holiday;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class HolidayRepository extends InMemoryRepository<Holiday> {

    public List<Holiday> findByYear(int year) {
        return storage.values().stream()
                .filter(h -> h.getYear() == year)
                .sorted(java.util.Comparator.comparing(Holiday::getHolidayDate))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean existsByHolidayDate(LocalDate date) {
        return storage.values().stream()
                .anyMatch(h -> h.getHolidayDate().equals(date));
    }

    public List<Holiday> findByDateRange(LocalDate start, LocalDate end) {
        return storage.values().stream()
                .filter(h -> !h.getHolidayDate().isBefore(start) && !h.getHolidayDate().isAfter(end))
                .sorted(java.util.Comparator.comparing(Holiday::getHolidayDate))
                .collect(java.util.stream.Collectors.toList());
    }
}
