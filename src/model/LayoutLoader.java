package model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class LayoutLoader {

    public static Hotel laadLayout(String bestandspad) throws Exception {
        if (!bestandspad.endsWith(".json")) {
            throw new Exception("Ongeldig bestandstype. Alleen .json bestanden zijn toegestaan.");
        }

        // Lees JSON bestand
        BufferedReader reader = new BufferedReader(new FileReader(bestandspad));
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Area>>(){}.getType();
        List<Area> areas = gson.fromJson(reader, listType);
        reader.close();

        // Bepaal gridgrootte
        int maxX = 0, maxY = 0;
        for (Area area : areas) {
            int rechts = area.getX() + area.getBreedte();
            int onder = area.getY() + area.getHoogte();
            if (rechts > maxX) maxX = rechts;
            if (onder > maxY) maxY = onder;
        }

        // Maak leeg grid
        String[][] grid = new String[maxY][maxX];
        for (int y = 0; y < maxY; y++)
            for (int x = 0; x < maxX; x++)
                grid[y][x] = "G"; // standaard gang

        // Vul grid met areas
        for (Area area : areas) {
            String type = getAfkorting(area);
            for (int dy = 0; dy < area.getHoogte(); dy++) {
                for (int dx = 0; dx < area.getBreedte(); dx++) {
                    int x = area.getX() + dx - 1;
                    int y = area.getY() + dy - 1;
                    grid[y][x] = type;
                }
            }
        }

        return new Hotel(grid, maxX, maxY, areas);
    }

    private static String getAfkorting(Area area) {
        switch (area.getAreaType()) {
            case "Room": return area.getClassification() != null ?
                    area.getClassification().replace(" Star", "★") : "K";
            case "Cinema": return "B";
            case "Restaurant": return "R";
            case "Fitness": return "FT";
            case "Lobby": return "L";
            case "Lift": return "F";
            case "Staircase": return "T";
            default: return "?";
        }
    }

    public static void valideerLayout(String[][] grid) throws Exception {
        boolean heeftLobby = false;
        boolean heeftLift = false;
        boolean heeftTrap = false;

        for (String[] rij : grid) {
            for (String vakje : rij) {
                if (vakje.equals("L")) heeftLobby = true;
                if (vakje.equals("F")) heeftLift = true;
                if (vakje.equals("T")) heeftTrap = true;
            }
        }

        if (!heeftLobby) throw new Exception("Layout mist een lobby!");
        if (!heeftLift) throw new Exception("Layout mist een lift!");
        if (!heeftTrap) throw new Exception("Layout mist een trap!");
    }
}