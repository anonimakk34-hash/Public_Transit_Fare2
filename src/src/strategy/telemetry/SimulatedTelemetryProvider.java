package strategy.telemetry;

public class SimulatedTelemetryProvider implements TransitTelemetryProvider {
    @Override
    public double getStationOccupancyRatio(String stationId) {
        if ("ST-CENTRAL".equalsIgnoreCase(stationId)) {
            return 0.88; // Busy central hub
        }
        if ("ST-EXPO".equalsIgnoreCase(stationId)) {
            return 0.65; // Moderate load
        }
        return 0.30; // Quiet suburban station
    }

    @Override
    public boolean isSevereWeatherAlert() {
        return false;
    }
}
