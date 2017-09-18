package com.kahl.silir.getstarted;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kahl.silir.R;
import com.kahl.silir.main.MainActivity;
import com.kahl.silir.main.navigationdrawermenu.SettingsActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

public class PhotoUploadActivity extends AppCompatActivity {
    private final int REQUEST_LOAD_IMG = 1204;
    public static final String LOCAL_FILENAME = "profile_picture.png";
    private boolean isFromSettingActivity;

    private CropImageView cropImageView;
    private MaterialIconView dummyImage;
    private ImageButton uploadButton;
    private ImageButton skipButton;
    private ImageButton agreeButton;
    private Button saveButton;
    private TextView takeItTextView;
    private TextView uploadTextView;
    private TextView skipTextView;
    private ImageView croppedImage;
    private LinearLayout cropperContainer;

    private Bitmap profilePhoto;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        bindObject();

        manageOnClickListener();
    }

    private void uploadToStorage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setProgressPercentFormat(null);
        progressDialog.setMessage(getString(R.string.compressing_label));
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                profilePhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                final byte[] convertedByte = stream.toByteArray();
                saveLocally(convertedByte);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgressPercentFormat(NumberFormat.getPercentInstance());
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMessage(getString(R.string.uploading_label));
                        progressDialog.setProgress(0);
                        progressDialog.setMax(convertedByte.length);
                        storageReference.child(firebaseUser.getUid() + ".png").putBytes(convertedByte)
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        //noinspection VisibleForTests
                                        progressDialog.setProgress((int) taskSnapshot.getBytesTransferred());
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.pp_updated,
                                                Toast.LENGTH_SHORT).show();
                                        //noinspection VisibleForTests
                                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                                        databaseReference.setValue(downloadUri.toString());
                                        progressDialog.dismiss();
                                        skipButton.callOnClick();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                R.string.failed_upload_pp_label, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }).start();

    }

    private void bindObject() {
        isFromSettingActivity = getIntent().getBooleanExtra(SettingsActivity.INTENT_TAG, false);

        storageReference = FirebaseStorage.getInstance().getReference().child("profilePictures");
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
        firebaseUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(firebaseUser.getUid()).child("profilePictureUri");

        uploadButton = (ImageButton) findViewById(R.id.upload_button);
        skipButton = (ImageButton) findViewById(R.id.skip_button);
        agreeButton = (ImageButton) findViewById(R.id.agree_button);
        takeItTextView = (TextView) findViewById(R.id.take_it_textview);
        uploadTextView = (TextView) findViewById(R.id.upload_textview);
        skipTextView = (TextView) findViewById(R.id.skip_text_view);
        dummyImage = (MaterialIconView) findViewById(R.id.dummy_image);
        cropImageView = (CropImageView) findViewById(R.id.cropper_view);
        croppedImage = (ImageView) findViewById(R.id.cropped_image);
        saveButton = (Button) findViewById(R.id.save_button);
        cropperContainer = (LinearLayout) findViewById(R.id.cropper_container);

        Drawable uploadIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.FOLDER_UPLOAD).setSizeDp(50)
                .setColor(getResources().getColor(R.color.white)).build();
        Drawable checkIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK_CIRCLE).setSizeDp(70)
                .setColor(getResources().getColor(R.color.white)).build();
        Drawable skipIcon;
        if (isFromSettingActivity) {
            skipIcon = MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.CLOSE_CIRCLE).setSizeDp(50)
                    .setColor(getResources().getColor(R.color.white)).build();
            skipTextView.setText(R.string.cancel_label);
        } else {
            skipIcon = MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.ARROW_RIGHT_BOLD_CIRCLE).setSizeDp(50)
                    .setColor(getResources().getColor(R.color.white)).build();
        }

        uploadButton.setImageDrawable(uploadIcon);
        agreeButton.setImageDrawable(checkIcon);
        skipButton.setImageDrawable(skipIcon);
    }

    private void manageOnClickListener() {
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFromGallery();
            }

            private void chooseFromGallery() {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, REQUEST_LOAD_IMG);
            }
        });
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToStorage();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCroppedImage();
            }

            private void saveCroppedImage() {
                profilePhoto = CropImage.toOvalBitmap(cropImageView.getCroppedImage());
                croppedImage.setImageBitmap(profilePhoto);
                cropperContainer.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                uploadTextView.setText(R.string.choose_another_label);
                croppedImage.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).playOn(saveButton);
                agreeButton.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.BounceIn).playOn(agreeButton);
                takeItTextView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).playOn(agreeButton);
            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipActivity();
            }

            private void skipActivity() {
                if (isFromSettingActivity) {
                    PhotoUploadActivity.super.onBackPressed();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void saveLocally(byte[] bytes) {
        try {
            FileOutputStream fos = openFileOutput(LOCAL_FILENAME, MODE_PRIVATE);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("SILIR", "file not found.");
        } catch (IOException e) {
            Log.d("SILIR", "something went wrong.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_LOAD_IMG:
                    Uri imageUri = data.getData();
                    dummyImage.setVisibility(View.GONE);
                    croppedImage.setVisibility(View.GONE);
                    agreeButton.setVisibility(View.GONE);
                    takeItTextView.setVisibility(View.GONE);
                    cropImageView.setImageUriAsync(imageUri);
                    cropperContainer.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            Toast.makeText(this, R.string.havent_pick_image_label, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        skipButton.callOnClick();
    }
}
