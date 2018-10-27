package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.models.Flat;

import java.util.ArrayList;
import java.util.List;

public class FlatsAdapter extends RecyclerView.Adapter<FlatsHolder> {

    Context context;
    private ArrayList<Flat> flats = new ArrayList<>();


    public FlatsAdapter(Context context, ArrayList<Flat> flats) {
        this.context = context;
        this.flats.addAll(flats);
    }

    @NonNull
    @Override
    public FlatsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_model, parent, false);
        return new FlatsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlatsHolder holder, int position) {
        holder.flatName.setText(flats.get(position).getName());
        holder.flatAddress.setText(flats.get(position).getAddress());
        holder.flatKey = flats.get(position).getKey();
    }

    @Override
    public int getItemCount() {
        return flats.size();
    }
}
