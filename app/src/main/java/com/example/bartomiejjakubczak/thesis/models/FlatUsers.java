package com.example.bartomiejjakubczak.thesis.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlatUsers {
    private Set<String> emailsOfUsersInFlat;

    public FlatUsers(String ownerEmail) {
        emailsOfUsersInFlat = new HashSet<>();
        emailsOfUsersInFlat.add(ownerEmail);
    }
}
