package model;

import java.awt.Color;

/**
 * De Area klasse representeert een specifieke ruimte in het hotel.
 * Deze klasse bevat de data die direct uit de layout-configuratie komt
 * en biedt handige methodes om deze data om te zetten naar bruikbare coördinaten.
 */
public class Area {
    public enum Type {
        ROOM      ("Room",       "Kamer",      new Color(200, 230, 255)),
        CINEMA    ("Cinema",     "Bioscoop",   new Color(200, 100, 200)),
        RESTAURANT("Restaurant", "Restaurant", new Color(255, 100, 100)),
        FITNESS   ("Fitness",    "Fitness",    new Color(100, 220, 180)),
        LOBBY     ("Lobby",      "Lobby",      new Color(255, 220,  50)),
        SCHACHT   ("Schacht",    "Schacht",    new Color(220, 220, 220)),
        LIFT      ("Lift",       "Lift",       new Color(255, 150,   0)),
        STAIRCASE ("Staircase",  "Trap",       new Color(100, 200, 100)),
        STORAGE   ("Storage",    "Opslag",     new Color(180, 140, 100)),
        LOUNGE    ("Lounge",     "Lounge",     new Color(230, 210, 150)),
        UNKNOWN   ("",           "",           Color.WHITE);

        private final String jsonKey;
        private final String label;
        private final Color color;

        Type(String jsonKey, String label, Color color) {
            this.jsonKey = jsonKey;
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public Color getColor() { return color; }

        public static Type fromString(String key) {
            if ("Opslag".equalsIgnoreCase(key)) return STORAGE;
            for (Type type : values()) {
                if (type.jsonKey.equalsIgnoreCase(key)) return type;
            }
            return UNKNOWN;
        }
    }

    // Ruwe datavelden (meestal ingevuld door een JSON-parser zoals Jackson of Gson)
    private String AreaType;       // Bijv: "Room", "Cinema", "Lobby"
    private String Position;       // Bijv: "1, 1" (X, Y)
    private String Dimension;      // Bijv: "2, 1" (Breedte, Hoogte)
    private String Classification; // Bijv: "3 Star" (alleen voor kamers)
    private int Capacity;          // Maximaal aantal personen

    // --- Standaard Getters ---
    public String getAreaType() { return AreaType; }
    public Type getType() { return Type.fromString(AreaType); }
    public String getClassification() { return Classification; }

    /**
     * Haalt de X-coördinaat op uit de Position string.
     * split(",") splitst de tekst op de komma, trim() verwijdert spaties,
     * en Integer.parseInt maakt er een echt getal van.
     */
    public int getX() {
        return Integer.parseInt(Position.split(",")[0].trim());
    }

    /**
     * Haalt de Y-coördinaat op uit de Position string (het tweede getal).
     */
    public int getY() {
        return Integer.parseInt(Position.split(",")[1].trim());
    }

    /**
     * Haalt de breedte op uit de Dimension string (het eerste getal).
     */
    public int getBreedte() {
        return Integer.parseInt(Dimension.split(",")[0].trim());
    }

    /**
     * Haalt de hoogte op uit de Dimension string (het tweede getal).
     */
    public int getHoogte() {
        return Integer.parseInt(Dimension.split(",")[1].trim());
    }
}
