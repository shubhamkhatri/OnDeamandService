
package com.shubham.ondeamandservice.model;

public class mapWorkerList {

    private String Name, Address, Distance, Ratings, Phone;
    private Double Latitude,Longitude;

    public mapWorkerList(String defaultName, String defaultAddress, String defaultDistance, String defaultRatings, String defaultPhone,Double defaultLatitude,Double defaultLongitude) {
        Name = defaultName;
        Address=defaultAddress;
        Distance=defaultDistance;
        Ratings=defaultRatings;
        Phone=defaultPhone;
        Latitude=defaultLatitude;
        Longitude=defaultLongitude;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return Address;
    }

    public String getDistance() {
        return Distance;
    }

    public String getRatings() {
        return Ratings;
    }

    public String getPhone() {
        return Phone;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }
}
