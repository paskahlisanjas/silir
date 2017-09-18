package com.kahl.silir.getstarted;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kahl.silir.R;
import com.kahl.silir.entity.User;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SignUpFragment extends Fragment {
    private View rootView;
    private EditText nameField;
    private EditText phoneNumberField;
    private EditText emailField;
    private EditText passwordField;
    private EditText passwordConfirmField;
    private Button signUpButton;
    private EditText dobField;
    private EditText genderField;
    private EditText heightField;
    private EditText weightField;
    private ProgressDialog progressDialog;

    private Calendar calendar = Calendar.getInstance();

    private User user;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        }
    };
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

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

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    private Drawable createIcon(MaterialDrawableBuilder.IconValue iconValue,
                                @ColorRes int colorId, int sizeDp) {
        return MaterialDrawableBuilder.with(getActivity())
                .setIcon(iconValue)
                .setColor(getResources().getColor(colorId))
                .setSizeDp(sizeDp)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        nameField = (EditText) rootView.findViewById(R.id.name_field);
        dobField = (EditText) rootView.findViewById(R.id.dob_field);
        genderField = (EditText) rootView.findViewById(R.id.gender_field);
        heightField = (EditText) rootView.findViewById(R.id.height_field);
        heightField.setNextFocusDownId(R.id.weight_field);
        weightField = (EditText) rootView.findViewById(R.id.weight_field);
        phoneNumberField = (EditText) rootView.findViewById(R.id.phone_number_field);
        emailField = (EditText) rootView.findViewById(R.id.email_field);
        emailField.setNextFocusDownId(R.id.password_field);
        passwordField = (EditText) rootView.findViewById(R.id.password_field);
        passwordField.setNextFocusDownId(R.id.password_confirm_field);
        passwordConfirmField = (EditText) rootView.findViewById(R.id.password_confirm_field);

        Drawable accountIcon = createIcon(MaterialDrawableBuilder.IconValue.ACCOUNT,
                R.color.colorPrimary, 25);
        Drawable phoneIcon = createIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID,
                R.color.colorPrimary, 25);
        Drawable emailIcon = createIcon(MaterialDrawableBuilder.IconValue.EMAIL,
                R.color.colorPrimary, 25);
        Drawable passwordIcon = createIcon(MaterialDrawableBuilder.IconValue.KEY,
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
        phoneNumberField.setCompoundDrawables(phoneIcon, null, null, null);
        emailField.setCompoundDrawables(emailIcon, null, null, null);
        passwordField.setCompoundDrawables(passwordIcon, null, null, null);
        passwordConfirmField.setCompoundDrawables(passwordIcon, null, null, null);

        signUpButton = (Button) rootView.findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        dobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(),
                        datePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        genderField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
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

        return rootView;
    }

    private void shakeView(View field) {
        YoYo.with(Techniques.Shake).playOn(field);
    }

    private void signUp() {
        boolean isEnough = true;
        String name = nameField.getText().toString();
        String dob = dobField.getText().toString();
        String gender = genderField.getText().toString();
        String height = heightField.getText().toString();
        String weight = weightField.getText().toString();
        String phoneNumber = phoneNumberField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        if (name.isEmpty()) {
            shakeView(nameField);
            isEnough = false;
        }
        if (dob.isEmpty()) {
            shakeView(dobField);
            isEnough = false;
        }
        if (gender.isEmpty()) {
            shakeView(genderField);
            isEnough = false;
        }
        if (height.isEmpty()) {
            shakeView(heightField);
            isEnough = false;
        }
        if (weight.isEmpty()) {
            shakeView(weightField);
            isEnough = false;
        }
        if (phoneNumber.isEmpty()) {
            shakeView(phoneNumberField);
            isEnough = false;
        }
        if (email.isEmpty()) {
            shakeView(emailField);
            isEnough = false;
        }
        boolean isPasswordEmpty = false;
        if (password.isEmpty()) {
            shakeView(passwordField);
            isEnough = false;
            isPasswordEmpty = true;
        }
        if (passwordConfirmField.getText().toString().isEmpty()) {
            shakeView(passwordConfirmField);
            isEnough = false;
            isPasswordEmpty = true;
        }
        if (!password.equals(passwordConfirmField.getText().toString())
                && !isPasswordEmpty) {
            shakeView(passwordField);
            shakeView(passwordConfirmField);
            passwordConfirmField.setError(getString(R.string.error_confirm_edit_text));
            isEnough = false;
        }
        if (!isEnough) return;
        user = new User(name, dob, gender, phoneNumber, Integer.parseInt(height),
                Integer.parseInt(weight), User.EMPTY_PROFILE_PICTURE);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.processing_label));
        progressDialog.show();
        progressDialog.setCancelable(false);
        registerUser(email, password);
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(),
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.setMessage(getString(R.string.sending_verif_email_label));
                                    sendVerification();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), R.string.registration_failed_label,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

    private void clearField() {
        nameField.setText("");
        phoneNumberField.setText("");
        emailField.setText("");
        passwordField.setText("");
        heightField.setText("");
        weightField.setText("");
        passwordConfirmField.setText("");
        dobField.setText("");
        genderField.setText("");
    }

    private void sendVerification() {
        final FirebaseUser firebaseUser = auth.getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.setMessage(getString(R.string.storing_data_label));
                    databaseReference.child(firebaseUser.getUid()).setValue(user);
                    progressDialog.dismiss();
                    auth.signOut();
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.setTitle(R.string.success_label);
                    dialog.setContentView(R.layout.registration_success_dialog);
                    dialog.setCancelable(false);
                    Button okButton = (Button) dialog.findViewById(R.id.ok_button);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    clearField();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),
                            R.string.cant_send_email_label,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
