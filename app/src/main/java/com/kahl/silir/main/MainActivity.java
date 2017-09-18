package com.kahl.silir.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.customstuff.NavigationDrawerListAdapter;
import com.kahl.silir.databasehandler.ProfileDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.User;
import com.kahl.silir.getstarted.GetStartedActivity;
import com.kahl.silir.getstarted.PhotoUploadActivity;
import com.kahl.silir.main.history.MeasurementHitoryFragment;
import com.kahl.silir.main.home.HomeFragment;
import com.kahl.silir.main.navigationdrawermenu.HelpActivity;
import com.kahl.silir.main.navigationdrawermenu.SettingsActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerMenuList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private ImageView profilePicture;
    private TextView userName;
    private TextView userEmail;
    private Button signOutButton;
    private BottomNavigationView navigation;

    private final String TAG_FRAGMENT = "ft_main";

    private boolean backPressedTwice = false;

    private final Activity activity = this;

    private ProfileDbHandler dbHandler;

    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        }
    };
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference().child("users")
            .child(auth.getCurrentUser().getUid());
    public static FirebaseUser user = auth.getCurrentUser();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.content_main_activity, new HomeFragment(), TAG_FRAGMENT);
                    transaction.commit();
                    return true;
                case R.id.navigation_measurement_history:
                    transaction.replace(R.id.content_main_activity, new MeasurementHitoryFragment(),
                            TAG_FRAGMENT);
                    transaction.commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setUpNavigationDrawer();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);

        dbHandler = new ProfileDbHandler(activity);
        if (!dbHandler.isDbExist()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User instance = dataSnapshot.getValue(User.class);
                    dbHandler.addProfile(instance, User.KEY_IN_LOCAL_DB);
                    userName.setText(instance.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(activity, "Failed to retrieve data from database.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            MeasurementProfile userProfile = dbHandler.getProfile(User.KEY_IN_LOCAL_DB);
            userName.setText(userProfile.getName());
        }
//        runTutorial();
    }

    private void runTutorial() {
        TapTargetView.showFor(this,
                TapTarget.forView(navigation, "This is a bottom navigation bar", "You can switch between to screen: home and measurement history")
                        .outerCircleColor(R.color.pink)
                        .outerCircleAlpha(0.70f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(25)
                        .textColor(R.color.white)
                        .descriptionTextSize(15)
                        .descriptionTextColor(R.color.softBrightBlue)
                        .textColor(R.color.white)
                        .textTypeface(Typeface.DEFAULT)
                        .dimColorInt(Color.BLACK)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .targetRadius(120));
        new TapTargetSequence(this).targets(
                TapTarget.forToolbarNavigationIcon(toolbar, "You can know more from here."),
                TapTarget.forView(findViewById(R.id.navigation_measurement_history), "Measurement history will be stored here."))
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Toast.makeText(getApplicationContext(), "Got " + lastTarget.toString() + " step",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authListener);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedTwice) {
                finish();
                System.exit(0);
            } else {
                backPressedTwice = true;
                Toast.makeText(this, R.string.exit_warning_label, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                backPressedTwice = false;
                            }
                        }, 3000);
            }
        }
    }

    private void setUpNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        drawerMenuList = (ListView) findViewById(R.id.navigation_drawer_list);
        signOutButton = (Button) findViewById(R.id.signout_button);
        userName = (TextView) findViewById(R.id.account_name);
        userEmail = (TextView) findViewById(R.id.account_email);

        userEmail.setText(auth.getCurrentUser().getEmail());

        Drawable deviceIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.LIBRARY_BOOKS)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();
        Drawable helpIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.HELP_CIRCLE)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();
        Drawable settingsIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.SETTINGS)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();
        Drawable aboutIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.INFORMATION)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSizeDp(25)
                .build();

        Drawable[] icons = {deviceIcon, settingsIcon, helpIcon, aboutIcon};
        String[] titles = {
                getString(R.string.header_menu_instruction),
                getString(R.string.header_menu_settings),
                getString(R.string.header_menu_help),
                getString(R.string.header_menu_about)
        };
        drawerMenuList.setAdapter(new NavigationDrawerListAdapter(this, icons, titles));
        drawerMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runCommandOn(position);
            }

            private void runCommandOn(int position) {
                switch (position) {
                    case 0: //Set Up Device
                        Toast.makeText(activity, "Still being developed.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: //settings
                        startActivity(new Intent(activity, SettingsActivity.class));
                        break;
                    case 2: //Help
                        startActivity(new Intent(activity, HelpActivity.class));
                        break;
                    case 3: //About
                        Toast.makeText(activity, "Still being developed.", Toast.LENGTH_SHORT).show();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarDrawerToggle.syncState();

        Drawable signOutIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.LOGOUT)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(20)
                .build();

        signOutButton.setCompoundDrawables(null, null, signOutIcon, null);
        signOutButton.setCompoundDrawablePadding(16);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.setTitle(getString(R.string.warning_label));
                dialog.setContentView(R.layout.signout_warning_dialog);
                Button okButton = (Button) dialog.findViewById(R.id.ok_button);
                Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage(getString(R.string.please_wait_label));
                        progressDialog.show();
                        auth.signOut();
                        Intent intent = new Intent(activity, GetStartedActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
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

        setUpHeaderNavigationDrawer();
    }

    private void setUpHeaderNavigationDrawer() {
        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        final Drawable accountIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(80)
                .build();
        profilePicture.setImageDrawable(accountIcon);
        databaseReference.child("profilePictureUri").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profilePictureUri = dataSnapshot.getValue(String.class);
                if (!profilePictureUri.equals(User.EMPTY_PROFILE_PICTURE)) {
                    try {
                        FileInputStream fis = openFileInput(PhotoUploadActivity.LOCAL_FILENAME);
                        byte[] bytes = new byte[(int) fis.getChannel().size()];
                        fis.read(bytes);
                        fis.close();
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profilePicture.setImageBitmap(image);
                        profilePicture.setBackgroundResource(R.drawable.white_circle);
                    } catch (FileNotFoundException e) {
                        Log.d("SILIR", "HomeFragment : file not found.");
                    } catch (IOException e) {
                        Log.d("SILIR", "something goes wrong.");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Failed to fetch photo from cloud storage.", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

}
