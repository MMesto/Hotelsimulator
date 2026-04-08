package model;

import model.Hotel;
import model.LayoutLoader;
import model.Simulator;
import ui.HotelPanel;
import model.Gast;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static Simulator simulator;
    private static JButton startPauseButton;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // CONTROL PANEL
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Layouts ophalen
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // LOAD BUTTON
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // START/PAUSE BUTTON
                startPauseButton = new JButton("Start");
                controlPanel.add(startPauseButton);

                startPauseButton.addActionListener(e -> {
                    if (!simulator.isRunning()) {
                        simulator.start();
                        startPauseButton.setText("Pause");
                        statusLabel.setText("Simulatie actief");
                    } else {
                        simulator.pause();
                        startPauseButton.setText("Start");
                        statusLabel.setText("Simulatie gepauzeerd");
                    }
                });

                // Status label
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // INIT FASE - eerste layout laden
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);

                // HotelPanel UI
                hotelPanel = new HotelPanel(hotel);

                // Voeg testgasten toe
                addTestGuests(hotel);

                // SIMULATOR INIT
                simulator = new Simulator(hotel, hotelPanel);
                simulator.pause(); // Start paused

                statusLabel.setText("Hotel geladen: " + layouts[0] + " (Klik Start)");

                // UI toevoegen
                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // HTE-TICK TIMER (500ms)
                new Timer(500, e -> {
                    if (simulator.isRunning()) {
                        simulator.tick();  // Tick alleen als simulatie actief
                    }
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

            // UI update
            hotelPanel.setHotel(newHotel);

            // Voeg testgasten toe
            addTestGuests(newHotel);

            // Simulator reset
            simulator = new Simulator(newHotel, hotelPanel);
            simulator.pause();

            // Reset UI-knop
            startPauseButton.setText("Start");
            statusLabel.setText("Hotel geladen: " + selectedLayout + " (Klik Start)");

        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }

    private static void addTestGuests(Hotel hotel) {
        // Testgasten toevoegen
        Gast gast1 = new Gast("Alice", 2, 2);
        gast1.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast1);

        Gast gast2 = new Gast("Bob", 3, 3);
        gast2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast2);

        Gast gast3 = new Gast("Charlie", 1, 1);
        gast3.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast3);
    }
}