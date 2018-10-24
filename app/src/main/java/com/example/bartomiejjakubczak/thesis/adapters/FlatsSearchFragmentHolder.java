package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class FlatsSearchFragmentHolder extends RecyclerView.ViewHolder {

    TextView flatName;
    TextView flatAddress;
    TextView flatOwner;
    Button requestJoin;

    public FlatsSearchFragmentHolder(View itemView) {
        super(itemView);
        flatName = itemView.findViewById(R.id.flat_search_name_model);
        flatAddress = itemView.findViewById(R.id.flat_search_address_model);
        flatOwner = itemView.findViewById(R.id.flat_search_owner_model);
        requestJoin = itemView.findViewById(R.id.flat_search_button);
    }
}
