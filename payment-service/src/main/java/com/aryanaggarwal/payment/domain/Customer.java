package com.aryanaggarwal.payment.domain;

import jakarta.persistence.*;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int amountAvailable;
    private int amountReserved;

    @Version
    private Long version; // prevents lost updates on concurrent bookings

    public Customer() {}

    public Customer(Long id, String name, int amountAvailable, int amountReserved) {
        this.id = id;
        this.name = name;
        this.amountAvailable = amountAvailable;
        this.amountReserved = amountReserved;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAmountAvailable() { return amountAvailable; }
    public void setAmountAvailable(int amountAvailable) { this.amountAvailable = amountAvailable; }
    public int getAmountReserved() { return amountReserved; }
    public void setAmountReserved(int amountReserved) { this.amountReserved = amountReserved; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', available=" + amountAvailable +
                ", reserved=" + amountReserved + '}';
    }
}
