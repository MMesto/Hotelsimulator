package model;

/**
 * Basisklasse voor alle personen
 */
public abstract class Persoon implements TickListener {

    private static int volgendeId = 1;

    private int id;
    private String naam;
    private String type;

    public Persoon(String naam, String type) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public String getType() {
        return type;
    }

    // 🔥 BELANGRIJK: nodig voor @Override in Gast
    @Override
    public abstract void onTick();
}