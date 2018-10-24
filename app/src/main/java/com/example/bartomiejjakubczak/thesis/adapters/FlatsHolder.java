package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;

public class FlatsHolder extends RecyclerView.ViewHolder implements SharedPrefs {

    TextView flatName;
    TextView flatAddress;
    String flatKey;

    public FlatsHolder(View itemView) {
        super(itemView);
        flatName = itemView.findViewById(R.id.flat_name_model);
        flatAddress = itemView.findViewById(R.id.flat_address_model);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putStringToSharedPrefs(MainActivity.getContext(), "flat_name", flatName.getText().toString());
                putStringToSharedPrefs(MainActivity.getContext(), "flat_address", flatAddress.getText().toString());
                putStringToSharedPrefs(MainActivity.getContext(), "flat_key", flatKey);
            }
        });
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No flat yet");
    }
}
