package com.example.bartomiejjakubczak.thesis.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.FlatsSearchFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FlatSearchActivity extends AppCompatActivity implements FirebaseConnection, SharedPrefs {

    private EditText codeInput;
    private Button submitButton;

    private String currentUserEmail;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mRequestSenderDatabaseReference;
    private DatabaseReference mRequestReceiverDatabaseReference;

    private void checkCode(final String inputCode) {
        submitButton.setEnabled(false);

        final boolean[] isFound = {false};
        final String[] flatKey = new String[1];
        final String[] flatOwnerKey = new String[1];
        final String[] flatName = new String[1];
        final String senderTag = loadStringFromSharedPrefs(getApplicationContext(), "shared_prefs_user_tag");
        final String[] sentNotificationKey = new String[1];

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (inputCode.equals(ds.child("searchCode").getValue())) {
                        isFound[0] = true;
                        flatKey[0] = ds.child("key").getValue().toString();
                        flatName[0] = ds.child("name").getValue().toString();
                        flatOwnerKey[0] = ds.child("owner").getValue().toString();
                        RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", currentUserEmail, flatKey[0]);
                        sentNotificationKey[0] = requestJoinNotification.getKey();
                        mRequestSenderDatabaseReference.child(requestJoinNotification.getKey()).setValue(requestJoinNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", currentUserEmail, flatOwnerKey[0], senderTag, flatKey[0], flatName[0], sentNotificationKey[0]);
                                mRequestReceiverDatabaseReference.child("receivedNotifications").child(requestJoinNotification.getKey()).setValue(requestJoinNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(), "The notification has been sent", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), WaitingActivity.class);
                                        finish();
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                    }
                }
                if (!isFound[0]) {
                    codeInput.setError("The code is invalid");
                    submitButton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_flats_withcode);
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
        codeInput = findViewById(R.id.flat_search_code_edittext);
        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = codeInput.getText().toString();
                checkCode(inputCode);
            }
        });
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child("flats");
        mRequestSenderDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("sentNotifications");
        mRequestReceiverDatabaseReference = mFirebaseDatabase.getReference().child("notifications");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No flat yet");
    }
}
