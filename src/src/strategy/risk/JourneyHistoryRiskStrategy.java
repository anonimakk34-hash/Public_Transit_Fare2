package strategy.risk;

import strategy.TripRequest;

/**
 * Algorithm 3: Evaluates fare evasion patterns based on historical unclosed journeys
 * (missing tap-outs) weighted by late-night multi-zone travel.
 */
public class JourneyHistoryRiskStrategy implements TapRiskStrategy {
    @Override
    public double evaluateRisk(TripRequest request) {
        // Exponential penalty for repeated missing tap-outs
        int unclosed = Math.max(0, request.getUnclosedTripsCount());
        double baseEvasionScore = Math.pow(unclosed, 1.6) * 16.0;

        // Late-night (22:00 - 04:59) multi-zone multiplier
        boolean isLateNight = request.getHourOfDay() >= 22 || request.getHourOfDay() <= 4;
        double zoneWeight = 1.0 + (request.getZonesCrossed() * 0.12);
        if (isLateNight) {
            zoneWeight += 0.25;
        }

        double total = baseEvasionScore * zoneWeight;
        return Math.min(100.0, Math.max(0.0, Math.round(total * 100.0) / 100.0));
    }
}