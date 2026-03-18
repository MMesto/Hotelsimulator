import model.LayoutLoader;
import model.Hotel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutLoadertest {


        // Test 1: Goede layout laden
        @Test
        void testGeldigeLayoutLaden() throws Exception {
            Hotel hotel = LayoutLoader.laadLayout("layouts/hotel1.layout");
            assertNotNull(hotel); // hotel mag niet null zijn
        }

        // Test 2: Verkeerde extensie
        @Test
        void testVerkeerdeExtensie() {
            assertThrows(Exception.class, () -> {
                LayoutLoader.laadLayout("layouts/hotel1.txt");
            });
        }

        // Test 3: Geen lobby
        @Test
        void testGeenLobby() {
            String[][] grid = {
                    {"G", "K", "K"},
                    {"G", "F", "K"},
                    {"T", "G", "G"}  // geen L
            };
            assertThrows(Exception.class, () -> {
                LayoutLoader.valideerLayout(grid);
            });
        }

        // Test 4: Geen lift
        @Test
        void testGeenLift() {
            String[][] grid = {
                    {"L", "K", "K"},
                    {"G", "G", "K"},
                    {"T", "G", "G"}  // geen F
            };
            assertThrows(Exception.class, () -> {
                LayoutLoader.valideerLayout(grid);
            });
        }

        // Test 5: Geen trap
        @Test
        void testGeenTrap() {
            String[][] grid = {
                    {"L", "K", "K"},
                    {"G", "F", "K"},
                    {"G", "G", "G"}  // geen T
            };
            assertThrows(Exception.class, () -> {
                LayoutLoader.valideerLayout(grid);
            });
        }

        // Test 6: Alles aanwezig
        @Test
        void testAllesAanwezig() {
            String[][] grid = {
                    {"L", "K", "K"},
                    {"G", "F", "K"},
                    {"T", "G", "G"}
            };
            assertDoesNotThrow(() -> {
                LayoutLoader.valideerLayout(grid);
            });
        }
    }

