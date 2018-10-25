package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.fragments.FlatSearchFragment;
import com.example.bartomiejjakubczak.thesis.models.Flat;

import java.util.ArrayList;

public class FlatsSearchFragmentAdapter extends RecyclerView.Adapter<FlatsSearchFragmentHolder> {
    Context context;
    private ArrayList<Flat> flats = new ArrayList<>();
    private ArrayList<Flat> flatsCopy = new ArrayList<>();

    public FlatsSearchFragmentAdapter(Context context, ArrayList<Flat> flats, ArrayList<Flat> flatsCopy) {
        this.context = context;
        this.flats.addAll(flats);
        this.flatsCopy.addAll(flatsCopy);
    }

    public void filter(String text) {
        flats.clear();
        if (text.isEmpty()) {
           flats.addAll(flatsCopy);
        } else {
            text = text.toLowerCase().trim().replace(" ", "");
            for (Flat flat: flatsCopy) {
                String name = flat.getName().toLowerCase().trim().replace(" ", "");
                String address = flat.getAddress().toLowerCase().trim().replace(" ", "");
                String owner = flat.getOwner().toLowerCase().trim().replace(" ", "");
                if (name.contains(text) || address.contains(text) || owner.contains(text)) {
                    flats.add(flat);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlatsSearchFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_search_model, parent, false);
        return new FlatsSearchFragmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlatsSearchFragmentHolder holder, int position) {
        holder.flatName.setText(flats.get(position).getName());
        holder.flatAddress.setText(flats.get(position).getAddress());
        holder.flatOwner.setText(flats.get(position).getOwner());
    }

    @Override
    public int getItemCount() {
        return flats.size();
    }

}
