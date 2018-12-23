package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.GroceryCompletedFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.CompletedGroceryList;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroceryCompletedFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private static final String TAG = "GroceryCompletedFragment";
    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private GroceryCompletedFragment fragment;

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private GroceryCompletedFragmentAdapter mGroceryCompletedFragmentAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroceryCompletedDatabaseReference;
    private ValueEventListener mCompletedGroceryValueListener;

    private void setListeners() {
        mCompletedGroceryValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<CompletedGroceryList> groceryLists = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    groceryLists.add(new CompletedGroceryList(
                            ds.child("key").getValue().toString(),
                            ds.child("completionDate").getValue().toString(),
                            ds.child("buyerID").getValue().toString(),
                            ds.child("receiptURI").getValue().toString()));
                }
                if (mGroceryCompletedFragmentAdapter == null) {
                    mGroceryCompletedFragmentAdapter = new GroceryCompletedFragmentAdapter(getActivity(), fragment, groceryLists);
                    recyclerView.setAdapter(mGroceryCompletedFragmentAdapter);
                } else {
                    mGroceryCompletedFragmentAdapter.receiveNewList(groceryLists);
                    recyclerView.setAdapter(mGroceryCompletedFragmentAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mGroceryCompletedDatabaseReference.addValueEventListener(mCompletedGroceryValueListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
        this.fragment = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        setListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGroceryCompletedDatabaseReference.removeEventListener(mCompletedGroceryValueListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_completed, container, false);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation_completed);
        recyclerView = view.findViewById(R.id.grocery_completed_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_pending:
                        GroceryPendingFragment groceryPendingFragment = new GroceryPendingFragment();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_placeholder, groceryPendingFragment, "groceryPendingFragment");
                        getFragmentManager().popBackStack();
                        fragmentTransaction.addToBackStack("functionalitiesFragment");
                        fragmentTransaction.commit();
                    case R.id.action_completed:
                        break;
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_completed);
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mGroceryCompletedDatabaseReference = mFirebaseDatabase.getReference()
                .child("grocery")
                .child("boughtGrocery")
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
