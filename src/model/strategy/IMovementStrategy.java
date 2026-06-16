package model.strategy;

import model.personen.Persoon;

/**
 * De blauwdruk voor alle loop- en navigatiestrategieën.
 */
public interface IMovementStrategy {
    void beweeg(Persoon persoon, MovementData data);
}
