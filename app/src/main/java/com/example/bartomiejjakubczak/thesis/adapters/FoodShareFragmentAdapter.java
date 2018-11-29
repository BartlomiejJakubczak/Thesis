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
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragmentInfo;
import com.example.bartomiejjakubczak.thesis.models.FoodShareItem;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FoodShareFragmentAdapter extends RecyclerView.Adapter<FoodShareFragmentHolder> {

    private Context context;
    private FoodShareFragment fragment;
    private ArrayList<FoodShareItem> foodShareItems = new ArrayList<>();
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    public FoodShareFragmentAdapter(Context context, FoodShareFragment fragment, ArrayList<FoodShareItem> foodShareItems) {
        this.context = context;
        this.fragment = fragment;
        this.foodShareItems.addAll(foodShareItems);
    }

    @NonNull
    @Override
    public FoodShareFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodshare_model, parent, false);
        return new FoodShareFragmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FoodShareFragmentHolder holder, int position) {
        holder.name.setText(foodShareItems.get(position).getName());
        holder.quantity.setText(foodShareItems.get(position).getQuantity());
        holder.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("foodshare_key", foodShareItems.get(holder.getAdapterPosition()).getKey());
                FoodShareFragmentInfo foodShareFragmentInfo = new FoodShareFragmentInfo();
                foodShareFragmentInfo.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, foodShareFragmentInfo, "foodShareFragmentInfo");
                fragmentTransaction.addToBackStack("foodShareFragment");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodShareItems.size();
    }
}
