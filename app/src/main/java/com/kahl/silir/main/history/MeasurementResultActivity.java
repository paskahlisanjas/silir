package com.kahl.silir.main.history;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.customstuff.MeasurementHistoryAdapter;
import com.kahl.silir.databasehandler.ProfileDbHandler;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.entity.RiemmanIntegrator;
import com.kahl.silir.main.MainActivity;
import com.kahl.silir.main.home.NewMeasurementActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MeasurementResultActivity extends AppCompatActivity {
    private final int MENU_UPLOAD_ID = 1204;
    private final int MENU_DELETE_ID = 1998;
    private final float DELAY = 0.01f;

    private LineChart flowVolumeChart;
    private LineChart volumeTimeChart;
    private LinearLayout mainContainer;
    private TextView name;
    private TextView pef;
    private TextView fev1;
    private TextView fvc;
    private TextView ratioFev1Fvc;
    private TextView ageTextView;
    private ImageView genderIcon;

    private ResultDbHandler dbHandler;

    private final Activity activity = this;

    private MeasurementProfile profile;
    private String keyProfile;
    private ArrayList<Float> flowTimeCurve = new ArrayList<>();

    private MeasurementResult currentResult;

    private boolean isFromHistory;
    private boolean isFromMeasurement;
    private boolean isCloud;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("measurements").child(firebaseAuth.getCurrentUser().getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_result);
        Intent intent = getIntent();

        ageTextView = (TextView) findViewById(R.id.age_status);
        name = (TextView) findViewById(R.id.name_text_view);
        pef = (TextView) findViewById(R.id.pef_value_textview);
        fev1 = (TextView) findViewById(R.id.fev1_value_textview);
        fvc = (TextView) findViewById(R.id.fvc_value_textview);
        ratioFev1Fvc = (TextView) findViewById(R.id.ratio_fev1_fvc_textview);
        genderIcon = (ImageView) findViewById(R.id.gender_status);

        dbHandler = new ResultDbHandler(activity);

        intent.getBooleanExtra((MeasurementHitoryFragment.FROM_HERE), false);

        isFromMeasurement = intent.getBooleanExtra(NewMeasurementActivity.FROM_HERE, false);
        isFromHistory = intent.getBooleanExtra((MeasurementHitoryFragment.FROM_HERE), false);
        isCloud = intent.getBooleanExtra(MeasurementHitoryFragment.IDENTIFY_CLOUD, false);

        Drawable genderFemale = MaterialDrawableBuilder.with(activity)
                .setIcon(MaterialDrawableBuilder.IconValue.GENDER_FEMALE).setSizeDp(15)
                .setColor(getResources().getColor(R.color.colorPrimary)).build();
        Drawable genderMale = MaterialDrawableBuilder.with(activity)
                .setIcon(MaterialDrawableBuilder.IconValue.GENDER_MALE).setSizeDp(15)
                .setColor(getResources().getColor(R.color.colorPrimary)).build();

        if (isFromMeasurement) {
            profile = (MeasurementProfile) intent.getSerializableExtra(NewMeasurementActivity.PROFILE_EXTRA);
            keyProfile = intent.getStringExtra(NewMeasurementActivity.KEY_EXTRA);
            float idleValue = intent.getFloatExtra(NewMeasurementActivity.IDLE_VALUE_EXTRA, 0f);
            flowTimeCurve = (ArrayList<Float>) intent.getSerializableExtra(NewMeasurementActivity.RESULT_EXTRA);

            int index = -1;
            for (float unit : flowTimeCurve)
                flowTimeCurve.set(++index, unit);
            flowTimeCurve.add(0, 0f);
            flowTimeCurve.add(0f);

            List<Float> volumes = RiemmanIntegrator.integrate(flowTimeCurve, DELAY);

            List<Entry> volumeTimeEntries = new ArrayList<>();
            float xValue = 0;
            for (Float yValue : volumes) {
                volumeTimeEntries.add(new Entry(xValue, yValue));
                xValue += DELAY;
            }

            drawVolumeTimeChart(volumeTimeEntries);

            List<Entry> flowVolumeEntries = new ArrayList<>();

            index = -1;
            for (Float volume : volumes) {
                flowVolumeEntries.add(new Entry(volume, flowTimeCurve.get(++index)));
            }

            drawFlowVolumeChart(flowVolumeEntries);

            ProfileDbHandler profileDbHandler = new ProfileDbHandler(activity);
            MeasurementProfile profile = profileDbHandler.getProfile(keyProfile);
            String nameValue = profile.getName();

            Drawable gender = profile.getGender().equals(MeasurementProfile.GENDER_FEMALE) ?
                    genderFemale : genderMale;
            int age = profile.getAge();

            /*set pef, fev1, and fvc*/
            Float pefValue = Collections.max(flowTimeCurve);
            Float fev1Value = volumes.size() >= 100 ? volumes.get(100) : volumes.get(volumes.size() - 1);
            Float fvcValue = volumes.get(volumes.size() - 1);
            Float ratioFev1FvcValue = fev1Value/fvcValue * 100;

            ageTextView.setText(age + "");
            name.setText(nameValue);
            pef.setText(formatNumber(pefValue) + " L/s");
            fev1.setText(formatNumber(fev1Value) + " L");
            fvc.setText(formatNumber(fvcValue) + " L");
            ratioFev1Fvc.setText(formatNumber(ratioFev1FvcValue) + " %");
            genderIcon.setImageDrawable(gender);

            /*store the graph*/
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - hh:mm:ss");
            final String TIME_STAMP = formatter.format(new Date());

            currentResult = new MeasurementResult(fvcValue, fev1Value, pefValue,
                    TIME_STAMP, keyProfile, volumeTimeEntries, flowVolumeEntries);
            dbHandler.addResult(currentResult, TIME_STAMP);

            Toast.makeText(this, "Data has been stored to the local storage.", Toast.LENGTH_SHORT).show();
        } else if (isFromHistory){
            String resultId = intent.getStringExtra(MeasurementHitoryFragment.CHOSEN_RESULT);
            currentResult = dbHandler.getMeasurementResult(resultId);

            ProfileDbHandler profileDbHandler = new ProfileDbHandler(activity);
            MeasurementProfile profile = profileDbHandler.getProfile(currentResult.getProfileId());
            String nameValue = profile.getName();

            Drawable gender = profile.getGender().equals(MeasurementProfile.GENDER_FEMALE) ?
                    genderFemale : genderMale;
            int age = profile.getAge();

            ageTextView.setText(age + "");
            name.setText(nameValue);
            pef.setText(formatNumber(currentResult.getPef()) + " L/s");
            fev1.setText(formatNumber(currentResult.getFev1()) + " L");
            fvc.setText(formatNumber(currentResult.getFvc()) + " L");
            Float ratioFev1FvcValue = currentResult.getFev1()/currentResult.getFvc() * 100;
            ratioFev1Fvc.setText(formatNumber(ratioFev1FvcValue) + " %");
            genderIcon.setImageDrawable(gender);

            drawVolumeTimeChart(currentResult.getVolumeTimeCurve());
            drawFlowVolumeChart(currentResult.getFlowVolumeCurve());
        } else {
            if (!dbHandler.isDbExist()) {
                Log.d("SILIR", "database doesnt exist");
                findViewById(R.id.no_data_label).setVisibility(View.VISIBLE);
                findViewById(R.id.main_container).setVisibility(View.GONE);
            } else {
                /*data has already been available, retrieve from local database*/
                currentResult = dbHandler.getCurrentMeasurement();
                ProfileDbHandler profileDbHandler = new ProfileDbHandler(activity);
                MeasurementProfile profile = profileDbHandler.getProfile(currentResult.getProfileId());
                String nameValue = profile.getName();

                Drawable gender = profile.getGender().equals(MeasurementProfile.GENDER_FEMALE) ?
                        genderFemale : genderMale;
                int age = profile.getAge();

                ageTextView.setText(age + "");
                name.setText(nameValue);
                pef.setText(currentResult.getPef() + " L/s");
                fev1.setText(currentResult.getFev1() + " L");
                fvc.setText(currentResult.getFvc() + " L");
                Float ratioFev1FvcValue = currentResult.getFev1()/currentResult.getFvc() * 100;
                ratioFev1Fvc.setText(formatNumber(ratioFev1FvcValue) + " %");
                genderIcon.setImageDrawable(gender);

                drawFlowVolumeChart(currentResult.getFlowVolumeCurve());
                drawVolumeTimeChart(currentResult.getVolumeTimeCurve());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem uploadItem = menu.add(0, MENU_UPLOAD_ID, 0, "Upload to cloud");
        uploadItem.setIcon(MaterialDrawableBuilder.with(this).setColor(Color.WHITE)
                .setIcon(MaterialDrawableBuilder.IconValue.CLOUD_UPLOAD).build());
        uploadItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem deleteItem = menu.add(0, MENU_DELETE_ID, 1, "Delete");
        deleteItem.setIcon(MaterialDrawableBuilder.with(this).setColor(Color.WHITE)
                .setIcon(MaterialDrawableBuilder.IconValue.DELETE).build());
        deleteItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (findViewById(R.id.no_data_label).getVisibility() == View.VISIBLE
                && item.getItemId() != android.R.id.home) {
            Toast.makeText(this, "There\'s no data", Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case MENU_DELETE_ID:
                if (isCloud) {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                MeasurementResult result = child.getValue(MeasurementResult.class);
                                if(result.getTime().equals(currentResult.getTime())) {
                                    child.getRef().removeValue();
                                    Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    return true;
                }

                dbHandler.deleteResult(currentResult.getTime());
                if (isFromHistory) {
                    onBackPressed();
                } else {
                    Intent intent;
                    intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;
            case MENU_UPLOAD_ID:
                if (isCloud) {
                    Toast.makeText(activity, "Already in cloud", Toast.LENGTH_SHORT).show();
                    return true;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy|hh:mm:ss");
                databaseReference.child(sdf.format(new Date())).setValue(currentResult)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(activity, "Stored to cloud", Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
        }

        return false;
    }

    private void drawVolumeTimeChart(List<Entry> volumeTimeEntries) {
        volumeTimeChart = (LineChart) findViewById(R.id.volume_time_curve);
        volumeTimeChart.getDescription().setEnabled(false);
        volumeTimeChart.setVisibleXRangeMinimum(2);
        volumeTimeChart.getLegend().setEnabled(false);

        XAxis xAxisVT = volumeTimeChart.getXAxis();
        xAxisVT.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisVT.setAxisMinimum(0f);
        xAxisVT.setAxisMaximum(2f);
        xAxisVT.setDrawGridLines(false);

        YAxis rightAxisVT = volumeTimeChart.getAxisRight();
        rightAxisVT.setEnabled(false);
        rightAxisVT.setDrawGridLines(false);

        LineDataSet volumeTimeDataSet = new LineDataSet(volumeTimeEntries, "");
        volumeTimeDataSet.setDrawCircles(false);
        volumeTimeDataSet.setDrawValues(false);
        volumeTimeDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        LineData volumeTimeLineData = new LineData(volumeTimeDataSet);
        volumeTimeChart.setData(volumeTimeLineData);
    }

    private void drawFlowVolumeChart(List<Entry> flowVolumeEntries) {
        flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
        flowVolumeChart.getDescription().setEnabled(false);
        flowVolumeChart.getLegend().setEnabled(false);

        XAxis xAxisFV = flowVolumeChart.getXAxis();
        xAxisFV.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisFV.setDrawGridLines(false);

        YAxis rightAxisFV = flowVolumeChart.getAxisRight();
        rightAxisFV.setEnabled(false);
        rightAxisFV.setDrawGridLines(false);

        LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
        flowVolumeDataSet.setDrawCircles(false);
        flowVolumeDataSet.setDrawValues(false);
        flowVolumeDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
        flowVolumeChart.setData(flowVolumeLineData);
    }

    private String formatNumber(float f) {
        return String.format("%.2f", f);
    }
}
