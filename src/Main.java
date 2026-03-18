import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class Main {
    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Maak het venster
                JFrame frame = new JFrame("Hotel Simulator - US1.2: Layout Switcher");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Maak het paneel voor bovenaan (controls)
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Laad alle beschikbare layouts
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // Voeg button toe om layout in te laden
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // Status label
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // Laad de eerste layout
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel);
                statusLabel.setText("Hotel geladen: " + layouts[0]);

                // Voeg alles toe aan het frame
                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                // Pas venster aan op de inhoud
                frame.pack();
                frame.setLocationRelativeTo(null); // midden op scherm
                frame.setVisible(true);

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // US1.2: Laad alle beschikbare layout bestanden
    private static String[] getAvailableLayouts() {
        System.out.println("Zoek layouts in: " + new File("layouts").getAbsolutePath());
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));
        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Geen layout bestanden gevonden in layouts/ map");
        }
        Arrays.sort(layouts); // Alfabetisch sorteren
        return layouts;
    }

    // US1.2: Laad de geselecteerde layout
    private static void loadLayout() {
        try {
            String selectedLayout = (String) layoutDropdown.getSelectedItem();
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);
            hotelPanel.setHotel(newHotel);
            statusLabel.setText("Hotel geladen: " + selectedLayout);
            System.out.println("Layout gewisseld naar: " + selectedLayout);
        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
            System.out.println("Fout bij laden: " + e.getMessage());
        }
    }
}