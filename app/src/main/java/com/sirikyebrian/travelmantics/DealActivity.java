package com.sirikyebrian.travelmantics;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_REQUEST_CODE = 102;
    private static final String LOG_TAG = DealActivity.class.getSimpleName();
    public static final String EXTRA_DEAL = "Deal";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    TravelDeal travelDeal;
    private TravelDealListActivity travelDealListActivity;
    ImageView travelDealImage;
    private Button uploadImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        travelDealListActivity = new TravelDealListActivity();

        FirebaseUtil.openFirebaseReference(getString(R.string.travel_deals_reference), travelDealListActivity);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        travelDealImage = findViewById(R.id.dealImageView);

        Intent intent = getIntent();
        TravelDeal travelDeal = (TravelDeal) intent.getSerializableExtra(EXTRA_DEAL);
        if (travelDeal == null) {
            travelDeal = new TravelDeal();
        }
        this.travelDeal = travelDeal;
        txtTitle.setText(travelDeal.getTitle());
        txtDescription.setText(travelDeal.getDescription());
        txtPrice.setText(travelDeal.getPrice());
        showImage(travelDeal.getImageUrl());


        uploadImageButton = findViewById(R.id.uploadButton);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mimeType = "image/jpeg";
                Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageIntent.setType(mimeType);
                imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(imageIntent.createChooser(imageIntent,
                        getResources().getString(R.string.string_choose_picture)),
                        PICTURE_REQUEST_CODE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference storageReference =
                    FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());

            UploadTask uploadTask = storageReference.putFile(imageUri);
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        String name = task.getResult().getPath();

                        travelDeal.setImageUrl(url);
                        travelDeal.setImageName(name);
                        Log.d(LOG_TAG, "Image Uri is: " + downloadUri);
                        Log.d(LOG_TAG,"Image name is: "+name);
                        showImage(url);
                    }
                }
            });
            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(LOG_TAG, getResources().getString(R.string.string_image_upload_success));
                    Toast.makeText(DealActivity.this,
                            getResources().getString(R.string.string_image_upload),
                            Toast.LENGTH_SHORT).show();
                }
            });
            uriTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(LOG_TAG, getResources().getString(R.string.string_image_upload_failure));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.action_delete_deal).setVisible(true);
            menu.findItem(R.id.action_save_deal).setVisible(true);
            enableEditTexts(true);
            uploadImageButton.setEnabled(true);
        } else {
            menu.findItem(R.id.action_delete_deal).setVisible(false);
            menu.findItem(R.id.action_save_deal).setVisible(false);
            enableEditTexts(false);
            uploadImageButton.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_save_deal:
                saveTravelDeal();
                Toast.makeText(this, "Deal saved!", Toast.LENGTH_SHORT).show();
                clean();
                backToList();
                return true;
            case R.id.action_delete_deal:
                deleteTravelDeal();
                Toast.makeText(this, "Deal Deleted!", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void saveTravelDeal() {
        travelDeal.setTitle(txtTitle.getText().toString());
        travelDeal.setDescription(txtDescription.getText().toString());
        travelDeal.setPrice(txtPrice.getText().toString());

        if (travelDeal.getId() == null) {
            mDatabaseReference.push().setValue(travelDeal);
        } else {
            mDatabaseReference.child(travelDeal.getId()).setValue(travelDeal);
        }

    }

    private void deleteTravelDeal() {
        if (travelDeal == null) {
            Toast.makeText(this, getResources().getString(R.string.string_save_before_deleting),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(travelDeal.getId()).removeValue();
        if (travelDeal.getImageName() != null && !travelDeal.getImageName().isEmpty()) {
            StorageReference imageReference =
                    FirebaseUtil.mFirebaseStorage.getReference().child(travelDeal.getImageName());
            imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG_TAG, getResources().getString(R.string.string_image_delete));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
            });
        }
    }

    private void backToList() {
//        startActivity(new Intent(this, TravelDealListActivity.class));
        this.finish();
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get().load(url)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(travelDealImage);
        }
    }
}
