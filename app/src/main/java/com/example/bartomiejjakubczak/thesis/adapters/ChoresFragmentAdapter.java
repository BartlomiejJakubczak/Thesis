package com.example.bartomiejjakubczak.thesis.adapters;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.fragments.ChoreFragmentInfo;
import com.example.bartomiejjakubczak.thesis.fragments.ChoresFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragmentInfo;
import com.example.bartomiejjakubczak.thesis.models.Chore;
import com.example.bartomiejjakubczak.thesis.models.FoodShareItem;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChoresFragmentAdapter extends RecyclerView.Adapter<ChoreHolder> {

    private Context context;
    private ChoresFragment fragment;
    private ArrayList<Chore> chores = new ArrayList<>();
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    public ChoresFragmentAdapter(Context context, ChoresFragment fragment, ArrayList<Chore> chores) {
        this.context = context;
        this.fragment = fragment;
        this.chores.addAll(chores);
    }

    public void receiveNewList(ArrayList<Chore> chores) {
        this.chores.clear();
        this.chores.addAll(chores);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chore_model, parent, false);
        return new ChoreHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChoreHolder holder, int position) {
        holder.date.setText("Due on: " + chores.get(position).getDate());
        holder.personAssigned.setText("Assigned: " + chores.get(position).getPersonAssigned());
        holder.name.setText("Name: " + chores.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("chore_key", chores.get(holder.getAdapterPosition()).getKey());
                ChoreFragmentInfo choreFragmentInfo = new ChoreFragmentInfo();
                choreFragmentInfo.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, choreFragmentInfo, "choreFragmentInfo");
                fragmentTransaction.addToBackStack("choresFragment");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chores.size();
    }
}
