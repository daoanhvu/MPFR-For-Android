package com.eager2tech.beervision.dto;

import java.util.List;

public class DetectionSummaryDTO {

    private int numPeople;
    private List<BoundingBoxDTO> boundingBoxes;
    private List<BrandNumberDTO> brands;
    private String conclusion;
    private String imageName;

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public List<BoundingBoxDTO> getBoundingBoxes() {
        return boundingBoxes;
    }

    public void setBoundingBoxes(List<BoundingBoxDTO> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    public List<BrandNumberDTO> getBrands() {
        return brands;
    }

    public void setBrands(List<BrandNumberDTO> brands) {
        this.brands = brands;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
