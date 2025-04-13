package com.hadkar.bondcalculator.service;
import com.hadkar.bondcalculator.model.CallSchedule;
import com.hadkar.bondcalculator.model.FloatingRateSchedule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
public class BondService {

    public double calculatePrice(double face, double couponRate, LocalDate settleDate, LocalDate maturityDate, double ytm, int frequency) {
        double price = 0.0;
        long totalDays = ChronoUnit.DAYS.between(settleDate, maturityDate);
        int numPeriods = (int) (totalDays / (365.0 / frequency));

        for (int i = 1; i <= numPeriods; i++) {
            price += (face * couponRate / frequency) / Math.pow(1 + ytm / frequency, i);
        }
        price += face / Math.pow(1 + ytm / frequency, numPeriods);
        return price;
    }

    public double calculateYieldToMaturity(double marketPrice, double face, double couponRate, LocalDate settleDate, LocalDate maturityDate, int frequency) {
        Function<Double, Double> f = (ytm) -> calculatePrice(face, couponRate, settleDate, maturityDate, ytm, frequency) - marketPrice;
        return solveForRate(f);
    }

    public double calculateYieldToCall(double marketPrice, double face, double couponRate, LocalDate settleDate, List<CallSchedule> callSchedules, int frequency) {
        return callSchedules.stream()
                .sorted(Comparator.comparing(CallSchedule::getCallDate))
                .mapToDouble(cs -> {
                    Function<Double, Double> f = (ytc) -> {
                        double callPrice = cs.getCallPrice();
                        LocalDate callDate = cs.getCallDate();
                        double price = 0.0;
                        long totalDays = ChronoUnit.DAYS.between(settleDate, callDate);
                        int numPeriods = (int) (totalDays / (365.0 / frequency));

                        for (int i = 1; i <= numPeriods; i++) {
                            price += (face * couponRate / frequency) / Math.pow(1 + ytc / frequency, i);
                        }
                        price += callPrice / Math.pow(1 + ytc / frequency, numPeriods);
                        return price - marketPrice;
                    };
                    return solveForRate(f);
                })
                .min()
                .orElseThrow(() -> new RuntimeException("No call schedule provided"));
    }

    public double calculateAccruedInterest(double face, double couponRate, LocalDate settle, LocalDate lastCoupon, int frequency) {
        long daysAccrued = ChronoUnit.DAYS.between(lastCoupon, settle);
        double periodDays = 365.0 / frequency;
        return (face * couponRate / frequency) * (daysAccrued / periodDays);
    }

    public double calculatePV01(double ytm, double face, double couponRate, LocalDate settle, LocalDate maturity, int frequency) {
        double basePrice = calculatePrice(face, couponRate, settle, maturity, ytm, frequency);
        double priceUp = calculatePrice(face, couponRate, settle, maturity, ytm + 0.0001, frequency);
        return basePrice - priceUp;
    }

    public double calculateDV01(double pv01, double face) {
        return pv01 * (face / 100.0);
    }

    public double calculateModifiedDuration(double price, double pv01) {
        return pv01 / price * 10000;
    }

    public double calculateFloatingRateBondPrice(double face, List<FloatingRateSchedule> schedule, LocalDate settleDate, LocalDate maturityDate, double discountRate, int frequency) {
        schedule.sort(Comparator.comparing(FloatingRateSchedule::getDate));
        double price = 0.0;
        int period = 1;
        for (FloatingRateSchedule rate : schedule) {
            if (rate.getDate().isAfter(settleDate) && !rate.getDate().isAfter(maturityDate)) {
                double coupon = face * rate.getRate() / frequency;
                price += coupon / Math.pow(1 + discountRate / frequency, period++);
            }
        }
        price += face / Math.pow(1 + discountRate / frequency, period - 1);
        return price;
    }

    private double solveForRate(Function<Double, Double> f) {
        double guess = 0.05;
        for (int i = 0; i < 100; i++) {
            double fVal = f.apply(guess);
            double fValUp = f.apply(guess + 0.0001);
            double derivative = (fValUp - fVal) / 0.0001;
            double next = guess - fVal / derivative;
            if (Math.abs(next - guess) < 1e-6) return next;
            guess = next;
        }
        throw new RuntimeException("Rate solving did not converge");
    }

    public double calculateAccruedInterestWithDayCount(double face, double couponRate, LocalDate settle, LocalDate lastCoupon, int frequency, String dayCountConvention) {
        double periodFraction;

        switch (dayCountConvention.toUpperCase()) {
            case "30/360":
                int d1 = Math.min(30, lastCoupon.getDayOfMonth());
                int d2 = Math.min(30, settle.getDayOfMonth());
                int months = (settle.getYear() - lastCoupon.getYear()) * 12 + settle.getMonthValue() - lastCoupon.getMonthValue();
                periodFraction = (360 * (settle.getYear() - lastCoupon.getYear()) + 30 * (settle.getMonthValue() - lastCoupon.getMonthValue()) + (d2 - d1)) / 360.0;
                break;
            case "ACT/360":
                long actual360 = ChronoUnit.DAYS.between(lastCoupon, settle);
                periodFraction = actual360 / 360.0;
                break;
            case "ACT/365":
                long actual365 = ChronoUnit.DAYS.between(lastCoupon, settle);
                periodFraction = actual365 / 365.0;
                break;
            case "ACT/ACT":
                long actualDays = ChronoUnit.DAYS.between(lastCoupon, settle);
                double yearLength = lastCoupon.isLeapYear() ? 366.0 : 365.0;
                periodFraction = actualDays / yearLength;
                break;
            default:
                throw new IllegalArgumentException("Unsupported day count convention: " + dayCountConvention);
        }

        return (face * couponRate) * periodFraction;
    }
}
