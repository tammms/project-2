package vttp.project.backend.model;

public class MapLocation {

    private String address;
    private double latitude;
    private double longtitude;
    private String postalCode;
    private String name;


    public MapLocation() {}

    public MapLocation(String address, double latitude, double longtitude) {
        this.address = address;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }


    public MapLocation(String postalCode, String name, String address, double latitude, double longtitude) {
        this.address = address;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.postalCode = postalCode;
        this.name = name;
    }

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {this.postalCode = postalCode;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    
    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongtitude() {return longtitude;}
    public void setLongtitude(double longtitude) {this.longtitude = longtitude;}

    @Override
    public String toString() {
        return "Location [address=" + address + ", latitude=" + latitude + ", longtitude=" + longtitude
                + ", postalCode=" + postalCode + ", name=" + name + "]";
    }
    
}
