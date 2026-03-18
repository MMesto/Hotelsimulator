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
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);

                // Teken de tekst alleen als het geen gang is
                if (!type.equals("G")) {
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 11));
                    String label = getLabel(type);
                    FontMetrics fm = g.getFontMetrics();
                    int textX = x * VAKJE_GROOTTE + (VAKJE_GROOTTE - fm.stringWidth(label)) / 2;
                    int textY = y * VAKJE_GROOTTE + (VAKJE_GROOTTE + fm.getAscent()) / 2 - 2;
                    g.drawString(label, textX, textY);
                }
            }
        }
    }

    private String getLabel(String type) {
        if (type.contains("★")) return type; // sterren kamers
        switch (type) {
            case "L": return "Lobby";
            case "F": return "Lift";
            case "T": return "Trap";
            case "R": return "Rest.";
            case "B": return "Bios";
            case "FT": return "Fit";
            default: return type;
        }
    }

    private Color getKleur(String type) {
        if (type.contains("★")) {
            // Kleur op basis van aantal sterren
            if (type.contains("1★")) return new Color(200, 230, 255); // lichtblauw
            if (type.contains("2★")) return new Color(150, 200, 255); // blauw
            if (type.contains("3★")) return new Color(100, 170, 255); // middelblauw
            if (type.contains("4★")) return new Color(50, 130, 255);  // donkerblauw
            if (type.contains("5★")) return new Color(0, 80, 200);    // diepblauw
        }
        switch (type) {
            case "L": return new Color(255, 220, 50);   // geel
            case "F": return new Color(255, 150, 0);    // oranje
            case "T": return new Color(100, 200, 100);  // groen
            case "G": return new Color(220, 220, 220);  // lichtgrijs
            case "R": return new Color(255, 100, 100);  // rood
            case "B": return new Color(200, 100, 200);  // paars
            case "FT": return new Color(100, 220, 180); // turquoise
            default: return Color.WHITE;
        }
    }
}