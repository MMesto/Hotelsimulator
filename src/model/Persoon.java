package model;

/**
 * Persoon - base klasse voor alle personen in het hotel (gasten, schoonmakers, etc.)
 */
public class Persoon {
    private static int volgendeId = 1;
    private int id;
    private String naam;
    private String type; // bijv. gast, schoonmaker

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

    @Override
    public String toString() {
        return "Persoon{" +
                "id=" + id +
                ", naam='" + naam + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
