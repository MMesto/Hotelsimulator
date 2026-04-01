package model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Verantwoordelijk voor het inladen en valideren van hotel layouts.
 * Leest een JSON bestand in en zet dit om naar een Hotel object.
 */
public class LayoutLoader {

    /**
     * Laadt een hotel layout vanuit een JSON bestand.
     * Controleert het bestandstype, bouwt het grid op en valideert de layout.
     *
     * @param bestandspad het pad naar het JSON bestand
     * @return een Hotel object met het grid en de lijst van areas
     * @throws Exception als het bestand ongeldig is of verplichte elementen mist
     */
    public static Hotel laadLayout(String bestandspad) throws Exception {

        // Controleer of het bestand een JSON bestand is
        if (!bestandspad.endsWith(".json")) {
            throw new Exception("Ongeldig bestandstype. Alleen .json bestanden zijn toegestaan.");
        }

        // Lees het JSON bestand in en zet het om naar een lijst van Area objecten
        BufferedReader reader = new BufferedReader(new FileReader(bestandspad));
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Area>>(){}.getType();
        List<Area> areas = gson.fromJson(reader, listType);
        reader.close();

        // Bepaal de grootte van het grid op basis van de posities en dimensies van de areas
        int maxX = 0, maxY = 0;
        for (Area area : areas) {
            int rechts = area.getX() + area.getBreedte();
            int onder = area.getY() + area.getHoogte();
            if (rechts > maxX) maxX = rechts;
            if (onder > maxY) maxY = onder;
        }

        // Maak een leeg grid aan, standaard gevuld met "G" (gang)
        String[][] grid = new String[maxY][maxX];
        for (int y = 0; y < maxY; y++)
            for (int x = 0; x < maxX; x++)
                grid[y][x] = "G";

        // Vul het grid met de juiste afkortingen op basis van de areas
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

        // Valideer het grid voordat het hotel wordt teruggegeven
        valideerLayout(grid);
        return new Hotel(grid, maxX, maxY, areas);
    }

    /**
     * Zet het type van een Area om naar een korte afkorting voor in het grid.
     * Kamers krijgen hun sterrenaanduiding (bijv. "1★").
     */
    private static String getAfkorting(Area area) {
        switch (area.getAreaType()) {
            case "Room":      return area.getClassification() != null ?
                    area.getClassification().replace(" Star", "★") : "K";
            case "Cinema":    return "B";
            case "Restaurant":return "R";
            case "Fitness":   return "FT";
            case "Lobby":     return "L";
            case "Lift":      return "F";
            case "Staircase": return "T";
            case "Storage":   return "S";
            default:          return "?";
        }
    }

    /**
     * Controleert of het grid een lift en een trap bevat.
     * Gooit een fout als één van deze verplichte elementen ontbreekt.
     *
     * @param grid het 2D grid om te valideren
     * @throws Exception als de lift of trap ontbreekt
     */
    public static void valideerLayout(String[][] grid) throws Exception {
        boolean heeftLift = false;
        boolean heeftTrap = false;

        // Doorzoek elk vakje op verplichte elementen
        for (String[] rij : grid) {
            for (String vakje : rij) {
                if (vakje.equals("F")) heeftLift = true;
                if (vakje.equals("T")) heeftTrap = true;
            }
        }

        if (!heeftLift) throw new Exception("Layout mist een lift!");
        if (!heeftTrap) throw new Exception("Layout mist een trap!");
    }

}