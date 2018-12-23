package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.CompletedGroceryList;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GroceryListBoughtFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private ArrayList<String> boughtItemIDs = new ArrayList<>();
    private ArrayList<GroceryItem> groceryItems = new ArrayList<>();
    private PackageManager packageManager;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath = "";
    private boolean isPic = false;
    private GroceryListBoughtFragment thisFragment;

    private ImageView photoImageView;
    private Button saveButton;
    private Button addPhotoButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroceryPendingDatabaseReference;
    private DatabaseReference mGroceryBoughtDatabaseReference;
    private FirebaseStorage mStorage;
    private StorageReference photoRef;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        photoImageView.setImageBitmap(bitmap);
        saveButton.setEnabled(true);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void savePicInFirebaseStorage(String groceryListKey) {
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        photoRef = mStorage.getReference().child("images/Receipts/" + loadStringFromSharedPrefs(getActivity(), "flat_key") + "/" + groceryListKey);
        UploadTask uploadTask = photoRef.putFile(contentUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }

    private void setButtons() {
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(false);
                addPhotoButton.setEnabled(false);
                Date date = new Date();
                Date newDate = new Date(date.getTime());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String stringDate = simpleDateFormat.format(newDate);
                final CompletedGroceryList completedGroceryList = new CompletedGroceryList(stringDate, currentUserKey, photoRef.getPath());
                completedGroceryList.setReceiptURI(photoRef.getPath() + "/" + completedGroceryList.getKey());
                mGroceryBoughtDatabaseReference.child(completedGroceryList.getKey()).setValue(completedGroceryList).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mGroceryBoughtDatabaseReference.child(completedGroceryList.getKey()).child("productList").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (GroceryItem groceryItem: groceryItems) {
                                    mGroceryBoughtDatabaseReference.child(completedGroceryList.getKey()).child("productList").child(groceryItem.getKey()).setValue(groceryItem);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                mGroceryPendingDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            if (boughtItemIDs.contains(ds.child("key").getValue().toString())) {
                                mGroceryPendingDatabaseReference.child(ds.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                savePicInFirebaseStorage(completedGroceryList.getKey());
                Toast.makeText(getActivity(), "Grocery list saved", Toast.LENGTH_SHORT).show();
                GroceryPendingFragment groceryPendingFragment = new GroceryPendingFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryPendingFragment, "groceryPendingFragment");
                getFragmentManager().popBackStack();
                fragmentTransaction.commit();
            }
        });
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            addPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
        }
    }

    private void loadData() {
        mGroceryPendingDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<GroceryItem> boughtGroceries = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (boughtItemIDs.contains(ds.child("key").getValue().toString())) {
                        boughtGroceries.add(new GroceryItem(
                                ds.child("key").getValue().toString(),
                                ds.child("name").getValue().toString(),
                                ds.child("quantity").getValue().toString(),
                                ds.child("notes").getValue().toString(),
                                ds.child("addingPersonKey").getValue().toString(),
                                ds.child("date").getValue().toString()));
                    }
                }
                groceryItems.addAll(boughtGroceries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisFragment = this;
        packageManager = MainActivity.getContext().getPackageManager();
        boughtItemIDs = getArguments().getStringArrayList("bought_items_ids");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_bought, container, false);
        photoImageView = view.findViewById(R.id.grocery_bought_receipt_photo);
        saveButton = view.findViewById(R.id.grocery_bought_save_button);
        addPhotoButton = view.findViewById(R.id.grocery_bought_photo_button);
        loadData();
        setButtons();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            setPic();
            galleryAddPic();
        }
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mGroceryPendingDatabaseReference = mFirebaseDatabase.getReference()
                .child("grocery")
                .child("pendingGrocery")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mGroceryBoughtDatabaseReference = mFirebaseDatabase.getReference()
                .child("grocery")
                .child("boughtGrocery")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        photoRef = mStorage.getReference().child("images/Receipts/" + loadStringFromSharedPrefs(getActivity(), "flat_key"));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
