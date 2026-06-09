package com.hrpayroll.tax.service;

import com.hrpayroll.config.PayrollProperties;
import com.hrpayroll.tax.entity.TaxBracket;
import com.hrpayroll.tax.entity.TaxCumulativeState;
import com.hrpayroll.tax.repository.TaxCumulativeStateRepository;
import com.hrpayroll.common.PayrollUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TaxCalculationService {

    @Autowired
    private TaxCumulativeStateRepository taxStateRepository;

    @Autowired
    private PayrollProperties payrollProperties;

    private static final List<TaxBracket> TAX_BRACKETS = Arrays.asList(
            new TaxBracket(BigDecimal.ZERO, new BigDecimal("36000"), new BigDecimal("0.03"), BigDecimal.ZERO),
            new TaxBracket(new BigDecimal("36000"), new BigDecimal("144000"), new BigDecimal("0.10"), new BigDecimal("2520")),
            new TaxBracket(new BigDecimal("144000"), new BigDecimal("300000"), new BigDecimal("0.20"), new BigDecimal("16920")),
            new TaxBracket(new BigDecimal("300000"), new BigDecimal("420000"), new BigDecimal("0.25"), new BigDecimal("31920")),
            new TaxBracket(new BigDecimal("420000"), new BigDecimal("660000"), new BigDecimal("0.30"), new BigDecimal("52920")),
            new TaxBracket(new BigDecimal("660000"), new BigDecimal("960000"), new BigDecimal("0.35"), new BigDecimal("85920")),
            new TaxBracket(new BigDecimal("960000"), new BigDecimal("99999999"), new BigDecimal("0.45"), new BigDecimal("181920"))
    );

    public TaxCumulativeState calculateMonthlyTax(String employeeId, String yearMonth,
                                                  BigDecimal monthlyIncome, BigDecimal socialSecurity,
                                                  BigDecimal housingFund, BigDecimal specialAdditionalTotal,
                                                  int monthIndex) {
        TaxCumulativeState previousState = taxStateRepository
                .findLatestByEmployeeAndYear(employeeId, parseYear(yearMonth))
                .orElse(null);

        TaxCumulativeState currentState = new TaxCumulativeState();
        currentState.setEmployeeId(employeeId);
        currentState.setYearMonth(yearMonth);
        currentState.setTaxYear(parseYear(yearMonth));
        currentState.setMonthIndex(monthIndex);
        currentState.setSocialSecurity(socialSecurity);
        currentState.setHousingFund(housingFund);
        currentState.setSpecialAdditionalTotal(specialAdditionalTotal);

        if (previousState != null && previousState.getMonthIndex() < monthIndex) {
            currentState.setCumulativeIncome(previousState.getCumulativeIncome().add(monthlyIncome));
            currentState.setCumulativeDeduction(
                    previousState.getCumulativeDeduction().add(payrollProperties.getTaxThreshold()));
            currentState.setCumulativeSpecialDeduction(
                    previousState.getCumulativeSpecialDeduction().add(socialSecurity).add(housingFund));
            currentState.setCumulativeSpecialAdditionalDeduction(
                    previousState.getCumulativeSpecialAdditionalDeduction().add(specialAdditionalTotal));
            currentState.setCumulativeTax(previousState.getCumulativeTax());
        } else {
            currentState.setCumulativeIncome(monthlyIncome);
            currentState.setCumulativeDeduction(payrollProperties.getTaxThreshold());
            currentState.setCumulativeSpecialDeduction(socialSecurity.add(housingFund));
            currentState.setCumulativeSpecialAdditionalDeduction(specialAdditionalTotal);
            currentState.setCumulativeTax(BigDecimal.ZERO);
        }

        BigDecimal taxableIncome = currentState.getCumulativeIncome()
                .subtract(currentState.getCumulativeDeduction())
                .subtract(currentState.getCumulativeSpecialDeduction())
                .subtract(currentState.getCumulativeSpecialAdditionalDeduction());

        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        currentState.setCumulativeTaxableIncome(PayrollUtils.setScale(taxableIncome));

        BigDecimal totalTax = calculateTax(taxableIncome);
        BigDecimal previousCumulativeTax = currentState.getCumulativeTax();
        BigDecimal monthlyTax = totalTax.subtract(previousCumulativeTax);

        if (monthlyTax.compareTo(BigDecimal.ZERO) < 0) {
            monthlyTax = BigDecimal.ZERO;
        }

        currentState.setCumulativeTax(PayrollUtils.setScale(totalTax));
        currentState.setMonthlyTax(PayrollUtils.setScale(monthlyTax));

        return currentState;
    }

    public TaxCumulativeState saveState(TaxCumulativeState state) {
        taxStateRepository.findByEmployeeIdAndYearMonth(state.getEmployeeId(), state.getYearMonth())
                .ifPresent(existing -> {
                    state.setId(existing.getId());
                    state.setCreateTime(existing.getCreateTime());
                });
        return taxStateRepository.save(state);
    }

    public void recalculateFromMonth(String employeeId, int taxYear, int fromMonthIndex,
                                     List<MonthlyTaxInput> inputs) {
        taxStateRepository.deleteByEmployeeAndYearFrom(employeeId, taxYear, fromMonthIndex);

        TaxCumulativeState carryOverState = null;
        if (fromMonthIndex > 1) {
            int prevMonth = fromMonthIndex - 1;
            carryOverState = taxStateRepository.findByEmployeeIdAndYear(employeeId, taxYear).stream()
                    .filter(s -> s.getMonthIndex() == prevMonth)
                    .findFirst()
                    .orElse(null);
        }

        for (MonthlyTaxInput input : inputs) {
            TaxCumulativeState state = new TaxCumulativeState();
            state.setEmployeeId(employeeId);
            state.setYearMonth(input.getYearMonth());
            state.setTaxYear(taxYear);
            state.setMonthIndex(input.getMonthIndex());
            state.setSocialSecurity(input.getSocialSecurity());
            state.setHousingFund(input.getHousingFund());
            state.setSpecialAdditionalTotal(input.getSpecialAdditionalTotal());

            if (carryOverState != null) {
                state.setCumulativeIncome(carryOverState.getCumulativeIncome().add(input.getMonthlyIncome()));
                state.setCumulativeDeduction(
                        carryOverState.getCumulativeDeduction().add(payrollProperties.getTaxThreshold()));
                state.setCumulativeSpecialDeduction(
                        carryOverState.getCumulativeSpecialDeduction()
                                .add(input.getSocialSecurity()).add(input.getHousingFund()));
                state.setCumulativeSpecialAdditionalDeduction(
                        carryOverState.getCumulativeSpecialAdditionalDeduction()
                                .add(input.getSpecialAdditionalTotal()));
                state.setCumulativeTax(carryOverState.getCumulativeTax());
            } else {
                state.setCumulativeIncome(input.getMonthlyIncome());
                state.setCumulativeDeduction(payrollProperties.getTaxThreshold());
                state.setCumulativeSpecialDeduction(
                        input.getSocialSecurity().add(input.getHousingFund()));
                state.setCumulativeSpecialAdditionalDeduction(input.getSpecialAdditionalTotal());
                state.setCumulativeTax(BigDecimal.ZERO);
            }

            BigDecimal taxableIncome = state.getCumulativeIncome()
                    .subtract(state.getCumulativeDeduction())
                    .subtract(state.getCumulativeSpecialDeduction())
                    .subtract(state.getCumulativeSpecialAdditionalDeduction());

            if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
                taxableIncome = BigDecimal.ZERO;
            }
            state.setCumulativeTaxableIncome(PayrollUtils.setScale(taxableIncome));

            BigDecimal totalTax = calculateTax(taxableIncome);
            BigDecimal monthlyTax = totalTax.subtract(state.getCumulativeTax());
            if (monthlyTax.compareTo(BigDecimal.ZERO) < 0) {
                monthlyTax = BigDecimal.ZERO;
            }

            state.setCumulativeTax(PayrollUtils.setScale(totalTax));
            state.setMonthlyTax(PayrollUtils.setScale(monthlyTax));

            taxStateRepository.save(state);
            carryOverState = state;
        }
    }

    public void resetYear(String employeeId, int taxYear) {
        List<TaxCumulativeState> states = taxStateRepository.findByEmployeeIdAndYear(employeeId, taxYear);
        for (TaxCumulativeState state : states) {
            taxStateRepository.deleteById(state.getId());
        }
    }

    public TaxCumulativeState getState(String employeeId, String yearMonth) {
        return taxStateRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth).orElse(null);
    }

    public List<TaxCumulativeState> listStates(String employeeId, int taxYear) {
        return taxStateRepository.findByEmployeeIdAndYear(employeeId, taxYear);
    }

    public static BigDecimal calculateTax(BigDecimal taxableIncome) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        for (TaxBracket bracket : TAX_BRACKETS) {
            if (taxableIncome.compareTo(bracket.getLowerBound()) > 0
                    && taxableIncome.compareTo(bracket.getUpperBound()) <= 0) {
                return PayrollUtils.setScale(
                        taxableIncome.multiply(bracket.getRate()).subtract(bracket.getQuickDeduction()));
            }
        }
        TaxBracket last = TAX_BRACKETS.get(TAX_BRACKETS.size() - 1);
        return PayrollUtils.setScale(
                taxableIncome.multiply(last.getRate()).subtract(last.getQuickDeduction()));
    }

    public static List<TaxBracket> getTaxBrackets() {
        return new ArrayList<>(TAX_BRACKETS);
    }

    private int parseYear(String yearMonth) {
        return Integer.parseInt(yearMonth.substring(0, 4));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MonthlyTaxInput {
        private String yearMonth;
        private int monthIndex;
        private BigDecimal monthlyIncome;
        private BigDecimal socialSecurity;
        private BigDecimal housingFund;
        private BigDecimal specialAdditionalTotal;
    }
}
