package strategy.risk;

import strategy.TripRequest;

public interface TapRiskStrategy {
    /**
     * @param request current tap and card state
     * @return risk score bounded in [0.0, 100.0]
     */
    double evaluateRisk(TripRequest request);
}
