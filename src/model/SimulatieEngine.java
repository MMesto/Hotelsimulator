package model;

import ui.HotelPanel;

/**
 * Simulatie-engine: beheert de HTE-ticks en laat alle entiteiten hun acties uitvoeren.
 */
public class SimulatieEngine {

    private Hotel hotel;
    private HotelPanel hotelPanel;
    private SimulationClock clock;
    private boolean running;

    // Tick-interval in ms
    private static final long DEFAULT_TICK_INTERVAL = 100;

    public SimulatieEngine(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.clock = new SimulationClock(DEFAULT_TICK_INTERVAL);
        this.running = false;
    }

    // Start de simulatie
    public void start() {
        running = true;
        clock.start();
        hotelPanel.setRunning(true);
    }

    // Pauzeer de simulatie
    public void pause() {
        running = false;
        clock.stop();
        hotelPanel.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    // Reset de klok (bij nieuw hotel of herstart)
    public void resetClock() {
        clock.reset();
    }

    public SimulationClock getClock() {
        return clock;
    }

    /**
     * Tick-methode die wordt aangeroepen door de Timer in de Main-class.
     * Alle acties van entiteiten worden alleen uitgevoerd bij een HTE-tick.
     */
    public void tick() {
        // Alleen uitvoeren als er een tick is
        if (clock.tick()) {
            if (running) {
                // Loop door alle personen
                for (Persoon persoon : hotel.getPersonen()) {
                    persoon.onTick(); // elke persoon handelt op de HTE-tick
                }
                // TODO: liften en andere events ook updaten op tick
            }
        }

        // Altijd de UI verversen
        hotelPanel.repaint();
    }
}