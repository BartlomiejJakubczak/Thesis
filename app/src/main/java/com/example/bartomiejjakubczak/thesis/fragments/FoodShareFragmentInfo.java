package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoodShareFragmentInfo extends Fragment implements FirebaseConnection, SharedPrefs {

    private final String TAG = "FoodShareFragmentInfo";
    private FoodShareFragmentInfo thisFragment;
    private String foodShareKey;
    private String photoStoragePath;
    private String oldName;
    private String oldQuantity;
    private String oldExpiration;

    private EditText name;
    private EditText quantity;
    private EditText expiration;
    private ImageButton editName;
    private ImageButton editQuantity;
    private ImageButton editExpiration;
    private Button saveChangesButton;
    private ImageView photo;
    private Button deleteButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFoodShareDatabaseReference;

    private boolean isDateValid(String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                date = sdf.parse(value);
                if (!value.equals(sdf.format(date))) {
                    date = null;
                }
            } catch (ParseException ex2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                    date = sdf.parse(value);
                    if (!value.equals(sdf.format(date))) {
                        date = null;
                    }
                } catch (ParseException ex3) {
                    ex3.printStackTrace();
                }
            }
        }
        return date != null;
    }

    private boolean checkIfSpecialCharacter(String string) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        return m.find();
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

        editExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expiration.isEnabled()) {
                    expiration.setEnabled(false);
                } else {
                    expiration.setEnabled(true);
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
                boolean validExpiration;
                final String newName = name.getText().toString();
                final String newQuantity = quantity.getText().toString();
                final String newExpiration = expiration.getText().toString();

                if (newName.trim().equals("")) {
                    name.setError("Field cannot be blank");
                    validName = false;
                } else {
                    validName = true;
                }

                if (checkIfSpecialCharacter(newName)) {
                    name.setError("This field cannot contain special characters");
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

                if (checkIfSpecialCharacter(newQuantity)) {
                    quantity.setError("This field cannot contain special characters");
                    validQuantity = false;
                } else {
                    validQuantity = true;
                }

                if (!isDateValid(newExpiration)) {
                    expiration.setError("Date is invalid");
                    validExpiration = false;
                } else {
                    validExpiration = true;
                }

                if (validName && validQuantity && validExpiration) {
                    DatabaseReference currentFoodShareReference = mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key))).child(foodShareKey);
                    currentFoodShareReference.child("name").setValue(newName);
                    currentFoodShareReference.child("quantity").setValue(newQuantity);
                    currentFoodShareReference.child("expirationDate").setValue(newExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.getContext(), "Item successfully updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                    oldName = newName;
                    oldQuantity = newQuantity;
                    oldExpiration = newExpiration;
                    name.setEnabled(false);
                    quantity.setEnabled(false);
                    expiration.setEnabled(false);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference currentFoodShareReference = mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key))).child(foodShareKey);
                currentFoodShareReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.getContext(), "Item successfully removed", Toast.LENGTH_SHORT).show();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.remove(thisFragment);
                        fragmentTransaction.commit();
                        getFragmentManager().popBackStack();
                    }
                });
                if (photoStoragePath != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(photoStoragePath);
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Storage image removed");
                        }
                    });
                }
            }
        });

    }

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
        expiration.setEnabled(false);
        expiration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (oldExpiration.equals(s.toString())) {
                    saveChangesButton.setEnabled(false);
                } else {
                    saveChangesButton.setEnabled(true);
                }
            }
        });
    }

    private void loadData(String foodShareKey) {
        mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key))).child(foodShareKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("name").getValue().toString());
                oldName = name.getText().toString();
                quantity.setText(dataSnapshot.child("quantity").getValue().toString());
                oldQuantity = quantity.getText().toString();
                expiration.setText(dataSnapshot.child("expirationDate").getValue().toString());
                oldExpiration = expiration.getText().toString();
                setEditTexts();
                if (dataSnapshot.child("photoURI").exists()) {
                    photoStoragePath = dataSnapshot.child("photoURI").getValue().toString();
                    setPic(photoStoragePath);
                }}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPic(String photoStoragePath) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(photoStoragePath);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String photoUri = uri.toString();
                try {
                    Glide.with(getActivity().getApplicationContext()).load(photoUri).into(photo).clearOnDetach();
                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisFragment = this;
        initializeFirebaseComponents();
        foodShareKey = getArguments().getString("foodshare_key");
        initializeFirebaseDatabaseReferences(FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.foodshare_info, container, false);
        name = view.findViewById(R.id.grocery_name_info);
        quantity = view.findViewById(R.id.grocery_quantity_info);
        expiration = view.findViewById(R.id.grocery_notes_info);
        editName = view.findViewById(R.id.edit_grocery_name);
        editQuantity = view.findViewById(R.id.edit_grocery_quantity);
        editExpiration = view.findViewById(R.id.edit_grocery_notes);
        saveChangesButton = view.findViewById(R.id.savechanges_grocery_button);
        photo = view.findViewById(R.id.foodshare_photo_info);
        deleteButton = view.findViewById(R.id.delete_grocery_button);
        loadData(foodShareKey);
        setButtons();
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFoodShareDatabaseReference = mFirebaseDatabase.getReference().child("foodShare");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
