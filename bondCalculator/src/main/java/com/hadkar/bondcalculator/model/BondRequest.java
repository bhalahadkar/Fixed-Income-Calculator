package com.hadkar.bondcalculator.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class BondRequest {
    private double faceValue;
    private double couponRate;
    private double marketPrice;
    private int frequency;
    private LocalDate settlementDate;
    private LocalDate maturityDate;
    private List<CallSchedule> callSchedules; // for callable bonds
    private List<FloatingRateSchedule> floatingRates; // for floating-rate bonds
}
