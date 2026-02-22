package com.aryanaggarwal.inventory.domain;

import jakarta.persistence.*;

@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int seatsAvailable;
    private int seatsReserved;

    @Version
    private Long version; // prevents overselling when concurrent bookings target the same seat

    public Seat() {}

    public Seat(Long id, String name, int seatsAvailable, int seatsReserved) {
        this.id = id;
        this.name = name;
        this.seatsAvailable = seatsAvailable;
        this.seatsReserved = seatsReserved;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSeatsAvailable() { return seatsAvailable; }
    public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }
    public int getSeatsReserved() { return seatsReserved; }
    public void setSeatsReserved(int seatsReserved) { this.seatsReserved = seatsReserved; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public String toString() {
        return "Seat{id=" + id + ", name='" + name + "', available=" + seatsAvailable +
                ", reserved=" + seatsReserved + '}';
    }
}
