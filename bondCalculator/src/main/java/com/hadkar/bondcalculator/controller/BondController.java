package com.hadkar.bondcalculator.controller;

import com.hadkar.bondcalculator.model.BondRequest;
import com.hadkar.bondcalculator.service.BondService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/bond")
@RequiredArgsConstructor
public class BondController {

    private final BondService bondService;

    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody BondRequest req) {
        LocalDate settle = req.getSettlementDate();
        LocalDate maturity = req.getMaturityDate();

        double ytm = bondService.calculateYieldToMaturity(req.getMarketPrice(), req.getFaceValue(), req.getCouponRate(), settle, maturity, req.getFrequency());
        double price = bondService.calculatePrice(req.getFaceValue(), req.getCouponRate(), settle, maturity, ytm, req.getFrequency());
        double accrued = bondService.calculateAccruedInterest(req.getFaceValue(), req.getCouponRate(), settle, maturity.minusMonths(12 / req.getFrequency()), req.getFrequency());
        double pv01 = bondService.calculatePV01(ytm, req.getFaceValue(), req.getCouponRate(), settle, maturity, req.getFrequency());
        double dv01 = bondService.calculateDV01(pv01, req.getFaceValue());
        double duration = bondService.calculateModifiedDuration(price, pv01);

        Double ytc = null;
        if (req.getCallSchedules() != null && !req.getCallSchedules().isEmpty()) {
            ytc = bondService.calculateYieldToCall(req.getMarketPrice(), req.getFaceValue(), req.getCouponRate(), settle, req.getCallSchedules(), req.getFrequency());
        }

        Double floatingPrice = null;
        if (req.getFloatingRates() != null && !req.getFloatingRates().isEmpty()) {
            floatingPrice = bondService.calculateFloatingRateBondPrice(req.getFaceValue(), req.getFloatingRates(), settle, maturity, ytm, req.getFrequency());
        }

        return Map.of(
                "ytm", ytm,
                "price", price,
                "accruedInterest", accrued,
                "pv01", pv01,
                "dv01", dv01,
                "modifiedDuration", duration,
                "ytc", ytc,
                "floatingBondPrice", floatingPrice
        );
    }
}
