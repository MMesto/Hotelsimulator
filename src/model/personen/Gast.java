package model.personen;

import model.*;

import hotelevents.HotelEventType;
import model.strategy.GastNormalStrategy;
import model.strategy.EvacuationMovement;
import model.strategy.MovementData;
import java.awt.Color;
import java.util.Random;

public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    public static final double SPEED = 0.5;
    public static final double GODZILLA_SPEED_MULTIPLIER = 3.0;
    public static final double LIFT_WAIT_X = 1.5;
    public static final double TRAP_X = 8.5;
    private static final int LOUNGE_CHILL_TICKS = 20;
    private static final int RESTAURANT_WACHT_TICKS = 120;

    private int maxX, maxY;
    private Hotel hotel;
    private Lift lift;
    private IEventBus eventBus;
    private String preferredRoomType = "Standaard";
    private double liftWaitX = LIFT_WAIT_X;
    private double trapX = TRAP_X;
    private boolean inLift = false;
    private boolean usesTrap = false;
    private Kamer huidigKamer;
    private final Color kleur;

    private int doelVerdieping;
    private int roomStayTimer = 0;
    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0;
    private String huidigerFaciliteitType = null;
    private int loungeStayTicks = 0;
    private double vorigeX = Double.NaN;
    private double vorigeY = Double.NaN;

    private int godzillaTicksRemaining = 0;
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public enum State {
        WANDELEN, NAAR_LIFT_WACHTEN, WACHTEN_OP_VERVOER, IN_LIFT,
        GAAT_NAAR_FACILITEIT, IN_FACILITEIT, GAAT_NAAR_LOBBY,
        GAAT_NAAR_KAMER, VERLAAT_HOTEL, EVACUATIE, BUITEN
    }

    private State state = State.WANDELEN;
    private State stateNaVerdiepingWissel = State.WANDELEN;

    public Gast(String naam, int ignoreX, int ignoreY) {
        super(naam, "Gast");
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));

        // STRATEGY PATTERN: Injecteer normale + evacuatie-strategie in de context
        setMovementStrategies(new GastNormalStrategy(), new EvacuationMovement());
    }

    @Override
    public void onTick() {
        // 1. Update timers
        if (godzillaTicksRemaining > 0) godzillaTicksRemaining--;

        // 2. Bepaal of we van strategie moeten wisselen wegens noodsituaties
        if (fireAlarmActive && !isEvacuating()) {
            startEvacuatie();
            useEvacuationStrategy();
        }
        else if (!fireAlarmActive && isEvacuatieBegonnen()) {
            resetNaEvacuatie();
        }

        if (fireAlarmActive && state == State.BUITEN) {
            updateActiviteitLabel();
            return; // Veilig buiten, blijf stilstaan tot het alarm voorbij is.
        }

        // 3. STRATEGY PATTERN: Voer het algoritme uit!
        performMovement(createMovementData());

        // 4. Update visuele state
        updateLoungeStayTicks();
        updateActiviteitLabel();
    }

    // --- EXTERNE ACTIES (Triggers van buitenaf) ---

    public boolean checkinKamer(Kamer k) {
        this.huidigKamer = k;
        k.setStatus(Kamer.KamerStatus.BEZET);
        this.destX = getAreaCenterX(k.getArea());
        this.doelVerdieping = k.getArea().getY() - 1;
        this.state = State.GAAT_NAAR_KAMER;

        if (eventBus != null) {
            eventBus.triggerHotelEvent(HotelEventType.CHECK_IN, getNaam().hashCode(), k.getKamernummer());
        }
        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            if (eventBus != null) {
                eventBus.triggerHotelEvent(HotelEventType.CHECK_OUT, getNaam().hashCode(), huidigKamer.getKamernummer());
            }
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            huidigKamer = null;
            this.state = State.GAAT_NAAR_LOBBY;
        }
    }

    public void gaatNaarFaciliteit(String type, double fX, double fY) {
        startFaciliteitBezoek(type, fX, fY);

        if (eventBus != null) {
            int guestId = getNaam().hashCode();
            switch (type.toLowerCase()) {
                case "restaurant", "eten", "food" -> eventBus.triggerHotelEvent(HotelEventType.NEED_FOOD, guestId, 0);
                case "cinema", "film" -> eventBus.triggerHotelEvent(HotelEventType.GOTO_CINEMA, guestId, 0);
                case "fitness", "gym" -> eventBus.triggerHotelEvent(HotelEventType.GOTO_FITNESS, guestId, 0);
            }
        }
    }

    public void gaNaarRestaurantDoorEvent(double fX, double fY) {
        startFaciliteitBezoek("Restaurant", fX, fY);
        this.faciliteitsBezoekDuur = RESTAURANT_WACHT_TICKS;
    }

    public void gaNaarFaciliteitDoorEvent(String type, double fX, double fY) {
        startFaciliteitBezoek(type, fX, fY);
        if ("Restaurant".equalsIgnoreCase(type)) {
            this.faciliteitsBezoekDuur = RESTAURANT_WACHT_TICKS;
        }
    }

    private void startFaciliteitBezoek(String type, double fX, double fY) {
        this.huidigerFaciliteitType = type;
        this.faciliteitX = fX;
        this.faciliteitY = fY;
        this.doelVerdieping = (int) fY;

        if (inLift) {
            this.stateNaVerdiepingWissel = State.GAAT_NAAR_FACILITEIT;
            this.state = State.IN_LIFT;
        } else if ((int)fY != (int)y) {
            wisselVerdiepingVoor(State.GAAT_NAAR_FACILITEIT);
        } else {
            this.state = State.GAAT_NAAR_FACILITEIT;
        }
    }

    public void activeerGodzilla() {
        this.godzillaTicksRemaining = 250;
        this.roomStayTimer = 0;
        this.faciliteitsBezoekDuur = 0;
        if (state == State.IN_FACILITEIT) {
            state = State.WANDELEN;
            huidigerFaciliteitType = null;
        }
        setHuidigeActiviteit("GODZILLA!");
    }

    // --- HELPER METHODES VOOR DE STRATEGIEËN ---

    public void wiltVerdiepingWisselen() {
        wisselVerdiepingVoor(state);
    }

    private void wisselVerdiepingVoor(State vervolgState) {
        this.stateNaVerdiepingWissel = vervolgState;
        this.usesTrap = RANDOM.nextBoolean();
        this.destX = usesTrap ? trapX : liftWaitX;
        this.state = State.NAAR_LIFT_WACHTEN;
    }

    private MovementData createMovementData() {
        return new MovementData(hotel, lift, eventBus, maxX, maxY,
                liftWaitX, trapX, 0, 0);
    }

    public boolean isGodzillaActive() {
        return godzillaTicksRemaining > 0;
    }

    public double getActueleSnelheid() {
        return (godzillaTicksRemaining > 0) ? SPEED * GODZILLA_SPEED_MULTIPLIER : SPEED;
    }

    private void startEvacuatie() {
        setEvacuatieBegonnen(true);
        if (inLift && lift != null) {
            lift.verwijderGast(this);
            inLift = false;
            this.x = lift.getX();
            this.y = (int)lift.getY() + 0.5;
        }
        huidigKamer = null;
        huidigerFaciliteitType = null;
        faciliteitsBezoekDuur = 0;
        roomStayTimer = 0;
        this.state = State.EVACUATIE;
        this.usesTrap = true;
        System.out.println("[FireAlarm] 🚨 " + getNaam() + " begint evacuatie naar lobby!");
    }

    private void resetNaEvacuatie() {
        setEvacuatieBegonnen(false);
        useNormalStrategy();
        if (state == State.EVACUATIE || state == State.VERLAAT_HOTEL || state == State.BUITEN) {
            state = State.WANDELEN;
            setHuidigeActiviteit("🚶 Wandel");
            this.x = 1.5;
            this.y = maxY - 1;
            this.destX = x;
        }
    }

    private void updateActiviteitLabel() {
        if (inLift) {
            setHuidigeActiviteit((lift != null && lift.isIdle()) ? "⏳ Wachten in lift" : "🛗 In lift");
            return;
        }
        if (godzillaTicksRemaining > 0 && state != State.EVACUATIE && state != State.VERLAAT_HOTEL) {
            setHuidigeActiviteit("GODZILLA!");
            return;
        }

        switch (state) {
            case WANDELEN -> setHuidigeActiviteit(staatInHuidigeKamer() ? "🛏️ In kamer" : (loungeStayTicks >= LOUNGE_CHILL_TICKS ? "🛋️ Chill" : "🚶 Wandel"));
            case NAAR_LIFT_WACHTEN -> setHuidigeActiviteit(usesTrap ? "⏳ Naar trap" : "⏳ Naar lift");
            case WACHTEN_OP_VERVOER -> setHuidigeActiviteit(usesTrap ? "🚶 Op trap" : "⏳ Wachten op lift");
            case GAAT_NAAR_FACILITEIT -> setHuidigeActiviteit("🚶 > Faciliteit");
            case GAAT_NAAR_KAMER -> setHuidigeActiviteit("✓ Check-in");
            case GAAT_NAAR_LOBBY -> setHuidigeActiviteit("✗ Check-out");
            case VERLAAT_HOTEL -> setHuidigeActiviteit("👋 Vertrekt");
            case EVACUATIE -> setHuidigeActiviteit("🔥 EVACUATIE!");
            case BUITEN -> setHuidigeActiviteit("👋 Buiten hotel");
            case IN_FACILITEIT -> setHuidigeActiviteit(huidigerFaciliteitType == null ? "" : "📍 " + huidigerFaciliteitType);
        }
    }

    private void updateLoungeStayTicks() {
        boolean inLounge = isInAreaType("Lounge");
        boolean staatStil = !Double.isNaN(vorigeX) && Math.abs(x - vorigeX) < 0.01 && Math.abs(y - vorigeY) < 0.01;
        if (state == State.WANDELEN && inLounge && staatStil) loungeStayTicks++;
        else loungeStayTicks = 0;
        vorigeX = x;
        vorigeY = y;
    }

    private boolean isInAreaType(String areaType) {
        if (hotel == null) return false;
        for (Area area : hotel.getAreas()) {
            if (!areaType.equals(area.getAreaType())) continue;
            if (x >= (area.getX() - 1) && x < (area.getX() - 1 + area.getBreedte()) &&
                    y >= (area.getY() - 1) && y < (area.getY() - 1 + area.getHoogte())) return true;
        }
        return false;
    }

    private boolean staatInHuidigeKamer() {
        if (huidigKamer == null || huidigKamer.getArea() == null) {
            return false;
        }

        double kamerX = getAreaCenterX(huidigKamer.getArea());
        int kamerY = huidigKamer.getArea().getY() - 1;
        return (int) y == kamerY && Math.abs(x - kamerX) < 0.1;
    }

    private double getAreaCenterX(Area area) { return area.getX() - 1 + area.getBreedte() / 2.0; }

    // --- GETTERS & SETTERS (Voor de Strategie) ---
    public State getGastState() { return state; }
    public void setGastState(State state) { this.state = state; }
    public State getStateNaVerdiepingWissel() { return stateNaVerdiepingWissel; }
    public void hervatStateNaVerdiepingWissel() {
        State volgendeState = stateNaVerdiepingWissel;
        this.state = volgendeState;
        this.stateNaVerdiepingWissel = State.WANDELEN;
    }
    public boolean isBezigMetFaciliteit() {
        // Voorkomt dat auto check-in/check-out een restaurant/cinema/fitness-event onderbreekt.
        boolean onderweg = state == State.NAAR_LIFT_WACHTEN
                || state == State.WACHTEN_OP_VERVOER
                || state == State.IN_LIFT;
        return state == State.GAAT_NAAR_FACILITEIT
                || state == State.IN_FACILITEIT
                || (onderweg && stateNaVerdiepingWissel == State.GAAT_NAAR_FACILITEIT);
    }
    public boolean isInLift() { return inLift; }
    public void setInLift(boolean inLift) { this.inLift = inLift; }
    public Lift getLift() { return lift; }
    public int getDoelVerdieping() { return doelVerdieping; }
    public void setDoelVerdieping(int doelVerdieping) { this.doelVerdieping = doelVerdieping; }
    public int getRoomStayTimer() { return roomStayTimer; }
    public void setRoomStayTimer(int timer) { this.roomStayTimer = timer; }
    public int getStapsInRichting() { return stapsInRichting; }
    public void setStapsInRichting(int staps) { this.stapsInRichting = staps; }
    public int getMaxStapsRichting() { return maxStapsRichting; }
    public void setMaxStapsRichting(int max) { this.maxStapsRichting = max; }
    public boolean isUsesTrap() { return usesTrap; }
    public double getFaciliteitX() { return faciliteitX; }
    public int getFaciliteitsBezoekDuur() { return faciliteitsBezoekDuur; }
    public void setFaciliteitsBezoekDuur(int duur) { this.faciliteitsBezoekDuur = duur; }
    public void setHuidigerFaciliteitType(String type) { this.huidigerFaciliteitType = type; }
    public int getMaxX() { return maxX; }
    @Override
    public int getMaxY() { return maxY; }
    public void setStateToLeft() { this.state = State.BUITEN; } //polymorfisme
    public void setGridBounds(int maxX, int maxY) { this.maxX = maxX; this.maxY = maxY; }
    public void setLift(Lift lift) { this.lift = lift; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public void setEventBus(IEventBus eventBus) { this.eventBus = eventBus; }
    public String getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(String preferredRoomType) {
        if (preferredRoomType != null && !preferredRoomType.isBlank()) {
            this.preferredRoomType = preferredRoomType;
        }
    }
    public void setTransportPoints(double liftWaitX, double trapX) {
        this.liftWaitX = liftWaitX;
        this.trapX = trapX;
    }
    public Color getKleur() { return kleur; }
    public Kamer getHuidigKamer() { return huidigKamer; }
}
