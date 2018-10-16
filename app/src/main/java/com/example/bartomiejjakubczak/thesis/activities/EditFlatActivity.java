package com.example.bartomiejjakubczak.thesis.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditFlatActivity extends AppCompatActivity implements FirebaseConnection, SharedPrefs {

    private final static String TAG = "EditProfileActivity";
    private String oldName;
    private String oldAddress;
    private boolean validFlatName = false;
    private boolean validFlatAddress = false;
    private List<String> parameters = new ArrayList<>();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedFlatDatabaseReference;

    private EditText flatName;
    private EditText flatAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(mFirebaseAuth.getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
        setEditTexts();
    }

    private void setViews() {
        flatAddress = findViewById(R.id.edit_flat_address);
        flatName = findViewById(R.id.edit_flat_name);
        Button doneButton = findViewById(R.id.edit_flat_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFlatName = flatName.getText().toString();
                String newFlatAddress = flatAddress.getText().toString();
                verdictFlatParameters(newFlatName, newFlatAddress);
            }
        });
    }

    private void verdictFlatParameters(String newFlatName, String newFlatAddress) {
        checkIfCorrectFlatName(newFlatName);
        checkIfCorrectFlatAddress(newFlatAddress);
        if (!validFlatName) {
            flatName.setError(getString(R.string.error_blank_field));
            flatName.setText(oldName);
            flatName.setHintTextColor(getResources().getColor(R.color.red));
            flatName.requestFocus();
        } else if (!validFlatAddress) {
            flatAddress.setError(getString(R.string.error_blank_field));
            flatAddress.setText(oldAddress);
            flatAddress.setHintTextColor(getResources().getColor(R.color.red));
            flatAddress.requestFocus();
        } else {
            updateFlatParameters(newFlatName, newFlatAddress);
        }
    }

    private void updateFlatParameters(String newFlatName, String newFlatAddress) {
        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), newFlatName.trim());
        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), newFlatAddress.trim());
        mSearchedFlatDatabaseReference.child(getString(R.string.flat_node_name)).setValue(newFlatName.trim());
        mSearchedFlatDatabaseReference.child(getString(R.string.flat_node_address)).setValue(newFlatAddress.trim());
        finish();
    }

    private void checkIfCorrectFlatName(String newFlatName) {
        validFlatName = !checkIfEmpty(newFlatName);
    }

    private void checkIfCorrectFlatAddress(String newFlatAddress) {
        validFlatAddress = !checkIfEmpty(newFlatAddress);
    }

    private boolean checkIfEmpty(String newParameter) {
        String testString = newParameter.trim();
        return "".equals(testString);
    }

    private void setEditTexts() {
        mSearchedFlatDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    parameters.add(ds.getValue().toString());
                }
                setContentView(R.layout.activity_edit_flat);
                setViews();
                flatName.setText(parameters.get(2));
                oldName = parameters.get(2);
                flatAddress.setText(parameters.get(0));
                oldAddress = parameters.get(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSearchedFlatDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats))
                .child(loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key)));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
