package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.adapters.FlatsSearchFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.utilities.TinyDB;

import java.util.List;

public class FlatSearchFragment extends Fragment {

    private EditText flatName;
    private RecyclerView recyclerView;
    private FlatsSearchFragmentAdapter flatsSearchFragmentAdapter;
    private List<String> flatNames;
    private List<String> flatAddresses;
    private List<String> flatOwners;

    private TinyDB tinyDB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_flats, container, false);
        setViews(view);
        setEditText();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(MainActivity.getContext());
        loadFlatsInformation();
    }

    private void setViews(View view) {
        flatName = view.findViewById(R.id.flats_search_editText);
        recyclerView = view.findViewById(R.id.fragment_flat_search_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        flatsSearchFragmentAdapter = new FlatsSearchFragmentAdapter(this.getActivity(), flatNames, flatAddresses, flatOwners);
        recyclerView.setAdapter(flatsSearchFragmentAdapter);
    }

    private void loadFlatsInformation() {
        flatNames = tinyDB.getListString(getString(R.string.shared_prefs_list_flat_names));
        flatAddresses = tinyDB.getListString(getString(R.string.shared_prefs_list_flat_addresses));
        flatOwners = tinyDB.getListString(getString(R.string.shared_prefs_list_flat_owners));
    }

    private void setEditText() {
        flatName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
