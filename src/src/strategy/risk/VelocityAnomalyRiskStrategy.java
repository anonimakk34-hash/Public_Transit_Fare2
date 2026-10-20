package strategy.risk;

import strategy.TripRequest;

/**
 * Algorithm 1: Detects physical velocity anomalies ("impossible travel" or card pass-back sharing)
 * by comparing distance traveled against minutes elapsed since the previous tap.
 */
public class VelocityAnomalyRiskStrategy implements TapRiskStrategy {
    @Override
    public double evaluateRisk(TripRequest request) {
        int minutes = Math.max(1, request.getMinutesSinceLastTap());
        double hours = minutes / 60.0;
        double impliedSpeedKmh = request.getDistanceKm() / hours;

        double rawScore = 0.0;
        // Rapid pass-back at turnstile (< 4 minutes)
        if (minutes <= 4) {
            rawScore += 55.0;
        }
        // Implied transit speed exceeds realistic metro/bus speed (80 km/h)
        if (impliedSpeedKmh > 80.0) {
            rawScore += Math.min(45.0, (impliedSpeedKmh - 80.0) * 0.75);
        }
        return Math.min(100.0, Math.max(0.0, Math.round(rawScore * 100.0) / 100.0));
    }
}