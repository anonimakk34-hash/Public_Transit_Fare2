package strategy.risk;

import strategy.TripRequest;

/**
 * Algorithm 2: Scores overdraft/default risk based on consecutive payment declines
 * and remaining card wallet balance relative to trip length.
 */
public class FinancialBehaviorRiskStrategy implements TapRiskStrategy {
    @Override
    public double evaluateRisk(TripRequest request) {
        double declinePenalty = request.getRecentDeclinesCount() * 22.0;
        double estimatedCost = 80.0 + (request.getDistanceKm() * 10.0);

        double balanceDeficitScore = 0.0;
        if (request.getCardBalance() < estimatedCost) {
            double deficitRatio = (estimatedCost - request.getCardBalance()) / estimatedCost;
            balanceDeficitScore = Math.min(50.0, Math.max(15.0, deficitRatio * 50.0));
        }

        double total = declinePenalty + balanceDeficitScore;
        return Math.min(100.0, Math.max(0.0, Math.round(total * 100.0) / 100.0));
    }
}