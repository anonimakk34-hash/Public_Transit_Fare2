package strategy.pricing;

import strategy.TripRequest;

public class FareCalculatorContext {
    private FarePricingStrategy strategy;

    public FareCalculatorContext(FarePricingStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(FarePricingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public FarePricingStrategy getStrategy() {
        return strategy;
    }

    public double executeCalculation(TripRequest request) {
        return strategy.calculateFare(request);
    }
}