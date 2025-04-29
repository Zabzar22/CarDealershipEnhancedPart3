package org.example;

public class SportsCar extends Vehicle {
    @Override
    public boolean isRentable() {
        return false; // Sports cars are not rentable
    }
}