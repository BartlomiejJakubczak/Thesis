package com.example.bartomiejjakubczak.thesis.adapters;


import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryCompletedFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryCompletedInfoFragment;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.models.CompletedGroceryList;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroceryCompletedFragmentAdapter extends RecyclerView.Adapter<GroceryCompletedHolder> implements FirebaseConnection {

    private ArrayList<CompletedGroceryList> groceryLists = new ArrayList<>();
    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private Context context;
    private GroceryCompletedFragment groceryCompletedFragment;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    public GroceryCompletedFragmentAdapter(Context context, GroceryCompletedFragment groceryCompletedFragment, ArrayList<CompletedGroceryList> groceryLists) {
        this.context = context;
        this.groceryLists.addAll(groceryLists);
        this.groceryCompletedFragment = groceryCompletedFragment;
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    public void receiveNewList(ArrayList<CompletedGroceryList> groceryLists) {
        this.groceryLists.clear();
        this.groceryLists.addAll(groceryLists);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroceryCompletedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_completed_model, parent, false);
        return new GroceryCompletedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroceryCompletedHolder holder, int position) {
        mUsersDatabaseReference.child(groceryLists.get(position).getBuyerID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.buyer.setText("Buyer: " + dataSnapshot.child("tag").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.date.setText(groceryLists.get(position).getCompletionDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("groceryListKey", groceryLists.get(holder.getAdapterPosition()).getKey());
                GroceryCompletedInfoFragment groceryCompletedInfoFragment = new GroceryCompletedInfoFragment();
                groceryCompletedInfoFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = groceryCompletedFragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryCompletedInfoFragment, "groceryCompletedInfoFragment");
                fragmentTransaction.addToBackStack("groceryCompletedFragment");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return groceryLists.size();
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }
}
