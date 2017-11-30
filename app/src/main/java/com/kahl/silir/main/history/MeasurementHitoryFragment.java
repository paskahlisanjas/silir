package com.kahl.silir.main.history;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.kahl.silir.R;
import com.kahl.silir.customstuff.MeasurementHistoryAdapter;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.entity.User;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Date;

/**
 * Created by Paskahlis Anjas Prabowo on 26/07/2017.
 */

public class MeasurementHitoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageButton localStorageButton;
    private ImageButton cloudStorageButton;
    private TextView noDataLabel;
    private TextView storageStatusLabel;
    private Activity activity;
    private ResultDbHandler db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = new ResultDbHandler(activity);
        final int countDb = db.countRecord();
        View rootView = inflater.inflate(R.layout.measurement_history_fragment, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.card_list);
        localStorageButton = (ImageButton) rootView.findViewById(R.id.local_storage_button);
        cloudStorageButton = (ImageButton) rootView.findViewById(R.id.cloud_storage_button);
        noDataLabel = (TextView) rootView.findViewById(R.id.no_data_label);
        storageStatusLabel = (TextView) rootView.findViewById(R.id.storage_status);
        localStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity).setColor(Color.WHITE)
                .setIcon(MaterialDrawableBuilder.IconValue.FOLDER).build());
        localStorageButton.setEnabled(false);
        cloudStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity)
                .setColor(activity.getResources().getColor(R.color.colorPrimary))
                .setIcon(MaterialDrawableBuilder.IconValue.CLOUD).build());
        localStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToLocal();
            }

            private void setToLocal() {
                cloudStorageButton.setEnabled(true);
                localStorageButton.setEnabled(false);
                storageStatusLabel.setText(R.string.local_storage_label);
                cloudStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity)
                        .setColor(activity.getResources().getColor(R.color.colorPrimary))
                        .setIcon(MaterialDrawableBuilder.IconValue.CLOUD).build());
                localStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity).setColor(Color.WHITE)
                        .setIcon(MaterialDrawableBuilder.IconValue.FOLDER).build());

                MeasurementResult[] results = db.getAllMeasurement();

                recyclerView.setAdapter(new MeasurementHistoryAdapter(getActivity(), results));
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).playOn(recyclerView);
                YoYo.with(Techniques.FadeOut).playOn(noDataLabel);
                noDataLabel.setVisibility(View.GONE);

            }
        });
        cloudStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToCloud();
            }

            private void setToCloud() {
                cloudStorageButton.setEnabled(false);
                localStorageButton.setEnabled(true);
                storageStatusLabel.setText(R.string.cloud_storage_label);
                cloudStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity).setColor(Color.WHITE)
                        .setIcon(MaterialDrawableBuilder.IconValue.CLOUD).build());
                localStorageButton.setImageDrawable(MaterialDrawableBuilder.with(activity)
                        .setColor(activity.getResources().getColor(R.color.colorPrimary))
                        .setIcon(MaterialDrawableBuilder.IconValue.FOLDER).build());
                YoYo.with(Techniques.FadeOut).playOn(recyclerView);
                recyclerView.setVisibility(View.GONE);
                noDataLabel.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).playOn(noDataLabel);
            }
        });
        localStorageButton.callOnClick();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem clearAll = menu.add(0, 0, 0, "Clear All");
        clearAll.setIcon(MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.DELETE)
                .setColor(Color.WHITE).build());
        clearAll.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Toast.makeText(getActivity(), "Still being developed.", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
