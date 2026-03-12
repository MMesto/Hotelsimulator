import model.Hotel;
import model.LayoutLoader;

public class Main {
    public static void main(String[] args) {
        try {
            Hotel hotel = LayoutLoader.laadLayout("layouts/hotel1.layout");
            System.out.println("Hotel geladen!");
            System.out.println("Breedte: " + hotel.getBreedte());
            System.out.println("Hoogte: " + hotel.getHoogte());

            // Print het grid
            for (String[] rij : hotel.getGrid()) {
                for (String vakje : rij) {
                    System.out.print(vakje + " ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Fout: " + e.getMessage());
        }
    }
}