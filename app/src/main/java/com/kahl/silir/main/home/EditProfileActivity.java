package com.kahl.silir.main.home;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.databasehandler.ProfileDbHandler;
import com.kahl.silir.entity.MeasurementProfile;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {
    private EditText nameField;
    private EditText dobField;
    private EditText genderField;
    private EditText heightField;
    private EditText weightField;
    private TextView keyTextView;
    private String key;
    private final Activity activity = this;

    private final int MENU_SAVE_ID = 1204;
    private final int MENU_DELETE_ID = 498;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("profiles").child(firebaseAuth.getCurrentUser().getUid());

    private Calendar calendar = Calendar.getInstance();

    private ProfileDbHandler dbHandler;

    private DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String format = "dd/MM/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);

            dobField.setText(dateFormat.format(calendar.getTime()));
        }
    };


    private Drawable createIcon(MaterialDrawableBuilder.IconValue iconValue,
                                @ColorRes int colorId, int sizeDp) {
        return MaterialDrawableBuilder.with(this)
                .setIcon(iconValue)
                .setColor(getResources().getColor(colorId))
                .setSizeDp(sizeDp)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        dbHandler = new ProfileDbHandler(activity);

        MeasurementProfile profile = (MeasurementProfile) getIntent()
                .getSerializableExtra(MeasurementProfilesActivity.PROFILE_EXTRA);
        key = getIntent().getStringExtra(MeasurementProfilesActivity.KEY_EXTRA);

        nameField = (EditText) findViewById(R.id.name_field);
        nameField.setText(profile.getName());
        dobField = (EditText) findViewById(R.id.dob_field);
        dobField.setText(profile.getDob());
        genderField = (EditText) findViewById(R.id.gender_field);
        genderField.setText(profile.getGender());
        heightField = (EditText) findViewById(R.id.height_field);
        heightField.setText(profile.getHeight() + "");
        weightField = (EditText) findViewById(R.id.weight_field);
        weightField.setText(profile.getWeight() + "");
        keyTextView = (TextView) findViewById(R.id.time_stamp);
        keyTextView.setText(key);

        Drawable accountIcon = createIcon(MaterialDrawableBuilder.IconValue.ACCOUNT,
                R.color.colorPrimary, 25);
        Drawable dobIcon = createIcon(MaterialDrawableBuilder.IconValue.CALENDAR,
                R.color.colorPrimary, 25);
        Drawable genderIcon = createIcon(MaterialDrawableBuilder.IconValue.HUMAN_MALE_FEMALE,
                R.color.colorPrimary, 25);
        Drawable heightIcon = createIcon(MaterialDrawableBuilder.IconValue.RULER,
                R.color.colorPrimary, 25);
        Drawable weightIcon = createIcon(MaterialDrawableBuilder.IconValue.WEIGHT_KILOGRAM,
                R.color.colorPrimary, 25);

        nameField.setCompoundDrawables(accountIcon, null, null, null);
        dobField.setCompoundDrawables(dobIcon, null, null, null);
        genderField.setCompoundDrawables(genderIcon, null, null, null);
        heightField.setCompoundDrawables(heightIcon, null, null, null);
        weightField.setCompoundDrawables(weightIcon, null, null, null);

        dobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity,
                        datePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        genderField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.gender_picker);
                dialog.setTitle("Choose one");
                Drawable maleIcon = createIcon(MaterialDrawableBuilder.IconValue.HUMAN_MALE,
                        R.color.white, 25);
                Drawable femaleIcon = createIcon(MaterialDrawableBuilder.IconValue.HUMAN_FEMALE,
                        R.color.white, 25);
                Button maleButton = (Button) dialog.findViewById(R.id.male_button);
                maleButton.setCompoundDrawables(maleIcon, null, null, null);
                Button femaleButton = (Button) dialog.findViewById(R.id.female_button);
                femaleButton.setCompoundDrawables(femaleIcon, null, null, null);
                maleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        genderField.setText("Male");
                        dialog.dismiss();
                    }
                });
                femaleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        genderField.setText("Female");
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.add(0, MENU_SAVE_ID, 0, "Save");
        saveItem.setIcon(MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.CONTENT_SAVE)
                .setColor(Color.WHITE).build());
        saveItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem deleteItem = menu.add(0, MENU_DELETE_ID, 1, "Delete");
        deleteItem.setIcon(MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                .setColor(Color.WHITE).build());
        deleteItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    private boolean isFieldEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            YoYo.with(Techniques.Shake).playOn(editText);
            return true;
        }
        return false;
    }

    private boolean isClear() {
        boolean isEnough = isFieldEmpty(nameField);
        isEnough = isFieldEmpty(dobField);
        isEnough = isFieldEmpty(genderField);
        isEnough = isFieldEmpty(genderField);
        isEnough = isFieldEmpty(heightField);
        isEnough = isFieldEmpty(weightField);
        return !isEnough;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case MENU_SAVE_ID:
                if (isClear()) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("updating profile...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String name = nameField.getText().toString();
                    String dob = dobField.getText().toString();
                    String gender = genderField.getText().toString();
                    int height = Integer.parseInt(heightField.getText().toString());
                    int weight = Integer.parseInt(weightField.getText().toString());
                    final MeasurementProfile updatedProfile = new MeasurementProfile(name, dob, gender, height, weight);
                    databaseReference.child(key)
                            .setValue(updatedProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                dbHandler.updateProfile(updatedProfile, key);
                                Toast.makeText(activity,
                                        "Profile has been updated.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(activity, MeasurementProfilesActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Toast.makeText(activity,
                                        "Failed to update profile, please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            case MENU_DELETE_ID:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.signout_warning_dialog);
                TextView warningText = (TextView) dialog.findViewById(R.id.warning_text);
                Button okButton = (Button) dialog.findViewById(R.id.ok_button);
                Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
                dialog.setTitle("Warning");
                warningText.setText("Are you sure want to delete this profile?");
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearProfile();
                    }

                    private void clearProfile() {
                        final ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setMessage("deleting...");
                        progressDialog.setCancelable(false);
                        databaseReference
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot target : dataSnapshot.getChildren()) {
                                            if (target.getKey().equals(key)) {
                                                target.getRef().removeValue();
                                                dbHandler.deleteProfile(key);
                                                progressDialog.dismiss();
                                                dialog.dismiss();
                                                Toast.makeText(activity,
                                                        "Profile has been deleted.",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(activity, MeasurementProfilesActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                        Toast.makeText(activity,
                                                "Failed to delete profile.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
        }
        return true;
    }
}
