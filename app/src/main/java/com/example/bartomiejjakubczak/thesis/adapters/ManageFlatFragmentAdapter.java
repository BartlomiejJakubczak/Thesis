package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.models.User;

import java.util.ArrayList;

public class ManageFlatFragmentAdapter extends RecyclerView.Adapter<UserHolder> {

    private Context context;
    private ArrayList<User> users = new ArrayList<>();

    public ManageFlatFragmentAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users.addAll(users);
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_model, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.userName.setText(users.get(position).getName());
        holder.userSurname.setText(users.get(position).getSurname());
        holder.userTag.setText(users.get(position).getTag());
        holder.deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
