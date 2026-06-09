package com.hrpayroll.salary.repository;

import com.hrpayroll.common.InMemoryRepository;
import com.hrpayroll.salary.entity.PositionGrade;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PositionGradeRepository extends InMemoryRepository<PositionGrade> {

    public Optional<PositionGrade> findByGradeCode(String gradeCode) {
        return storage.values().stream()
                .filter(g -> gradeCode.equals(g.getGradeCode()))
                .findFirst();
    }

    public boolean existsByGradeCode(String gradeCode) {
        return storage.values().stream()
                .anyMatch(g -> gradeCode.equals(g.getGradeCode()));
    }
}
