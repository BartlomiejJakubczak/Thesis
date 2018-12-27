package com.example.bartomiejjakubczak.thesis.models;


import java.security.SecureRandom;

public class Chore {

    private String key;
    private String name;
    private String date;
    private String notes;
    private String personAssigning;
    private String personAssigned;

    public Chore(String name, String date, String notes, String personAssigning, String personAssigned) {
        this.key = generateKey(20);
        this.name = name;
        this.date = date;
        this.notes = notes;
        this.personAssigning = personAssigning;
        this.personAssigned = personAssigned;
    }

    public Chore(String key, String name, String date, String notes, String personAssigning, String personAssigned) {
        this.key = key;
        this.name = name;
        this.date = date;
        this.notes = notes;
        this.personAssigning = personAssigning;
        this.personAssigned = personAssigned;
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

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    public String getPersonAssigned() {
        return personAssigned;
    }

    public String getPersonAssigning() {
        return personAssigning;
    }
}
