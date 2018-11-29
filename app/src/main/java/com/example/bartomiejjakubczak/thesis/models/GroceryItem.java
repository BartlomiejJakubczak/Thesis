package com.example.bartomiejjakubczak.thesis.models;

import java.security.SecureRandom;

public class GroceryItem {

    private String key;
    private String name;
    private String quantity;
    private String notes;
    private String addingPersonKey;
    private String date;

    public GroceryItem(String name, String quantity, String notes, String addingPersonKey, String date) {
        this.key = generateKey(20);
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
        this.addingPersonKey = addingPersonKey;
        this.date = date;
    }


    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    public String getAddingPersonKey() {
        return addingPersonKey;
    }

    public String getDate() {
        return date;
    }

    private String generateKey(int length) {
        final String characters = "0123456789ABCDEFGHIJKLMNPQRSTUWVXYZabcdefghijklmnoprqstuvwxyz";
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }

    public String getKey() {
        return key;
    }
}
