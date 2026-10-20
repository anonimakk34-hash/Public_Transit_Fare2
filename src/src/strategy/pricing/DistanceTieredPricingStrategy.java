package strategy.pricing;

import strategy.TripRequest;

/**
 * Algorithm 2: Computes fare using progressive marginal kilometer brackets:
 * - Tier 1 (0 to 3 km): flat starter fare of 80.0
 * - Tier 2 (3 to 12 km): 18.0 per km
 * - Tier 3 (above 12 km): 9.5 per km (long-haul decay rate)
 */
public class DistanceTieredPricingStrategy implements FarePricingStrategy {
    @Override
    public double calculateFare(TripRequest request) {
        double km = request.getDistanceKm();
        double fare = 80.0;

        if (km > 3.0) {
            double tier2Km = Math.min(km - 3.0, 9.0);
            fare += tier2Km * 18.0;
        }
        if (km > 12.0) {
            double tier3Km = km - 12.0;
            fare += tier3Km * 9.5;
        }
        return Math.round(fare * 100.0) / 100.0;
    }
}