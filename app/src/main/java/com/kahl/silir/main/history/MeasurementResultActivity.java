package com.kahl.silir.main.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kahl.silir.R;
import com.kahl.silir.customstuff.MeasurementHistoryAdapter;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.entity.RiemmanIntegrator;
import com.kahl.silir.entity.User;
import com.kahl.silir.main.MainActivity;
import com.kahl.silir.main.home.HomeFragment;
import com.kahl.silir.main.home.NewMeasurementActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.Format;
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
    private TextView pef;
    private TextView fev1;
    private TextView fvc;
    private TextView fev1_fvc;

    private ResultDbHandler dbHandler;
    private MeasurementResult result;
    private Date date = new Date();

    private final Activity activity = this;

    private MeasurementProfile profile;
    private String keyProfile;
    private ArrayList<Float> flowTimeCurve = new ArrayList<>();

    private float set_pef;
    private float set_fev1;
    private float set_fvc;
    private float set_fev1_fvc;

    private boolean isFromHistory;
    private String measurementTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_result);
        Intent intent = getIntent();

        pef = (TextView) findViewById(R.id.pef_value_textview);
        fev1 = (TextView) findViewById(R.id.fev1_value_textview);
        fvc = (TextView) findViewById(R.id.fvc_value_textview);
        fev1_fvc = (TextView) findViewById(R.id.fev1_fvc_value_textview);

        dbHandler = new ResultDbHandler(activity);
        dbHandler.isDbExist();
        Boolean isFromMeasurement = intent.getBooleanExtra(NewMeasurementActivity.FROM_HERE, false);
        isFromHistory = intent.getBooleanExtra(MeasurementHistoryAdapter.FROM_HERE, false);
        if (isFromMeasurement) {
            profile = (MeasurementProfile) intent.getSerializableExtra(NewMeasurementActivity.PROFILE_EXTRA);
            keyProfile = intent.getStringExtra(NewMeasurementActivity.KEY_EXTRA);
            flowTimeCurve = (ArrayList<Float>) intent.getSerializableExtra(NewMeasurementActivity.RESULT_EXTRA);
            Log.d("SILIR", "profile_id (measurementresultactivity) = " + keyProfile);

            List<Float> volumes = RiemmanIntegrator.integrate(flowTimeCurve, DELAY);

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
                flowVolumeEntries.add(new Entry(volume, flowTimeCurve.get(++index)));
            }

            flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
            LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
            LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
            flowVolumeChart.setData(flowVolumeLineData);

            String flowString = convertFloatListArrayToString(flowTimeCurve);
            String volumeString = convertFloatListToString(volumes);

            /* Get the value of PEF, FEV1, and FVC
             * from flowTimeCurve and volumes arrays
             */

            set_pef = Collections.max(flowTimeCurve);
            float roundPef = round(set_pef, 2);
            pef.setText(Float.toString(roundPef));

            set_fvc = volumes.get(volumes.size() - 1);
            float roundFvc = round(set_fvc, 2);
            fvc.setText(Float.toString(roundFvc));

            if (volumes.size() >= 100) {
                set_fev1 = volumes.get(100);
            } else {
                set_fev1 = set_fvc;
            }
            float roundFev1 = round(set_fev1, 2);
            fev1.setText(Float.toString(roundFev1));

            set_fev1_fvc = (set_fev1 / set_fvc) * 100;
            float roundRatio = round(set_fev1_fvc, 2);
            fev1_fvc.setText(Float.toString(roundRatio) + "%");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy - hh:mm:ss");
            measurementTime = sdf.format(date);
            result = new MeasurementResult(roundFvc, roundFev1, roundPef, measurementTime,
                    keyProfile, flowString, volumeString);
            dbHandler.addResult(result);

        } else if (isFromHistory) {
            MeasurementResult result = (MeasurementResult) intent.getSerializableExtra(MeasurementHistoryAdapter.RESULT);

            pef.setText(result.getPef() + "");
            fev1.setText(result.getFev1() + "");
            fvc.setText(result.getFvc() + "");
            fev1_fvc.setText(result.getFev1() / result.getFvc() + "");

            String getFlowArray = result.getArrayFlow();
            String getVolumeArray = result.getArrayVolume();

            List<Float> loadFlow = convertStringToFloatArrayList(getFlowArray);
            List<Float> loadVolume = convertStringToFloatArrayList(getVolumeArray);

            List<Entry> volumeTimeEntries = new ArrayList<>();
            float xValue = 0;
            for (Float yValue : loadFlow) {
                volumeTimeEntries.add(new Entry(xValue, yValue));
                xValue += DELAY;
            }

            volumeTimeChart = (LineChart) findViewById(R.id.volume_time_curve);
            LineDataSet volumeTimeDataSet = new LineDataSet(volumeTimeEntries, "");
            LineData volumeTimeLineData = new LineData(volumeTimeDataSet);
            volumeTimeChart.setData(volumeTimeLineData);

            List<Entry> flowVolumeEntries = new ArrayList<>();
            int index = -1;
            for (Float volume : loadVolume) {
                flowVolumeEntries.add(new Entry(volume, loadFlow.get(++index)));
            }

            flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
            LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
            LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
            flowVolumeChart.setData(flowVolumeLineData);

            measurementTime = result.getTime();
        } else {
            if (!dbHandler.isDbExist()) {
                Log.d("SILIR", "database doesnt exist");
                findViewById(R.id.no_data_label).setVisibility(View.VISIBLE);
                findViewById(R.id.main_container).setVisibility(View.GONE);
            } else {
                /*data have already been available*/
                Log.d("SILIR", "database exist");
                pef = (TextView) findViewById(R.id.pef_value_textview);
                fev1 = (TextView) findViewById(R.id.fev1_value_textview);
                fvc = (TextView) findViewById(R.id.fvc_value_textview);
                fev1_fvc = (TextView) findViewById(R.id.fev1_fvc_value_textview);

                MeasurementResult getResult = dbHandler.getCurrentMeasurement();
                float getPef = getResult.getPef();
                float getFvc = getResult.getFvc();
                float getFev1 = getResult.getFev1();
                float getFev1_Fvc = round((getFev1 / getFvc) * 100, 2);
                String getFlowArray = getResult.getArrayFlow();
                String getVolumeArray = getResult.getArrayVolume();
                measurementTime = getResult.getTime();

                List<Float> loadFlow = convertStringToFloatArrayList(getFlowArray);
                List<Float> loadVolume = convertStringToFloatArrayList(getVolumeArray);

                List<Entry> volumeTimeEntries = new ArrayList<>();
                float xValue = 0;
                for (Float yValue : loadFlow) {
                    volumeTimeEntries.add(new Entry(xValue, yValue));
                    xValue += DELAY;
                }

                volumeTimeChart = (LineChart) findViewById(R.id.volume_time_curve);
                LineDataSet volumeTimeDataSet = new LineDataSet(volumeTimeEntries, "");
                LineData volumeTimeLineData = new LineData(volumeTimeDataSet);
                volumeTimeChart.setData(volumeTimeLineData);

                List<Entry> flowVolumeEntries = new ArrayList<>();
                int index = -1;
                for (Float volume : loadVolume) {
                    flowVolumeEntries.add(new Entry(volume, loadFlow.get(++index)));
                }

                flowVolumeChart = (LineChart) findViewById(R.id.flow_volume_curve);
                LineDataSet flowVolumeDataSet = new LineDataSet(flowVolumeEntries, "");
                LineData flowVolumeLineData = new LineData(flowVolumeDataSet);
                flowVolumeChart.setData(flowVolumeLineData);

                pef.setText(Float.toString(getPef));
                fvc.setText(Float.toString(getFvc));
                fev1.setText(Float.toString(getFev1));
                fev1_fvc.setText(Float.toString(getFev1_Fvc) + "%");

            }
        }
    }

    private static String strSeparator = ",";

    private static String convertFloatListArrayToString(ArrayList<Float> arrayList) {
        String str = "";
        for (int i = 0; i < arrayList.size(); i++) {
            str = str + arrayList.get(i);
            if (i < arrayList.size() - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    private static String convertFloatListToString(List<Float> list) {
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str = str + list.get(i);
            if (i < list.size() - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    private static ArrayList<Float> convertStringToFloatArrayList(String str) {
        String[] arr = str.split(strSeparator);
        ArrayList<Float> floatDummy = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            floatDummy.add(Float.parseFloat(arr[i]));
        }
        return floatDummy;
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
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
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                return true;
            case MENU_DELETE_ID:
                if (measurementTime.equals("")) {
                    Toast.makeText(this, "Data isn't available", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Delete this data?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHandler.deleteResult(measurementTime);
                            Intent intent = new Intent(MeasurementResultActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null){
                                dialog.dismiss();
                            }
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
        }

        return false;
    }
}
