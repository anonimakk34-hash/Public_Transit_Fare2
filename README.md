# Assignment 5 — Strategy Pattern: Public Transit Fare & Fraud Risk Engine

## 1. Single Responsibility Principle (SRP) Class Table

| Class / Interface | Responsibility (One Sentence, Max 25 Words, Without Forbidden Conjunctions) |
| :--- | :--- |
| `TripRequest` | Encapsulates immutable passenger journey attributes required during fare calculation plus fraud risk evaluation. |
| `TransitTelemetryProvider` | Defines the abstraction contract for retrieving external station occupancy metrics plus weather alert statuses. |
| `SimulatedTelemetryProvider` | Generates simulated station crowd density values alongside weather statuses for live console demonstrations. |
| `FakeTelemetryProvider` | Supplies predetermined deterministic sensor readings to isolate unit tests from external telemetry fluctuations. |
| `FarePricingStrategy` | Declares the common contract for interchangeable algorithms that compute monetary transit journey fares. |
| `ZoneBasedPricingStrategy` | Calculates passenger trip cost according to the total number of transit zones crossed. |
| `DistanceTieredPricingStrategy` | Computes journey cost by applying progressive marginal rates across predefined kilometer distance brackets. |
| `LiveCongestionPricingStrategy` | Adjusts trip pricing dynamically using real-time station occupancy ratios from the telemetry provider. |
| `FareCalculatorContext` | Maintains a reference to the active pricing strategy while delegating fare calculation requests. |
| `FareStrategySelector` | Selects the optimal pricing strategy automatically by evaluating current trip timing plus zone parameters. |
| `TapRiskStrategy` | Declares the common interface for interchangeable algorithms that quantify turnstile card fraud risk. |
| `VelocityAnomalyRiskStrategy` | Scores card-sharing probability by comparing elapsed tap intervals against physical transit travel distance. |
| `FinancialBehaviorRiskStrategy` | Evaluates payment default risk using recent card decline counts alongside remaining wallet balance. |
| `JourneyHistoryRiskStrategy` | Quantifies fare evasion likelihood from historical unclosed tap records during late-night transit hours. |
| `RiskAssessmentContext` | Holds the active risk scoring strategy to execute turnstile fraud evaluations at runtime. |
| `RiskStrategySelector` | Chooses the appropriate fraud scoring strategy based on passenger tap frequency plus trip history. |
| `StrategyDemo` | Demonstrates manual strategy execution, automatic selector routing, plus runtime algorithm switching through console output. |

---

## 2. Why This Solution Is Strategy (Not State or Template Method)

### Strategy vs. State Pattern
Although both Strategy and State rely on composition and polymorphic delegation to interchangeable object implementations, their intent and lifecycle transitions differ fundamentally. In the **State** pattern, representable states correspond to distinct lifecycle phases of an entity (such as a turnstile tap moving sequentially from `PENDING` to `VALIDATED` to `COMPLETED`), and the state objects themselves—or the context enforcing state-machine transitions—dictate legal transitions from one state to the next. In contrast, our `FarePricingStrategy` and `TapRiskStrategy` implementations do not represent lifecycle stages of a journey, nor do they transition into one another after execution. `ZoneBasedPricingStrategy` never mutates `FareCalculatorContext` into `DistanceTieredPricingStrategy`. Instead, each strategy is a self-contained, stateless mathematical algorithm for computing a price or a fraud score, selected externally by a client or `FareStrategySelector` and swapped freely at runtime.

### Strategy vs. Template Method Pattern
The **Template Method** pattern uses **inheritance** to define an invariant high-level skeleton of an algorithm in an abstract base class while deferring specific customizable steps to subclasses via overridden `protected` methods. That approach only works when all variants share the exact same sequential workflow structure and is locked at compile time once an object is instantiated. In our public transit domain, `ZoneBasedPricingStrategy` (discrete zone boundary step functions), `DistanceTieredPricingStrategy` (marginal kilometer brackets), and `LiveCongestionPricingStrategy` (real-time sensor occupancy multipliers and weather rebates) do not share a rigid multi-step skeleton. By using **object composition** behind `FarePricingStrategy` and `TapRiskStrategy` interfaces, the entire algorithm is replaced wholesalely at runtime via `setStrategy()`, and new algorithms can be introduced without subclassing a fragile base class—strictly satisfying the Open/Closed Principle.

---

## 3. Refactoring Commit Links

* **Before Refactoring (`switch` / `if-else` in one class):** `<PASTE_YOUR_COMMIT_1_GITHUB_URL_HERE>`
* **After Refactoring (Refactored to Strategy Pattern):** `<PASTE_YOUR_COMMIT_2_GITHUB_URL_HERE>`
