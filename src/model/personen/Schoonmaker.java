package model.personen;

import model.*;

import hotelevents.HotelEventType;
import model.strategy.SchoonmakerNormalStrategy;
import model.strategy.EvacuationMovement;
import model.strategy.MovementData;
import java.awt.Color;

public class Schoonmaker extends Persoon {
    public static final double SPEED = 0.5;
    public static final double LIFT_X = 1.5;
    public static final int SCHOONMAAK_DUUR = 100;

    private Hotel hotel;
    private Lift lift;
    private IEventBus eventBus;
    private Kamer huidigKamer;
    private int schoonmaakTimer = 0;
    private boolean inLift = false;
    private int doelVerdieping;
    private int maxY = 6;
    private double storageX = 7.5;
    private double storageY = 5.5;
    private double liftWaitX = LIFT_X;
    private double trapX = 8.5;

    public enum State { VRIJ, NAAR_DOEL, SCHOONMAKEN, IN_LIFT, EVACUATIE, BUITEN }
    private State state = State.VRIJ;

    public Schoonmaker(String naam, int startX, int startY) {
        super(naam, "Schoonmaker");

        // Forceer startpositie naar de opslag
        this.x = 7.5;
        this.y = 5.5;
        this.destX = 7.5;
        this.destY = 5.5;

        // STRATEGY PATTERN: Injecteer normale + evacuatie-strategie in de context
        setMovementStrategies(new SchoonmakerNormalStrategy(), new EvacuationMovement());

        System.out.println("[Schoonmaker] " + naam + " is in de Opslag (7,5)");
    }

    @Override
    public void onTick() {
        updateActiviteitLabel();

        // 1. Brandalarm is geactiveerd: Wissel naar vlucht-strategie
        if (fireAlarmActive && !isEvacuating()) {
            startEvacuatie();
            useEvacuationStrategy();
        }

        // 2. Brandalarm is weer voorbij: Reset naar normaal gedrag
        if (!fireAlarmActive && isEvacuatieBegonnen()) {
            setEvacuatieBegonnen(false);
            useNormalStrategy();
            this.state = State.VRIJ;
            setHuidigeActiviteit("⏳ Idle");
            this.x = storageX; // Terug naar opslag
            this.y = storageY;
        }

        if (fireAlarmActive && state == State.BUITEN) {
            return; // Veilig buiten, doe niks meer
        }

        // 3. STRATEGY PATTERN: Voer de actieve strategie uit!
        performMovement(createMovementData());
    }

    private void updateActiviteitLabel() {
        if (inLift) {
            setHuidigeActiviteit("🛗 In Lift");
            return;
        }
        switch (state) {
            case VRIJ -> setHuidigeActiviteit("⏳ Idle");
            case NAAR_DOEL -> setHuidigeActiviteit("🚶 Naar Kamer");
            case SCHOONMAKEN -> setHuidigeActiviteit("🧹 Schoonmaken");
            case IN_LIFT -> setHuidigeActiviteit("🛗 In Lift");
            case EVACUATIE -> setHuidigeActiviteit("🔥 EVACUATIE!");
            case BUITEN -> setHuidigeActiviteit("👋 Buiten hotel");
            default -> setHuidigeActiviteit("");
        }
    }

    private void startEvacuatie() {
        setEvacuatieBegonnen(true);
        if (inLift && lift != null) {
            lift.verwijderGast(this);
            inLift = false;
        }
        huidigKamer = null;
        state = State.EVACUATIE;
        System.out.println("[FireAlarm] 🚨 " + getNaam() + " begint evacuatie!");
    }

    // --- PUBLIEKE HELPER METHODES VOOR DE STRATEGIE ---

    public Kamer zoekViezeKamer() {
        if (hotel == null) return null;
        for (Kamer k : hotel.getKamers()) {
            if (k.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                return k;
            }
        }
        return null;
    }

    public void stapInLift(int verdieping) {
        if (lift != null && Math.abs(lift.getY() - y) < 0.3 && lift.isIdle()) {
            if (lift.voegGastToe(this, verdieping)) {
                this.inLift = true;
                this.doelVerdieping = verdieping;
                this.state = State.IN_LIFT;
            }
        }
    }

    // Getters en Setters zodat de Strategie data kan lezen/schrijven
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public boolean isInLift() { return inLift; }
    public void setInLift(boolean inLift) { this.inLift = inLift; }
    public Kamer getHuidigKamer() { return huidigKamer; }
    public void setHuidigKamer(Kamer kamer) { this.huidigKamer = kamer; }
    public int getSchoonmaakTimer() { return schoonmaakTimer; }
    public void setSchoonmaakTimer(int timer) { this.schoonmaakTimer = timer; }
    public Lift getLift() { return lift; }
    public int getDoelVerdieping() { return doelVerdieping; }
    @Override
    public int getMaxY() { return maxY; }
    @Override
    public void setStateToLeft() { this.setState(State.BUITEN); } //polymorfisme
    public IEventBus getEventBus() { return eventBus; }

    private MovementData createMovementData() {
        return new MovementData(hotel, lift, eventBus, 0, maxY,
                liftWaitX, trapX, storageX, storageY);
    }

    // Initialisatie setters
    public void setHotel(Hotel h) { this.hotel = h; }
    public void setLift(Lift l) { this.lift = l; }
    public void setEventBus(EventBusImpl eventBus) { this.eventBus = eventBus; }
    public void setGridBounds(int mx, int my) { this.maxY = my; }
    public void setStoragePosition(double x, double y) {
        this.storageX = x;
        this.storageY = y;
    }
    public void setTransportPoints(double liftWaitX, double trapX) {
        this.liftWaitX = liftWaitX;
        this.trapX = trapX;
    }
    public Color getKleur() { return Color.GRAY; }
}
