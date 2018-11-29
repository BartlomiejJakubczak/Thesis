package com.example.bartomiejjakubczak.thesis.models;

import android.icu.text.StringPrepParseException;

import java.security.SecureRandom;

public class FoodShareItem {

    private String key;
    private String name;
    private String quantity;
    private String expirationDate;
    private String photoURI;

    public FoodShareItem(String name, String quantity, String expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.key = generateKey(20);
        this.photoURI = null;
    }

    public FoodShareItem(String name, String quantity, String expirationDate, String photoURI) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.key = generateKey(20);
        this.photoURI = photoURI;
    }

    public FoodShareItem(String name, String quantity, String expirationDate, String key, int something) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.key = key;
        this.photoURI = null;
    }

    public FoodShareItem(String name, String quantity, String expirationDate, String photoURI, String key) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.key = key;
        this.photoURI = photoURI;
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

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }
}
