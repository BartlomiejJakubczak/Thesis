package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.GroceryCompletedInfoAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class GroceryCompletedInfoFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String groceryListKey;
    private String photoStoragePath;
    private ArrayList<GroceryItem> productsList = new ArrayList<>();

    private RecyclerView recyclerView;
    private Button receiptButton;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mGroceryBoughtDatabaseReference;

    private void loadData() {
        mGroceryBoughtDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photoStoragePath = dataSnapshot.child("receiptURI").getValue().toString();
                Log.i("GroceryCompletedInfo", " " + photoStoragePath);
                receiptButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mGroceryBoughtDatabaseReference.child("productList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    productsList.add(new GroceryItem(
                            ds.child("key").getValue().toString(),
                            ds.child("name").getValue().toString(),
                            ds.child("quantity").getValue().toString(),
                            ds.child("notes").getValue().toString(),
                            ds.child("addingPersonKey").getValue().toString(),
                            ds.child("date").getValue().toString()
                    ));
                }
                GroceryCompletedInfoAdapter groceryCompletedInfoAdapter = new GroceryCompletedInfoAdapter(getActivity(), productsList);
                recyclerView.setAdapter(groceryCompletedInfoAdapter);
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
        groceryListKey = getArguments().getString("groceryListKey");
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_completed_info, container, false);
        recyclerView = view.findViewById(R.id.grocery_completed_info_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        receiptButton = view.findViewById(R.id.grocery_completed_receipt_button);
        receiptButton.setEnabled(false);
        receiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("photoStoragePath", photoStoragePath);
                GroceryReceiptFragment groceryReceiptFragment = new GroceryReceiptFragment();
                groceryReceiptFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryReceiptFragment, "groceryReceiptFragment");
                fragmentTransaction.addToBackStack("groceryCompletedInfoFragment");
                fragmentTransaction.commit();
            }
        });
        loadData();
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mGroceryBoughtDatabaseReference = mFirebaseDatabase.getReference()
                .child("grocery")
                .child("boughtGrocery")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)))
                .child(groceryListKey);
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
