import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Laad het hotel
                Hotel hotel = LayoutLoader.laadLayout("layouts/hotel1.layout");
                System.out.println("Hotel geladen!");

                // Maak het venster
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Voeg het hotelpaneel toe
                HotelPanel panel = new HotelPanel(hotel);
                frame.add(panel);

                // Pas venster aan op de inhoud
                frame.pack();
                frame.setLocationRelativeTo(null); // midden op scherm
                frame.setVisible(true);

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
            }
        });
    }
}