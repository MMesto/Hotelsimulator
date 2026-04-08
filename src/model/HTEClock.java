import model.TickListener;

import java.util.ArrayList;
import java.util.List;

// De centrale klok van de simulatie
public class HTEClock {

    // Lijst met alle objecten die moeten reageren op ticks
    private final List<TickListener> listeners = new ArrayList<>();

    // Houdt bij hoeveel ticks er zijn geweest
    private int tickCount = 0;

    // Voeg een entiteit toe aan de klok
    public void addListener(TickListener listener) {
        listeners.add(listener);
    }

    // Verwijder een entiteit (optioneel)
    public void removeListener(TickListener listener) {
        listeners.remove(listener);
    }

    // Deze methode stelt één tijdstap (tick) voor
    public void tick() {
        tickCount++;

        // Debug output (handig tijdens testen)
        System.out.println("Tick: " + tickCount);

        // BELANGRIJK:
        // Alle entiteiten reageren hier tegelijk op dezelfde tick
        for (TickListener listener : listeners) {
            listener.onTick();
        }
    }
}