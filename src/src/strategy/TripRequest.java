package strategy;

public class TripRequest {
    private final String stationId;
    private final int originZone;
    private final int destinationZone;
    private final double distanceKm;
    private final int hourOfDay; // 0 - 23
    private final int minutesSinceLastTap;
    private final int unclosedTripsCount;
    private final int recentDeclinesCount;
    private final double cardBalance;

    public TripRequest(
            String stationId,
            int originZone,
            int destinationZone,
            double distanceKm,
            int hourOfDay,
            int minutesSinceLastTap,
            int unclosedTripsCount,
            int recentDeclinesCount,
            double cardBalance) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        if (hourOfDay < 0 || hourOfDay > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        this.stationId = stationId;
        this.originZone = originZone;
        this.destinationZone = destinationZone;
        this.distanceKm = distanceKm;
        this.hourOfDay = hourOfDay;
        this.minutesSinceLastTap = minutesSinceLastTap;
        this.unclosedTripsCount = unclosedTripsCount;
        this.recentDeclinesCount = recentDeclinesCount;
        this.cardBalance = cardBalance;
    }

    public String getStationId() { return stationId; }
    public int getOriginZone() { return originZone; }
    public int getDestinationZone() { return destinationZone; }
    public int getZonesCrossed() { return Math.abs(destinationZone - originZone); }
    public double getDistanceKm() { return distanceKm; }
    public int getHourOfDay() { return hourOfDay; }
    public int getMinutesSinceLastTap() { return minutesSinceLastTap; }
    public int getUnclosedTripsCount() { return unclosedTripsCount; }
    public int getRecentDeclinesCount() { return recentDeclinesCount; }
    public double getCardBalance() { return cardBalance; }
}
