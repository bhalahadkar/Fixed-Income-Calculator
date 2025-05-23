
from fixed_income_calculator import FixedIncomeCalculator
import QuantLib as ql

# Setup
today = ql.Date(26, 4, 2025)
calc = FixedIncomeCalculator.FixedIncomeCalculator(today)

# Build bond
issue_date = ql.Date(1, 1, 2022)
maturity_date = ql.Date(1, 1, 2032)
schedule = calc.create_schedule(issue_date, maturity_date, ql.Period(ql.Semiannual), ql.Unadjusted, ql.DateGeneration.Backward)

bond = calc.create_fixed_rate_bond(
    face_amount=1000,
    schedule=schedule,
    coupons=[0.05],
    day_count=ql.Thirty360(ql.Thirty360.BondBasis),
    issue_date=issue_date,
    payment_convention=ql.Unadjusted
)

# Calculate
ytm =  bond.bondYield(98.5, ql.Thirty360(ql.Thirty360.BondBasis), ql.Compounded, ql.Semiannual)
price = bond.cleanPrice(0.052, ql.Thirty360(ql.Thirty360.BondBasis), ql.Compounded, ql.Semiannual)
accrued = calc.accrued_interest(bond)
#duration = calc.duration(bond, 0.052, ql.Thirty360.BondBasis)
#convexity = calc.convexity(bond, 0.052, ql.Thirty360.BondBasis)
#dv01 = calc.dv01(bond, 0.052, ql.Thirty360())


#
print(f"Yield to Maturity: {ytm*100:.4f}%")
print(f"Price from Yield: {price:.4f}")
print(f"Accrued Interest: {accrued:.4f}")
#print(f"Duration: {duration:.4f}")
#print(f"Convexity: {convexity:.4f}")
#print(f"DV01: {dv01:.4f}")
print(f"Cash Flows: {calc.cashflows(bond)}")
#
#
# put_dates = [ql.Date(1, 1, 2027), ql.Date(1, 1, 2029)]  # Bondholder can put at these dates
# puttable_bond = calc.create_puttable_bond(
#     face_amount=1000,
#     schedule=schedule,
#     coupons=[0.04],
#     day_count=ql.Thirty360(),
#     issue_date=issue_date,
#     put_dates=put_dates,
#     put_price=100.0
# )
#
#
# coupon_steps = [
#     (ql.Date(1, 1, 2022), 0.03),  # 3% starting
#     (ql.Date(1, 1, 2027), 0.05),  # 5% from 2027
#     (ql.Date(1, 1, 2030), 0.06),  # 6% from 2030
# ]
# step_bond = calc.create_step_coupon_bond(
#     face_amount=1000,
#     schedule=schedule,
#     coupon_steps=coupon_steps,
#     day_count=ql.Thirty360(),
#     issue_date=issue_date,
#     payment_convention=ql.Unadjusted
# )
#
#
# principal_reduction = {
#     ql.Date(1, 1, 2027): 0.10,  # 10% principal paid
#     ql.Date(1, 1, 2028): 0.10,
#     ql.Date(1, 1, 2029): 0.10,
# }
#
# sinking_bond = calc.create_sinking_fund_bond(
#     face_amount=1000,
#     schedule=schedule,
#     coupons=[0.045],
#     principal_reduction=principal_reduction,
#     day_count=ql.Thirty360(),
#     issue_date=issue_date,
#     payment_convention=ql.Unadjusted
# )
#
#
# # European Callable Bond (single call in 2028)
# european_callable = calc.create_european_callable_bond(
#     face_amount=1000,
#     schedule=schedule,
#     coupons=[0.045],
#     call_date=ql.Date(1, 1, 2028),
#     call_price=100.0,
#     day_count=ql.Thirty360(),
#     issue_date=issue_date
# )
#
# def create_puttable_bond(self, face_amount, schedule, coupons, day_count, issue_date, put_dates, put_price):
#     bond = ql.PuttableFixedRateBond(
#         0,
#         face_amount,
#         schedule,
#         coupons,
#         day_count,
#         ql.Following,
#         100.0,
#         issue_date,
#         ql.CallabilitySchedule([
#             ql.Callability(
#                 ql.CallabilityPrice(put_price, ql.CallabilityPrice.Clean),
#                 ql.Callability.Put,
#                 put_date
#             ) for put_date in put_dates
#         ])
#     )
#     return bond
#
#
# def create_step_coupon_bond(self, face_amount, schedule, coupon_steps, day_count, issue_date, payment_convention):
#     """coupon_steps = list of (date, coupon)"""
#     coupons = []
#     last_rate = 0.0
#     for d in schedule:
#         matching_steps = [c for (step_date, c) in coupon_steps if d >= step_date]
#         if matching_steps:
#             last_rate = matching_steps[-1]
#         coupons.append(last_rate)
#     bond = ql.FixedRateBond(
#         0,
#         face_amount,
#         schedule,
#         coupons,
#         day_count,
#         payment_convention,
#         100.0,
#         issue_date
#     )
#     return bond
#
#
# def create_sinking_fund_bond(self, face_amount, schedule, coupons, principal_reduction, day_count, issue_date, payment_convention):
#     bond = ql.FixedRateBond(
#         0,
#         face_amount,
#         schedule,
#         coupons,
#         day_count,
#         payment_convention,
#         100.0,
#         issue_date
#     )
#
#     # Adjust cashflows manually
#     for cf in bond.cashflows():
#         if isinstance(cf, ql.Redeemable):
#             date = cf.date()
#             reduction = principal_reduction.get(date, 0.0)
#             if reduction:
#                 cf.setAmount(cf.amount() * (1 - reduction))
#     return bond
#
#
# def create_european_callable_bond(self, face_amount, schedule, coupons, call_date, call_price, day_count, issue_date):
#     call_schedule = ql.CallabilitySchedule([
#         ql.Callability(
#             ql.CallabilityPrice(call_price, ql.CallabilityPrice.Clean),
#             ql.Callability.Call,
#             call_date
#         )
#     ])
#     bond = ql.CallableFixedRateBond(
#         0,
#         face_amount,
#         schedule,
#         coupons,
#         day_count,
#         ql.Following,
#         100.0,
#         issue_date,
#         call_schedule
#     )
#     return bond
