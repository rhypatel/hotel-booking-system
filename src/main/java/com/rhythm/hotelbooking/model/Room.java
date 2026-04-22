package com.rhythm.hotelbooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String roomNumber;

    private int floor;

    private String side; // LEFT or RIGHT

    private int position; // position in hallway

    private String type; // Single, Double, Suite

    private double pricePerNight;

    private String status;

    private int x;

    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Room() {
    }

    public Room(String roomNumber, int floor, String side, int position, String type, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.side = side;
        this.position = position;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public String getSide() {
        return side;
    }

    public int getPosition() {
        return position;
    }

    public String getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}