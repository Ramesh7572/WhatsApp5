package com.example.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int galleryPick = 1;
    private StorageReference userProfileImagesReference;
    private ProgressDialog loadingbar;

    private Toolbar SettingsToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        Initializefields();


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UpdateSettings();
            }
        });
        retrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                //Send to gallery
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);

            }
        });

    }
    private void Initializefields()
    {
        UpdateAccountSettings = (Button)findViewById(R.id.update_settings_buttons);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar = new ProgressDialog(this);

        SettingsToolbar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryPick && resultCode == RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.e("crop", "Image cropped");
            //getting cropped image
            if (resultCode == RESULT_OK) {
                loadingbar.setTitle("Setting Profile Image");
                loadingbar.setMessage("Please wait while uploading your image");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
                Uri resultUri = result.getUri();
                //this will put the name of image as currentUserId, hence every new profile pic will replace old one.

                final StorageReference filepath = userProfileImagesReference.child(currentUserId + ".jpg");

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                RootRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Image saved in database successfully.", Toast.LENGTH_SHORT).show();
                                            loadingbar.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                                            loadingbar.dismiss();
                                        }
                                    }
                                });
                            }
                        });


                    }
                });
            }
        }
    }

    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this,"Please enter your username",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this,"Please enter your status",Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);

            RootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this,"Profile Updated Successfully.",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this,message,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo()
    {
         RootRef.child("Users").child(currentUserId)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                     {
                         if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                         {
                             String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                             String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                             String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                             userName.setText(retrieveUserName);
                             userStatus.setText(retrieveUserStatus);
                             //Using implementation 'com.squareup.picasso:picasso:2.71828' to install picasso library
                             Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                         }
                         else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                         {
                             String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                             String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                             userName.setText(retrieveUserName);
                             userStatus.setText(retrieveUserStatus);
                         }
                         else{
                             Toast.makeText(SettingsActivity.this,"Please update your profile...",Toast.LENGTH_SHORT).show();
                         }
                     }
                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        //This will make sure that user can not go back.
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
