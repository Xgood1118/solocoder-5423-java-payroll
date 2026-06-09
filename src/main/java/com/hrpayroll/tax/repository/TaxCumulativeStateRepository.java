package com.hrpayroll.tax.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.tax.entity.TaxCumulativeState;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class TaxCumulativeStateRepository extends InMemoryRepository<TaxCumulativeState> {

    public Optional<TaxCumulativeState> findByEmployeeIdAndYearMonth(String employeeId, String yearMonth) {
        return storage.values().stream()
                .filter(s -> employeeId.equals(s.getEmployeeId()) && yearMonth.equals(s.getYearMonth()))
                .findFirst();
    }

    public List<TaxCumulativeState> findByEmployeeIdAndYear(String employeeId, int taxYear) {
        return storage.values().stream()
                .filter(s -> employeeId.equals(s.getEmployeeId()) && s.getTaxYear() == taxYear)
                .sorted(Comparator.comparing(TaxCumulativeState::getMonthIndex))
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<TaxCumulativeState> findLatestByEmployeeAndYear(String employeeId, int taxYear) {
        return storage.values().stream()
                .filter(s -> employeeId.equals(s.getEmployeeId()) && s.getTaxYear() == taxYear)
                .max(Comparator.comparing(TaxCumulativeState::getMonthIndex));
    }

    public List<TaxCumulativeState> findByYearMonthFrom(String employeeId, int taxYear, int fromMonth) {
        return storage.values().stream()
                .filter(s -> employeeId.equals(s.getEmployeeId())
                        && s.getTaxYear() == taxYear
                        && s.getMonthIndex() >= fromMonth)
                .sorted(Comparator.comparing(TaxCumulativeState::getMonthIndex))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteByEmployeeAndYearFrom(String employeeId, int taxYear, int fromMonth) {
        List<String> idsToDelete = storage.values().stream()
                .filter(s -> employeeId.equals(s.getEmployeeId())
                        && s.getTaxYear() == taxYear
                        && s.getMonthIndex() >= fromMonth)
                .map(TaxCumulativeState::getId)
                .collect(java.util.stream.Collectors.toList());
        idsToDelete.forEach(storage::remove);
    }
}
