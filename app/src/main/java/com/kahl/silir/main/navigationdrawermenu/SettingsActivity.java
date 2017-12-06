package com.kahl.silir.main.navigationdrawermenu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.entity.User;
import com.kahl.silir.getstarted.PhotoUploadActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    public final static String INTENT_TAG = "settings_activity";

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        }
    };
    private FirebaseUser firebaseUser = auth.getCurrentUser();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(firebaseUser.getUid());

    private TextView nameValue;
    private TextView emailValue;
    private TextView phoneNumberValue;
    private TextView dobValue;
    private TextView genderValue;
    private TextView heightValue;
    private TextView weightValue;

    private Button modifyName;
    private Button modifyEmail;
    private Button modifyPassword;
    private Button modifyProfilePicture;
    private Button modifyPhoneNumber;
    private Button modifyDob;
    private Button modifyGender;
    private Button modifyHeight;
    private Button modifyWeight;

    private ProgressDialog progressDialog;

    private Dialog modifierDialog;
    private Button okButton;
    private Button cancelButton;
    private EditText field1;
    private EditText field2;
    private EditText field3;
    private EditText field4;
    private EditText dobField;

    private Calendar calendar = Calendar.getInstance();

    private Activity activity = this;

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

    private void bindObject() {
        nameValue = (TextView) findViewById(R.id.name_value);
        emailValue = (TextView) findViewById(R.id.email_value);
        emailValue.setText(firebaseUser.getEmail());
        phoneNumberValue = (TextView) findViewById(R.id.phone_number_value);
        dobValue = (TextView) findViewById(R.id.dob_value);
        genderValue = (TextView) findViewById(R.id.gender_value);
        heightValue = (TextView) findViewById(R.id.height_value);
        weightValue = (TextView) findViewById(R.id.weight_value);

        modifyName = (Button) findViewById(R.id.modify_name);
        modifyEmail = (Button) findViewById(R.id.modify_email);
        modifyPassword = (Button) findViewById(R.id.modify_password);
        modifyProfilePicture = (Button) findViewById(R.id.modify_profile_picture);
        modifyPhoneNumber = (Button) findViewById(R.id.modify_phone_number);
        modifyDob = (Button) findViewById(R.id.modify_dob);
        modifyGender = (Button) findViewById(R.id.modify_gender);
        modifyHeight = (Button) findViewById(R.id.modify_height);
        modifyWeight = (Button) findViewById(R.id.modify_weight);

        modifierDialog = new Dialog(this);
        modifierDialog.setContentView(R.layout.common_modifier_dialog);
        dobField = (EditText) modifierDialog.findViewById(R.id.dob_field);
        okButton = (Button) modifierDialog.findViewById(R.id.ok_button);
        cancelButton = (Button) modifierDialog.findViewById(R.id.cancel_button);
        field1 = (EditText) modifierDialog.findViewById(R.id.field1);
        field2 = (EditText) modifierDialog.findViewById(R.id.field2);
        field3 = (EditText) modifierDialog.findViewById(R.id.field3);
        field4 = (EditText) modifierDialog.findViewById(R.id.field4);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait_label));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        bindObject();
        manageOnClickListener();
        progressDialog.show();
    }

    private void clearModifierDialog() {
        field1.setText("");
        field2.setText("");
        field3.setText("");
        field4.setText("");
    }

    private void manageOnClickListener() {
        modifyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }

            private void changeName() {
                modifierDialog.setCancelable(false);
                modifierDialog.setTitle(R.string.modify_name);
                clearModifierDialog();
                show(field1);
                hide(field2);
                hide(field3);
                hide(field4);
                field1.setHint(R.string.hint_new_name);
                field1.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doChangeName();
                    }

                    private void doChangeName() {
                        String newName = field1.getText().toString();
                        if (newName.isEmpty()) {
                            shake(field1);
                            return;
                        }

                        progressDialog.setMessage(getString(R.string.updating_label));
                        progressDialog.setCancelable(false);
                        databaseReference.child(User.NAME).setValue(newName)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        modifierDialog.dismiss();
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.failed_update_user_name,
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.success_update_user_name,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmail();
            }

            private void changeEmail() {
                clearModifierDialog();
                modifierDialog.setCancelable(false);
                modifierDialog.setTitle(R.string.modify_email_address);
                hide(field4);
                show(field1);
                show(field2);
                show(field3);
                field1.setHint(R.string.hint_old_email);
                field2.setHint(R.string.password_label);
                field3.setHint(R.string.hint_new_email);
                field1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                field2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                field3.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doChangeEmail();
                    }

                    private void doChangeEmail() {
                        String oldEmail = field1.getText().toString();
                        String password = field2.getText().toString();
                        final String newEmail = field3.getText().toString();
                        boolean isEnough = true;
                        if (oldEmail.isEmpty()) {
                            shake(field1);
                            isEnough = false;
                        }
                        if (password.isEmpty()) {
                            shake(field2);
                            isEnough = false;
                        }
                        if (newEmail.isEmpty()) {
                            shake(field3);
                            isEnough = false;
                        }
                        if (!isEnough) return;
                        progressDialog.setMessage(getString(R.string.reauthecticating_label));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(oldEmail, password);
                        firebaseUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.setMessage(getString(R.string.updating_email_address_label));
                                            firebaseUser.updateEmail(newEmail)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                progressDialog.setMessage(getString(R.string.sending_verif_email_label));
                                                                firebaseUser.sendEmailVerification()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                emailValue.setText(firebaseUser.getEmail());
                                                                                modifierDialog.dismiss();
                                                                                progressDialog.dismiss();
                                                                                if (!task.isSuccessful()) {
                                                                                    Toast.makeText(getApplicationContext(),
                                                                                            "Email has already been updated, but " +
                                                                                                    "verification email cannot be sent. " +
                                                                                                    "Please change the current email address.",
                                                                                            Toast.LENGTH_LONG).show();
                                                                                } else {
                                                                                    Toast.makeText(getApplicationContext(),
                                                                                            R.string.success_email_update,
                                                                                            Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                progressDialog.dismiss();
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getApplicationContext(),
                                                                        R.string.failed_email_update,
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.failed_reautheticate_label,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }

            private void changePassword() {
                modifierDialog.setTitle(R.string.modify_password);
                modifierDialog.setCancelable(false);
                clearModifierDialog();
                show(field1);
                show(field2);
                show(field3);
                show(field4);
                field1.setHint(R.string.email_label);
                field2.setHint(R.string.hint_old_password);
                field3.setHint(R.string.hint_new_password);
                field4.setHint(R.string.hint_confirm_new_password);
                field1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                field2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                field3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                field4.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doChangePassword();
                    }

                    private void doChangePassword() {
                        String email = field1.getText().toString();
                        String oldPassword = field2.getText().toString();
                        final String newPassword = field3.getText().toString();
                        String confirmPassword = field4.getText().toString();
                        boolean isEnough = true;
                        if (email.isEmpty()) {
                            shake(field1);
                            isEnough = false;
                        }
                        if (oldPassword.isEmpty()) {
                            shake(field2);
                            isEnough = false;
                        }
                        boolean passwordFieldEmpty = false;
                        if (newPassword.isEmpty()) {
                            shake(field3);
                            isEnough = false;
                            passwordFieldEmpty = true;
                        }
                        if (confirmPassword.isEmpty()) {
                            shake(field4);
                            isEnough = false;
                            passwordFieldEmpty = true;
                        }
                        if (!passwordFieldEmpty) {
                            if (!confirmPassword.equals(newPassword)) {
                                shake(field4);
                                field4.setError(getString(R.string.error_confirm_edit_text));
                                isEnough = false;
                            }
                        }
                        if (!isEnough) return;
                        progressDialog.setMessage(getString(R.string.reauthecticating_label));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
                        firebaseUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.setMessage(getString(R.string.updating_password_label));
                                            firebaseUser.updatePassword(newPassword)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressDialog.dismiss();
                                                            modifierDialog.dismiss();
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(),
                                                                        R.string.success_password_update,
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(),
                                                                        R.string.failed_update_password,
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.failed_reautheticate_label),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePicture();
            }

            private void changePicture() {
                Intent intent = new Intent(activity, PhotoUploadActivity.class);
                intent.putExtra(INTENT_TAG, true);
                startActivity(intent);
            }
        });
        modifyPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhoneNumber();
            }

            private void changePhoneNumber() {
                clearModifierDialog();
                hide(field4);
                hide(field3);
                hide(field2);
                show(field1);
                field1.setHint(R.string.hint_new_phone);
                field1.setInputType(InputType.TYPE_CLASS_PHONE);
                modifierDialog.setTitle(R.string.modify_phone);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String number = field1.getText().toString();
                        if (number.isEmpty()) {
                            shake(field1);
                            return;
                        }
                        progressDialog.setMessage(getString(R.string.updating_phone_label));
                        progressDialog.setCancelable(false);
                        databaseReference.child(User.PHONE_NUMBER).setValue(number)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        modifierDialog.dismiss();
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.failed_update_phone_label,
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.success_update_phone_label,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDob();
            }

            private void changeDob() {
                clearModifierDialog();
                modifierDialog.setCancelable(false);
                hide(field1);
                hide(field2);
                hide(field3);
                hide(field4);
                modifierDialog.setTitle(R.string.modify_dob);
                show(dobField);
                dobField.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePicker();
                    }

                    private void showDatePicker() {
                        new DatePickerDialog(activity,
                                datePicker,
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeDob();
                    }

                    private void changeDob() {
                        String dob = dobField.getText().toString();
                        if (dob.isEmpty()) {
                            shake(dobField);
                            return;
                        }
                        progressDialog.setMessage(getString(R.string.updating_dob));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        databaseReference.child(User.DOB).setValue(dob)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        modifierDialog.dismiss();
                                        hide(dobField);
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.failed_update_dob,
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.success_update_dob,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hide(dobField);
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGender();
            }

            private void changeGender() {
                progressDialog.setMessage(getString(R.string.updating_gender));
                if (genderValue.getText().toString().equals("Male")) {
                    databaseReference.child(User.GENDER).setValue("Female")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.failed_update_gender,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.success_update_gender,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    databaseReference.child(User.GENDER).setValue("Male")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.failed_update_gender,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.success_update_gender,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        modifyHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeHeight();
            }

            private void changeHeight() {
                show(field1);
                hide(field2);
                hide(field3);
                hide(field4);
                field1.setHint("Height in cm");
                field1.setInputType(InputType.TYPE_CLASS_NUMBER);
                modifierDialog.setTitle("Modify Height");
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String heightString = field1.getText().toString();
                        if (heightString.isEmpty()) {
                            shake(field1);
                            return;
                        }
                        progressDialog.setMessage("updating height...");
                        progressDialog.setCancelable(false);
                        databaseReference.child(User.HEIGHT).setValue(Integer.parseInt(heightString))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        modifierDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(activity, "Height is successfully updated",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(activity, "Failed to update height",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
        modifyWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeWeight();
            }

            private void changeWeight() {
                show(field1);
                hide(field2);
                hide(field3);
                hide(field4);
                field1.setHint("Weight in kg");
                field1.setInputType(InputType.TYPE_CLASS_NUMBER);
                modifierDialog.setTitle("Modify Weight");
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String weightString = field1.getText().toString();
                        if (weightString.isEmpty()) {
                            shake(field1);
                            return;
                        }
                        progressDialog.setMessage("updating Weight...");
                        progressDialog.setCancelable(false);
                        databaseReference.child(User.WEIGHT).setValue(Integer.parseInt(weightString))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        modifierDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(activity, "Weight is successfully updated",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(activity, "Failed to update weight",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifierDialog.dismiss();
                    }
                });
                modifierDialog.show();
            }
        });
    }

    private void hide(View view) {
        view.setVisibility(View.GONE);
    }

    private void show(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void shake(View view) {
        YoYo.with(Techniques.Shake).playOn(view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nameValue.setText(user.getName());
                phoneNumberValue.setText(user.getPhoneNumber());
                dobValue.setText(user.getDob());
                genderValue.setText(user.getGender());
                heightValue.setText(String.format("%d cm", user.getHeight()));
                weightValue.setText(String.format("%d kg", user.getWeight()));
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Couldn't retrieve data from database server." +
                        "Please try again later.", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
