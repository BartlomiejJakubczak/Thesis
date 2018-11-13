package com.example.bartomiejjakubczak.thesis.models;

import java.security.SecureRandom;

public class FoodShareItem {

    private String key;
    private String name;
    private String quantity;
    private String photoURI;

    public FoodShareItem(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
        this.key = generateKey(20);
        this.photoURI = null;
    }

    public FoodShareItem(String name, String quantity, String photoURI) {
        this.name = name;
        this.quantity = quantity;
        this.key = generateKey(20);
        this.photoURI = photoURI;
    }

    public FoodShareItem(String name, String quantity, String photoURI, String key) {
        this.name = name;
        this.quantity = quantity;
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
}
