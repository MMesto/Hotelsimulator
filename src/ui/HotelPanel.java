package ui;

import model.Area;
import model.Hotel;
import model.Gast;
import model.Persoon;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dit paneel is verantwoordelijk voor het visueel weergeven van het hotel.
 */
public class HotelPanel extends JPanel {

    private Hotel hotel;

    private final int VAKJE_GROOTTE = 50;

    // status van simulatie (optioneel)
    private boolean running = true;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    // Simulator kan dit gebruiken
    public void setRunning(boolean running) {
        this.running = running;
    }

    private void updateDimensions() {
        if (hotel == null) return;

        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
        revalidate(); // belangrijk voor UI refresh
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hotel == null) return;

        // Achtergrond (vloer)
        g.setColor(new Color(220, 220, 220));
        g.fillRect(0, 0, hotel.getBreedte() * VAKJE_GROOTTE, hotel.getHoogte() * VAKJE_GROOTTE);

        // Grid tekenen
        g.setColor(new Color(200, 200, 200));
        for (int y = 0; y < hotel.getHoogte(); y++) {
            for (int x = 0; x < hotel.getBreedte(); x++) {
                g.drawRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);
            }
        }

        // Areas tekenen
        List<Area> areas = hotel.getAreas();
        for (Area area : areas) {

            int x = (area.getX() - 1) * VAKJE_GROOTTE;
            int y = (area.getY() - 1) * VAKJE_GROOTTE;

            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // Achtergrond kleur
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);

            // Rand
            g.setColor(Color.DARK_GRAY);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g.drawRect(x, y, breedte, hoogte);
            g2.setStroke(new BasicStroke(1));

            // Label
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics();

            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;

            g.drawString(label, textX, textY);
        }

        // Teken alle personen
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof Gast) {
                Gast gast = (Gast) persoon;
                int px = gast.getX() * VAKJE_GROOTTE + VAKJE_GROOTTE / 2 - 8;
                int py = gast.getY() * VAKJE_GROOTTE + VAKJE_GROOTTE / 2 - 8;

                // Teken met gast's eigen kleur
                g.setColor(gast.getKleur());
                g.fillOval(px, py, 16, 16);

                // Teken rand (donkerder versie van dezelfde kleur)
                g.setColor(new Color(
                    Math.max(0, gast.getKleur().getRed() - 80),
                    Math.max(0, gast.getKleur().getGreen() - 80),
                    Math.max(0, gast.getKleur().getBlue() - 80)
                ));
                g.drawOval(px, py, 16, 16);

                // Teken naam als tooltip (optioneel, heel klein)
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString(gast.getNaam().substring(0, Math.min(3, gast.getNaam().length())), px + 2, py + 8);
            }
        }

        // toon status (pause/active)
        if (!running) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2);
        }
    }

    private String getLabel(Area area) {
        // ...existing code...
        switch (area.getAreaType()) {
            case "Room":
                return area.getClassification() != null ?
                        area.getClassification().replace(" Star", "★") : "Kamer";
            case "Cinema": return "Bioscoop";
            case "Restaurant": return "Restaurant";
            case "Fitness": return "Fitness";
            case "Lobby": return "Lobby";
            case "Lift": return "Lift";
            case "Staircase": return "Trap";
            case "Storage": return "Opslag";
            default: return area.getAreaType();
        }
    }

    private Color getKleur(Area area) {
        switch (area.getAreaType()) {
            case "Room":
                String c = area.getClassification();
                if (c == null) return new Color(200, 230, 255);

                if (c.contains("1")) return new Color(200, 230, 255);
                if (c.contains("2")) return new Color(150, 200, 255);
                if (c.contains("3")) return new Color(100, 170, 255);
                if (c.contains("4")) return new Color(50, 130, 255);
                if (c.contains("5")) return new Color(0, 80, 200);

                return new Color(200, 230, 255);

            case "Lobby": return new Color(255, 220, 50);
            case "Lift": return new Color(255, 150, 0);
            case "Staircase": return new Color(100, 200, 100);
            case "Restaurant": return new Color(255, 100, 100);
            case "Cinema": return new Color(200, 100, 200);
            case "Fitness": return new Color(100, 220, 180);
            case "Storage": return new Color(180, 140, 100);
            default: return Color.WHITE;
        }
    }
}