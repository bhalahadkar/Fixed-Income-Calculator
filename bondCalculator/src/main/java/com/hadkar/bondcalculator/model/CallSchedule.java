package com.hadkar.bondcalculator.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CallSchedule {
    private LocalDate callDate;
    private double callPrice;
}
