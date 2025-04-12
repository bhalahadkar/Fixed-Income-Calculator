package com.hadkar.bondcalculator.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class FloatingRateSchedule {
    private LocalDate date;
    private double rate;
}

