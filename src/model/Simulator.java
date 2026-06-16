package model;

import model.personen.*;

import ui.HotelPanel;
import hotelevents.HotelEventType;
import model.strategy.EvacuationMovement;
import model.strategy.GastNormalStrategy;
import model.strategy.IMovementStrategy;
import model.strategy.SchoonmakerNormalStrategy;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Simulator {
    private boolean running = false;
    private int lastGuestSpawnTime = 0;
    private static final int CHECKOUT_NA_TICKS = 600;
    private Hotel hotel;                           // Hotel-model met kamers, areas en personen
    private HotelPanel hotelPanel;                 // UI-paneel om te verversen
    private SimulationClock clock;                 // Regelt het aantal ticks (tempo)
    private HTEClock hteClock;                     // High-level tick event dispatcher
    private EventBusImpl eventBus;                  // Event system (HotelEventType)
    private Lift lift;
    private Area lobbyArea;
    private Area restaurantArea;
    private Area cinemaArea;
    private Area fitnessArea;
    private double liftWaitX = Gast.LIFT_WAIT_X;
    private double trapX = Gast.TRAP_X;
    private int foodEventTimer = 0;
    private Map<String, Integer> guestCheckInTime = new HashMap<>();
    private static final int FOOD_EVENT_INTERVAL = 450;

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        // Requirement: Simulatieklok, eventafhandeling en basisopstart.
        // De simulator koppelt het model, de klok, events, lift en personen aan elkaar.
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.clock = new SimulationClock(100);
        this.hteClock = new HTEClock();
        this.eventBus = new EventBusImpl();
        this.eventBus.setSimulator(this);

        initialiseerLift();
        initialiseerPersonen();
    }
    private void initialiseerLift() {
        // Requirement: Lift- en/of traplogica.
        // De liftpositie en liftgrenzen komen uit de ingeladen layout.
        for (Area area : hotel.getAreas()) {
            String type = area.getAreaType();
            if (type != null && (type.equalsIgnoreCase("Schacht") ||
                    type.equalsIgnoreCase("Lift") ||
                    type.equalsIgnoreCase("Elevator"))) {

                double liftX = (area.getX() - 1) + 0.5;
                double liftY = (area.getY() + area.getHoogte() - 2) + 0.5;
                int minY = area.getY() - 1;
                int maxY = area.getY() + area.getHoogte() - 2;

                this.lift = new Lift(liftX, liftY, minY, maxY);
                this.lift.setEventBus(eventBus);
                hteClock.addListener(this.lift);

                System.out.println("[Simulator] Lift succesvol geïnitialiseerd.");
                return;
            }
        }
    }
    private void initialiseerPersonen() {
        // Requirement: Bewegingssysteem.
        // Personen krijgen hier hun strategies, hoteldata, lift/trap-posities en startpositie.
        Area opslagArea = null;
        for (Area area : hotel.getAreas()) {
            if (area.getAreaType() != null) {
                if (area.getAreaType().equalsIgnoreCase("Lobby")) {
                    this.lobbyArea = area;
                }
                if (area.getAreaType().equalsIgnoreCase("Restaurant")) {
                    // Requirement: Minimaal een faciliteit.
                    // Restaurant wordt gebruikt voor het food-event.
                    this.restaurantArea = area;
                }
                if (area.getAreaType().equalsIgnoreCase("Cinema")) {
                    this.cinemaArea = area;
                }
                if (area.getAreaType().equalsIgnoreCase("Fitness")) {
                    this.fitnessArea = area;
                }
                if (area.getAreaType().equalsIgnoreCase("Staircase")) {
                    // Requirement: Lift- en/of traplogica.
                    // De traplocatie uit de layout wordt gebruikt bij verdieping wisselen en evacuatie.
                    this.trapX = getAreaCenterX(area);
                }
                if (area.getAreaType().equalsIgnoreCase("Storage") || area.getAreaType().equalsIgnoreCase("Opslag")) {
                    opslagArea = area;
                }
            }
        }
        if (lift != null) {
            // Wachtende gasten staan op de rand naast de schacht; in de lift staan ze op lift.getX().
            this.liftWaitX = lift.getX() + 0.5;
        }
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof TickListener) {
                hteClock.addListener(persoon);
            }
            if (persoon instanceof Gast gast) {
                gast.setMovementStrategies(createGuestMovementStrategy(), createEvacuationMovementStrategy()); // SS0.C: injectie via Simulator (overschrijft de default uit de constructor)
                gast.setLift(lift);
                gast.setHotel(hotel);
                gast.setEventBus(eventBus);  // Set event bus
                gast.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
                gast.setTransportPoints(liftWaitX, trapX);

                if (lobbyArea != null) {
                    double startX = -1.0; // Start net buiten het hotel
                    double startY = (lobbyArea.getY() - 1) + 0.5;
                    gast.setStartPositie(startX, startY);
                }
            }
            if (persoon instanceof Schoonmaker schoonmaker) {
                schoonmaker.setMovementStrategies(createCleanerMovementStrategy(), createEvacuationMovementStrategy()); // SS0.C: injectie via Simulator
                schoonmaker.setLift(lift);
                schoonmaker.setHotel(hotel);
                schoonmaker.setEventBus(eventBus);
                schoonmaker.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
                schoonmaker.setTransportPoints(liftWaitX, trapX);

                if (opslagArea != null) {
                    double startX = opslagArea.getX() - 0.5;
                    double startY = opslagArea.getY() - 0.5;
                    schoonmaker.setStoragePosition(startX, startY);
                    schoonmaker.setStartPositie(startX, startY);
                }
            }
        }
    }
    public void tick() {
        // Requirement: Simulatieklok (HTE).
        // Een simulatietick werkt alleen door als de SimulationClock aangeeft dat het tijd is.
        if (!running || !clock.tick()) {
            hotelPanel.repaint();
            return;
        }

        hteClock.tick(); // SS0.1: klok tikt → roept onTick() aan op elke Persoon (TickListener)
        List<Persoon> teVerwijderen = hotel.getPersonen().stream()
                .filter(p -> (p instanceof Gast || p instanceof Schoonmaker) && p.getX() < -2.0)
                .toList();
        for (Persoon persoon : teVerwijderen) {
            hteClock.removeListener(persoon);
        }
        hotel.getPersonen().removeAll(teVerwijderen);

        autoCheckInGuests();       // Requirement: Inchecken van gasten.
        autoCheckoutGuests();      // Requirement: Uitchecken van gasten.
        triggerFoodEventAlsNodig(); // Requirement: Minimaal een faciliteit.

        if (!isEvacuatieActief()) {
            lastGuestSpawnTime++;
            if (lastGuestSpawnTime >= 100 && hasAvailableRoom()) {
                spawnNewGuest();
                lastGuestSpawnTime = 0;
            }
        } else {
            lastGuestSpawnTime = 0;
        }

        if (Math.random() < 0.001 && eventBus != null) {
            java.util.List<Persoon> gasten = hotel.getPersonen().stream()
                    .filter(p -> p instanceof Gast)
                    .toList();
            if (!gasten.isEmpty()) {
                Persoon randomGast = gasten.get((int)(Math.random() * gasten.size()));
                if (randomGast instanceof Gast gast) {
                    gast.activeerGodzilla();
                }
                eventBus.triggerHotelEvent(HotelEventType.GODZILLA,
                        randomGast.getNaam().hashCode(), 0);
            }
        }
        hotelPanel.repaint();
    }

    private void autoCheckInGuests() {
        // Requirement: Inchecken van gasten.
        // Gasten in de lobby krijgen automatisch een vrije kamer toegewezen.
        if (lift != null && lift.isFireAlarmActive()) {
            return;
        }

        try {
            List<Persoon> personenKopie = new ArrayList<>(hotel.getPersonen());

            for (Persoon persoon : personenKopie) {
                if (persoon instanceof Gast gast) {
                    if (gast.isBezigMetFaciliteit()) {
                        continue; // Faciliteit-event heeft tijdelijk voorrang op automatisch inchecken.
                    }
                    if (gast.getHuidigKamer() == null && isInArea(gast, lobbyArea) && gast.getX() > 1.0) {

                        Kamer kamer = hotel.zoekVrijeKamer(gast.getPreferredRoomType());
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("PentHouse");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Luxe");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Standaard");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Budget");

                        if (kamer != null) {
                            gast.checkinKamer(kamer);
                            guestCheckInTime.put(gast.getNaam(), 0);
                            System.out.println("[AUTO-CHECKIN] " + gast.getNaam() + " ingecheckt in kamer " + kamer.getKamernummer());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckInGuests] Fout: " + e.getMessage());
        }
    }

    private boolean isInArea(Persoon persoon, Area area) {
        if (area == null) return false;

        double minX = area.getX() - 1;
        double maxX = minX + area.getBreedte();
        double minY = area.getY() - 1;
        double maxY = minY + area.getHoogte();

        return persoon.getX() >= minX
                && persoon.getX() < maxX
                && persoon.getY() >= minY
                && persoon.getY() < maxY;
    }

    private void autoCheckoutGuests() {
        // Requirement: Uitchecken van gasten.
        // Na een vaste verblijfsduur checkt een gast uit en wordt de kamer SCHOONMAKEN.
        if (lift != null && lift.isFireAlarmActive()) {
            return;
        }

        try {
            List<Persoon> personenKopie = new ArrayList<>(hotel.getPersonen());

            for (Persoon persoon : personenKopie) {
                if (persoon instanceof Gast gast) {
                    if (gast.isBezigMetFaciliteit()) {
                        continue; // Gasten mogen niet tijdens een restaurant-event ineens uitchecken.
                    }
                    if (gast.getHuidigKamer() != null && guestCheckInTime.containsKey(gast.getNaam())) {

                        int stayTime = guestCheckInTime.get(gast.getNaam()) + 1;
                        guestCheckInTime.put(gast.getNaam(), stayTime);

                        if (stayTime >= CHECKOUT_NA_TICKS) {
                            gast.checkoutKamer();
                            guestCheckInTime.remove(gast.getNaam());
                            System.out.println("[AUTO-CHECKOUT] " + gast.getNaam() + " heeft uitgecheckt.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckoutGuests] Fout: " + e.getMessage());
        }
    }
    private boolean hasAvailableRoom() {
        // Requirement: Kamerbeheer.
        // Nieuwe gasten worden alleen gespawned als er minimaal een vrije kamer is.
        for (Kamer kamer : hotel.getKamers()) {
            if (kamer.getStatus() == Kamer.KamerStatus.VRIJ) {
                return true;
            }
        }
        return false;
    }

    private void triggerFoodEventAlsNodig() {
        // Requirement: Minimaal een faciliteit.
        // Periodiek wordt een food-event gestuurd waardoor gasten naar het restaurant gaan.
        if (restaurantArea == null || isEvacuatieActief()) {
            foodEventTimer = 0;
            return;
        }

        foodEventTimer++;
        if (foodEventTimer < FOOD_EVENT_INTERVAL) {
            return;
        }
        foodEventTimer = 0;

        List<Gast> gasten = hotel.getPersonen().stream()
                .filter(p -> p instanceof Gast)
                .map(p -> (Gast) p)
                .toList();

        if (gasten.isEmpty()) {
            return;
        }

        eventBus.triggerHotelEvent(HotelEventType.NEED_FOOD, 0, gasten.size());
    }

    public void stuurAlleGastenNaarRestaurant() {
        stuurAlleGastenNaarFaciliteit("Restaurant");
    }

    public void stuurAlleGastenNaarFaciliteit(String type) {
        // Requirement: Minimaal een faciliteit.
        // Events kunnen gasten naar Restaurant, Cinema of Fitness sturen als die bestaan in de layout.
        Area area = zoekFaciliteit(type);
        if (area == null || isEvacuatieActief()) {
            return;
        }

        double targetY = getAreaCenterY(area);
        List<Gast> gasten = new ArrayList<>();

        for (Persoon persoon : new ArrayList<>(hotel.getPersonen())) {
            if (persoon instanceof Gast gast) {
                gasten.add(gast);
            }
        }

        for (int i = 0; i < gasten.size(); i++) {
            double targetX = getGespreideAreaX(area, i);
            gasten.get(i).gaNaarFaciliteitDoorEvent(type, targetX, targetY);
        }
    }

    private Area zoekFaciliteit(String type) {
        if ("Restaurant".equalsIgnoreCase(type)) return restaurantArea;
        if ("Cinema".equalsIgnoreCase(type)) return cinemaArea;
        if ("Fitness".equalsIgnoreCase(type)) return fitnessArea;
        return null;
    }

    private double getAreaCenterX(Area area) {
        return area.getX() - 1 + area.getBreedte() / 2.0;
    }

    private double getAreaCenterY(Area area) {
        return area.getY() - 1 + area.getHoogte() / 2.0;
    }

    private double getGespreideAreaX(Area area, int index) {
        // Spreid gasten binnen de faciliteit, zodat ze niet allemaal exact op elkaar staan.
        int plekken = Math.max(1, area.getBreedte() * 2);
        if (plekken == 1) {
            return getAreaCenterX(area);
        }

        double minX = area.getX() - 1 + 0.35;
        double maxX = area.getX() - 1 + area.getBreedte() - 0.35;
        int plek = index % plekken;
        return minX + ((maxX - minX) * plek / (plekken - 1));
    }

    private boolean isEvacuatieActief() {
        // Requirement: Eventafhandeling.
        // Tijdens evacuatie worden normale processen zoals nieuwe gasten en faciliteit-events gepauzeerd.
        if (lift != null && lift.isFireAlarmActive()) {
            return true;
        }

        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon.isFireAlarmActive() || persoon.isEvacuatieBegonnen()) {
                return true;
            }
        }
        return false;
    }

    private void spawnNewGuest() {
        // Requirement: Inchecken van gasten.
        // Nieuwe gasten komen buiten bij de lobby binnen en kunnen daarna automatisch inchecken.
        if (lobbyArea == null) return;

        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "Elijah", "Sophia", "Mason"};
        String[] types = {"Luxe", "Standaard", "Budget"};

        String randomName = firstNames[(int)(Math.random() * firstNames.length)];
        String randomType = types[(int)(Math.random() * types.length)];

        double startX = -1.0;
        double startY = (lobbyArea.getY() - 1) + 0.5;

        Gast newGuest = new GastBuilder()
                .naam(randomName)
                .hotel(hotel)
                .lift(lift)
                .eventBus(eventBus)
                .preferredRoomType(randomType)
                .gridBounds(hotel.getBreedte(), hotel.getHoogte())
                .startPos(startX, startY)
                .build();
        newGuest.setTransportPoints(liftWaitX, trapX);

        hteClock.addListener(newGuest);
        hotel.addPersoon(newGuest);

        System.out.println("[Simulator] Nieuwe gast '" + randomName + "' komt aan (kamertype: " + randomType + ")");
    }
    private IMovementStrategy createGuestMovementStrategy() {
        return new GastNormalStrategy();
    }

    private IMovementStrategy createCleanerMovementStrategy() {
        return new SchoonmakerNormalStrategy();
    }

    private IMovementStrategy createEvacuationMovementStrategy() {
        return new EvacuationMovement();
    }

    public void triggerFireAlarm() {
        // Requirement: Eventafhandeling + lift/traplogica.
        // Brandalarm zet personen in evacuatiemodus en schakelt de lift uit.
        System.out.println("\n============================================================");
        System.out.println("🚨 🚨 🚨  BRANDALARM GEACTIVEERD - EVACUATIE BEGONNEN  🚨 🚨 🚨");
        System.out.println("============================================================\n");

        if (lift != null) {
            lift.activeerFireAlarm();
        }
        for (Persoon persoon : hotel.getPersonen()) {
            persoon.activeerFireAlarm();
        }
    }
    public void clearFireAlarm() {
        System.out.println("\n============================================================");
        System.out.println("✓ ✓ ✓  BRANDALARM REPROGMANSEERD - EVACUATIE AFGEROND  ✓ ✓ ✓");
        System.out.println("============================================================\n");

        if (lift != null) {
            lift.deactiveerFireAlarm();
        }
        for (Persoon persoon : hotel.getPersonen()) {
            persoon.deactiveerFireAlarm();
        }
    }
    public void start() { this.running = true; clock.start(); }
    public void pause() { this.running = false; clock.stop(); }
    public boolean isRunning() { return running; }
    public SimulationClock getClock() { return clock; }
    public Lift getLift() { return lift; }
    public EventBusImpl getEventBus() { return eventBus; }  // Get event bus for UI/debug
}
