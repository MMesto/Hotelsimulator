package model;

/**
 * Simulatieklok voor HTE-ticks.
 * Beheert de tijdstappen van de simulatie.
 */
public class SimulationClock {

    private long tickInterval;      // tijd tussen ticks in ms
    private long lastTickTime;      // tijd van laatste tick
    private int timestep;           // huidige HTE-timestep

    public SimulationClock(long tickInterval) {
        this.tickInterval = tickInterval;
        this.timestep = 0;
        this.lastTickTime = System.currentTimeMillis();
    }

    /**
     * Controleer of het tijd is voor een tick.
     * @return true als een tick moet plaatsvinden
     */
    public boolean tick() {
        long now = System.currentTimeMillis();
        if (now - lastTickTime >= tickInterval) {
            lastTickTime = now;
            timestep++;
            return true;
        }
        return false;
    }

    public int getTimestep() {
        return timestep;
    }

    public void start() {
        lastTickTime = System.currentTimeMillis();
    }

    public void stop() {
        // geen extra actie nodig
    }

    public void reset() {
        timestep = 0;
        lastTickTime = System.currentTimeMillis();
    }
}