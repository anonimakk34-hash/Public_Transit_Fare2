package strategy;

import strategy.pricing.*;
import strategy.risk.*;
import strategy.telemetry.SimulatedTelemetryProvider;
import strategy.telemetry.TransitTelemetryProvider;

import java.util.List;

public class StrategyDemo {
    public static void main(String[] args) {
        TransitTelemetryProvider telemetry = new SimulatedTelemetryProvider();

        TripRequest sampleTrip = new TripRequest(
                "ST-CENTRAL", // Busy station (0.88 occupancy)
                1,            // Origin Zone 1
                3,            // Destination Zone 3 (2 zones crossed)
                14.5,         // 14.5 km trip
                8,            // 08:00 AM (Morning Rush Hour)
                6,            // 6 minutes since last tap
                2,            // 2 unclosed previous trips
                1,            // 1 recent payment decline
                120.0         // 120.0 KZT balance
        );

        System.out.println("=== 1. SAME INPUT PROCESSED BY ALL STRATEGIES (FAMILY 1: PRICING) ===");
        List<FarePricingStrategy> pricingStrategies = List.of(
                new ZoneBasedPricingStrategy(),
                new DistanceTieredPricingStrategy(),
                new LiveCongestionPricingStrategy(telemetry)
        );
        FareCalculatorContext fareContext = new FareCalculatorContext(pricingStrategies.get(0));
        for (FarePricingStrategy strategy : pricingStrategies) {
            fareContext.setStrategy(strategy);
            System.out.printf("%-32s -> %.2f KZT%n",
                    strategy.getClass().getSimpleName(),
                    fareContext.executeCalculation(sampleTrip));
        }

        System.out.println("\n=== 2. SAME INPUT PROCESSED BY ALL STRATEGIES (FAMILY 2: RISK SCORING) ===");
        List<TapRiskStrategy> riskStrategies = List.of(
                new VelocityAnomalyRiskStrategy(),
                new FinancialBehaviorRiskStrategy(),
                new JourneyHistoryRiskStrategy()
        );
        RiskAssessmentContext riskContext = new RiskAssessmentContext(riskStrategies.get(0));
        for (TapRiskStrategy strategy : riskStrategies) {
            riskContext.setStrategy(strategy);
            System.out.printf("%-32s -> Risk Score: %.2f / 100%n",
                    strategy.getClass().getSimpleName(),
                    riskContext.assessTapRisk(sampleTrip));
        }

        System.out.println("\n=== 3. MANUAL CHOICE VS. AUTOMATIC CHOICE BY SELECTOR ===");
        // Manual choice
        fareContext.setStrategy(new DistanceTieredPricingStrategy());
        System.out.printf("Manual Choice (DistanceTiered)   -> %.2f KZT%n",
                fareContext.executeCalculation(sampleTrip));

        // Automatic choice by Selector (detects 08:00 rush hour -> LiveCongestionPricingStrategy)
        FareStrategySelector fareSelector = new FareStrategySelector(telemetry);
        FarePricingStrategy autoSelectedPricing = fareSelector.selectStrategy(sampleTrip);
        fareContext.setStrategy(autoSelectedPricing);
        System.out.printf("Auto Selector (%s) -> %.2f KZT%n",
                autoSelectedPricing.getClass().getSimpleName(),
                fareContext.executeCalculation(sampleTrip));

        System.out.println("\n=== 4. RUNTIME STRATEGY SWITCHING ON ACTIVE CONTEXT ===");
        RiskStrategySelector riskSelector = new RiskStrategySelector();
        TapRiskStrategy initialRiskStrategy = riskSelector.selectStrategy(sampleTrip);
        riskContext.setStrategy(initialRiskStrategy);
        System.out.printf("Initial Runtime Strategy (%s): %.2f%n",
                riskContext.getStrategy().getClass().getSimpleName(),
                riskContext.assessTapRisk(sampleTrip));

        // Switch at runtime to FinancialBehaviorRiskStrategy without recreating context
        riskContext.setStrategy(new FinancialBehaviorRiskStrategy());
        System.out.printf("Switched Runtime Strategy (%s): %.2f%n",
                riskContext.getStrategy().getClass().getSimpleName(),
                riskContext.assessTapRisk(sampleTrip));
    }
}