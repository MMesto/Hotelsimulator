package model.strategy;

import model.personen.Persoon;


/**
 * Strategie voor wanneer het brandalarm afgaat.
 * Personen negeren de lift en rennen via de trap (x=8.5) naar buiten.
 */
public class EvacuationMovement implements IMovementStrategy {
    private static final double SPEED = 0.5;
    private static final double STAIR_X = 8.5;
    private static final double LOBBY_X = 1.5;

    @Override
    public void beweeg(Persoon persoon, MovementData data) {
        // Requirement: Lift- en/of traplogica.
        // Bij evacuatie wordt de lift genegeerd en loopt iedereen via de trap naar buiten.
        double pX = persoon.getX();
        double pY = persoon.getY();

        // Hotelhoogte en trappositie komen expliciet uit de movement-data.
        int lobbyY = data.getMaxY() - 1;
        double stairX = data.getTrapX() > 0 ? data.getTrapX() : STAIR_X;

        // Stap 1: Als we nog boven zitten, loop eerst horizontaal naar de trap
        if ((int)pY != lobbyY) {
            if (Math.abs(pX - stairX) > SPEED) {
                persoon.setX(pX + (pX < stairX ? SPEED : -SPEED));
            } else {
                // Eenmaal bij de trap: zak verticaal naar beneden richting lobby
                persoon.setY(pY + (pY < lobbyY ? SPEED : -SPEED));
            }
        } else {
            // Stap 2: We zijn in de lobby, loop nu horizontaal naar de uitgang
            double dx = LOBBY_X - pX;
            if (Math.abs(dx) < SPEED) {
                persoon.setX(-1.5); // Loop de simulatie uit (buiten het scherm)

                // Update de specifieke status van de persoon
                persoon.setStateToLeft();
            } else {
                persoon.setX(pX + (dx > 0 ? SPEED : -SPEED));
            }
        }
    }
}
