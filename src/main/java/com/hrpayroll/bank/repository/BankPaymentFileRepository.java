package com.hrpayroll.bank.repository;

import com.hrpayroll.bank.entity.BankPaymentFile;
import com.hrpayroll.common.InMemoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class BankPaymentFileRepository extends InMemoryRepository<BankPaymentFile> {

    public Optional<BankPaymentFile> findByFileId(String fileId) {
        return storage.values().stream()
                .filter(f -> fileId.equals(f.getFileId()))
                .findFirst();
    }

    public List<BankPaymentFile> findByYearMonth(String yearMonth) {
        return storage.values().stream()
                .filter(f -> yearMonth.equals(f.getYearMonth()))
                .sorted(Comparator.comparing(BankPaymentFile::getGenerateTime).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<BankPaymentFile> findByYearMonthAndBankType(String yearMonth, String bankType) {
        return storage.values().stream()
                .filter(f -> yearMonth.equals(f.getYearMonth()) && bankType.equals(f.getBankType()))
                .sorted(Comparator.comparing(BankPaymentFile::getGenerateTime).reversed())
                .collect(java.util.stream.Collectors.toList());
    }
}
