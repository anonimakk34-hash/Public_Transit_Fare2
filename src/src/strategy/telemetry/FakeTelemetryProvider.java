package strategy.telemetry;

public class FakeTelemetryProvider implements TransitTelemetryProvider {
    private final double fixedOccupancyRatio;
    private final boolean fixedWeatherAlert;

    public FakeTelemetryProvider(double fixedOccupancyRatio, boolean fixedWeatherAlert) {
        this.fixedOccupancyRatio = fixedOccupancyRatio;
        this.fixedWeatherAlert = fixedWeatherAlert;
    }

    @Override
    public double getStationOccupancyRatio(String stationId) {
        return fixedOccupancyRatio;
    }

    @Override
    public boolean isSevereWeatherAlert() {
        return fixedWeatherAlert;
    }
}