package com.hadkar.bondcalculator;

// BondServiceTest.java
import com.hadkar.bondcalculator.service.BondService;
import com.hadkar.bondcalculator.model.CallSchedule;
import com.hadkar.bondcalculator.model.FloatingRateSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BondServiceTest {

    private BondService bondService;

    @BeforeEach
    public void setUp() {
        bondService = new BondService();
    }

    @Test
    public void testCalculatePrice() {
        double price = bondService.calculatePrice(1000, 0.05, LocalDate.now(), LocalDate.now().plusYears(5), 0.05, 2);
        assertTrue(price > 900 && price < 1100);
    }

    @Test
    public void testCalculateYieldToMaturity() {
        double ytm = bondService.calculateYieldToMaturity(980, 1000, 0.05, LocalDate.now(), LocalDate.now().plusYears(5), 2);
        assertTrue(ytm > 0);
    }

    @Test
    public void testCalculateYieldToCall() {
        List<CallSchedule> calls = List.of(
                createCallSchedule(LocalDate.now().plusYears(3), 1020),
                createCallSchedule(LocalDate.now().plusYears(4), 1010)
        );
        double ytc = bondService.calculateYieldToCall(1000, 1000, 0.05, LocalDate.now(), calls, 2);
        assertTrue(ytc > 0);
    }

    @Test
    public void testCalculateAccruedInterest() {
        double accrued = bondService.calculateAccruedInterest(1000, 0.05, LocalDate.now(), LocalDate.now().minusMonths(6), 2);
        assertTrue(accrued > 0);
    }

    @Test
    public void testCalculatePV01() {
        double pv01 = bondService.calculatePV01(0.05, 1000, 0.05, LocalDate.now(), LocalDate.now().plusYears(5), 2);
        assertTrue(pv01 > 0);
    }

    @Test
    public void testCalculateDV01() {
        double dv01 = bondService.calculateDV01(0.85, 1000);
        assertEquals(8.5, dv01, 0.01);
    }

    @Test
    public void testCalculateModifiedDuration() {
        double duration = bondService.calculateModifiedDuration(950, 0.85);
        assertTrue(duration > 0);
    }

    @Test
    public void testFloatingRateBondPrice() {
////        List<FloatingRateSchedule> schedule = List.of(
//                createFloatingRate(LocalDate.now().plusMonths(6), 0.04),
//                createFloatingRate(LocalDate.now().plusMonths(12), 0.045)
//        );
        List<FloatingRateSchedule> schedule = new ArrayList<>(List.of(
                createFloatingRate(LocalDate.now().plusMonths(6), 0.04),
                createFloatingRate(LocalDate.now().plusMonths(12), 0.045)
        ));


        double price = bondService.calculateFloatingRateBondPrice(1000, schedule, LocalDate.now(), LocalDate.now().plusYears(1), 0.05, 2);
        assertTrue(price > 900 && price < 1100);
    }

    private CallSchedule createCallSchedule(LocalDate date, double price) {
        CallSchedule cs = new CallSchedule();
        cs.setCallDate(date);
        cs.setCallPrice(price);
        return cs;
    }

    private FloatingRateSchedule createFloatingRate(LocalDate date, double rate) {
        FloatingRateSchedule fr = new FloatingRateSchedule();
        fr.setDate(date);
        fr.setRate(rate);
        return fr;
    }
}
