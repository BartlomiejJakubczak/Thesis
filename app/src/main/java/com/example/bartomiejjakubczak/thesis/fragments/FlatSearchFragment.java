package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.adapters.FlatsSearchFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.example.bartomiejjakubczak.thesis.utilities.TinyDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FlatSearchFragment extends Fragment implements FirebaseConnection {

    private EditText flatName;
    private RecyclerView recyclerView;
    private FlatsSearchFragmentAdapter flatsSearchFragmentAdapter;

    public ArrayList<Flat> flats = new ArrayList<>();
    public ArrayList<Flat> flatsCopy = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserFlatsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;

    private TinyDB tinyDB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return setViews(inflater, container);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(MainActivity.getContext());
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    private View setViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_search_flats, container, false);
        flatName = view.findViewById(R.id.flats_search_editText);
        recyclerView = view.findViewById(R.id.fragment_flat_search_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        loadFlatsInformation();
        return view;
    }

    private void loadFlatsInformation() {
        final ArrayList<String> currentSearchedUserFlatsKeys = new ArrayList<>();
        mSearchedUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    currentSearchedUserFlatsKeys.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (!currentSearchedUserFlatsKeys.contains(ds.getKey())) {
                        flats.add(new Flat(ds.child("name").getValue().toString(),
                                ds.child("address").getValue().toString(),
                                ds.child("owner").getValue().toString()));
                    }
                }
                flatsCopy.addAll(flats);
                flatsSearchFragmentAdapter = new FlatsSearchFragmentAdapter(MainActivity.getContext(), flats, flatsCopy);
                recyclerView.setAdapter(flatsSearchFragmentAdapter);
                setEditText();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setEditText() {
        flatName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                flatsSearchFragmentAdapter.filter(s.toString());
            }
        });
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSearchedUserFlatsDatabaseReference = mFirebaseDatabase.getReference()
                .child(getString(R.string.firebase_reference_user_flats))
                .child(dotlessEmail);
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
    }
}
