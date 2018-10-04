package com.example.bartomiejjakubczak.thesis.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserFlats {
    private Set<String> keysOfUserFlats;

    public UserFlats(String roomKey) {
        keysOfUserFlats = new HashSet<>();
        keysOfUserFlats.add(roomKey);
    }
}
