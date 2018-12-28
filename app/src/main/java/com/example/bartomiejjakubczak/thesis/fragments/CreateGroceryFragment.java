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
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.AddedGroceryNotification;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private void sendNotifications() {
        final ArrayList<String> userIDs = new ArrayList<>();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String formattedDate = sdf.format(date);
        final AddedGroceryNotification addedGroceryNotification = new AddedGroceryNotification("Grocery", formattedDate, currentUserKey);
        mFlatUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    userIDs.add(ds.getKey());
                }
                for (String key: userIDs) {
                    mUsersDatabaseReference.child(key).child("notifications").child(addedGroceryNotification.getKey()).setValue(addedGroceryNotification);
                }
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
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_grocery, container, false);
        InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                            .child("pendingGrocery")
                            .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)))
                            .child(newItem.getKey())
                            .setValue(newItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.getContext(), "Item successfully added", Toast.LENGTH_SHORT).show();
                        }
                    });
                    sendNotifications();
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
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child("flatUsers")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
