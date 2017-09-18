package com.kahl.silir.getstarted;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.entity.User;
import com.kahl.silir.main.MainActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

public class SignInFragment extends Fragment {
    private View rootView;
    private TextView forgotPasswordTextView;
    private EditText emailField;
    private EditText passwordField;
    private Button signInButton;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        }
    };
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    private Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        Drawable emailIcon = MaterialDrawableBuilder.with(activity)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();
        Drawable passwordIcon = MaterialDrawableBuilder.with(activity)
                .setIcon(MaterialDrawableBuilder.IconValue.KEY)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();

        emailField = (EditText) rootView.findViewById(R.id.email_field);
        emailField.setCompoundDrawables(emailIcon, null, null, null);
        passwordField = (EditText) rootView.findViewById(R.id.password_field);
        passwordField.setCompoundDrawables(passwordIcon, null, null, null);
        signInButton = (Button) rootView.findViewById(R.id.sign_in_button);
        forgotPasswordTextView = (TextView) rootView.findViewById(R.id.forgot_password_text_view);
        signInButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                }
        );
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailField.getText().toString().isEmpty()) {
                    shakeObject(emailField);
                    return;
                }
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.reset_password_dialog);
                dialog.setTitle(R.string.warning_label);
                Button okButton = (Button) dialog.findViewById(R.id.ok_button);
                Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        resetPassword();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return rootView;
    }

    private void resetPassword() {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(getString(R.string.sending_email_label));
        progressDialog.setCancelable(false);
        progressDialog.show();
        auth.sendPasswordResetEmail(emailField.getText()
                .toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(activity, R.string.email_sent_label, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, R.string.email_cant_be_sent_label, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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

    private void signIn() {
        boolean isEnough = true;
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        if (email.isEmpty()) {
            shakeObject(emailField);
            isEnough = false;
        }
        if (password.isEmpty()) {
            shakeObject(passwordField);
            isEnough = false;
        }
        if (!isEnough) return;
        authenticate(email, password);
    }

    private void authenticate(String email, String password) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(getString(R.string.authenticating_label));
        progressDialog.setCancelable(false);
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (auth.getCurrentUser().isEmailVerified()) {
                        databaseReference.child(auth.getCurrentUser().getUid()).child(User.PROFILE_PICTURE_URL)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String profilePicture = dataSnapshot.getValue(String.class);
                                        Intent intent;
                                        if (profilePicture.equals(User.EMPTY_PROFILE_PICTURE)) {
                                            intent = new Intent(activity, PhotoUploadActivity.class);
                                        } else {
                                            intent = new Intent(activity, MainActivity.class);
                                        }
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        activity.finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        activity.finish();
                                    }
                                });
                    } else {
                        auth.signOut();
                        final Dialog dialog = new Dialog(activity);
                        dialog.setTitle(getString(R.string.warning_label));
                        dialog.setContentView(R.layout.email_not_verified_dialog);
                        Button okButton = (Button) dialog.findViewById(R.id.ok_button);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } else {
                    Toast.makeText(activity, R.string.login_failed_label, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void shakeObject(EditText field) {
        YoYo.with(Techniques.Shake).playOn(field);
    }
}
