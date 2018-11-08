package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ManageFlatFragmentAdapter extends RecyclerView.Adapter<UserHolder> implements SharedPrefs, FirebaseConnection {

    private Context context;
    private ArrayList<User> users = new ArrayList<>();
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUserFlatsDatabaseReference;

    public ManageFlatFragmentAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users.addAll(users);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(userDotlessEmail);
    }

    private void removeAt(int position) {
        users.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, users.size());
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_model, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserHolder holder, int position) {
        final String userKey = users.get(position).getEmail().replaceAll("[\\s.]", "");

        holder.userName.setText(users.get(position).getName());
        holder.userSurname.setText(users.get(position).getSurname());
        holder.userTag.setText(users.get(position).getTag());
        if (loadStringFromSharedPrefs(context, "shared_prefs_is_owner").equals("yes")
                && !userKey.equals(userDotlessEmail)) {
            holder.deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserFlatsDatabaseReference.child(userKey).child(loadStringFromSharedPrefs(context, "flat_key")).removeValue();
                    mFlatUsersDatabaseReference.child(loadStringFromSharedPrefs(context, "flat_key")).child(userKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "User successfully deleted", Toast.LENGTH_SHORT).show();
                            removeAt(holder.getAdapterPosition());
                        }
                    });
                }
            });
        } else {
            holder.deleteUserButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No owner set");
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child("flatUsers");
        mUserFlatsDatabaseReference = mFirebaseDatabase.getReference()
                .child("userFlats");
    }
}
