package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deze testklasse controleert of de LayoutLoader correct werkt.
 * Elke test is opgebouwd volgens het Arrange-Act-Assert patroon.
 */
public class LayoutLoaderTest {

    @Test
    void testGeldigeLayoutLaden() throws Exception {
        // Arrange: gebruik een bestaand geldig JSON-layoutbestand.
        String pad = "layouts/hotel_multistory.json";

        // Act: laad het layoutbestand via de LayoutLoader.
        Hotel hotel = LayoutLoader.laadLayout(pad);

        // Assert: controleer dat er een Hotel-object is aangemaakt.
        assertNotNull(hotel, "Het ingeladen hotel mag niet null zijn bij een geldig JSON-bestand.");
    }

    @Test
    void testVerkeerdeExtensie() {
        // Arrange: gebruik een bestandspad met een niet-toegestane extensie.
        String pad = "layouts/hotel1.txt";

        // Act + Assert: LayoutLoader moet alleen JSON-bestanden accepteren.
        assertThrows(Exception.class, () -> {
            LayoutLoader.laadLayout(pad);
        }, "Er moet een Exception gegooid worden als de bestandsextensie onjuist is.");
    }

    @Test
    void testGeenLobby() {
        // Arrange: maak een grid zonder lobby, maar met lift en trap.
        String[][] grid = {
                {"G", "K", "K"},
                {"G", "Elevator", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert: de huidige LayoutLoader valideert lobby niet als verplichte harde fout.
        assertDoesNotThrow(() -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder lobby wordt momenteel niet afgekeurd door LayoutLoader.");
    }

    @Test
    void testGeenLift() {
        // Arrange: maak een grid zonder lift.
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "G", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert: lift is verplicht.
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder lift moet worden afgekeurd.");
    }

    @Test
    void testGeenTrap() {
        // Arrange: maak een grid zonder trap.
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "Elevator", "K"},
                {"G", "G", "G"}
        };

        // Act + Assert: trap is verplicht.
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder trap moet worden afgekeurd.");
    }

    @Test
    void testAllesAanwezig() {
        // Arrange: maak een grid met lobby, lift en trap.
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "Elevator", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert: een correcte layout mag geen Exception gooien.
        assertDoesNotThrow(() -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een correcte layout met lobby, lift en trap mag geen Exception gooien.");
    }
}
