package com.kahl.silir.slpash;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.kahl.silir.R;

/**
 * Created by Paskahlis Anjas Prabowo on 21/07/2017.
 */

public class LastViewFragment extends Fragment {
    private View main;
    private ImageView second;
    private TextView third;
    private View rootView;

    private static boolean secondTime = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        secondTime = true;
        rootView = inflater.inflate(R.layout.last_view_fragment, container, false);
        second = (ImageView) rootView.findViewById(R.id.second_appear);
        third = (TextView) rootView.findViewById(R.id.third_appear);
        main = rootView.findViewById(R.id.main_lastview);
        YoYo.with(Techniques.FadeOut).duration(10).playOn(second);
        YoYo.with(Techniques.FadeOut).duration(10).playOn(third);
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && secondTime) {
            YoYo.with(Techniques.FadeInRight).playOn(second);
            YoYo.with(Techniques.FadeInLeft).playOn(third);
        }
    }
}
