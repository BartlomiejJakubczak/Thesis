package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;

import java.util.ArrayList;

public class GroceryCompletedInfoAdapter extends RecyclerView.Adapter<GroceryCompletedInfoHolder> {

    private ArrayList<GroceryItem> productsList = new ArrayList<>();
    private Context context;

    public GroceryCompletedInfoAdapter(Context context, ArrayList<GroceryItem> productsList) {
        this.context = context;
        this.productsList.addAll(productsList);
    }

    @NonNull
    @Override
    public GroceryCompletedInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_grocery_completed_model, parent, false);
        return new GroceryCompletedInfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryCompletedInfoHolder holder, int position) {
        holder.name.setText("Name: " + productsList.get(position).getName());
        holder.quantity.setText("Quantity: " + productsList.get(position).getQuantity());
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }
}
