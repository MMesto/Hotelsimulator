package ui;

import model.*;
import model.personen.*;
import javax.swing.*;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static JLabel timestepLabel;
    private static JLabel speedLabel;
    private static JButton startPauseButton;
    private static JSlider speedSlider;
    private static Simulator simulator;
    private static Timer simulationTimer;
    private static GuestListWindow guestListWindow;

    // Venster-caches voor hergebruik per kamer of persoon
    private static final Map<Kamer, RoomDetailWindow> roomWindows = new HashMap<>();
    private static final Map<Persoon, PersonDetailWindow> personWindows = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new StartApplicationTask());
    }

    private static void startApplication() {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JPanel controlPanel = createControlPanel();
                Hotel hotel = loadInitialHotel();
                setupSimulator(hotel);
                startExternalEvents();
                showFrame(frame, controlPanel);
                startTimer();

            } catch (Exception e) {
                System.err.println("Error initializing simulator: " + e.getMessage());
                e.printStackTrace();
            }
    }

    private static JPanel createControlPanel() {
        JPanel panel = new JPanel();

        layoutDropdown = new JComboBox<>(getAvailableLayouts());
        startPauseButton = createButton("Start", "toggle");
        speedLabel = new JLabel("Tick Interval: 100ms");
        speedSlider = new JSlider(50, 1000, 100);
        speedSlider.addChangeListener(new SpeedSliderAction());
        statusLabel = new JLabel("Hotel laden...");
        timestepLabel = new JLabel("Timestep: 0");

        JButton fireAlarmButton = createButton("🔥 BRANDALARM", "fire");
        JButton clearAlarmButton = createButton("✓ All Clear", "clear");
        styleAlarmButton(fireAlarmButton, new Color(255, 90, 90));
        styleAlarmButton(clearAlarmButton, new Color(120, 230, 120));

        panel.add(new JLabel("Layout: "));
        panel.add(layoutDropdown);
        panel.add(createButton("Laden", "load"));
        panel.add(startPauseButton);
        panel.add(fireAlarmButton);
        panel.add(clearAlarmButton);
        panel.add(speedLabel);
        panel.add(speedSlider);
        panel.add(statusLabel);
        panel.add(timestepLabel);
        return panel;
    }

    private static JButton createButton(String text, String action) {
        JButton button = new JButton(text);
        button.addActionListener(new ButtonAction(action));
        return button;
    }

    private static Hotel loadInitialHotel() throws Exception {
        String eersteLayout = (String) layoutDropdown.getItemAt(0);
        Hotel hotel = LayoutLoader.laadLayout("layouts/" + eersteLayout);
        initializeKamers(hotel);
        addTestGuests(hotel);
        addSchoonmakers(hotel);
        return hotel;
    }

    private static void setupSimulator(Hotel hotel) {
        hotelPanel = new HotelPanel(hotel);
        simulator = new Simulator(hotel, hotelPanel);
        hotelPanel.setEventBus(simulator.getEventBus());
        if (simulator.getLift() != null) hotelPanel.setLift(simulator.getLift());
        setupHotelPanelCallbacks(hotel, hotelPanel);
    }

    private static void startExternalEvents() {
        System.out.println("\n🚀 [Main] US4.1 Activeren: Externe DLL Provider koppelen...");
        EventBusImpl eventBus = simulator.getEventBus();
        ExternalEventProvider provider = new ExternalEventProvider(eventBus);
        provider.start();
    }

    private static void showFrame(JFrame frame, JPanel controlPanel) {
        frame.add(controlPanel, "North");
        frame.add(hotelPanel, "Center");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void startTimer() {
        simulationTimer = new Timer(50, new TimerAction());
        simulationTimer.start();
    }

    private static void runTick() {
        if (simulator == null || !simulator.isRunning()) return;

        simulator.tick();
        int timestep = simulator.getClock().getTimestep();
        timestepLabel.setText("Timestep: " + timestep);
    }

    private static void styleAlarmButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
    }

    private static void toggleSimulation() {
        if (!simulator.isRunning()) {
            simulator.start();
            startPauseButton.setText("Pause");
            statusLabel.setText("Simulatie actief");
        } else {
            simulator.pause();
            startPauseButton.setText("Resume");
            statusLabel.setText("Simulatie gepauzeerd");
        }
    }

    private static void loadLayout() {
        try {
            String selected = (String) layoutDropdown.getSelectedItem();
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selected);

            initializeKamers(newHotel);
            addTestGuests(newHotel);
            addSchoonmakers(newHotel);

            hotelPanel.setHotel(newHotel);
            simulator = new Simulator(newHotel, hotelPanel);
            hotelPanel.setEventBus(simulator.getEventBus());

            if (simulator.getLift() != null) hotelPanel.setLift(simulator.getLift());

            setupHotelPanelCallbacks(newHotel, hotelPanel);

            startPauseButton.setText("Start");
            statusLabel.setText("Geladen: " + selected);
        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }

    private static void initializeKamers(Hotel hotel) {
        List<Area> roomAreas = new ArrayList<>();
        for (Area a : hotel.getAreas()) {
            if ("Room".equals(a.getAreaType())) roomAreas.add(a);
        }
        roomAreas.sort(new AreaPositionComparator());

        int huidigeVerdieping = -1;
        int kamerIndex = 0;
        for (Area roomArea : roomAreas) {
            if (roomArea.getY() != huidigeVerdieping) {
                huidigeVerdieping = roomArea.getY();
                kamerIndex = 1;
            }

            int kamernummer = isPentHouse(roomArea) ? 506 : huidigeVerdieping * 100 + kamerIndex;
            Kamer k = new Kamer(kamernummer, getKamerType(roomArea));
            k.setArea(roomArea);
            hotel.addKamer(k);
            kamerIndex++;
        }
    }

    private static String getKamerType(Area roomArea) {
        String classification = roomArea.getClassification();
        if (classification == null) return "Standaard";
        if (isPentHouse(roomArea)) return "PentHouse";
        if (classification.contains("5")) return "Luxe";
        if (classification.contains("3") || classification.contains("4")) return "Standaard";
        return "Budget";
    }

    private static boolean isPentHouse(Area roomArea) {
        return roomArea.getClassification() != null
                && roomArea.getClassification().equalsIgnoreCase("PentHouse");
    }

    private static void addTestGuests(Hotel hotel) {
        String[] namen = {"Alice", "Bob", "Charlie", "Diana", "Frank", "Grace"};
        for (String naam : namen) {
            Gast g = new GastBuilder()
                    .naam(naam)
                    .hotel(hotel)
                    .gridBounds(hotel.getBreedte(), hotel.getHoogte())
                    .startPos(-1.0, 6.5)
                    .build();
            hotel.addPersoon(g);
        }
    }

    private static void addSchoonmakers(Hotel hotel) {
        Schoonmaker s1 = new Schoonmaker("Schoonmaker1", 8, 6);
        Schoonmaker s2 = new Schoonmaker("Schoonmaker2", 8, 6);

        s1.setHotel(hotel);
        s2.setHotel(hotel);

        hotel.addPersoon(s1);
        hotel.addPersoon(s2);
    }

    private static String[] getAvailableLayouts() {
        File dir = new File("layouts");
        String[] files = dir.list(new JsonLayoutFilter());
        if (files != null) Arrays.sort(files);
        return (files != null) ? files : new String[0];
    }

    // === FIRE ALARM METHODS ===

    private static void triggerFireAlarm() {
        if (simulator != null && simulator.isRunning()) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🚨 🚨 🚨  FIRE ALARM ACTIVATED - EVACUATIE BEGONNEN  🚨 🚨 🚨");
            System.out.println("=".repeat(70) + "\n");

            simulator.triggerFireAlarm();
            statusLabel.setText("🔥 FIRE ALARM ACTIVE - EVACUATIE IN PROGRESS");
        } else {
            statusLabel.setText("⚠️ Simulatie moet actief zijn voor brandalarm!");
        }
    }

    private static void clearFireAlarm() {
        if (simulator != null) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("✓ ✓ ✓  FIRE ALARM CLEARED - EVACUATION COMPLETE  ✓ ✓ ✓");
            System.out.println("=".repeat(70) + "\n");

            simulator.clearFireAlarm();
            statusLabel.setText("✓ Brandalarm uitgeschakeld - Normaal operatie");
        }
    }

    // === GUEST LIST WINDOW METHODS ===

    private static void openGuestListWindow(Hotel hotel) {
        if (guestListWindow != null && guestListWindow.isVisible()) {
            guestListWindow.toFront();
            guestListWindow.requestFocus();
        } else {
            guestListWindow = new GuestListWindow(hotel);
            guestListWindow.setVisible(true);
        }
    }

    // === DETAIL WINDOW METHODS ===

    private static void openRoomDetailWindow(Kamer kamer) {
        if (roomWindows.containsKey(kamer) && roomWindows.get(kamer).isVisible()) {
            roomWindows.get(kamer).toFront();
            roomWindows.get(kamer).requestFocus();
        } else {
            RoomDetailWindow window = new RoomDetailWindow(kamer);
            roomWindows.put(kamer, window);
            window.setVisible(true);
        }
    }

    private static void openPersonDetailWindow(Persoon persoon) {
        if (personWindows.containsKey(persoon) && personWindows.get(persoon).isVisible()) {
            personWindows.get(persoon).toFront();
            personWindows.get(persoon).requestFocus();
        } else {
            PersonDetailWindow window = new PersonDetailWindow(persoon);
            personWindows.put(persoon, window);
            window.setVisible(true);
        }
    }

    private static void setupHotelPanelCallbacks(Hotel hotel, HotelPanel panel) {
        panel.setOnLobbyClick(new LobbyClickAction(hotel));
        panel.setOnRoomClick(new RoomClickAction());
        panel.setOnPersonClick(new PersonClickAction());
    }

    private static class StartApplicationTask implements Runnable {
        @Override
        public void run() {
            startApplication();
        }
    }

    private static class ButtonAction implements ActionListener {
        private final String action;

        public ButtonAction(String action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ("load".equals(action)) loadLayout();
            if ("toggle".equals(action)) toggleSimulation();
            if ("fire".equals(action)) triggerFireAlarm();
            if ("clear".equals(action)) clearFireAlarm();
        }
    }

    private static class TimerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            runTick();
        }
    }

    private static class SpeedSliderAction implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int val = speedSlider.getValue();
            if (simulator != null) simulator.getClock().setTickInterval(val);
            speedLabel.setText("Tick Interval: " + val + "ms");
        }
    }

    private static class AreaPositionComparator implements Comparator<Area> {
        @Override
        public int compare(Area a1, Area a2) {
            if (a1.getY() != a2.getY()) {
                return a1.getY() - a2.getY();
            }
            return a1.getX() - a2.getX();
        }
    }

    private static class JsonLayoutFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    }

    private static class LobbyClickAction implements Runnable {
        private final Hotel hotel;

        public LobbyClickAction(Hotel hotel) {
            this.hotel = hotel;
        }

        @Override
        public void run() {
            openGuestListWindow(hotel);
        }
    }

    private static class RoomClickAction implements Consumer<Kamer> {
        @Override
        public void accept(Kamer kamer) {
            openRoomDetailWindow(kamer);
        }
    }

    private static class PersonClickAction implements Consumer<Persoon> {
        @Override
        public void accept(Persoon persoon) {
            openPersonDetailWindow(persoon);
        }
    }
}
