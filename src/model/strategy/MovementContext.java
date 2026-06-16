package model.strategy;

import model.personen.Persoon;

/**
 * Context class voor het Strategy Pattern.
 *
 * Deze class bewaart de actieve bewegingsstrategie en voert die uit.
 * Persoon gebruikt deze context, maar hoeft niet te weten welke concrete
 * strategy actief is: GastNormalStrategy, SchoonmakerNormalStrategy of
 * EvacuationMovement.
 */
public class MovementContext {
    private IMovementStrategy currentStrategy; // De huidige strategy
    private IMovementStrategy normalStrategy; // De normale strategy, bijvoorbeeld GastNormalStrategy.
    private IMovementStrategy evacuationStrategy; // De strategy voor brandalarm/evacuatie.

    public MovementContext(IMovementStrategy normalStrategy, IMovementStrategy evacuationStrategy) { // Constructor krijgt strategies van buitenaf.
        setStrategies(normalStrategy, evacuationStrategy); // Sla de meegegeven strategies op.
        useNormalStrategy(); // Start standaard met normaal beweeggedrag.
    }
    /**
     * Strategy-injectie: van buitenaf worden de concrete algorithms meegegeven.
     */
    public void setStrategies(IMovementStrategy normalStrategy, IMovementStrategy evacuationStrategy) { // Injecteer/vervang de strategies.
        this.normalStrategy = normalStrategy; // Bewaar de normale strategy.
        this.evacuationStrategy = evacuationStrategy; // Bewaar de evacuatie-strategy.
        this.currentStrategy = normalStrategy; // Zet de actieve strategy terug naar normaal.
    } // Einde setStrategies.

    public void beweeg(Persoon persoon, MovementData data) { // Enige methode waarmee de context beweging uitvoert.
        if (currentStrategy != null) { // Controleer of er een actieve strategy is.
            currentStrategy.beweeg(persoon, data); // Polymorfisme: Java kiest de juiste concrete beweeg-methode.
        }
    }

    public void useNormalStrategy() { // Wissel naar normaal gedrag.
        this.currentStrategy = normalStrategy; // Maak de normale strategy actief.
    }

    public void useEvacuationStrategy() { // Wissel naar evacuatiegedrag.
        this.currentStrategy = evacuationStrategy; // Maak de evacuatie-strategy actief.
    }

    public boolean isUsingEvacuationStrategy() { // Check of evacuatiegedrag nu actief is.
        return currentStrategy == evacuationStrategy; // True als de actieve strategy de evacuatie-strategy is.
    }
}
