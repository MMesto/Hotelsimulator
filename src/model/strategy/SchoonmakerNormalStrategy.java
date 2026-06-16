package model.strategy;

import hotelevents.HotelEventType;
import model.Kamer;
import model.personen.Persoon;
import model.personen.Schoonmaker;
import model.Area;

public class SchoonmakerNormalStrategy implements IMovementStrategy {

    @Override
    public void beweeg(Persoon persoon, MovementData data) {
        if (!(persoon instanceof Schoonmaker)) return;
        Schoonmaker sm = (Schoonmaker) persoon;

        if (sm.isInLift()) {
            handleLiftMovement(sm, data);
            return;
        }

        Kamer viezeKamer = sm.zoekViezeKamer();

        if (viezeKamer == null) {
            if (sm.getState() == Schoonmaker.State.NAAR_DOEL) {
                sm.setState(Schoonmaker.State.VRIJ);
                sm.setHuidigKamer(null);
            }

            // Opslagpositie komt uit de layout via MovementData.
            if (Math.abs(sm.getX() - data.getStorageX()) > 0.1) {
                sm.setX(sm.getX() + (sm.getX() < data.getStorageX() ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else if (Math.abs(sm.getY() - data.getStorageY()) > 0.1) {
                sm.setY(sm.getY() + (sm.getY() < data.getStorageY() ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                sm.setState(Schoonmaker.State.VRIJ);
            }
            return;
        }

        double targetX = getAreaCenterX(viezeKamer.getArea());
        double targetY = getAreaCenterY(viezeKamer.getArea());

        if (sm.getState() == Schoonmaker.State.SCHOONMAKEN) {
            werkAanKamer(sm);
        } else {
            beweegNaarKamer(sm, targetX, targetY, viezeKamer, data);
        }
    }

    private void beweegNaarKamer(Schoonmaker sm, double tx, double ty, Kamer doelKamer, MovementData data) {
        if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) {
            sm.setState(Schoonmaker.State.VRIJ);
            sm.setHuidigKamer(null);
            return;
        }

        if ((int)sm.getY() != (int)ty) {
            if (Math.abs(sm.getX() - data.getLiftWaitX()) > 0.1) {
                sm.setX(sm.getX() + (sm.getX() < data.getLiftWaitX() ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                sm.stapInLift((int)ty);
            }
        } else {
            if (Math.abs(sm.getX() - tx) > 0.1) {
                sm.setX(sm.getX() + (sm.getX() < tx ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                if (doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                    sm.setHuidigKamer(doelKamer);
                    sm.setState(Schoonmaker.State.SCHOONMAKEN);
                    sm.setSchoonmaakTimer(Schoonmaker.SCHOONMAAK_DUUR);

                    if (sm.getEventBus() != null) {
                        sm.getEventBus().triggerHotelEvent(HotelEventType.CLEANING_EMERGENCY,
                                sm.getNaam().hashCode(), doelKamer.getKamernummer());
                    }
                } else {
                    sm.setState(Schoonmaker.State.VRIJ);
                }
            }
        }
    }

    private void werkAanKamer(Schoonmaker sm) {
        sm.setSchoonmaakTimer(sm.getSchoonmaakTimer() - 1);
        if (sm.getSchoonmaakTimer() <= 0) {
            sm.getHuidigKamer().setStatus(Kamer.KamerStatus.VRIJ);
            System.out.println("[Schoonmaker] Kamer " + sm.getHuidigKamer().getKamernummer() + " is klaar.");
            sm.setHuidigKamer(null);
            sm.setState(Schoonmaker.State.VRIJ);
        }
    }

    private void handleLiftMovement(Schoonmaker sm, MovementData data) {
        if (data.getLift() == null) return;
        sm.setX(data.getLift().getX());
        sm.setY(data.getLift().getY());

        if ((int)data.getLift().getY() == sm.getDoelVerdieping() && data.getLift().isIdle()) {
            data.getLift().verwijderGast(sm);
            sm.setInLift(false);
            sm.setY(sm.getDoelVerdieping() + 0.5);

            if (sm.zoekViezeKamer() == null) {
                sm.setState(Schoonmaker.State.VRIJ);
            } else {
                sm.setState(Schoonmaker.State.NAAR_DOEL);
            }
        }
    }

    private double getAreaCenterX(Area area) { return area.getX() - 1 + area.getBreedte() / 2.0; }
    private double getAreaCenterY(Area area) { return area.getY() - 1 + area.getHoogte() / 2.0; }
}
