package com.motive.challenge.model;

import javax.persistence.*;

@Entity
@Table(name = "cities", indexes = @Index(name = "idx_cities_state", columnList = "stateCode"))
public class City {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String stateCode;
    private double latitude;
    private double longitude;

    protected City() { }
    public City(String city, String stateCode, double latitude, double longitude) {
        this.city = city; this.stateCode = stateCode; this.latitude = latitude; this.longitude = longitude;
    }
    public Long getId() { return id; }
    public String getCity() { return city; }
    public String getStateCode() { return stateCode; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
