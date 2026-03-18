package model;

import java.io.*;
import java.util.*;

public class LayoutLoader {

    public static Hotel laadLayout(String bestandspad) throws Exception {
        // Controleer of het bestand .layout extensie heeft
        if (!bestandspad.endsWith(".layout")) {
            throw new Exception("Ongeldig bestandstype. Alleen .layout bestanden zijn toegestaan.");
        }

        List<String[]> rijen = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(bestandspad));
        String lijn;

        while ((lijn = reader.readLine()) != null) {
            rijen.add(lijn.split(","));
        }
        reader.close();

        int hoogte = rijen.size();
        int breedte = rijen.get(0).length;
        String[][] grid = rijen.toArray(new String[0][]);
        valideerLayout(grid);

        return new Hotel(grid, breedte, hoogte);


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

        if (!heeftLobby) throw new Exception("Layout mist een lobby (L)!");
        if (!heeftLift) throw new Exception("Layout mist een lift (F)!");
        if (!heeftTrap) throw new Exception("Layout mist een trap (T)!");
    }
}