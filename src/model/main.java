package model;

import model.Hotel;
import model.LayoutLoader;
import model.Simulator;
import ui.HotelPanel;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class main {

    // UI componenten (globaal zodat meerdere methodes ze kunnen gebruiken)
    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static Simulator simulator;
    private static JButton startPauseButton;

    public static void main(String[] args) {

        // UI moet op de Swing thread draaien
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // CONTROL PANEL - alle knoppen en interactie
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Beschikbare layouts ophalen
                String[] layouts = getAvailableLayouts();

                // Dropdown voor layouts
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // LOAD BUTTON - laadt nieuwe layout
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // START/PAUSE BUTTON - bestuurt simulatie
                startPauseButton = new JButton("Start");
                controlPanel.add(startPauseButton);

                startPauseButton.addActionListener(e -> {

                    // STATE CHECK - draait simulatie?
                    if (!simulator.isRunning()) {

                        // START FASE
                        simulator.start();
                        startPauseButton.setText("Pause");
                        statusLabel.setText("Simulatie actief");

                    } else {

                        // PAUSE FASE
                        simulator.pause();
                        startPauseButton.setText("Start");
                        statusLabel.setText("Simulatie gepauzeerd");
                    }
                });

                // Status label voor feedback
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // INIT FASE - eerste layout laden
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);

                // UI component makenn
                hotelPanel = new HotelPanel(hotel);



                // SIMULATOR INIT - koppelt logic aan UI
                simulator = new Simulator(hotel, hotelPanel);

                // START IN PAUSED STATE
                simulator.pause();

                statusLabel.setText("Hotel geladen: " + layouts[0] + " (Klik Start)");

                // UI opbouwen
                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // HTE TICK LOOP - wordt elke 500ms uitgevoerd
                new Timer(500, e -> {

                    // UPDATE FASE - alleen als simulatie actief is
                    simulator.tick();

                }).start();

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // HAALT ALLE LAYOUT BESTANDEN OP
    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");

        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));

        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Geen layout bestanden gevonden");
        }

        Arrays.sort(layouts);
        return layouts;
    }

    // LOAD NIEUWE LAYOUT
    private static void loadLayout() {
        try {
            String selectedLayout = (String) layoutDropdown.getSelectedItem();

            // NIEUW HOTEL LADEN
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);

            // UI UPDATE
            hotelPanel.setHotel(newHotel);



            // SIMULATOR RESET
            simulator = new Simulator(newHotel, hotelPanel);
            simulator.pause();

            // UI RESET (BELANGRIJK!)
            startPauseButton.setText("Start");

            statusLabel.setText("Hotel geladen: " + selectedLayout + " (Klik Start)");

        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }


}