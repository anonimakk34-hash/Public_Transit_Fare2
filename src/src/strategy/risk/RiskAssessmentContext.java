package strategy.risk;

import strategy.TripRequest;

public class RiskAssessmentContext {
    private TapRiskStrategy strategy;

    public RiskAssessmentContext(TapRiskStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(TapRiskStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Risk strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public TapRiskStrategy getStrategy() {
        return strategy;
    }

    public double assessTapRisk(TripRequest request) {
        return strategy.evaluateRisk(request);
    }
}