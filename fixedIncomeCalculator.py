import QuantLib as ql
from typing import List, Dict, Optional

class FixedIncomeCalculator:
    def __init__(self, settlement_date: ql.Date, calendar=ql.UnitedStates(ql.UnitedStates.GovernmentBond)):
        self.settlement_date = settlement_date
        self.calendar = calendar
        ql.Settings.instance().evaluationDate = settlement_date

    def create_schedule(self, start_date, maturity_date, tenor, convention, rule):
        return ql.Schedule(
            start_date,
            maturity_date,
            tenor,
            self.calendar,
            convention,
            convention,
            rule,
            False
        )

    def create_fixed_rate_bond(self, face_amount, schedule, coupons, day_count, issue_date, payment_convention):
        bond = ql.FixedRateBond(
            0,  # settlement days
            face_amount,
            schedule,
            coupons,
            day_count,
            payment_convention,
            100.0,
            issue_date
        )
        return bond

    def price_from_yield(self, bond: ql.Bond, yield_rate, day_count, compounding=ql.Compounded, freq=ql.Annual):
        return bond.cleanPrice(yield_rate, day_count, compounding, freq)

    def yield_from_price(self, bond: ql.Bond, price, day_count, compounding=ql.Compounded, freq=ql.Annual):
        return bond.bondYield(price, day_count, compounding, freq)

    def accrued_interest(self, bond: ql.Bond):
        return bond.accruedAmount()

    def accrued_days(self, bond: ql.Bond):
        return bond.accruedDays()

    def duration(self, bond: ql.Bond, yield_rate, day_count, duration_type=ql.Duration.Modified):
        return ql.BondFunctions.duration(bond, yield_rate, day_count, duration_type)

    def convexity(self, bond: ql.Bond, yield_rate, day_count):
        return ql.BondFunctions.convexity(bond, yield_rate, day_count)

    def pv01(self, bond: ql.Bond, yield_rate, day_count):
        dirty_price = bond.dirtyPrice(yield_rate, day_count, ql.Compounded, ql.Annual)
        price_plus = bond.dirtyPrice(yield_rate + 0.0001, day_count, ql.Compounded, ql.Annual)
        return price_plus - dirty_price

    def dv01(self, bond: ql.Bond, yield_rate, day_count):
        return self.pv01(bond, yield_rate, day_count)

    def clean_price(self, bond: ql.Bond):
        return bond.cleanPrice()

    def dirty_price(self, bond: ql.Bond):
        return bond.dirtyPrice()

    def cashflows(self, bond: ql.Bond) -> List[Dict]:
        flows = []
        for cf in bond.cashflows():
            if cf.date() >= self.settlement_date:
                flows.append({
                    "date": cf.date().ISO(),
                    "amount": cf.amount()
                })
        return flows

    def payment_schedule(self, bond: ql.Bond) -> List[str]:
        return [cf.date().ISO() for cf in bond.cashflows() if cf.amount() > 0.0]

    # TODO: Add callable/puttable/sinking floating/step bonds

    def create_callable_bond(self, face_amount, schedule, coupons, day_count, issue_date, call_dates, call_price):
        # Simplified callable bond (more advanced handling needed in production)
        bond = ql.CallableFixedRateBond(
            0,
            face_amount,
            schedule,
            coupons,
            day_count,
            ql.Following,
            100.0,
            issue_date,
            ql.CallabilitySchedule([
                ql.Callability(
                    ql.CallabilityPrice(call_price, ql.CallabilityPrice.Clean),
                    ql.Callability.Call,
                    call_date
                ) for call_date in call_dates
            ])
        )
        return bond

    def create_floating_rate_bond(self, face_amount, schedule, index, spread, day_count, issue_date):
        bond = ql.FloatingRateBond(
            0,
            face_amount,
            schedule,
            index,
            day_count,
            ql.Following,
            fixingDays=2,
            gearings=[1.0],
            spreads=[spread],
            caps=[],
            floors=[],
            inArrears=False,
            redemption=100.0,
            issueDate=issue_date
        )
        return bond

    def create_mbs_bond(self, face_amount, schedule, coupons, day_count, factor, issue_date):
        bond = self.create_fixed_rate_bond(face_amount, schedule, coupons, day_count, issue_date, ql.Following)
        bond.setSettlementDays(0)
        for cf in bond.cashflows():
            if isinstance(cf, ql.FixedRateCoupon):
                cf.setAmount(cf.amount() * factor)
        return bond
