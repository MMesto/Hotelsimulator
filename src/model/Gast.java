package model;
import java.awt.Color;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final int LIFT_X = 0;   // Column 1 in layout = 0 in gast coords
    private static final int TRAP_X = 8;   // Column 9 in layout = 8 in gast coords
    private double x, y;
    private double destX, destY;
    private double finalDestX, finalDestY;
    private int maxX, maxY;
    private Hotel hotel;
    private Color kleur;
    private enum State {
        WANDELEN, NAAR_VERVOER, IN_VERVOER, VAN_VERVOER_WEG
    }
    private State state = State.WANDELEN;
    private int vervoerX = LIFT_X;
    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast");
        this.x = startX + 0.5;
        this.y = startY + 0.5;
        this.destX = x;
        this.destY = y;
        this.finalDestX = x;
        this.finalDestY = y;
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }
    @Override
    public void onTick() {
        beweegStap();
    }
    private void beweegStap() {
        double dx = destX - x;
        double dy = destY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < SPEED) {
            handleStateTransition();
            return;
        }
        double dirX = dx / distance;
        double dirY = dy / distance;
        x += dirX * SPEED;
        y += dirY * SPEED;
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        y = Math.max(0.5, Math.min(y, maxY - 0.5));
    }
    private void handleStateTransition() {
        switch (state) {
            case WANDELEN:
                kiesNieuwebestemming();
                int currentFloor = (int) y;
                int newFloor = (int) finalDestY;
                if (currentFloor != newFloor) {
                    this.vervoerX = RANDOM.nextBoolean() ? LIFT_X : TRAP_X;
                    this.destX = vervoerX + 0.5;
                    this.destY = y;
                    this.state = State.NAAR_VERVOER;
                } else {
                    this.destX = finalDestX;
                    this.destY = finalDestY;
                }
                break;
            case NAAR_VERVOER:
                this.destX = vervoerX + 0.5;
                this.destY = finalDestY;
                this.state = State.IN_VERVOER;
                break;
            case IN_VERVOER:
                this.destX = finalDestX;
                this.destY = finalDestY;
                this.state = State.VAN_VERVOER_WEG;
                break;
            case VAN_VERVOER_WEG:
                this.state = State.WANDELEN;
                handleStateTransition();
                break;
        }
    }
    private void kiesNieuwebestemming() {
        if (hotel == null) {
            this.finalDestX = RANDOM.nextInt(Math.max(1, maxX - 1)) + 0.5;
            this.finalDestY = RANDOM.nextInt(Math.max(1, maxY - 1)) + 0.5;
            return;
        }
        List<Area> areas = hotel.getAreas();
        if (areas.isEmpty()) return;
        List<Area> validAreas = new ArrayList<>();
        for (Area area : areas) {
            if (!area.getAreaType().equals("Lift") && !area.getAreaType().equals("Staircase")) {
                validAreas.add(area);
            }
        }
        if (validAreas.isEmpty()) return;
        Area target = validAreas.get(RANDOM.nextInt(validAreas.size()));
        
        // Area.getX/Y() zijn 1-indexed, converteer naar 0-indexed voor gast
        int randomX = (target.getX() - 1) + RANDOM.nextInt(Math.max(1, target.getBreedte()));
        int randomY = (target.getY() - 1) + RANDOM.nextInt(Math.max(1, target.getHoogte()));
        
        this.finalDestX = randomX + 0.5;
        this.finalDestY = randomY + 0.5;
    }
    public void setGridBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getKleur() { return kleur; }
}