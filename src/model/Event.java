// Simpel event dat een aantal ticks duurt

import model.TickListener;

public class Event implements TickListener {

    private int ticksRemaining;

    public Event(int duration) {
        this.ticksRemaining = duration;
    }

    @Override
    public void onTick() {

        // Elke tick telt het event af
        if (ticksRemaining > 0) {
            ticksRemaining--;
            System.out.println("Event bezig, resterende ticks: " + ticksRemaining);
        } else {
            System.out.println("Event klaar");
        }
    }
}