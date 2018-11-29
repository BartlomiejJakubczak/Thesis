package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateGroceryFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    private EditText name;
    private EditText quantity;
    private EditText notes;
    private Button saveButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroceryDatabaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_grocery, container, false);
        name = view.findViewById(R.id.grocery_name);
        quantity = view.findViewById(R.id.grocery_quantity);
        notes = view.findViewById(R.id.grocery_notes);
        saveButton = view.findViewById(R.id.grocery_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groceryName = name.getText().toString();
                String groceryQuantity = quantity.getText().toString();
                String groceryNotes = notes.getText().toString();

                if (!groceryName.trim().equals("")) {
                    Date date = new Date();
                    Date newDate = new Date(date.getTime());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String stringDate = simpleDateFormat.format(newDate);
                    GroceryItem newItem = new GroceryItem(groceryName, groceryQuantity, groceryNotes, currentUserKey, stringDate);
                    mGroceryDatabaseReference
                            .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)))
                            .child("pendingGrocery")
                            .child(newItem.getKey())
                            .setValue(newItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.getContext(), "Item successfully added", Toast.LENGTH_SHORT).show();
                        }
                    });
                    name.setText("");
                    quantity.setText("");
                    notes.setText("");
                } else {
                    name.setError("Field cannot be blank");
                }
            }
        });
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mGroceryDatabaseReference = mFirebaseDatabase.getReference().child("grocery");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
