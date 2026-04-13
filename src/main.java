import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import model.Simulator;
import model.Gast;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

/**
 * De MainUI klasse is het startpunt van de applicatie.
 * Het regelt de GUI (Swing), de interactie met de gebruiker en de aansturing van de simulator.
 */
public class main {

    // --- VARIABELEN (State) ---
    private static HotelPanel hotelPanel;      // Het canvas waarop het hotel getekend wordt
    private static JComboBox<String> layoutDropdown; // Keuzemenu voor de JSON-bestanden
    private static JLabel statusLabel;         // Tekstbalk onderin voor statusberichten
    private static JLabel timestepLabel;       // Toont de huidige tijdstap (HTE)
    private static JButton startPauseButton;   // De knop die wisselt tussen Start en Pause
    private static Simulator simulator;        // De 'hersenen' van de simulatie (logica)

    /** * US2.3: De Timer is de 'hartslag' van de UI.
     * We maken er een variabele van zodat de slider de hartslag kan versnellen of vertragen.
     */
    private static Timer simulationTimer;

    public static void main(String[] args) {
        // SwingUtilities zorgt ervoor dat de UI veilig op de 'Event Dispatch Thread' draait.
        // Dit voorkomt dat je scherm vastloopt (freezes).
        SwingUtilities.invokeLater(() -> {
            try {
                // --- FRAME INITIALISATIE ---
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stopt programma bij afsluiten

                // ControlPanel is de grijze balk bovenin met alle knoppen.
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Haal de lijst met .json bestanden op uit de map 'layouts'.
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // --- KNOPPEN LOGICA ---

                // Laden-knop: roept de methode aan om een nieuw hotel in te laden.
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // Start/Pause-knop:
                startPauseButton = new JButton("Start");
                startPauseButton.addActionListener(e -> {
                    // Check of de simulator op dit moment draait
                    if (!simulator.isRunning()) {
                        simulator.start(); // Logische start
                        startPauseButton.setText("Pause"); // Verander tekst op knop
                        statusLabel.setText("Simulatie actief");
                    } else {
                        simulator.pause(); // Logische pauze
                        startPauseButton.setText("Resume"); // Verander tekst op knop
                        statusLabel.setText("Simulatie gepauzeerd");
                    }
                });
                controlPanel.add(startPauseButton);

                // --- US2.3: DE SNELHEID SLIDER ---
                // We maken een label om de huidige snelheid in tekst te tonen (ms).
                JLabel speedLabel = new JLabel("Delay: 500ms");

                /**
                 * JSlider(min, max, startwaarde).
                 * We gebruiken 50ms als snelste (1000ms / 50 = 20 ticks per sec).
                 * We gebruiken 1000ms als langzaamste (1 tick per sec).
                 */
                JSlider speedSlider = new JSlider(50, 1000, 500);

                // ChangeListener: wordt uitgevoerd zodra je het schuifje beweegt.
                speedSlider.addChangeListener(e -> {
                    int delay = speedSlider.getValue(); // Haal de nieuwe waarde uit de slider

                    if (simulationTimer != null) {
                        // Cruciaal: We vertellen de Timer dat hij minder lang moet wachten tussen ticks.
                        simulationTimer.setDelay(delay);
                    }
                    // Werk de tekst op het scherm bij.
                    speedLabel.setText("Delay: " + delay + "ms");
                });

                controlPanel.add(speedLabel);
                controlPanel.add(speedSlider);

                // --- LABELS VOOR INFORMATIE ---
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                timestepLabel = new JLabel("Timestep: 0");
                controlPanel.add(timestepLabel);

                // --- EERSTE SETUP ---
                // We laden direct het eerste hotel uit de lijst zodat het scherm niet leeg is.
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel); // Maak het teken-veld aan
                addTestGuests(hotel); // Zet Alice, Bob en Charlie in het hotel

                // Maak de simulator-engine aan.
                simulator = new Simulator(hotel, hotelPanel);
                simulator.pause(); // De simulatie staat stil tot de gebruiker op Start klikt.

                // Voeg de onderdelen toe aan het hoofdvenster.
                frame.add(controlPanel, "North"); // Knoppen boven
                frame.add(hotelPanel, "Center"); // Hotel in het midden
                frame.pack(); // Maak venster precies groot genoeg
                frame.setLocationRelativeTo(null); // Centreer venster op scherm
                frame.setVisible(true); // Toon het venster

                /**
                 * DE KLOK (Timer):
                 * Dit is de motor van het programma. De timer 'vuurt' elke X milliseconden.
                 */
                simulationTimer = new Timer(500, e -> {
                    // Alleen als de gebruiker op Start heeft geklikt (isRunning == true)
                    if (simulator.isRunning()) {
                        simulator.tick(); // De simulator berekent de nieuwe posities van gasten

                        // We halen de huidige tijdstap op uit de Simulator-klok en tonen dit in de UI.
                        timestepLabel.setText("Timestep: " + simulator.getClock().getTimestep());
                    }
                });
                simulationTimer.start(); // De timer begint met 'lopen' op de achtergrond.

            } catch (Exception e) {
                // Foutopsporing: als er iets misgaat (bijv. bestand niet gevonden), zie je dat in de console.
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * getAvailableLayouts:
     * Deze methode kijkt in de map 'layouts' en filtert alle bestanden die eindigen op .json.
     * Zo hoef je de namen van de bestanden niet hardcoded in je code te zetten.
     */
    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));
        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Fout: Geen .json layouts gevonden in de map.");
        }
        Arrays.sort(layouts); // Zet ze op alfabetische volgorde
        return layouts;
    }

    /**
     * loadLayout:
     * Wordt aangeroepen als je op de 'Laden' knop klikt.
     * Het 'ververst' het huidige hotel met een nieuw bestand.
     */
    private static void loadLayout() {
        try {
            // Pak de geselecteerde naam uit de dropdown
            String selectedLayout = (String) layoutDropdown.getSelectedItem();

            // Gebruik de LayoutLoader om de JSON te vertalen naar een Hotel-object
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);

            // Geef het nieuwe hotel door aan het HotelPanel zodat de tekening verandert
            hotelPanel.setHotel(newHotel);

            addTestGuests(newHotel); // Zet de gasten weer terug op hun startpositie

            // Maak de simulator helemaal opnieuw aan voor het nieuwe hotel.
            simulator = new Simulator(newHotel, hotelPanel);
            simulator.resetClock(); // Belangrijk: De klok moet weer op 0 beginnen.
            simulator.pause(); // Het nieuwe hotel begint altijd gepauzeerd.

            // Reset de Start-knop tekst
            if (startPauseButton != null) {
                startPauseButton.setText("Start");
            }
            statusLabel.setText("Nieuw hotel geladen: " + selectedLayout);

        } catch (Exception e) {
            statusLabel.setText("Fout bij laden: " + e.getMessage());
        }
    }

    /**
     * addTestGuests:
     * Omdat we nog geen reserveringssysteem hebben, voegen we handmatig
     * drie Gast-objecten toe aan de lijst met personen in het Hotel.
     */
    private static void addTestGuests(Hotel hotel) {
        // Maak Alice aan op positie (2,2)
        Gast gast1 = new Gast("Alice", 2, 2);
        gast1.setGridBounds(hotel.getBreedte(), hotel.getHoogte()); // Zorg dat ze niet buiten de muren loopt
        hotel.addPersoon(gast1);

        // Doe hetzelfde voor Bob en Charlie
        Gast gast2 = new Gast("Bob", 3, 3);
        gast2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast2);

        Gast gast3 = new Gast("Charlie", 1, 1);
        gast3.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast3);
    }
}