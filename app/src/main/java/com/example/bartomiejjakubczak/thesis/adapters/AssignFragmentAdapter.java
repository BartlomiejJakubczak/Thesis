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
import com.example.bartomiejjakubczak.thesis.fragments.AssignPersonFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateChoreFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragmentInfo;
import com.example.bartomiejjakubczak.thesis.models.FoodShareItem;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AssignFragmentAdapter extends RecyclerView.Adapter<AssignHolder> {

    private Context context;
    private AssignPersonFragment fragment;
    private ArrayList<User> usersInFlat = new ArrayList<>();
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    public AssignFragmentAdapter(Context context, AssignPersonFragment fragment, ArrayList<User> usersInFlat) {
        this.context = context;
        this.fragment = fragment;
        this.usersInFlat.addAll(usersInFlat);
    }

    @NonNull
    @Override
    public AssignHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assign_model, parent, false);
        return new AssignHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AssignHolder holder, int position) {
        holder.name.setText(usersInFlat.get(position).getName() + " " + usersInFlat.get(position).getSurname());
        holder.tag.setText(usersInFlat.get(position).getTag());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("user_assigned_key", usersInFlat.get(holder.getAdapterPosition()).getEmail().replaceAll("[\\s.]", ""));
                bundle.putString("user_assigned_tag", usersInFlat.get(holder.getAdapterPosition()).getTag());
                CreateChoreFragment createChoreFragment = new CreateChoreFragment();
                createChoreFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, createChoreFragment, "createChoreFragment");
                fragmentTransaction.addToBackStack("assignPersonFragment");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersInFlat.size();
    }
}
