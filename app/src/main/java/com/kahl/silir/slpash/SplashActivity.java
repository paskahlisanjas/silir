package com.kahl.silir.slpash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.kahl.silir.R;
import com.kahl.silir.getstarted.GetStartedActivity;

/**
 * Created by Paskahlis Anjas Prabowo on 21/07/2017.
 */

public class SplashActivity extends AppIntro {

    private boolean backPressedTwice = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        addSlide(new FirstViewFragment());
        addSlide(new SecondViewFragment());
        addSlide(new ThirdViewFragment());
        addSlide(new LastViewFragment());

        setDoneText(getResources().getString(R.string.get_staterted_button));
        showSeparator(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(this, GetStartedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        super.onDonePressed();
        Intent intent = new Intent(this, GetStartedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
}
