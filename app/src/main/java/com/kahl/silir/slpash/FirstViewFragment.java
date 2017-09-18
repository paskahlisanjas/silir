package com.kahl.silir.slpash;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.kahl.silir.R;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Paskahlis Anjas Prabowo on 21/07/2017.
 */

public class FirstViewFragment extends Fragment {
    private MaterialIconView image1;
    private MaterialIconView image2;
    private TextView description;
    private View rootView;

    private static boolean secondTime = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        secondTime = true;
        rootView = inflater.inflate(R.layout.first_view_fragment, container, false);
        image1 = (MaterialIconView) rootView.findViewById(R.id.cellphone);
        image2 = (MaterialIconView) rootView.findViewById(R.id.bluetooth);
        description = (TextView) rootView.findViewById(R.id.description);
        animate();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && secondTime) {
            animate();
        }
    }

    private void animate() {
        YoYo.with(Techniques.FadeInRight).playOn(image1);
        YoYo.with(Techniques.FadeInLeft).playOn(image2);
        YoYo.with(Techniques.FadeInUp).playOn(description);
    }
}
