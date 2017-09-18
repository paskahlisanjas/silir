package com.kahl.silir.main.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.kahl.silir.R;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.getstarted.PhotoUploadActivity;
import com.kahl.silir.main.history.MeasurementResultActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Paskahlis Anjas Prabowo on 26/07/2017.
 */

public class HomeFragment extends Fragment {
    private ImageButton addMeasurement;
    private ImageButton currentMeasurement;
    private ImageButton measurementProfile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.farment_home, container, false);

        addMeasurement = (ImageButton) rootView.findViewById(R.id.add_measurement_button);
        currentMeasurement = (ImageButton) rootView.findViewById(R.id.current_measurement);
        measurementProfile = (ImageButton) rootView.findViewById(R.id.measurement_profiles);

        final Drawable plusButton = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.PLUS_CIRCLE)
                .setSizeDp(130)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .build();
        final Drawable multipleAccountIcon = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE)
                .setSizeDp(40)
                .setColor(Color.WHITE)
                .build();
        final Drawable currentMeasurementIcon = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHART_HISTOGRAM)
                .setSizeDp(40)
                .setColor(Color.WHITE)
                .build();
        addMeasurement.setImageDrawable(plusButton);
        measurementProfile.setImageDrawable(multipleAccountIcon);
        currentMeasurement.setImageDrawable(currentMeasurementIcon);

        measurementProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MeasurementProfilesActivity.class));
            }
        });
        addMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChooseProfileActivity.class));
            }
        });
        currentMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MeasurementResultActivity.class));
            }
        });

        return rootView;
    }
}
