package model;

import model.TickListener;

// Lift is ook een entiteit die alleen beweegt per tick
public class Lift implements TickListener {

    private int huidigeVerdieping = 0;
    private int doelVerdieping = 5;

    @Override
    public void onTick() {

        // Beweging alleen tijdens tick
        beweeg();

        System.out.println("Lift op verdieping: " + huidigeVerdieping);
    }

    private void beweeg() {

        // Lift beweegt 1 verdieping per tick
        if (huidigeVerdieping < doelVerdieping) {
            huidigeVerdieping++;
        } else if (huidigeVerdieping > doelVerdieping) {
            huidigeVerdieping--;
        }
    }

    public void setDoelVerdieping(int doel) {
        this.doelVerdieping = doel;
    }
}