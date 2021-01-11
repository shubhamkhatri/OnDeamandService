
package com.shubham.ondeamandservice.model;

public class bookingList {

    private String Id, Model, Number, Service, DateTime, CarType, Price,Status;


    public String getModel() {
        return Model;
    }

    public String getNumber() {
        return Number;
    }

    public String getService() {
        return Service;
    }

    public String getDateTime() {
        return DateTime;
    }

    public String getCarType() {
        return CarType;
    }

    public String getPrice() {
        return Price;
    }

    public String getId() {
        return Id;
    }

    public String getStatus() {
        return Status;
    }

    public bookingList(String defaultId, String defaultModel, String defaultNumber, String defaultService, String defaultDateTime, String defaultCarType, String defaultPrice, String defaultStatus) {

        Id=defaultId;
        Model = defaultModel;
        Number = defaultNumber;
        Service = defaultService;
        DateTime = defaultDateTime;
        CarType = defaultCarType;
        Price = defaultPrice;
        Status=defaultStatus;
    }

}
