package com.finnair.gamifiedpartnermap;

/**
 * Created by ala-hazla on 14.3.2018.
 */

public class InfoWindowData {
    private int image = -1;
    private String name;
    private String address;
    private String description;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setData(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }
}
