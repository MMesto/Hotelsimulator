import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import model.Simulator;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static JLabel timestepLabel;
    private static Simulator simulator;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // layouts ophalen
                String[] layouts = getAvailableLayouts();

                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // load knop
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // Start/Pause toggle knop (1 knop voor beide functies)
                JButton startPauseButton = new JButton("Start");
                startPauseButton.addActionListener(e -> {
                    if (!simulator.isRunning()) {
                        // Start simulatie
                        simulator.start();
                        startPauseButton.setText("Pause");
                        statusLabel.setText("Simulatie actief");
                    } else {
                        // Pause simulatie
                        simulator.pause();
                        startPauseButton.setText("Resume");
                        statusLabel.setText("Simulatie gepauzeerd");
                    }
                });
                controlPanel.add(startPauseButton);

                // status label
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // timestep label (toont klok)
                timestepLabel = new JLabel("Timestep: 0");
                controlPanel.add(timestepLabel);

                // eerste layout laden
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel);

                // Voeg test gasten toe
                addTestGuests(hotel);

                // 🔥 simulator hier maken (BELANGRIJK)
                simulator = new Simulator(hotel, hotelPanel);
                simulator.pause(); // Start in paused state

                statusLabel.setText("Hotel geladen: " + layouts[0] + " (Klik 'Start' om de simulatie te beginnen)");

                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // 🔥 HTE ticks
                new Timer(500, e -> {
                    simulator.tick();
                    // Update timestep display
                    timestepLabel.setText("Timestep: " + simulator.getClock().getTimestep());
                }).start();

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));

        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Geen layout bestanden gevonden");
        }

        Arrays.sort(layouts);
        return layouts;
    }

    private static void loadLayout() {
        try {
            String selectedLayout = (String) layoutDropdown.getSelectedItem();

            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);
            hotelPanel.setHotel(newHotel);

            // Voeg test gasten toe aan het nieuwe hotel
            addTestGuests(newHotel);

            // 🔥 simulator resetten op paused state
            simulator = new Simulator(newHotel, hotelPanel);
            simulator.resetClock();
            simulator.pause();

            statusLabel.setText("Hotel geladen: " + selectedLayout + " (Klik 'Start' om de simulatie te beginnen)");

        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }

    private static void addTestGuests(Hotel hotel) {
        // Voeg enkele test gasten toe
        model.Gast gast1 = new model.Gast("Alice", 2, 2);
        gast1.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast1);

        model.Gast gast2 = new model.Gast("Bob", 3, 3);
        gast2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast2);

        model.Gast gast3 = new model.Gast("Charlie", 1, 1);
        gast3.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast3);
    }
}