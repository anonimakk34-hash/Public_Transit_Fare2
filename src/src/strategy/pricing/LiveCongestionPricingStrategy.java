package strategy.pricing;

import strategy.TripRequest;
import strategy.telemetry.TransitTelemetryProvider;

/**
 * Algorithm 3: Dynamically adjusts fare using isolated external sensor telemetry
 * (station occupancy ratio and city weather alerts).
 */
public class LiveCongestionPricingStrategy implements FarePricingStrategy {
    private final TransitTelemetryProvider telemetryProvider;

    public LiveCongestionPricingStrategy(TransitTelemetryProvider telemetryProvider) {
        if (telemetryProvider == null) {
            throw new IllegalArgumentException("Telemetry provider cannot be null");
        }
        this.telemetryProvider = telemetryProvider;
    }

    @Override
    public double calculateFare(TripRequest request) {
        double baseRate = 70.0 + (request.getDistanceKm() * 12.0);
        double occupancy = telemetryProvider.getStationOccupancyRatio(request.getStationId());

        // Apply dynamic congestion surge or off-peak discount based on live sensor load
        double loadFactor;
        if (occupancy >= 0.75) {
            loadFactor = 1.0 + ((occupancy - 0.50) * 0.80); // Surge multiplier
        } else if (occupancy <= 0.35) {
            loadFactor = 0.85; // Quiet station incentive
        } else {
            loadFactor = 1.0;
        }

        double adjustedFare = baseRate * loadFactor;

        // Apply municipal eco-rebate if severe weather/smog alert is active
        if (telemetryProvider.isSevereWeatherAlert()) {
            adjustedFare *= 0.70;
        }

        return Math.max(0.0, Math.round(adjustedFare * 100.0) / 100.0);
    }
}
