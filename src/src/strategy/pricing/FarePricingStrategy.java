package strategy.pricing;

import strategy.TripRequest;

public interface FarePricingStrategy {
    double calculateFare(TripRequest request);
}