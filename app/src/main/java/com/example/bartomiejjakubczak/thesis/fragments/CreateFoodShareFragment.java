package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Activity;
import android.app.Fragment;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.FoodShareItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateFoodShareFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private final String TAG = "CreateFoodShareFragment";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath = "";
    private boolean isPic = false;
    private PackageManager packageManager;

    private EditText foodName;
    private EditText foodQuantity;
    private Button addPhotoButton;
    private Button saveButton;
    private ImageView photoImageView;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mFoodShareDatabaseReference;

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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
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
        isPic = true;
    }

    private void savePicInFirebaseStorage(String foodShareKey) {
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        StorageReference riversRef = mStorage.getReference().child("images/FoodShare/" + loadStringFromSharedPrefs(getActivity(), "flat_key") + "/ " + foodShareKey);
        UploadTask uploadTask = riversRef.putFile(contentUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Picture successfully uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkIfEmpty(String string) {
        String testString = string.trim();
        return "".equals(testString);
    }

    private void saveInfoInDatabase(String name, String quantity, String photoURI) {

        boolean validName;
        boolean validQuantity;

        if (checkIfEmpty(name)) {
            foodName.setError("This field cannot be blank");
            validName = false;
        } else {
            validName = true;
        }

        if (checkIfEmpty(quantity)) {
            foodQuantity.setError("This field cannot be blank");
            validQuantity = false;
        } else {
            validQuantity = true;
        }

        Log.i(TAG, validName + " " + validQuantity + " " + isPic + " " + mCurrentPhotoPath.isEmpty() + mCurrentPhotoPath);

        if (validName && validQuantity && isPic) {
            saveButton.setEnabled(false);
            final FoodShareItem foodShareItem = new FoodShareItem(name, quantity, photoURI);
            mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), "flat_key")).child(foodShareItem.getKey()).setValue(foodShareItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    saveButton.setEnabled(true);
                    Toast.makeText(getActivity(), "Food item successfully added", Toast.LENGTH_SHORT).show();
                    foodName.setText("");
                    foodQuantity.setText("");
                    photoImageView.setImageResource(R.drawable.ic_photo_camera);
                    isPic = false;
                    savePicInFirebaseStorage(foodShareItem.getKey());
                }
            });

        } else if (validName && validQuantity && !isPic) {
            saveButton.setEnabled(false);
            FoodShareItem foodShareItem = new FoodShareItem(name, quantity);
            mFoodShareDatabaseReference.child(loadStringFromSharedPrefs(getActivity(), "flat_key")).child(foodShareItem.getKey()).setValue(foodShareItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    saveButton.setEnabled(true);
                    foodName.setText("");
                    foodQuantity.setText("");
                    Toast.makeText(getActivity(), "Food item successfully added", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (!validName && !validQuantity && isPic) {
            saveButton.setEnabled(false);
            foodName.setError("This field cannot be blank");
            foodQuantity.setError("This field cannot be blank");
            saveButton.setEnabled(true);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult called");
            setPic();
            galleryAddPic();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = MainActivity.getContext().getPackageManager();
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_foodshare, container, false);
        foodName = view.findViewById(R.id.foodshare_name);
        foodQuantity = view.findViewById(R.id.foodshare_quantity);
        photoImageView = view.findViewById(R.id.foodshare_imageView);

        addPhotoButton = view.findViewById(R.id.add_photo);
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            addPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
        } else {
            addPhotoButton.setVisibility(View.GONE);
            photoImageView.setVisibility(View.GONE);
        }
        saveButton = view.findViewById(R.id.save_foodshare);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoInDatabase(foodName.getText().toString(), foodQuantity.getText().toString(), mCurrentPhotoPath);
            }
        });
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFoodShareDatabaseReference = mFirebaseDatabase.getReference().child("foodShare");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
