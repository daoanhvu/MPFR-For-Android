package com.eager2tech.beervision.dto;

public class BrandNumberDTO {

    private String brand;
    private int numberBox;
    private int numPeople;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getNumberBox() {
        return numberBox;
    }

    public void setNumberBox(int numberBox) {
        this.numberBox = numberBox;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }
}
