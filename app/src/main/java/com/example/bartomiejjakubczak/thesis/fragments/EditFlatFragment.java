package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class EditFlatFragment extends Fragment implements SharedPrefs, FirebaseConnection {

    private final static String TAG = "EditFlatFragment";
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
    private Button doneButton;

    private View setViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.activity_edit_flat, container, false);
        flatAddress = view.findViewById(R.id.edit_flat_address);
        flatName = view.findViewById(R.id.edit_flat_name);
        doneButton = view.findViewById(R.id.edit_flat_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String newFlatName = flatName.getText().toString();
                String newFlatAddress = flatAddress.getText().toString();
                verdictFlatParameters(newFlatName, newFlatAddress);
            }
        });
        setEditTexts();
        return view;
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

    private void updateFlatParameters(String newFlatName, String newFlatAddress) {
        doneButton.setEnabled(false);
        putStringToSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_name), newFlatName.trim());
        putStringToSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_address), newFlatAddress.trim());
        mSearchedFlatDatabaseReference.child(getString(R.string.flat_node_name)).setValue(newFlatName.trim());
        mSearchedFlatDatabaseReference.child(getString(R.string.flat_node_address)).setValue(newFlatAddress.trim());
        doneButton.setEnabled(true);
    }

    private void setEditTexts() {
        mSearchedFlatDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    parameters.add(ds.getValue().toString());
                }
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return setViews(inflater, container);
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSearchedFlatDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats))
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
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