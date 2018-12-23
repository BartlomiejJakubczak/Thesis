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
import com.example.bartomiejjakubczak.thesis.fragments.GroceryFragmentInfo;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryPendingFragment;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GroceryFragmentPendingAdapter extends RecyclerView.Adapter<GroceryPendingHolder> {

    private ArrayList<GroceryItem> groceryItems = new ArrayList<>();
    private ArrayList<String> boughtItemsIDs = new ArrayList<>();
    private Context context;
    private GroceryPendingFragment fragment;

    public GroceryFragmentPendingAdapter(Context context, GroceryPendingFragment fragment, ArrayList<GroceryItem> groceryItems) {
        this.context = context;
        this.fragment = fragment;
        this.groceryItems.addAll(groceryItems);
    }

    public void receiveNewList(ArrayList<GroceryItem> groceryItems) {
        this.groceryItems.clear();
        this.groceryItems.addAll(groceryItems);
        this.notifyDataSetChanged();
    }

    public ArrayList<String> getBoughtItemsIDs() {
        return boughtItemsIDs;
    }

    private void removeAt(int position) {
        groceryItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, groceryItems.size());
    }

    @NonNull
    @Override
    public GroceryPendingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_pending_model, parent, false);
        return new GroceryPendingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroceryPendingHolder holder, int position) {
        holder.name.setText(groceryItems.get(position).getName());
        holder.date.setText(groceryItems.get(position).getDate());
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("grocery_key", groceryItems.get(holder.getAdapterPosition()).getKey());
                GroceryFragmentInfo groceryFragmentInfo = new GroceryFragmentInfo();
                groceryFragmentInfo.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryFragmentInfo, "groceryFragmentInfo");
                fragmentTransaction.addToBackStack("groceryPendingFragment");
                fragmentTransaction.commit();
            }
        });
        holder.bought.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boughtItemsIDs.add(groceryItems.get(holder.getAdapterPosition()).getKey());
                removeAt(holder.getAdapterPosition());
                if (!fragment.fabBought.isShown()) {
                    fragment.fabBought.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return groceryItems.size();
    }
}
