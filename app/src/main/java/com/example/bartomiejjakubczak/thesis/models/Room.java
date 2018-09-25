package com.example.bartomiejjakubczak.thesis.models;

import com.example.bartomiejjakubczak.thesis.R;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Room {

    private String name;
    private String address;
    private String key;

    public Room(String name, String address) {
        this.name = name;
        this.address = address;
        key = generateKey();
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
