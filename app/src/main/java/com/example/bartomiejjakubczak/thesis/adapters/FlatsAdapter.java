package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;

import java.util.ArrayList;
import java.util.List;

public class FlatsAdapter extends RecyclerView.Adapter<FlatsHolder> {

    Context context;
    List<String> flatNames = new ArrayList<>();
    List<String> flatAddresses = new ArrayList<>();
    List<String> flatOwners = new ArrayList<>();

    public FlatsAdapter(Context context, List<String> flatNames, List<String> flatAddresses, List<String> flatOwners) {
        this.context = context;
        this.flatNames = flatNames;
        this.flatAddresses = flatAddresses;
        this.flatOwners = flatOwners;
    }

    @NonNull
    @Override
    public FlatsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_model, parent, false);
        return new FlatsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlatsHolder holder, int position) {
        holder.flatName.setText(flatNames.get(position));
        holder.flatAddress.setText(flatAddresses.get(position));
        holder.flatOwner.setText(flatOwners.get(position));
    }

    @Override
    public int getItemCount() {
        return flatNames.size();
    }
}
