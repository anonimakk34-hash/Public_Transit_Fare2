package strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import strategy.pricing.*;
import strategy.risk.*;
import strategy.telemetry.FakeTelemetryProvider;
import strategy.telemetry.TransitTelemetryProvider;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TransitStrategyTest {

    private TripRequest createSampleRequest(
            int originZone, int destZone, double km, int hour,
            int minsSinceLastTap, int unclosedTrips, int declines, double balance) {
        return new TripRequest(
                "ST-TEST", originZone, destZone, km, hour,
                minsSinceLastTap, unclosedTrips, declines, balance
        );
    }

    // =========================================================================
    // 1. INDIVIDUAL TESTS FOR EVERY STRATEGY IN FAMILY 1 (PRICING)
    // =========================================================================

    @Test
    void testZoneBasedPricingStrategy() {
        FareCalculatorContext context = new FareCalculatorContext(new ZoneBasedPricingStrategy());
        TripRequest twoZones = createSampleRequest(1, 3, 10.0, 14, 30, 0, 0, 500.0);
        // billableZones = 2 -> (90 + 2*45) * (1 + 2*0.15) = 180 * 1.3 = 234.0
        assertEquals(234.0, context.executeCalculation(twoZones), 0.001);
    }

    @Test
    void testDistanceTieredPricingStrategy() {
        FareCalculatorContext context = new FareCalculatorContext(new DistanceTieredPricingStrategy());
        TripRequest fourteenKm = createSampleRequest(1, 1, 14.0, 14, 30, 0, 0, 500.0);
        // Base (3km) = 80 + Tier2 (9km * 18) = 162 + Tier3 (2km * 9.5) = 19 -> Total = 261.0
        assertEquals(261.0, context.executeCalculation(fourteenKm), 0.001);
    }

    @Test
    void testLiveCongestionPricingStrategyWithFakeTelemetrySurge() {
        // Fake telemetry: 90% occupancy (surge), no weather alert
        TransitTelemetryProvider fakeSurge = new FakeTelemetryProvider(0.90, false);
        FareCalculatorContext context = new FareCalculatorContext(new LiveCongestionPricingStrategy(fakeSurge));
        TripRequest tenKm = createSampleRequest(1, 2, 10.0, 8, 30, 0, 0, 500.0);
        // baseRate = 70 + 10*12 = 190. loadFactor = 1.0 + (0.90 - 0.50)*0.80 = 1.32 -> 190 * 1.32 = 250.8
        assertEquals(250.8, context.executeCalculation(tenKm), 0.001);
    }

    @Test
    void testLiveCongestionPricingStrategyWithFakeWeatherAlertDiscount() {
        // Fake telemetry: 50% normal occupancy, severe weather alert = true (30% eco discount)
        TransitTelemetryProvider fakeWeather = new FakeTelemetryProvider(0.50, true);
        FareCalculatorContext context = new FareCalculatorContext(new LiveCongestionPricingStrategy(fakeWeather));
        TripRequest tenKm = createSampleRequest(1, 2, 10.0, 12, 30, 0, 0, 500.0);
        // baseRate = 190. loadFactor = 1.0. Weather rebate = 190 * 0.70 = 133.0
        assertEquals(133.0, context.executeCalculation(tenKm), 0.001);
    }

    // =========================================================================
    // 2. INDIVIDUAL TESTS FOR EVERY STRATEGY IN FAMILY 2 (RISK SCORING)
    // =========================================================================

    @Test
    void testVelocityAnomalyRiskStrategyDetectsRapidPassbackAndHighSpeed() {
        RiskAssessmentContext context = new RiskAssessmentContext(new VelocityAnomalyRiskStrategy());
        // 10 km in 3 minutes -> implied speed = 200 km/h + rapid passback (<= 4 mins)
        TripRequest anomalyTap = createSampleRequest(1, 2, 10.0, 12, 3, 0, 0, 500.0);
        assertEquals(100.0, context.assessTapRisk(anomalyTap), 0.001);
    }

    @Test
    void testFinancialBehaviorRiskStrategyDetectsDeclinesAndLowBalance() {
        RiskAssessmentContext context = new RiskAssessmentContext(new FinancialBehaviorRiskStrategy());
        // 2 recent declines (44 pts) + balance 90.0 on estimatedCost 180.0 (50% deficit = 25 pts) -> 69.0
        TripRequest riskyWallet = createSampleRequest(1, 2, 10.0, 12, 45, 0, 2, 90.0);
        assertEquals(69.0, context.assessTapRisk(riskyWallet), 0.001);
    }

    @Test
    void testJourneyHistoryRiskStrategyLateNightMultiZone() {
        RiskAssessmentContext context = new RiskAssessmentContext(new JourneyHistoryRiskStrategy());
        // 2 unclosed trips, late night (23:00), 2 zones crossed
        TripRequest lateEvasion = createSampleRequest(1, 3, 8.0, 23, 60, 2, 0, 500.0);
        double score = context.assessTapRisk(lateEvasion);
        assertTrue(score > 70.0 && score <= 100.0);
    }

    // =========================================================================
    // 3. SELECTOR RULE TESTS (BOTH FAMILIES)
    // =========================================================================

    @Test
    void testFareStrategySelectorRules() {
        TransitTelemetryProvider normalTelemetry = new FakeTelemetryProvider(0.50, false);
        FareStrategySelector selector = new FareStrategySelector(normalTelemetry);

        // Rule 1: Peak hour (8 AM) -> LiveCongestionPricingStrategy
        TripRequest peakTrip = createSampleRequest(1, 1, 5.0, 8, 30, 0, 0, 500.0);
        assertInstanceOf(LiveCongestionPricingStrategy.class, selector.selectStrategy(peakTrip));

        // Rule 2: Off-peak multi-zone (>= 2 zones crossed) -> ZoneBasedPricingStrategy
        TripRequest multiZoneTrip = createSampleRequest(1, 4, 15.0, 13, 30, 0, 0, 500.0);
        assertInstanceOf(ZoneBasedPricingStrategy.class, selector.selectStrategy(multiZoneTrip));

        // Rule 3: Off-peak local trip (< 2 zones crossed) -> DistanceTieredPricingStrategy
        TripRequest localTrip = createSampleRequest(1, 2, 6.0, 14, 30, 0, 0, 500.0);
        assertInstanceOf(DistanceTieredPricingStrategy.class, selector.selectStrategy(localTrip));
    }

    @Test
    void testRiskStrategySelectorRules() {
        RiskStrategySelector selector = new RiskStrategySelector();

        // Rule 1: Rapid consecutive tap (<= 10 mins) -> VelocityAnomalyRiskStrategy
        TripRequest rapidTap = createSampleRequest(1, 1, 2.0, 12, 5, 3, 2, 100.0);
        assertInstanceOf(VelocityAnomalyRiskStrategy.class, selector.selectStrategy(rapidTap));

        // Rule 2: Unclosed trips >= 2 (when tap interval > 10 mins) -> JourneyHistoryRiskStrategy
        TripRequest unclosedTap = createSampleRequest(1, 2, 8.0, 15, 25, 2, 0, 500.0);
        assertInstanceOf(JourneyHistoryRiskStrategy.class, selector.selectStrategy(unclosedTap));

        // Rule 3: Normal interval and < 2 unclosed trips -> FinancialBehaviorRiskStrategy
        TripRequest standardTap = createSampleRequest(1, 1, 4.0, 15, 40, 0, 1, 50.0);
        assertInstanceOf(FinancialBehaviorRiskStrategy.class, selector.selectStrategy(standardTap));
    }

    // =========================================================================
    // 4. PARAMETERIZED TESTS ACROSS ALL STRATEGIES OF A FAMILY
    // =========================================================================

    static Stream<FarePricingStrategy> allPricingStrategiesProvider() {
        TransitTelemetryProvider fakeTelemetry = new FakeTelemetryProvider(0.60, false);
        return Stream.of(
                new ZoneBasedPricingStrategy(),
                new DistanceTieredPricingStrategy(),
                new LiveCongestionPricingStrategy(fakeTelemetry)
        );
    }

    @ParameterizedTest
    @MethodSource("allPricingStrategiesProvider")
    void testAllPricingStrategiesNeverReturnNegativeFare(FarePricingStrategy strategy) {
        FareCalculatorContext context = new FareCalculatorContext(strategy);
        TripRequest zeroDistanceTrip = createSampleRequest(1, 1, 0.0, 12, 30, 0, 0, 200.0);
        TripRequest longTrip = createSampleRequest(1, 5, 25.0, 18, 30, 0, 0, 200.0);

        assertTrue(context.executeCalculation(zeroDistanceTrip) >= 0.0,
                "Fare must never be negative for 0 km trip");
        assertTrue(context.executeCalculation(longTrip) > 0.0,
                "Fare must be strictly positive for valid trip");
    }

    static Stream<TapRiskStrategy> allRiskStrategiesProvider() {
        return Stream.of(
                new VelocityAnomalyRiskStrategy(),
                new FinancialBehaviorRiskStrategy(),
                new JourneyHistoryRiskStrategy()
        );
    }

    @ParameterizedTest
    @MethodSource("allRiskStrategiesProvider")
    void testAllRiskStrategiesReturnScoreWithinZeroToHundredBounds(TapRiskStrategy strategy) {
        RiskAssessmentContext context = new RiskAssessmentContext(strategy);
        TripRequest cleanTrip = createSampleRequest(1, 1, 3.0, 14, 120, 0, 0, 5000.0);
        TripRequest extremeTrip = createSampleRequest(1, 6, 50.0, 23, 1, 10, 10, 0.0);

        double cleanScore = context.assessTapRisk(cleanTrip);
        double extremeScore = context.assessTapRisk(extremeTrip);

        assertTrue(cleanScore >= 0.0 && cleanScore <= 100.0, "Score must be in [0, 100]");
        assertTrue(extremeScore >= 0.0 && extremeScore <= 100.0, "Score must be capped at 100");
    }
}