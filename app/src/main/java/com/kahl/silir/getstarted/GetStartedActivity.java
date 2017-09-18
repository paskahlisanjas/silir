package com.kahl.silir.getstarted;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.kahl.silir.R;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

public class GetStartedActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private boolean backPressedTwice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Drawable registerIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_PLUS)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(25)
                .build();
        Drawable loginIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CHECK)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(25)
                .build();

        tabLayout.getTabAt(0).setIcon(registerIcon);
        tabLayout.getTabAt(1).setIcon(loginIcon);
    }


    @Override
    public void onBackPressed() {
        if (backPressedTwice) {
            finish();
            System.exit(0);
        } else {
            backPressedTwice = true;
            Toast.makeText(this, "Touch back again to exit.", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            backPressedTwice = false;
                        }
                    }, 3000);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SignUpFragment();
                case 1:
                    return new SignInFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Resources resources = getResources();
            switch (position) {
                case 0:
                    return resources.getString(R.string.sign_up_tab_title);
                case 1:
                    return resources.getString(R.string.sign_in_tab_title);
            }
            return null;
        }
    }
}
