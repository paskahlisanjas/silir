package com.kahl.silir.main.history;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kahl.silir.R;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.RiemmanIntegrator;
import com.kahl.silir.main.home.NewMeasurementActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

public class MeasurementResultActivity extends AppCompatActivity {
    private final int MENU_UPLOAD_ID = 1204;
    private final int MENU_DELETE_ID = 1998;
    private final float DELAY = 0.1f;

    private LineChart flowVolumeChart;
    private LineChart volumeTimeChart;
    private LinearLayout mainContainer;

    private ResultDbHandler dbHandler;

    private final Activity activity = this;

    private MeasurementProfile profile;
    private String keyProfile;
    private ArrayList<Float> flowTimeCurve = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_result);
        Intent intent = getIntent();

        dbHandler = new ResultDbHandler(activity);
        dbHandler.isDbExist();
        Log.d("SILIR", "passed");
        Boolean isFromMeasurement = intent.getBooleanExtra(NewMeasurementActivity.FROM_HERE, false);
        if (isFromMeasurement) {
            profile = (MeasurementProfile) intent.getSerializableExtra(NewMeasurementActivity.PROFILE_EXTRA);
            keyProfile = intent.getStringExtra(NewMeasurementActivity.KEY_EXTRA);
            flowTimeCurve = (ArrayList<Float>) intent.getSerializableExtra(NewMeasurementActivity.RESULT_EXTRA);

            List<Entry> flowVolumeEntries = new ArrayList<>();
            float xValue = 0;
            for (Float yValue : flowTimeCurve) {
                flowVolumeEntries.add(new Entry(xValue, yValue));
                xValue += DELAY;
            }

            flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
            LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
            LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
            flowVolumeChart.setData(flowVolumeLineData);

            float prevFlowTime = 0;
            float prevVolumeTime = 0;
            float currVolumeTime = 0;
            xValue = 0;
            List<Entry> volumeTimeEntries = new ArrayList<>();
            for (Float flowTime : flowTimeCurve) {
                currVolumeTime = prevVolumeTime + RiemmanIntegrator.integrate(prevFlowTime, flowTime);
                volumeTimeEntries.add(new Entry(xValue, currVolumeTime));
                prevFlowTime = flowTime;
                prevVolumeTime = currVolumeTime;
                xValue += DELAY;
            }

            volumeTimeChart = (LineChart) findViewById(R.id.volume_time_curve);
            LineDataSet volumeTimeDataSet = new LineDataSet(volumeTimeEntries, "");
            LineData volumeTimeLineData = new LineData(volumeTimeDataSet);
            flowVolumeChart.setData(volumeTimeLineData);
        } else {
            if (!dbHandler.isDbExist()) {
                Log.d("SILIR", "database doesnt exist");
                findViewById(R.id.no_data_label).setVisibility(View.VISIBLE);
                findViewById(R.id.main_container).setVisibility(View.GONE);
            } else {
                /*data have already been available*/
            }
        }
    }

//    private class AsyncCaller extends AsyncTask<Void, Integer, Void> {
//        private ProgressDialog progressDialog = new ProgressDialog(activity);
//        private LineData data;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog.setMessage("Calculating...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            double[] flowRateData = new DataGenerator().generate();
//            flowVolumeData[0] = 0;
//            List<Entry> entries = new ArrayList<>();
//            for (int i = 0; i < 6000; i++) {
//                entries.add(new Entry(i, (float) flowRateData[i]));
//            }
//            LineDataSet dataSet = new LineDataSet(entries, "Flow rate");
//            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
//            data = new LineData(dataSet);
//            return null;
//        }
//
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
//            flowVolumeChart.setData(data);
//            flowVolumeChart.invalidate();
//        }
//    }

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
}
