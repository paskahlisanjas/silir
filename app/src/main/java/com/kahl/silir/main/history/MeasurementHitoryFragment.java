package com.kahl.silir.main.history;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.customstuff.MeasurementHistoryAdapter;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.entity.User;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static final String FROM_HERE = "fromMeasurementHistory";
    public static final String CHOSEN_RESULT = "chosenResult";
    public static final String IDENTIFY_CLOUD = "identifyCloud";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("measurements").child(firebaseAuth.getCurrentUser().getUid());

    @Override
    public void onResume() {
        super.onResume();
        localStorageButton.callOnClick();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

                ResultDbHandler dbHandler = new ResultDbHandler(getActivity());
                if (dbHandler.isDbExist()) {
                    List<MeasurementResult> results = dbHandler.getAllResult();
                    recyclerView.setAdapter(new MeasurementHistoryAdapter(getActivity(), results, false));
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).playOn(recyclerView);
                    YoYo.with(Techniques.FadeOut).playOn(noDataLabel);
                    noDataLabel.setVisibility(View.GONE);
                } else {
                    noDataLabel.setVisibility(View.VISIBLE);
                }
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

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Please wait");
                progressDialog.show();
                final List<MeasurementResult> results = new ArrayList<>();
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            MeasurementResult result = child.getValue(MeasurementResult.class);
                            results.add(result);
                        }
                        progressDialog.dismiss();
                        if (results.size() > 0) {
                            recyclerView.setAdapter(new MeasurementHistoryAdapter(getActivity(), results, true));
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.FadeIn).playOn(recyclerView);
                            YoYo.with(Techniques.FadeOut).playOn(noDataLabel);
                            noDataLabel.setVisibility(View.GONE);
                        } else {
                            YoYo.with(Techniques.FadeOut).playOn(recyclerView);
                            recyclerView.setVisibility(View.GONE);
                            noDataLabel.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.FadeIn).playOn(noDataLabel);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
