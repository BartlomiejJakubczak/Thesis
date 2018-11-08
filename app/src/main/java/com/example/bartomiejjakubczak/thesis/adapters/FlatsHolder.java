package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;

public class FlatsHolder extends RecyclerView.ViewHolder {

    TextView flatName;
    TextView flatAddress;
    String flatKey;
    ImageButton switchButton;

    public FlatsHolder(View itemView) {
        super(itemView);
        flatName = itemView.findViewById(R.id.flat_name_model);
        flatAddress = itemView.findViewById(R.id.flat_address_model);
        switchButton = itemView.findViewById(R.id.switch_button);
    }

}
