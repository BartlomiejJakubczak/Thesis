package com.example.bartomiejjakubczak.thesis.models;

public class User {
    private String name;
    private String surname;
    private String email;
    private boolean hasRoom;

    public User() {

    }

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        // TODO think if it would be better to create separate child in database "rooms" with owner, or something
        this.hasRoom = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHasRoom() {
        return hasRoom;
    }

    public void setHasRoom(boolean hasRoom) {
        this.hasRoom = hasRoom;
    }
}
