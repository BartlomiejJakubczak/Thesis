package com.example.bartomiejjakubczak.thesis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CreateRoomActivity extends AppCompatActivity{

    private static final String TAG = "CreateRoomActivity";
    private EditText roomName;
    private EditText roomAddress;
    private TextView nameLabel;
    private TextView addressLabel;
    private Button createRoom;
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRoomsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mSearchedUserDatabaseReference;
    private DatabaseReference mUsersRoomsDatabaseReference;
    private DatabaseReference mRoomsUsersDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        setViews();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        initializeFirebaseComponents();

        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = roomName.getText().toString();
                String address = roomAddress.getText().toString();
                boolean validName;
                boolean validAddress;

                if (checkIfEmpty(name)) {
                    roomName.setError("This field cannot be blank");
                    roomName.setText("");
                    roomName.setHintTextColor(getResources().getColor(R.color.red));
                    validName = false;
                } else {
                    validName = true;
                }

                if (checkIfEmpty(address)) {
                    roomAddress.setError("This field cannot be blank");
                    roomAddress.setText("");
                    roomAddress.setHintTextColor(getResources().getColor(R.color.red));
                    validAddress = false;
                } else {
                    validAddress = true;
                }

                if (validAddress && validName) {
                    createNewRoom(roomName.getText().toString(), roomAddress.getText().toString());
                }

            }
        });
    }

    private void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRoomsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_rooms));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mUsersRoomsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_rooms));
        mRoomsUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_room_users));
    }

    private void setViews() {
        roomName = findViewById(R.id.room_name);
        roomAddress = findViewById(R.id.room_address);
        nameLabel = findViewById(R.id.name_label);
        addressLabel = findViewById(R.id.address_label);
        createRoom = findViewById(R.id.create_room_button);
    }

    private boolean checkIfEmpty(String string) {
        String testString = string.trim();
        return "".equals(testString);
    }

//    private boolean checkIfNumbers(String string) {
//        return string.matches(".*\\d+.*");
//    }

    private void createNewRoom(final String roomName, final String roomAddress) {
        final Room newRoom = new Room(roomName, roomAddress);

        mRoomsUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoomsUsersDatabaseReference.child(newRoom.getKey()).setValue(currentUser.getEmail().replaceAll("[\\s.]", ""));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUsersRoomsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsersRoomsDatabaseReference.child(currentUser.getEmail().replaceAll("[\\s.]", "")).setValue(newRoom.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRoomsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoomsDatabaseReference.child(newRoom.getKey()).setValue(newRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, getString(R.string.logs_room_created));
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
