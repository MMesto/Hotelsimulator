import model.LayoutLoader;
import model.Hotel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutLoadertest {

    // Test 1: Goede layout laden
    @Test
    void testGeldigeLayoutLaden() throws Exception {
        // Arrange
        String pad = "layouts/hotel1.json";

        // Act
        Hotel hotel = LayoutLoader.laadLayout(pad);

        // Assert
        assertNotNull(hotel);
    }

    // Test 2: Verkeerde extensie
    @Test
    void testVerkeerdeExtensie() {
        // Arrange
        String pad = "layouts/hotel1.txt";

        // Act + Assert
        assertThrows(Exception.class, () -> {
            LayoutLoader.laadLayout(pad);
        });
    }

    // Test 3: Geen lobby
    @Test
    void testGeenLobby() {
        // Arrange
        String[][] grid = {
                {"G", "K", "K"},
                {"G", "F", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        });
    }

    // Test 4: Geen lift
    @Test
    void testGeenLift() {
        // Arrange
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "G", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        });
    }

    // Test 5: Geen trap
    @Test
    void testGeenTrap() {
        // Arrange
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "F", "K"},
                {"G", "G", "G"}
        };

        // Act + Assert
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        });
    }

    // Test 6: Alles aanwezig
    @Test
    void testAllesAanwezig() {
        // Arrange
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "F", "K"},
                {"T", "G", "G"}
        };

        // Act + Assert
        assertDoesNotThrow(() -> {
            LayoutLoader.valideerLayout(grid);
        });
    }
}