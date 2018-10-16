package com.example.bartomiejjakubczak.thesis.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateFlatActivity extends AppCompatActivity implements SharedPrefs, FirebaseConnection {

    private static final String TAG = "CreateFlatActivity";
    private String userDotlessEmail;
    private EditText flatName;
    private EditText flatAddress;
    private TextView nameLabel;
    private TextView addressLabel;
    private Button createFlat;
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mUsersFlatsDatabaseReference;
    private DatabaseReference mFlatsUsersDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flat);
        setViews();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userDotlessEmail = currentUser.getEmail().replaceAll("[\\s.]", "");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(userDotlessEmail);

        createFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = flatName.getText().toString();
                String address = flatAddress.getText().toString();
                boolean validName;
                boolean validAddress;

                if (checkIfEmpty(name)) {
                    flatName.setError(getString(R.string.error_blank_field));
                    flatName.setText("");
                    flatName.setHintTextColor(getResources().getColor(R.color.red));
                    validName = false;
                } else {
                    validName = true;
                }

                if (checkIfEmpty(address)) {
                    flatAddress.setError(getString(R.string.error_blank_field));
                    flatAddress.setText("");
                    flatAddress.setHintTextColor(getResources().getColor(R.color.red));
                    validAddress = false;
                } else {
                    validAddress = true;
                }

                if (validAddress && validName) {
                    createNewFlat(flatName.getText().toString().trim(), flatAddress.getText().toString().trim(), userDotlessEmail.trim());
                }

            }
        });
    }

    /**
     * Puts given String into shared preferences for later use, for example to indicate the currently selected flat.
     * @param context context from which preferences are taken
     * @param label key under which the String is stored
     * @param string the String value to be stored
     */

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    /**
     * Loads String values located in shared preferences.
     * @param context context from which preferences are taken
     * @param label key under which the String is stored
     * @return the String located under specified label
     */

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return null;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mUsersFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats));
        mFlatsUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_flats_users));
    }

    /**
     * Sets all views visible inside this activity.
     */

    private void setViews() {
        flatName = findViewById(R.id.flat_name);
        flatAddress = findViewById(R.id.flat_address);
        nameLabel = findViewById(R.id.name_label);
        addressLabel = findViewById(R.id.address_label);
        createFlat = findViewById(R.id.create_flat_button);
    }

    /**
     * Checks if given String is empty.
     * @param string The given String
     * @return returns true if given String is empty, false otherwise.
     */

    private boolean checkIfEmpty(String string) {
        String testString = string.trim();
        return "".equals(testString);
    }

//    private boolean checkIfNumbers(String string) {
//        return string.matches(".*\\d+.*");
//    }

    /**
     * Creates new flat and stores it in Firebase Database.
     * The name and address of the new flat are being stored in shared preferences for later use.
     * @param roomName Name of the flat specified by the user
     * @param roomAddress Address of the flat specified by the user
     * @param userDotlessEmail Email address without dots of the user
     */

    private void createNewFlat(final String roomName, final String roomAddress, String userDotlessEmail) {
        final Flat newFlat = new Flat(roomName, roomAddress, userDotlessEmail);

        mFlatsUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFlatsUsersDatabaseReference.child(newFlat.getKey()).child(currentUser.getEmail().replaceAll("[\\s.]", "")).setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUsersFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsersFlatsDatabaseReference.child(currentUser.getEmail().replaceAll("[\\s.]", "")).child(newFlat.getKey()).setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFlatsDatabaseReference.child(newFlat.getKey()).setValue(newFlat).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), newFlat.getName());
                            putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), newFlat.getAddress());
                            putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key), newFlat.getKey());
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
