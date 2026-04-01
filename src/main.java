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

                // pause knop
                JButton pauseButton = new JButton("Pause");
                controlPanel.add(pauseButton);

                pauseButton.addActionListener(e -> {
                    if (simulator.isRunning()) {
                        simulator.pause();
                        pauseButton.setText("Resume");
                        statusLabel.setText("Simulatie gepauzeerd");
                    } else {
                        simulator.start();
                        pauseButton.setText("Pause");
                        statusLabel.setText("Simulatie actief");
                    }
                });

                // status label
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // eerste layout laden
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel);

                // 🔥 simulator hier maken (BELANGRIJK)
                simulator = new Simulator(hotelPanel);
                simulator.start();

                statusLabel.setText("Hotel geladen: " + layouts[0]);

                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // 🔥 HTE ticks
                new Timer(500, e -> simulator.tick()).start();

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

            // 🔥 simulator resetten
            simulator = new Simulator(hotelPanel);
            simulator.start();

            statusLabel.setText("Hotel geladen: " + selectedLayout);

        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }
}