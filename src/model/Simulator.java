package model;

import ui.HotelPanel;

public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;

    public Simulator(HotelPanel hotelPanel) {
        this.hotelPanel = hotelPanel;
        this.running = false;
    }

    public void start() {
        running = true;
        hotelPanel.setRunning(true);
    }

    public void pause() {
        running = false;
        hotelPanel.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (!running) return;

        // 🔥 BELANGRIJK: dit triggert jouw paintComponent()
        hotelPanel.repaint();
    }
}