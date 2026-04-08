package model;

import java.awt.Color;
import java.util.Random;

/**
 * Gast - een persoon in het hotel die zich beweegt op basis van HTE-ticks
 */
public class Gast extends Persoon {

    private static final Random RANDOM = new Random();

    private int x, y;           // huidige positie
    private int destX, destY;   // bestemming
    private int maxX, maxY;     // grenzen van het grid
    private Color kleur;        // kleur voor UI

    /**
     * Constructor: startpositie meegeven
     * @param naam naam van de gast
     * @param startX startpositie X
     * @param startY startpositie Y
     */
    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast"); // Roept Persoon constructor aan

        this.x = startX;
        this.y = startY;

        // Random bestemming binnen 10x10 grid (kan later aangepast worden)
        this.destX = RANDOM.nextInt(10);
        this.destY = RANDOM.nextInt(10);

        this.kleur = new Color(
                RANDOM.nextInt(256),
                RANDOM.nextInt(256),
                RANDOM.nextInt(256)
        );
    }

    /**
     * Wordt aangeroepen bij elke tick
     */
    public void onTick() {
        beweegStap();
        // Output positie bij elke tick (debug / console)
        System.out.println(getNaam() + " positie: (" + x + "," + y + ")");
    }

    /**
     * Beweeg 1 stap richting doel
     */
    private void beweegStap() {
        if (x < destX) x++;
        else if (x > destX) x--;

        if (y < destY) y++;
        else if (y > destY) y--;

        // Nieuwe bestemming kiezen als huidige bereikt
        if (x == destX && y == destY) {
            destX = RANDOM.nextInt(maxX);
            destY = RANDOM.nextInt(maxY);
        }
    }

    /**
     * Stel de grenzen van het grid in (zodat bestemming binnen hotel blijft)
     */
    public void setGridBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // --- Getters voor UI of debugging ---
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getKleur() { return kleur; }
}