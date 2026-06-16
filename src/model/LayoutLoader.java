package model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Verantwoordelijk voor het inladen en valideren van hotel layouts.
 * Zet JSON om naar een Hotel object en zorgt dat alle ruimtes correct herkend worden.
 */
public class LayoutLoader {

    public static Hotel laadLayout(String bestandspad) throws Exception {

        // 1. BESTANDSTYPE CHECK
        if (!bestandspad.endsWith(".json")) {
            throw new Exception("Alleen JSON bestanden toegestaan");
        }

        // 2. DATA INLEZEN
        BufferedReader reader = new BufferedReader(new FileReader(bestandspad));
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Area>>(){}.getType();
        List<Area> areas = gson.fromJson(reader, listType);
        reader.close();

        if (areas == null || areas.isEmpty()) {
            throw new Exception("Layout bestand is leeg");
        }

        // 3. AFMETINGEN BEREKENEN
        int maxX = 0, maxY = 0;
        for (Area area : areas) {
            int rechts = area.getX() + area.getBreedte();
            int onder = area.getY() + area.getHoogte();
            if (rechts > maxX) maxX = rechts;
            if (onder > maxY) maxY = onder;
        }

        // 4. GRID INITIALISEREN
        // We gebruiken maxY en maxX direct.
        String[][] grid = new String[maxY][maxX];
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                grid[y][x] = "G"; // Standaard overal Gang
            }
        }

        // 5. GRID VULLEN MET AREAS
        for (Area area : areas) {
            String typeAfkorting = getAfkorting(area);

            for (int dy = 0; dy < area.getHoogte(); dy++) {
                for (int dx = 0; dx < area.getBreedte(); dx++) {
                    // Belangrijk: JSON coördinaten zijn vaak 1-based,
                    // maar in Java arrays gebruiken we 0-based.
                    int xPos = area.getX() + dx - 1;
                    int yPos = area.getY() + dy - 1;

                    // Voorkom IndexOutOfBounds
                    if (yPos >= 0 && yPos < maxY && xPos >= 0 && xPos < maxX) {
                        grid[yPos][xPos] = typeAfkorting;
                    }
                }
            }
        }

        // 6. VALIDATIE
        valideerLayout(grid);

        // 7. RETURN HOTEL
        return new Hotel(grid, maxX, maxY, areas);
    }

    private static String getAfkorting(Area area) {
        // We matchen hier op de "Type" string uit de JSON
        String type = area.getAreaType();
        if (type == null) return "?";

        switch (type) {
            case "Room":
                return area.getClassification() != null ?
                        area.getClassification().replace(" Star", "★") : "K";
            case "Cinema": return "C";
            case "Restaurant": return "R";
            case "Fitness": return "F";
            case "Lobby": return "L";
            case "Schacht":
            case "Elevator": // Sommige JSONs noemen het Elevator
            case "Lift":
                return "Elevator"; // We noemen dit intern Elevator voor de Schacht
            case "Staircase": return "T";
            case "Storage": return "S";
            case "Opslag": return "S";
            case "Lounge": return "Lounge";
            default: return "?";
        }
    }

    public static void valideerLayout(String[][] grid) throws Exception {
        boolean heeftLift = false;
        boolean heeftTrap = false;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if ("Elevator".equals(grid[y][x])) heeftLift = true;
                if ("T".equals(grid[y][x])) heeftTrap = true;
            }
        }

        // Layouts zonder lift of trap zijn niet bruikbaar voor deze simulatie.
        if (!heeftLift) throw new Exception("Geen lift gevonden in layout");
        if (!heeftTrap) throw new Exception("Geen trap gevonden in layout");
    }
}
