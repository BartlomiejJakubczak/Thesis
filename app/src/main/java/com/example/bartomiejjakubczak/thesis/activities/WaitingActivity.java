package com.example.bartomiejjakubczak.thesis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WaitingActivity extends AppCompatActivity implements FirebaseConnection {

    private String currentUserEmail;
    private boolean firstCheck = true;

    private TextView explanation;
    private Button createFlat;
    private Button signOut;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSentNotificationsDatabaseReference;
    private DatabaseReference mUserFlatsDatabaseReference;

    private ValueEventListener mUserFlatsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_for_flat_join);
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);

        explanation = findViewById(R.id.waiting_explanation);
        createFlat = findViewById(R.id.create_flat_waiting_button);
        signOut = findViewById(R.id.sign_out_button);

        createFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateFlatActivity.class);
                finish();
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                finish();
                startActivity(intent);
            }
        });

        mUserFlatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> keys = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.i("WaitingActivity", ds.getKey() + " " + ds.child("personInvolvedKey").getValue().toString());
                    if (ds.child("personInvolvedKey").getValue().toString().equals(currentUserEmail)) {
                        keys.add(ds.getKey());
                    }
                }
                Log.i("WaitingActivity", keys.toString());
                mUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            if (keys.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "You got rejected by everyone", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSentNotificationsDatabaseReference.addValueEventListener(mUserFlatsListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSentNotificationsDatabaseReference.removeEventListener(mUserFlatsListener);
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSentNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("sentNotifications");
        mUserFlatsDatabaseReference = mFirebaseDatabase.getReference().child("userFlats").child(dotlessEmail);
    }
}
