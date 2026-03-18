package ui;

import model.Hotel;
import javax.swing.*;
import java.awt.*;

public class HotelPanel extends JPanel {

    private Hotel hotel;
    private final int VAKJE_GROOTTE = 50;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
    }

    // Methode om het hotel te wijzigen (voor layout wisseling)
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    private void updateDimensions() {
        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String[][] grid = hotel.getGrid();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                String type = grid[y][x].trim();
                Color kleur = getKleur(type);

                // Vul het vakje met kleur
                g.setColor(kleur);
                g.fillRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);

                // Teken de rand
                g.setColor(Color.BLACK);
                g.drawRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);

                // Teken de tekst
                g.setColor(Color.BLACK);
                g.drawString(type, x * VAKJE_GROOTTE + 15, y * VAKJE_GROOTTE + 30);
            }
        }
    }

    private Color getKleur(String type) {
        switch (type) {
            case "L": return Color.YELLOW;       // Lobby
            case "K": return Color.CYAN;         // Kamer
            case "F": return Color.ORANGE;       // Lift
            case "T": return Color.GREEN;        // Trap
            case "G": return Color.LIGHT_GRAY;   // Gang
            case "R": return Color.RED;          // Restaurant
            case "B": return Color.MAGENTA;      // Bioscoop
            case "FT": return Color.PINK;        // Fitness
            default: return Color.WHITE;
        }
    }
}