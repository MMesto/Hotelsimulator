package ui;

import model.personen.*;
import model.Area;
import model.EventBusImpl;
import model.Hotel;
import model.personen.Gast;
import model.personen.Persoon;
import model.Lift;
import model.personen.Schoonmaker;
import model.Kamer;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Paneel verantwoordelijk voor het visueel weergeven van het hotel en de simulatie-objecten.
 */
public class HotelPanel extends JPanel implements MouseInputListener {

    private Hotel hotel;
    private Lift lift;
    private EventBusImpl eventBus;
    private final int VAKJE_GROOTTE = 50;
    private final int EVENT_PANEL_BREEDTE = 240;
    private final int EVENT_PANEL_GAP = 16;
    private boolean running = true;

    // Callbacks voor kliks
    private Runnable onLobbyClick;
    private Consumer<Kamer> onRoomClick;
    private Consumer<Persoon> onPersonClick;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        addMouseListener(this);
        setFocusable(true);
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    public void setLift(Lift lift) { this.lift = lift; }
    public void setEventBus(EventBusImpl eventBus) { this.eventBus = eventBus; }
    public void setRunning(boolean running) { this.running = running; }
    public void setOnLobbyClick(Runnable callback) { this.onLobbyClick = callback; }
    public void setOnRoomClick(Consumer<Kamer> callback) { this.onRoomClick = callback; }
    public void setOnPersonClick(Consumer<Persoon> callback) { this.onPersonClick = callback; }

    private void updateDimensions() {
        if (hotel == null) return;
        int breedte = EVENT_PANEL_BREEDTE + EVENT_PANEL_GAP + hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = Math.max(300, hotel.getHoogte() * VAKJE_GROOTTE);
        this.setPreferredSize(new Dimension(breedte, hoogte));
        revalidate();
    }

    // Centraal punt om de offsets te berekenen (voorkomt herhaling in code)
    private Point getHotelOffset() {
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        int totaleBreedte = EVENT_PANEL_BREEDTE + EVENT_PANEL_GAP + hotelBreedte;
        int offsetX = (getWidth() - totaleBreedte) / 2 + EVENT_PANEL_BREEDTE + EVENT_PANEL_GAP;
        int offsetY = (getHeight() - hotelHoogte) / 2;
        return new Point(Math.max(EVENT_PANEL_BREEDTE + EVENT_PANEL_GAP, offsetX), offsetY);
    }

    // --- TEKEN LOGICA (PAINT) ---

