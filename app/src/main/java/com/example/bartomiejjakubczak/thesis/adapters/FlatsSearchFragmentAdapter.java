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

public class FlatsSearchFragmentAdapter extends RecyclerView.Adapter<FlatsSearchFragmentHolder> {
    Context context;
    List<String> flatNames = new ArrayList<>();
    List<String> flatAddresses = new ArrayList<>();
    List<String> flatOwners = new ArrayList<>();

    public FlatsSearchFragmentAdapter(Context context, List<String> flatNames, List<String> flatAddresses, List<String> flatOwners) {
        this.context = context;
        this.flatNames = flatNames;
        this.flatAddresses = flatAddresses;
        this.flatOwners = flatOwners;
    }

    @NonNull
    @Override
    public FlatsSearchFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_search_model, parent, false);
        return new FlatsSearchFragmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlatsSearchFragmentHolder holder, int position) {
        holder.flatName.setText(flatNames.get(position));
        holder.flatAddress.setText(flatAddresses.get(position));
        holder.flatOwner.setText(flatOwners.get(position));
    }

    @Override
    public int getItemCount() {
        return flatNames.size();
    }

}
