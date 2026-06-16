package model.strategy;

import model.Hotel;
import model.IEventBus;
import model.Lift;

/**
 * Expliciete invoer voor een movement strategy.
 * Zo is zichtbaar welke simulatiegegevens een strategy mag gebruiken.
 */
public class MovementData {
    private final Hotel hotel;
    private final Lift lift;
    private final IEventBus eventBus;
    private final int maxX;
    private final int maxY;
    private final double liftWaitX;
    private final double trapX;
    private final double storageX;
    private final double storageY;

    public MovementData(Hotel hotel, Lift lift, IEventBus eventBus,
                        int maxX, int maxY,
                        double liftWaitX, double trapX,
                        double storageX, double storageY) {
        this.hotel = hotel;
        this.lift = lift;
        this.eventBus = eventBus;
        this.maxX = maxX;
        this.maxY = maxY;
        this.liftWaitX = liftWaitX;
        this.trapX = trapX;
        this.storageX = storageX;
        this.storageY = storageY;
    }

    public Hotel getHotel() { return hotel; }
    public Lift getLift() { return lift; }
    public IEventBus getEventBus() { return eventBus; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public double getLiftWaitX() { return liftWaitX; }
    public double getTrapX() { return trapX; }
    public double getStorageX() { return storageX; }
    public double getStorageY() { return storageY; }
}
