package model;

import model.personen.*;
import hotelevents.HotelEventType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lift implements TickListener {

    // Requirement: Lift- en/of traplogica.
    // De lift is zelf een TickListener en beweegt per HTE-tick tussen verdiepingen.
    private final double x;
    private double y;
    private final int schachtMinY;
    private final int schachtMaxY;
    private IEventBus eventBus;  // Event system

    private final List<Persoon> passagiers = new ArrayList<>();
    private final Map<Persoon, Integer> passengerDestinations = new HashMap<>();

    private static final int MAX_CAPACITY = 100;
    private static final double LIFT_SPEED = 0.3;
    private static final int STATION_WAIT_TICKS = 15;

    private boolean fireAlarmActive = false;

    private enum LiftState { MOVING_UP, MOVING_DOWN, AT_STATION }
    private LiftState state = LiftState.MOVING_UP;

    private int currentFloor;
    private boolean movingUp;
    private int stationWaitCounter = 0;

    public Lift(double startX, double startY, int schachtMinY, int schachtMaxY) {
        this.x = startX;
        this.y = startY;
        this.schachtMinY = schachtMinY;
        this.schachtMaxY = schachtMaxY;
        this.currentFloor = (int)(startY - 0.5);
        this.movingUp = (startY < (schachtMinY + schachtMaxY) / 2.0 + 0.5);
    }


    public boolean voegGastToe(Persoon persoon, int doelVerdieping) {
        // Requirement: Lift- en/of traplogica.
        // Instappen kan alleen als de lift stilstaat, op dezelfde verdieping is en geen brandalarm heeft.
        if (fireAlarmActive || !isIdle() || passagiers.size() >= MAX_CAPACITY) {
            return false;
        }

        int persoonFloor = (int) persoon.getY();
        int liftFloor = (int) y;
        if (persoonFloor != liftFloor) {
            return false;
        }

        passagiers.add(persoon);
        passengerDestinations.put(persoon, doelVerdieping);
        persoon.setLiftPosition(this.x, this.y);
        System.out.println("[Lift] " + persoon.getNaam() + " ingestapt op verdieping " + liftFloor);
        return true;
    }

    public void verwijderGast(Persoon persoon) {
        if (passagiers.remove(persoon)) {
            passengerDestinations.remove(persoon);
            System.out.println("[Lift] " + persoon.getNaam() + " uitgestapt op verdieping " + currentFloor);
        }
    }

    @Override
    public void onTick() {
        // Requirement: Lift- en/of traplogica.
        // Elke tick beweegt de lift richting de volgende verdieping en houdt passagiers op liftpositie.
        if (fireAlarmActive) {
            state = LiftState.AT_STATION;
            return;
        }

        if (stationWaitCounter > 0) {
            state = LiftState.AT_STATION;
            stationWaitCounter--;
            if (stationWaitCounter == 0) {
                determineNextFloor();
            }
            syncPassagierPosities();
            return;
        }

        double targetY = currentFloor + 0.5;
        double distance = Math.abs(targetY - y);

        if (distance < 0.15) {
            y = targetY;
            state = LiftState.AT_STATION;
            stationWaitCounter = STATION_WAIT_TICKS;
            syncPassagierPosities();
            return;
        }

        // Beweeg de lift richting de doelverdieping
        state = (targetY > y) ? LiftState.MOVING_UP : LiftState.MOVING_DOWN;
        y += Math.signum(targetY - y) * LIFT_SPEED;

        // Zorg dat we niet voorbij het doel schieten
        if (state == LiftState.MOVING_UP) y = Math.min(y, targetY);
        else y = Math.max(y, targetY);
        syncPassagierPosities();
    }

    private void determineNextFloor() {
        Integer passagierDoel = getEerstePassagierDoel();
        if (passagierDoel != null) {
            currentFloor = passagierDoel;
            movingUp = currentFloor > (int) y;
            return;
        }

        if (movingUp) {
            currentFloor++;
            if (currentFloor > schachtMaxY) {
                currentFloor = schachtMaxY;
                movingUp = false;
            }
        } else {
            currentFloor--;
            if (currentFloor < schachtMinY) {
                currentFloor = schachtMinY;
                movingUp = true;
            }
        }
    }

    private Integer getEerstePassagierDoel() {
        int liftFloor = (int) y;
        for (Persoon persoon : passagiers) {
            Integer doel = passengerDestinations.get(persoon);
            if (doel != null && doel != liftFloor) {
                return Math.max(schachtMinY, Math.min(schachtMaxY, doel));
            }
        }
        return null;
    }

    private void syncPassagierPosities() {
        for (Persoon p : passagiers) {
            p.setLiftPosition(this.x, this.y);
        }
    }

    public void activeerFireAlarm() {
        // Requirement: Eventafhandeling + liftlogica.
        // Bij brandalarm wordt de lift buiten werking gezet en verlaten passagiers de lift.
        this.fireAlarmActive = true;
        System.out.println("[Lift] 🔥 FIRE ALARM: Lift buiten werking gesteld!");

        // Trigger EVACUATE event
        if (eventBus != null) {
            eventBus.triggerHotelEvent(HotelEventType.EVACUATE, 0, passagiers.size());
        }

        // Evacueer onmiddellijk alle passagiers
        new ArrayList<>(passagiers).forEach(this::verwijderGast);
    }

    public void deactiveerFireAlarm() {
        this.fireAlarmActive = false;
        System.out.println("[Lift] ✓ Brandalarm voorbij, lift keert terug naar normaal bedrijf");
    }

    // --- GETTERS & SETTERS ---
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIdle() { return state == LiftState.AT_STATION && stationWaitCounter > 0; }
    public boolean isFireAlarmActive() { return fireAlarmActive; }
    public void setEventBus(IEventBus eventBus) { this.eventBus = eventBus; }
}
