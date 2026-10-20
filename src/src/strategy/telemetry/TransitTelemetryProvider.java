package strategy.telemetry;

/**
 * Clean Code Ch. 10 — Isolates strategies from external hardware/API changes.
 */
public interface TransitTelemetryProvider {
    /**
     * @param stationId station identifier
     * @return real-time station crowd occupancy ratio in range [0.0, 1.0]
     */
    double getStationOccupancyRatio(String stationId);

    /**
     * @return true if city smog or severe weather alert is active (triggers eco-discount)
     */
    boolean isSevereWeatherAlert();
}