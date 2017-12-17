package com.finnair.gamifiedpartnermap;

/**
 * Created by noctuaPC on 16.12.2017.
 */

public class PartnerData {
    private String companyName;
    private String companyAddress;
    private String fieldOfBusiness;
    private String companyDescription;

    private Double latitude;
    private Double longitude;

    public void setAllData(String name, String address, String business, String description, Double lat, Double lng){
        this.companyName = name;
        this.companyAddress = address;
        this.fieldOfBusiness = business;
        this.companyDescription = description;
        this.latitude = lat;
        this.longitude = lng;
    }


    public String getCompanyName(){ return this.companyName; }
    public String getCompanyAddress(){ return this.companyAddress; }
    public String getFieldOfBusiness(){ return this.fieldOfBusiness; }
    public String getCompanyDescription(){ return this.companyDescription; }
    public Double getLatitude(){ return this.latitude; }
    public Double getLongitude(){ return this.longitude; }

}
