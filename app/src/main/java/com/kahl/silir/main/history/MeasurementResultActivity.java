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
import java.util.Collections;
import java.util.List;

public class MeasurementResultActivity extends AppCompatActivity {
    private final int MENU_UPLOAD_ID = 1204;
    private final int MENU_DELETE_ID = 1998;
    private final float DELAY = 0.05f;

    private LineChart flowVolumeChart;
    private LineChart volumeTimeChart;
    private LinearLayout mainContainer;
    private TextView pef;
    private TextView fev1;
    private TextView fvc;

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
        Boolean isFromMeasurement = intent.getBooleanExtra(NewMeasurementActivity.FROM_HERE, false);
        if (isFromMeasurement) {
            profile = (MeasurementProfile) intent.getSerializableExtra(NewMeasurementActivity.PROFILE_EXTRA);
            keyProfile = intent.getStringExtra(NewMeasurementActivity.KEY_EXTRA);
            flowTimeCurve = (ArrayList<Float>) intent.getSerializableExtra(NewMeasurementActivity.RESULT_EXTRA);
            Log.d("SILIR", "flowTimeCurve = " + flowTimeCurve.size());

            List<Float> volumes = RiemmanIntegrator.integrate(flowTimeCurve, DELAY);
            Log.d("SILIR", "flowTimeCurve = " + volumes.size());

            List<Entry> volumeTimeEntries = new ArrayList<>();
            float xValue = 0;
            for (Float yValue : volumes) {
                volumeTimeEntries.add(new Entry(xValue, yValue));
                xValue += DELAY;
            }

            volumeTimeChart = (LineChart) findViewById(R.id.volume_time_curve);
            LineDataSet volumeTimeDataSet = new LineDataSet(volumeTimeEntries, "");
            LineData volumeTimeLineData = new LineData(volumeTimeDataSet);
            volumeTimeChart.setData(volumeTimeLineData);

            List<Entry> flowVolumeEntries = new ArrayList<>();

            flowTimeCurve.add(flowTimeCurve.get(0));
            /*adding one data so that it can be plotted*/

            int index = -1;
            for (Float volume : volumes) {
//                if (index < flowTimeCurve.size())
                flowVolumeEntries.add(new Entry(volume, flowTimeCurve.get(++index)));
                Log.d("SILIR", "index = " + index);
            }

            flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
            LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
            LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
            flowVolumeChart.setData(flowVolumeLineData);

            pef = (TextView) findViewById(R.id.pef_value_textview);
            fev1 = (TextView) findViewById(R.id.fev1_value_textview);
            fvc = (TextView) findViewById(R.id.fvc_value_textview);

            pef.setText(Collections.max(flowTimeCurve) + " L/s");
            if (volumes.size() >= 20)
                fev1.setText(volumes.get(20) + " L");
            else
                fev1.setText("NaN");

            fvc.setText(volumes.get(volumes.size() - 1) + " L");

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
