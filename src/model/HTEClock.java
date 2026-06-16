package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * De centrale klok van de simulatie die alle luisteraars (listeners) aanstuurt.
 */
public class HTEClock {

    private final List<TickListener> listeners = new CopyOnWriteArrayList<>();
    private long tickCount = 0; // Telt het aantal gelopen ticks

    // Voeg een object toe dat moet reageren op de klok
    public void addListener(TickListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    // Verwijder een object van de klok
    public void removeListener(TickListener listener) {
        listeners.remove(listener);
    }

    // Voer één tijdstap (tick) uit voor de hele simulatie
    public void tick() {
        // Requirement: Simulatieklok (HTE).
        // Een HTE-tick stuurt alle objecten aan die TickListener implementeren.
        tickCount++;

        try {
            // Laat alle luisteraars tegelijk reageren op de tick
             for (TickListener listener : listeners) {
                if (listener != null) {
                    listener.onTick(); // polymorfisme
                }
            }
        } catch (Exception e) {
            System.err.println("[HTEClock] Fout tijdens tick #" + tickCount + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Print elke 100 ticks een status-update in de console
        if (tickCount % 100 == 0) {
            System.out.println("--- HTEClock TICK #" + tickCount + " afgehandeld (" + listeners.size() + " listeners) ---");
        }
    }

    public long getTickCount() {
        return tickCount;
    }
}
