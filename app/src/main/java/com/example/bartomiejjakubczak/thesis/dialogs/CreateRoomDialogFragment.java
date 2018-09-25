package com.example.bartomiejjakubczak.thesis.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.bartomiejjakubczak.thesis.CreateRoomActivity;
import com.example.bartomiejjakubczak.thesis.MainActivity;
import com.example.bartomiejjakubczak.thesis.R;

public class CreateRoomDialogFragment extends DialogFragment {

    private Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.create_room_dialog_title)
                .setMessage(R.string.create_room_dialog_message)
                .setPositiveButton(R.string.create_room_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, CreateRoomActivity.class);
                        startActivity(intent);
                        //TODO find some way to open up create room activity safely
                    }
                })
                .setNegativeButton(R.string.create_room_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
