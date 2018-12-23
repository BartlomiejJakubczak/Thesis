package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.bartomiejjakubczak.thesis.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GroceryReceiptFragment extends Fragment {

    private ImageView receipt;
    private String photoStoragePath;

    private void setPic(String photoStoragePath) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(photoStoragePath);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String photoUri = uri.toString();
                try {
                    Glide.with(getActivity().getApplicationContext()).load(photoUri).into(receipt).clearOnDetach();
                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoStoragePath = getArguments().getString("photoStoragePath");
        Log.i("GroceryReceiptFragment", " " + photoStoragePath);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_completed_receipt, container, false);
        receipt = view.findViewById(R.id.receipt_imageView);
        setPic(photoStoragePath);
        return view;
    }

}
