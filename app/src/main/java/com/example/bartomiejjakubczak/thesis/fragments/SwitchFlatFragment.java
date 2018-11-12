package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.adapters.FlatsAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SwitchFlatFragment extends Fragment implements SharedPrefs, FirebaseConnection {

    private static final String TAG = "SwitchFlatFragment";
    private ArrayList<Flat> flats = new ArrayList<>();

    private RecyclerView recyclerView;
    private FlatsAdapter flatsAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mSearchedUserFlatsDatabaseReference;

    private void loadFlatsInformation() {
        final ArrayList<String> currentSearchedUserFlatsKeys = new ArrayList<>();
        mSearchedUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    currentSearchedUserFlatsKeys.add(ds.getKey());
                }
                Log.d(TAG, currentSearchedUserFlatsKeys.get(0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (currentSearchedUserFlatsKeys.contains(ds.getKey())) {
                        flats.add(new Flat(ds.child("name").getValue().toString(),
                                ds.child("address").getValue().toString(),
                                ds.child("owner").getValue().toString(),
                                ds.child("key").getValue().toString(),
                                ds.child("searchCode").getValue().toString(),
                                ds.child("ownerTag").getValue().toString()));
                    }
                }
                flatsAdapter = new FlatsAdapter(getActivity(), flats);
                recyclerView.setAdapter(flatsAdapter);
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
        View rootView = inflater.inflate(R.layout.dialogfragment_switch_flat, container, false);
        recyclerView = rootView.findViewById(R.id.flat_switch_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        loadFlatsInformation();
        return rootView;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
        mSearchedUserFlatsDatabaseReference = mFirebaseDatabase.getReference()
                .child(getString(R.string.firebase_reference_user_flats))
                .child(dotlessEmail);
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return null;
    }
}