    @Override
    protected void paintComponent(Graphics g) {
        // Requirement: Visualisatie van gasten en/of personeel.
        // Elke repaint tekent de actuele hotelkaart, lift, personen, legenda en eventlijst.
        super.paintComponent(g);
        if (hotel == null) return;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        Point offset = getHotelOffset();
        tekenEventLijst(g, offset.x, offset.y);
        tekenGrid(g, offset.x, offset.y);
        tekenAreas(g, offset.x, offset.y);
        tekenLift(g, offset.x, offset.y);
        tekenPersonen(g, offset.x, offset.y);
        tekenLegenda(g);

        // Pauze-scherm overlay
        if (!running) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2);
        }
    }

    private void tekenEventLijst(Graphics g, int hotelOffsetX, int hotelOffsetY) {
        // Requirement: Eventafhandeling.
        // De laatste hotel-events worden links naast het hotel zichtbaar gemaakt.
        Graphics2D g2 = (Graphics2D) g.create();

        int panelX = Math.max(10, hotelOffsetX - EVENT_PANEL_GAP - EVENT_PANEL_BREEDTE);
        int panelY = Math.max(10, hotelOffsetY);
        int panelHoogte = Math.min(280, Math.max(180, getHeight() - panelY - 20));

        g2.setColor(new Color(250, 250, 250));
        g2.fillRoundRect(panelX, panelY, EVENT_PANEL_BREEDTE, panelHoogte, 10, 10);
        g2.setColor(new Color(190, 190, 190));
        g2.drawRoundRect(panelX, panelY, EVENT_PANEL_BREEDTE, panelHoogte, 10, 10);

        g2.setColor(new Color(30, 30, 30));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Gestarte events", panelX + 12, panelY + 24);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        List<String> events = getGestarteEvents();
        if (events.isEmpty()) {
            g2.setColor(new Color(110, 110, 110));
            g2.drawString("Nog geen events gestart", panelX + 12, panelY + 52);
            g2.dispose();
            return;
        }

        int y = panelY + 52;
        int maxY = panelY + panelHoogte - 12;
        for (String event : events) {
            if (y > maxY) break;
            if (event.startsWith("GODZILLA")) {
                g2.setColor(new Color(190, 35, 35));
                g2.setFont(new Font("Arial", Font.BOLD, 11));
            } else {
                g2.setColor(new Color(40, 40, 40));
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
            }
            g2.drawString("• " + event, panelX + 12, y);
            y += 18;
        }

        g2.dispose();
    }

    private List<String> getGestarteEvents() {
        if (eventBus == null) {
            return List.of();
        }

        List<String> logs = eventBus.getEventLog();
        java.util.ArrayList<String> events = new java.util.ArrayList<>();
        for (String log : logs) {
            if (log.contains("HOTEL_EVENT:")) {
                events.add(maakEventLabel(log));
            }
        }

        int start = Math.max(0, events.size() - 10);
        return events.subList(start, events.size());
    }

    private String maakEventLabel(String log) {
        int start = log.indexOf("HOTEL_EVENT:");
        String eventDetails = log.substring(start + "HOTEL_EVENT:".length()).trim();
        String eventType = eventDetails.split(" ", 2)[0];
        String guestName = zoekGastNaam(eventDetails);
        String data = haalWaardeOp(eventDetails, "Data: ", ")");

        String label = switch (eventType) {
            case "CHECK_IN" -> guestName + " checkt in" + kamerTekst(data);
            case "CHECK_OUT" -> guestName + " checkt uit" + kamerTekst(data);
            case "CLEANING_EMERGENCY" -> "Schoonmaak spoed kamer " + data;
            case "EVACUATE" -> "Evacuatie gestart";
            case "GODZILLA" -> "GODZILLA bij " + guestName + "!";
            case "NEED_FOOD" -> guestName + " wil eten";
            case "GOTO_CINEMA" -> guestName + " naar bioscoop";
            case "GOTO_FITNESS" -> guestName + " naar fitness";
            case "START_CINEMA" -> "Bioscoop start";
            default -> eventType;
        };

        FontMetrics fm = getFontMetrics(new Font("Arial", Font.PLAIN, 11));
        int maxBreedte = EVENT_PANEL_BREEDTE - 34;
        while (fm.stringWidth(label) > maxBreedte && label.length() > 4) {
            label = label.substring(0, label.length() - 4) + "...";
        }
        return label;
    }

    private String zoekGastNaam(String eventDetails) {
        String guestId = haalWaardeOp(eventDetails, "Guest: ", ",");
        if (guestId == null || "0".equals(guestId)) {
            return "iedereen";
        }

        for (Persoon persoon : hotel.getPersonen()) {
            if (String.valueOf(persoon.getNaam().hashCode()).equals(guestId)) {
                return persoon.getNaam();
            }
        }

        return "onbekende gast";
    }

    private String haalWaardeOp(String tekst, String prefix, String eindTeken) {
        int start = tekst.indexOf(prefix);
        if (start < 0) {
            return null;
        }

        start += prefix.length();
        int einde = tekst.indexOf(eindTeken, start);
        if (einde < 0) {
            einde = tekst.length();
        }
        return tekst.substring(start, einde).trim();
    }

    private String kamerTekst(String data) {
        if (data == null || "0".equals(data)) {
            return "";
        }
        return " kamer " + data;
    }

    private void tekenGrid(Graphics g, int offsetX, int offsetY) {
        // Requirement: Hotelgrid en layout.
        // De rasterlijnen maken de layout visueel als grid zichtbaar.
        g.setColor(new Color(230, 230, 230));
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;

        for (int i = 0; i <= hotel.getBreedte(); i++) {
            g.drawLine(offsetX + i * VAKJE_GROOTTE, offsetY, offsetX + i * VAKJE_GROOTTE, offsetY + hotelHoogte);
        }
        for (int j = 0; j <= hotel.getHoogte(); j++) {
            g.drawLine(offsetX, offsetY + j * VAKJE_GROOTTE, offsetX + hotelBreedte, offsetY + j * VAKJE_GROOTTE);
        }
    }

    private void tekenAreas(Graphics g, int offsetX, int offsetY) {
        // Requirement: Hotelgrid en layout.
        // Elke Area uit de layout wordt als gekleurd vlak met label getekend.
        Graphics2D g2 = (Graphics2D) g;

        for (Area area : hotel.getAreas()) {
            if ("Lift".equals(area.getAreaType())) continue; // Wordt dynamisch getekend

            int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
            int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;
            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // Achtergrond en rand
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);
            g.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g.drawRect(x, y, breedte, hoogte);

            // Label text
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;
            g.drawString(label, textX, textY);
        }
        g2.setStroke(new BasicStroke(1)); // Reset stroke
    }

    private void tekenLift(Graphics g, int offsetX, int offsetY) {
        // Requirement: Lift- en/of traplogica.
        // De lift wordt los getekend, omdat de positie tijdens de simulatie beweegt.
        if (lift == null) return;

        int liftX = (int)(lift.getX() * VAKJE_GROOTTE) + offsetX - VAKJE_GROOTTE / 2;
        int liftY = (int)(lift.getY() * VAKJE_GROOTTE) + offsetY - VAKJE_GROOTTE / 2;

        g.setColor(new Color(200, 100, 0)); // Oranje
        g.fillRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);

        g.setColor(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g.drawRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g.getFontMetrics();
        int lx = liftX + (VAKJE_GROOTTE - fm.stringWidth("L")) / 2;
        int ly = liftY + (VAKJE_GROOTTE + fm.getAscent()) / 2 - 5;
        g.drawString("L", lx, ly);
    }

    private void tekenPersonen(Graphics g, int offsetX, int offsetY) {
        // Requirement: Visualisatie van gasten en/of personeel.
        // Gasten worden als cirkel getekend; schoonmakers als vierkant met S.
        int liftGastIndex = 0;
        for (Persoon persoon : hotel.getPersonen()) {
            int px = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - 8;
            int py = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - 8;

            if (persoon instanceof Gast gast) {
                if (gast.isInLift()) {
                    if (lift == null) continue;
                    // Kleine bolletjes in het liftvak: passagiers zitten in de lift, niet los in de schacht.
                    int liftX = (int)(lift.getX() * VAKJE_GROOTTE) + offsetX - VAKJE_GROOTTE / 2;
                    int liftY = (int)(lift.getY() * VAKJE_GROOTTE) + offsetY - VAKJE_GROOTTE / 2;
                    int drawX = liftX + 7 + (liftGastIndex % 3) * 13;
                    int drawY = liftY + 32 + (liftGastIndex / 3) * 10;
                    liftGastIndex++;

                    g.setColor(gast.getKleur());
                    g.fillOval(drawX, drawY, 9, 9);
                    g.setColor(Color.BLACK);
                    g.drawOval(drawX, drawY, 9, 9);
                    continue;
                }
                int diameter = gast.isGodzillaActive() ? 40 : 16;
                int drawX = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - diameter / 2;
                int drawY = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - diameter / 2;

                g.setColor(gast.getKleur());
                g.fillOval(drawX, drawY, diameter, diameter);
                g.setColor(Color.BLACK);
                g.drawOval(drawX, drawY, diameter, diameter);

                if (gast.isGodzillaActive()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(3));
                    g.setColor(new Color(190, 35, 35));
                    g.drawOval(drawX - 3, drawY - 3, diameter + 6, diameter + 6);
                    g2.setStroke(new BasicStroke(1));
                }

                // Label boven gast
                g.setFont(new Font("Arial", Font.BOLD, 9));
                g.setColor(Color.BLACK);
                g.drawString(gast.getNaam(), drawX - 2, drawY - 5);

            } else if (persoon instanceof Schoonmaker schoonmaker) {
                g.setColor(schoonmaker.getKleur());
                g.fillRect(px, py, 16, 16);
                g.setColor(Color.BLACK);
                g.drawRect(px, py, 16, 16);

                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("S", px + 5, py + 8);

                // Label boven schoonmaker
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString(schoonmaker.getNaam(), px - 5, py - 3);
            }
        }
    }

    private void tekenLegenda(Graphics g) {
        int lx = 10;
        int ly = getHeight() - 70;

        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(lx, ly, 130, 60);
        g.setColor(Color.BLACK);
        g.drawRect(lx, ly, 130, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 11));

        g.setColor(Color.BLUE);
        g.fillOval(lx + 10, ly + 10, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Gast", lx + 30, ly + 20);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(lx + 10, ly + 25, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Schoonmaker", lx + 30, ly + 35);

        g.setColor(new Color(200, 100, 0));
        g.fillRect(lx + 10, ly + 40, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Lift", lx + 30, ly + 50);
    }

    // --- KLIK DETECTIE (MOUSE LISTENER) ---

    @Override
    public void mouseClicked(MouseEvent e) {
        // Requirement: Visualisatie van gasten en/of personeel.
        // Klikken op personen, kamers of lobby opent extra detailvensters.
        if (hotel == null) return;

        int clickX = e.getX();
        int clickY = e.getY();
        Point offset = getHotelOffset();

        // 1. Check personen (Hoogste prioriteit). Laatst getekende personen liggen visueel bovenop.
        List<Persoon> personen = hotel.getPersonen();
        for (int i = personen.size() - 1; i >= 0; i--) {
            Persoon persoon = personen.get(i);
            if (isKlikOpPersoon(persoon, clickX, clickY, offset.x, offset.y)) {
                System.out.println("[Click] 👤 Persoon aangeklikt: " + persoon.getNaam());
                if (onPersonClick != null) onPersonClick.accept(persoon);
                return;
            }
        }

        // 2. Check kamers
        for (Area area : hotel.getAreas()) {
            if ("Room".equals(area.getAreaType()) && isKlikOpArea(area, clickX, clickY, offset.x, offset.y)) {
                for (Kamer kamer : hotel.getKamers()) {
                    if (kamer.getArea() != null && kamer.getArea().equals(area)) {
                        System.out.println("[Click] 🚪 Kamer aangeklikt: " + kamer.getKamernummer());
                        if (onRoomClick != null) onRoomClick.accept(kamer);
                        return;
                    }
                }
            }
        }

        // 3. Check lobby
        for (Area area : hotel.getAreas()) {
            if ("Lobby".equals(area.getAreaType()) && isKlikOpArea(area, clickX, clickY, offset.x, offset.y)) {
                System.out.println("[US2.1] 🏨 Lobby aangeklikt - Gastenoverzicht openen");
                if (onLobbyClick != null) onLobbyClick.run();
                return;
            }
        }
    }

    private boolean isKlikOpPersoon(Persoon persoon, int clickX, int clickY, int offsetX, int offsetY) {
        int px = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - 8;
        int py = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - 8;
        int labelBreedte = getFontMetrics(new Font("Arial", Font.BOLD, 9)).stringWidth(persoon.getNaam());

        boolean klikOpNaam = clickX >= px - 5
                && clickX <= px - 5 + labelBreedte
                && clickY >= py - 18
                && clickY <= py - 3;

        if (persoon instanceof Gast) {
            int centerX = px + 8;
            int centerY = py + 8;
            double afstand = Math.sqrt(Math.pow(clickX - centerX, 2) + Math.pow(clickY - centerY, 2));
            int klikRadius = persoon instanceof Gast gast && gast.isGodzillaActive() ? 26 : 16;
            return afstand <= klikRadius || klikOpNaam;
        }
        if (persoon instanceof Schoonmaker) {
            boolean klikOpBlok = clickX >= px - 4 && clickX <= px + 20 && clickY >= py - 4 && clickY <= py + 20;
            return klikOpBlok || klikOpNaam;
        }
        return false;
    }

    private boolean isKlikOpArea(Area area, int clickX, int clickY, int offsetX, int offsetY) {
        int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
        int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;
        int breedte = area.getBreedte() * VAKJE_GROOTTE;
        int hoogte = area.getHoogte() * VAKJE_GROOTTE;

        return clickX >= x && clickX <= x + breedte && clickY >= y && clickY <= y + hoogte;
    }

    // --- HULPMETHODEN VOOR LABELS EN KLEUREN (MODERNE SWITCH) ---

    private String getLabel(Area area) {
        Area.Type type = area.getType();
        if (type == Area.Type.ROOM) return getKamerLabel(area);
        return type.getLabel();
    }

    private String getKamerLabel(Area area) {
        for (Kamer kamer : hotel.getKamers()) {
            if (kamer.getArea() == area || (kamer.getArea() != null && kamer.getArea().equals(area))) {
                return String.valueOf(kamer.getKamernummer());
            }
        }
        return "Kamer";
    }

    private Color getKleur(Area area) {
        Area.Type type = area.getType();
        if (type == Area.Type.ROOM) {
            String c = area.getClassification();
            if (c == null)                        return new Color(200, 230, 255);
            if (c.equalsIgnoreCase("PentHouse"))  return new Color(120,   0,   0);
            if (c.contains("1"))                  return new Color(200, 230, 255);
            if (c.contains("2"))                  return new Color(150, 200, 255);
            if (c.contains("3"))                  return new Color(100, 170, 255);
            if (c.contains("4"))                  return new Color( 50, 130, 255);
            if (c.contains("5"))                  return new Color(  0,  80, 200);
            return new Color(200, 230, 255);
        }
        return type.getColor();
    }

    // --- ONGEBRUIKTE INTERFACE METHODS COMPACT ONDERAAN ---
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
