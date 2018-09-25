package com.example.bartomiejjakubczak.thesis.models;

import java.util.ArrayList;
import java.util.List;

public class UserRooms {
    private List<String> keysOfUserRooms;

    public UserRooms(String roomKey) {
        keysOfUserRooms = new ArrayList<>();
        keysOfUserRooms.add(roomKey);
    }
}
