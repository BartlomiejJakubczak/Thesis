package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.FoodShareFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.FoodShareItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FoodShareFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private final String TAG = "FoodShareFragment";
    private FoodShareFragment fragment;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private FoodShareFragmentAdapter foodShareFragmentAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFoodShareDatabaseReference;

    private void setRecyclerView() {
        final ArrayList<FoodShareItem> foodShareItems = new ArrayList<>();
        mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key))).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("photoURI").exists()) {
                    foodShareItems.add(new FoodShareItem(
                            ds.child("name").getValue().toString(),
                            ds.child("quantity").getValue().toString(),
                            ds.child("expirationDate").getValue().toString(),
                            ds.child("photoURI").getValue().toString(),
                            ds.child("key").getValue().toString()));
                    } else {
                        foodShareItems.add(new FoodShareItem(
                                ds.child("name").getValue().toString(),
                                ds.child("quantity").getValue().toString(),
                                ds.child("expirationDate").getValue().toString(),
                                ds.child("key").getValue().toString(),
                                1));
                    }
                }
                foodShareFragmentAdapter = new FoodShareFragmentAdapter(getActivity(), fragment, foodShareItems);
                recyclerView.setAdapter(foodShareFragmentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;
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
        setRecyclerView();
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
