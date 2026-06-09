package com.hrpayroll.salary.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.salary.entity.CityStandard;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CityStandardRepository extends InMemoryRepository<CityStandard> {

    public Optional<CityStandard> findByCityCodeAndYear(String cityCode, int year) {
        return storage.values().stream()
                .filter(c -> cityCode.equals(c.getCityCode()) && c.getYear() == year)
                .findFirst();
    }

    public List<CityStandard> findByYear(int year) {
        return storage.values().stream()
                .filter(c -> c.getYear() == year)
                .collect(java.util.stream.Collectors.toList());
    }
}
