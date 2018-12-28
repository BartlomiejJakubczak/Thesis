package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroceryFragmentInfo extends Fragment implements FirebaseConnection, SharedPrefs {

    private final String TAG = "GroceryFragmentInfo";
    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String groceryKey;
    private GroceryFragmentInfo thisFragment;
    private String oldName;
    private String oldQuantity;
    private String oldNotes;

    private EditText name;
    private EditText quantity;
    private EditText notes;
    private TextView owner;
    private ImageButton editName;
    private ImageButton editQuantity;
    private ImageButton editNotes;
    private Button saveChangesButton;
    private Button delete;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPendingGroceryDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private void setEditTexts() {
        name.setEnabled(false);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (oldName.equals(s.toString())) {
                    saveChangesButton.setEnabled(false);
                } else {
                    saveChangesButton.setEnabled(true);
                }
            }
        });
        quantity.setEnabled(false);
        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (oldQuantity.equals(s.toString())) {
                    saveChangesButton.setEnabled(false);
                } else {
                    saveChangesButton.setEnabled(true);
                }
            }
        });
        notes.setEnabled(false);
        notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (oldNotes.equals(s.toString())) {
                    saveChangesButton.setEnabled(false);
                } else {
                    saveChangesButton.setEnabled(true);
                }
            }
        });
    }

    private void setButtons() {
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.isEnabled()) {
                    name.setEnabled(false);
                } else {
                    name.setEnabled(true);
                }
            }
        });
        editQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity.isEnabled()) {
                    quantity.setEnabled(false);
                } else {
                    quantity.setEnabled(true);
                }
            }
        });
        editNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notes.isEnabled()) {
                    notes.setEnabled(false);
                } else {
                    notes.setEnabled(true);
                }
            }
        });
        saveChangesButton.setEnabled(false);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesButton.setEnabled(false);
                boolean validName;
                boolean validQuantity;
                boolean validNotes;
                final String newName = name.getText().toString();
                final String newQuantity = quantity.getText().toString();
                final String newNotes = notes.getText().toString();
                if (newName.trim().equals("")) {
                    name.setError("Field cannot be blank");
                    validName = false;
                } else {
                    validName = true;
                }

                if (newQuantity.trim().equals("")) {
                    quantity.setError("Field cannot be blank");
                    validQuantity = false;
                } else {
                    validQuantity = true;
                }

                if (newNotes.trim().equals("")) {
                    notes.setError("Field cannot be blank");
                    validNotes = false;
                } else {
                    validNotes = true;
                }
                if (validName && validQuantity && validNotes) {
                    mPendingGroceryDatabaseReference.child(groceryKey).child("name").setValue(newName);
                    mPendingGroceryDatabaseReference.child(groceryKey).child("quantity").setValue(newQuantity);
                    mPendingGroceryDatabaseReference.child(groceryKey).child("notes").setValue(newNotes.trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.getContext(), "Item successfully updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                    oldName = newName;
                    oldQuantity = newQuantity;
                    oldNotes = newNotes;
                    name.setEnabled(false);
                    quantity.setEnabled(false);
                    notes.setEnabled(false);
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPendingGroceryDatabaseReference.child(groceryKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.getContext(), "Item successfully removed", Toast.LENGTH_SHORT).show();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.remove(thisFragment);
                        fragmentTransaction.commit();
                        getFragmentManager().popBackStack();
                    }
                });
            }
        });
    }

    private void loadData() {
        mPendingGroceryDatabaseReference.child(groceryKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!currentUserKey.equals(dataSnapshot.child("addingPersonKey").getValue())) {
                    editName.setVisibility(View.GONE);
                    editQuantity.setVisibility(View.GONE);
                    editNotes.setVisibility(View.GONE);
                    saveChangesButton.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);

                    name.setText(dataSnapshot.child("name").getValue().toString());
                    quantity.setText(dataSnapshot.child("quantity").getValue().toString());
                    notes.setText(dataSnapshot.child("notes").getValue().toString());
                    String ownerKey = dataSnapshot.child("addingPersonKey").getValue().toString();
                    mUsersDatabaseReference.child(ownerKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            owner.setText("Person adding: " + dataSnapshot.child("tag").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    String ownerKey = dataSnapshot.child("addingPersonKey").getValue().toString();
                    mUsersDatabaseReference.child(ownerKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            owner.setText("Person adding: " + dataSnapshot.child("tag").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    editName.setVisibility(View.VISIBLE);
                    editQuantity.setVisibility(View.VISIBLE);
                    editNotes.setVisibility(View.VISIBLE);
                    saveChangesButton.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    name.setText(dataSnapshot.child("name").getValue().toString());
                    oldName = name.getText().toString();
                    quantity.setText(dataSnapshot.child("quantity").getValue().toString());
                    oldQuantity = quantity.getText().toString();
                    notes.setText(dataSnapshot.child("notes").getValue().toString());
                    oldNotes = notes.getText().toString();
                    setButtons();
                    setEditTexts();
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
        thisFragment = this;
        groceryKey = getArguments().getString("grocery_key");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grocery_pending_added_info, container, false);
        name = view.findViewById(R.id.grocery_name_info);
        quantity = view.findViewById(R.id.grocery_quantity_info);
        notes = view.findViewById(R.id.grocery_notes_info);
        owner = view.findViewById(R.id.grocery_owner_info);
        editName = view.findViewById(R.id.edit_grocery_name);
        editQuantity = view.findViewById(R.id.edit_grocery_quantity);
        editNotes = view.findViewById(R.id.edit_grocery_notes);
        saveChangesButton = view.findViewById(R.id.savechanges_grocery_button);
        delete = view.findViewById(R.id.delete_grocery_button);
        loadData();
        return view;
    }


    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mPendingGroceryDatabaseReference = mFirebaseDatabase.getReference()
                .child("grocery")
                .child("pendingGrocery")
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
