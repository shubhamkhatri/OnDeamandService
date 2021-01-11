
package com.shubham.ondeamandservice.model;

public class workerList {

    private String Name, Address, Distance, Ratings, Phone;

    public workerList(String defaultName, String defaultAddress, String defaultDistance, String defaultRatings, String defaultphone) {
        Name = defaultName;
        Address=defaultAddress;
        Distance=defaultDistance;
        Ratings=defaultRatings;
        Phone=defaultphone;
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
}
