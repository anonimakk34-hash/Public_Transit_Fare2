package strategy.pricing;

import strategy.TripRequest;

/**
 * Algorithm 1: Computes fare based on zone boundary crossings with a progressive step surcharge
 * and a commuter cap at 4 zones.
 */
public class ZoneBasedPricingStrategy implements FarePricingStrategy {
    private static final double SINGLE_ZONE_BASE = 90.0;
    private static final double ZONE_STEP_SURCHARGE = 45.0;
    private static final int MAX_BILLABLE_ZONES = 4;

    @Override
    public double calculateFare(TripRequest request) {
        int billableZones = Math.min(request.getZonesCrossed(), MAX_BILLABLE_ZONES);
        double progressiveMultiplier = 1.0 + (billableZones * 0.15);
        double total = (SINGLE_ZONE_BASE + (billableZones * ZONE_STEP_SURCHARGE)) * progressiveMultiplier;
        return Math.round(total * 100.0) / 100.0;
    }
}