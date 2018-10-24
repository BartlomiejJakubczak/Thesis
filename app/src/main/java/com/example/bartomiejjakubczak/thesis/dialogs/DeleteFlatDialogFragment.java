package com.example.bartomiejjakubczak.thesis.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.DeleteDialogCloseListener;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.utilities.TinyDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class DeleteFlatDialogFragment extends DialogFragment implements SharedPrefs, FirebaseConnection{

    private Context context;
    private boolean noButton = false;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatToDeleteDatabaseReference;
    private DatabaseReference mUserToDeleteFlatDatabaseReference;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUserFlatsDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFlatsDatabaseReference;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(mFirebaseAuth.getCurrentUser().getEmail().replaceAll("[\\s.]", ""));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Deleting " + loadStringFromSharedPrefs(context, getString(R.string.shared_prefs_flat_name)))
                .setMessage(getString(R.string.delete_flat_dialog_message))
                .setPositiveButton(getString(R.string.delete_flat_dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mUserToDeleteFlatDatabaseReference.removeValue();
                        mFlatUsersDatabaseReference.removeValue();
                        mFlatToDeleteDatabaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.delete_flat_dialog_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noButton = true;
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatToDeleteDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats)).child(loadStringFromSharedPrefs(context, getString(R.string.shared_prefs_flat_key)));
        mUserToDeleteFlatDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats)).child(dotlessEmail).child(loadStringFromSharedPrefs(context, getString(R.string.shared_prefs_flat_key)));
        mUserFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats)).child(dotlessEmail);
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_flats_users)).child(loadStringFromSharedPrefs(context, getString(R.string.shared_prefs_flat_key)));
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).commit();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No flat yet");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DeleteDialogCloseListener && !noButton)
            ((DeleteDialogCloseListener)activity).handleDeleteDialogClose();
    }
}
