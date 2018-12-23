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

public class GroceryFragmentBoughtAdapter extends RecyclerView.Adapter<GroceryBoughtHolder> {

    private ArrayList<GroceryItem> boughtGrocery = new ArrayList<>();
    private Context context;

    public GroceryFragmentBoughtAdapter(Context context, ArrayList<GroceryItem> boughtGrocery) {
        this.context = context;
        this.boughtGrocery.addAll(boughtGrocery);
    }

    public ArrayList<String> getBoughtGroceryIDs() {
        ArrayList<String> boughtGroceryIDs = new ArrayList<>();
        for (GroceryItem groceryItem: boughtGrocery) {
            boughtGroceryIDs.add(groceryItem.getKey());
        }
        return boughtGroceryIDs;
    }

    private void removeAt(int position) {
        boughtGrocery.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, boughtGrocery.size());
    }

    @NonNull
    @Override
    public GroceryBoughtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_bought_model, parent, false);
        return new GroceryBoughtHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroceryBoughtHolder holder, int position) {
        holder.name.setText(boughtGrocery.get(position).getName());
        holder.quantity.setText(boughtGrocery.get(position).getQuantity());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAt(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return boughtGrocery.size();
    }

}
