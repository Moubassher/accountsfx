package com.moubassher.accountsfx.business;

public class BusinessAddress {

    private String name, city, coordinates, addressDescription;

    public BusinessAddress(String name, String city, String coordinates, String addressDescription) {
        this.name = name;
        this.city = city;
        this.coordinates = coordinates;
        this.addressDescription = addressDescription;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getAddressDescription() {
        return addressDescription;
    }
}
