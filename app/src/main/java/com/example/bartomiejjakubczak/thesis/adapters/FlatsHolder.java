package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class FlatsHolder extends RecyclerView.ViewHolder{

    TextView flatName;
    TextView flatAddress;
    TextView flatOwner;

    public FlatsHolder(View itemView) {
        super(itemView);
        flatName = itemView.findViewById(R.id.flat_name_model);
        flatAddress = itemView.findViewById(R.id.flat_address_model);
        flatOwner = itemView.findViewById(R.id.flat_owner_model);
    }

}
