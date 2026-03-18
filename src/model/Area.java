package model;

public class Area {
    private String AreaType;
    private String Position;
    private String Dimension;
    private String Classification;
    private int Capacity;

    public String getAreaType() { return AreaType; }
    public String getPosition() { return Position; }
    public String getDimension() { return Dimension; }
    public String getClassification() { return Classification; }
    public int getCapacity() { return Capacity; }

    public int getX() { return Integer.parseInt(Position.split(",")[0].trim()); }
    public int getY() { return Integer.parseInt(Position.split(",")[1].trim()); }
    public int getBreedte() { return Integer.parseInt(Dimension.split(",")[0].trim()); }
    public int getHoogte() { return Integer.parseInt(Dimension.split(",")[1].trim()); }
}