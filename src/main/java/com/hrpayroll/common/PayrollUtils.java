package com.hrpayroll.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class PayrollUtils {

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static BigDecimal setScale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static int monthsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(
                start.withDayOfMonth(1),
                end.withDayOfMonth(1));
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static String maskIdCardLast6(String idCard) {
        if (idCard == null || idCard.length() < 6) {
            return "000000";
        }
        return idCard.substring(idCard.length() - 6);
    }
}
