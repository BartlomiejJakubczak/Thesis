package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CreateFoodShareFragment extends Fragment implements FirebaseConnection {

    private final String TAG = "CreateFoodShareFragment";

    private EditText foodName;
    private EditText foodQuantity;
    private Button addPhotoButton;
    private Button saveButton;
    private ImageView photoImageView;

    private FirebaseDatabase mFirebaseDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_foodshare, container, false);
        foodName = view.findViewById(R.id.foodshare_name);
        foodQuantity = view.findViewById(R.id.foodshare_quantity);
        photoImageView = view.findViewById(R.id.foodshare_imageView);
        addPhotoButton = view.findViewById(R.id.add_photo);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        saveButton = view.findViewById(R.id.save_foodshare);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    }
}
