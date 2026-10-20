package strategy.risk;

import strategy.TripRequest;

public class RiskStrategySelector {
    public TapRiskStrategy selectStrategy(TripRequest request) {
        if (request.getMinutesSinceLastTap() <= 10) {
            return new VelocityAnomalyRiskStrategy();
        } else if (request.getUnclosedTripsCount() >= 2) {
            return new JourneyHistoryRiskStrategy();
        } else {
            return new FinancialBehaviorRiskStrategy();
        }
    }
}