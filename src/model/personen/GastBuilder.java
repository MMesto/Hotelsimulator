package model.personen;

import model.*;

import java.util.Objects;

/**
 * BUILDER PATTERN.
 * Bouwt een Gast stap voor stap op. Handig omdat een Gast veel
 * afhankelijkheden heeft (hotel, lift, eventbus, positie, gridgrenzen).
 * Elke setter geeft 'this' terug, zodat je de aanroepen kunt koppelen
 * (method chaining). Pas bij build() ontstaat het echte Gast-object.
 */
public class GastBuilder {
    // --- Tijdelijke opslag: hier verzamelt de builder alle gegevens ---
    private String naam;        // naam van de gast (verplicht)
    private Hotel hotel;        // het hotel waarin de gast leeft (verplicht)
    private Lift lift;          // de lift die de gast mag gebruiken
    private IEventBus eventBus; // interface-type, niet de concrete EventBusImpl (Dependency Inversion)
    private String preferredRoomType = "Standaard";
    private double startX;      // start-x op het grid
    private double startY;      // start-y op het grid
    private int maxX;           // breedte van het grid (rechtergrens)
    private int maxY;           // hoogte van het grid (aantal verdiepingen)

    // --- Setter-methoden: slaan een waarde op en geven de builder zelf terug ---
    // Doordat ze 'this' returnen kun je ze achter elkaar koppelen: .naam(...).hotel(...)...
    public GastBuilder naam(String naam)            { this.naam = naam;     return this; }
    public GastBuilder hotel(Hotel hotel)           { this.hotel = hotel;   return this; }
    public GastBuilder lift(Lift lift)              { this.lift = lift;     return this; }
    public GastBuilder eventBus(IEventBus eventBus) { this.eventBus = eventBus; return this; }
    public GastBuilder preferredRoomType(String type) { this.preferredRoomType = type; return this; }
    public GastBuilder startPos(double x, double y) { this.startX = x; this.startY = y; return this; }
    public GastBuilder gridBounds(int w, int h)     { this.maxX = w; this.maxY = h; return this; }

    /**
     * Maakt de uiteindelijke Gast aan op basis van alle verzamelde gegevens.
     */
    public Gast build() {
        // Validatie: zonder naam of hotel is een gast zinloos -> meteen stoppen
        // met een duidelijke melding i.p.v. een vage fout later in de simulatie.
        Objects.requireNonNull(naam,  "naam is required");
        Objects.requireNonNull(hotel, "hotel is required");

        Gast g = new Gast(naam, -1, 0);   // maak de gast (constructor zet kleur + strategieën)
        g.setHotel(hotel);                // koppel het hotel
        g.setLift(lift);                  // koppel de lift
        g.setEventBus(eventBus);          // koppel de eventbus
        g.setGridBounds(maxX, maxY);      // stel de grid-grenzen in
        g.setPreferredRoomType(preferredRoomType);
        g.setStartPositie(startX, startY);// zet de startpositie
        return g;                         // geef de volledig opgebouwde gast terug
    }
}
