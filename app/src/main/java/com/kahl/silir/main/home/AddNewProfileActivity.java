package com.kahl.silir.main.home;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.kahl.silir.entity.User;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("profiles").child(firebaseAuth.getCurrentUser().getUid());

    private Calendar calendar = Calendar.getInstance();

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

    private EditText nameField;
    private EditText dobField;
    private EditText genderField;
    private EditText heightField;
    private EditText weightField;
    private Button saveButton;

    private boolean isFromProfileChooser = false;

    private ProfileDbHandler dbHandler;

    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_profile);

        dbHandler = new ProfileDbHandler(activity);

        isFromProfileChooser = getIntent().getBooleanExtra(ChooseProfileActivity.FROM_HERE, false);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nameField = (EditText) findViewById(R.id.name_field);
        dobField = (EditText) findViewById(R.id.dob_field);
        genderField = (EditText) findViewById(R.id.gender_field);
        heightField = (EditText) findViewById(R.id.height_field);
        weightField = (EditText) findViewById(R.id.weight_field);
        saveButton = (Button) findViewById(R.id.save_button);

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameField.getText().toString();
                String dob = dobField.getText().toString();
                String gender = genderField.getText().toString();
                String height = heightField.getText().toString();
                String weight = weightField.getText().toString();
                boolean isEnough = true;
                isEnough = check(nameField);
                isEnough = check(dobField);
                isEnough = check(genderField);
                isEnough = check(heightField);
                isEnough = check(weightField);
                if (!isEnough) return;
                final MeasurementProfile profile = new MeasurementProfile(name, dob, gender,
                        Integer.parseInt(height), Integer.parseInt(weight));
                final ProgressDialog progressDialog = new ProgressDialog(activity);
                final String key = Calendar.getInstance().getTime().toString();
                progressDialog.setMessage("Saving profile...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                databaseReference.child(key).setValue(profile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    dbHandler.addProfile(profile, key);
                                    Toast.makeText(activity,
                                            "Profile has been saved.", Toast.LENGTH_SHORT)
                                            .show();
                                    Intent intent;
                                    if (isFromProfileChooser) {
                                        intent = new Intent(activity, NewMeasurementActivity.class);
                                        intent.putExtra(ChooseProfileActivity.CHOSEN_PROFILE, profile);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    } else {
                                        intent = new Intent(activity, MeasurementProfilesActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    }
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(activity,
                                            "Failed to save profile, please try again.", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
            }

            private boolean check(EditText editText) {
                if (nameField.getText().toString().isEmpty()) {
                    shake(editText);
                    return false;
                }
                return true;
            }

            private void shake(EditText editText) {
                YoYo.with(Techniques.Shake).playOn(editText);
            }
        });
    }

    private Drawable createIcon(MaterialDrawableBuilder.IconValue iconValue,
                                @ColorRes int colorId, int sizeDp) {
        return MaterialDrawableBuilder.with(this)
                .setIcon(iconValue)
                .setColor(getResources().getColor(colorId))
                .setSizeDp(sizeDp)
                .build();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
