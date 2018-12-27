package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.ChoresFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Chore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChoresFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private ChoresFragment fragment;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ChoresFragmentAdapter adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChoresDatabaseReference;
    private ValueEventListener mChoresValueListener;

    private void loadChores() {
        mChoresValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Chore> chores = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    chores.add(new Chore(
                            ds.child("key").getValue().toString(),
                            ds.child("name").getValue().toString(),
                            ds.child("date").getValue().toString(),
                            ds.child("notes").getValue().toString(),
                            ds.child("personAssigning").getValue().toString(),
                            ds.child("personAssigned").getValue().toString()));
                }
                if (adapter == null) {
                    adapter = new ChoresFragmentAdapter(getActivity(), fragment, chores);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.receiveNewList(chores);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mChoresDatabaseReference.addValueEventListener(mChoresValueListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChores();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChoresDatabaseReference.removeEventListener(mChoresValueListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chores, container, false);
        fab = view.findViewById(R.id.chores_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignPersonFragment assignPersonFragment = new AssignPersonFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, assignPersonFragment, "assignPersonFragment");
                fragmentTransaction.addToBackStack("choresFragment");
                fragmentTransaction.commit();
            }
        });
        recyclerView = view.findViewById(R.id.chores_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mChoresDatabaseReference = mFirebaseDatabase.getReference()
                .child("chores")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
