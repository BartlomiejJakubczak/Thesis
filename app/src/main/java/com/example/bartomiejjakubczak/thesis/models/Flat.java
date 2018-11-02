package com.example.bartomiejjakubczak.thesis.models;

import java.security.SecureRandom;

public class Flat {

    private String name;
    private String address;
    private String key;
    private String owner;
    private String ownerTag;

    public Flat(String name, String address, String ownerEmailAddress, String ownerTag) {
        this.name = name;
        this.address = address;
        key = generateKey();
        this.owner = ownerEmailAddress;
        this.ownerTag = ownerTag;
    }

    public Flat(String name, String address, String ownerEmailAddress, String key, String ownerTag) {
        this.name = name;
        this.address = address;
        this.key = key;
        this.owner = ownerEmailAddress;
        this.ownerTag = ownerTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerTag() {
        return ownerTag;
    }

    private String generateKey() {
        final String characters = "0123456789ABCDEFGHIJKLMNPQRSTUWVXYZabcdefghijklmnoprqstuvwxyz";
        final int length = 20;
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }
}
