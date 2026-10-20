package strategy.pricing;

import strategy.TripRequest;
import strategy.telemetry.TransitTelemetryProvider;

public class FareStrategySelector {
    private final TransitTelemetryProvider telemetryProvider;

    public FareStrategySelector(TransitTelemetryProvider telemetryProvider) {
        this.telemetryProvider = telemetryProvider;
    }

    public FarePricingStrategy selectStrategy(TripRequest request) {
        int hour = request.getHourOfDay();
        boolean isPeakHour = (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);

        if (isPeakHour || telemetryProvider.isSevereWeatherAlert()) {
            return new LiveCongestionPricingStrategy(telemetryProvider);
        } else if (request.getZonesCrossed() >= 2) {
            return new ZoneBasedPricingStrategy();
        } else {
            return new DistanceTieredPricingStrategy();
        }
    }
}