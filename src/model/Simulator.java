package model;

import model.SimulationClock;
import ui.HotelPanel;

/**
 * Simulator - Stuurt de simulatie aan via HTE-ticks
 */
public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;
    private Hotel hotel;
    private SimulationClock clock;

    private static final long TICK_INTERVAL = 100; // 100 ms

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false;
        this.clock = new SimulationClock(TICK_INTERVAL);
    }

    public void start() {
        running = true;
        clock.start(); // start interne tijdmeting
        hotelPanel.setRunning(true);
    }

    public void pause() {
        running = false;
        clock.stop();
        hotelPanel.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    public SimulationClock getClock() {
        return clock;
    }

    public void resetClock() {
        clock.reset();
    }

    /**
     * HOOFDMETHODE: Wordt aangeroepen door Timer in main
     */
    public void tick() {

        // Check of er een echte HTE-tick plaatsvindt
        if (running && clock.tick()) {

            // Elke persoon handelt zijn eigen gedrag af
            for (Persoon persoon : hotel.getPersonen()) {
                persoon.onTick(); // ✅ vervang notify() door onTick()
            }

            // Later uitbreiden:
            // for (Lift lift : hotel.getLiften()) lift.onTick();
            // for (Event event : hotel.getEvents()) event.onTick();
        }

        // UI altijd updaten (ook bij pauze)
        hotelPanel.repaint();
    }
}