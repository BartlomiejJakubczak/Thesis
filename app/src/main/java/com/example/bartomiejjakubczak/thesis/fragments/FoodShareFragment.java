package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FoodShareFragment extends Fragment implements FirebaseConnection {

    private final String TAG = "FoodShareFragment";

    private FloatingActionButton fab;
    private RecyclerView recyclerView;

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
        View view = inflater.inflate(R.layout.fragment_foodshare, container, false);
        recyclerView = view.findViewById(R.id.foodShare_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateFoodShareFragment createFoodShareFragment = new CreateFoodShareFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, createFoodShareFragment, "createFoodShareFragment");
                fragmentTransaction.addToBackStack("foodShareFragment");
                fragmentTransaction.commit();
            }
        });

        // setting adapter here

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
