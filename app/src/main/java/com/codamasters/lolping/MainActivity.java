package com.codamasters.lolping;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.ganfra.materialspinner.MaterialSpinner;
import info.hoang8f.widget.FButton;


public class MainActivity extends AppCompatActivity {

    public static String pingError = null;

    // EUROPE
    private final String IP_EUW = "104.160.141.3";
    private final String IP_EUNE = "104.160.142.3";

    // AMERICA
    private final String IP_NA = "104.160.131.1";
    private final String IP_LAN = "104.160.136.3";
    private final String IP_BR = "8.23.24.1";

    // OCEANIA
    private final String IP_OCE = "104.160.156.1";

    // PING VALUE LIMITS
    private final int NORMAL_PING = 120;
    private final int HIGH_PING = 220;

    private TextView ping_value, ping_avg, ping_result;

    public static LineChart mChart;
    private LineData lineData;

    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    public static ArrayList<Float> pingValues;

    private FButton checkPing_button;
    private Context context;
    private MaterialSpinner spinner;

    private boolean clicked = false;

    //Ads
    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ping_value = (TextView) findViewById(R.id.ping_value);
        ping_avg = (TextView) findViewById(R.id.ping_avg);
        ping_result = (TextView) findViewById(R.id.ping_result);

        spinner = (MaterialSpinner) findViewById(R.id.spinner);


        String[] ITEMS = {"EUW", "EUNE" , "NA", "LAN", "BR", "OCE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (MaterialSpinner) findViewById(R.id.spinner);
        spinner.setBackgroundColor(Color.parseColor("#FFFFFF"));
        spinner.setAdapter(adapter);

        checkPing_button = (FButton) findViewById(R.id.checkPing);
        context = this;

        pingValues = new ArrayList<Float>();
        
        checkPing_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!spinner.getSelectedItem().toString().equals("Server")) {

                    if (!clicked) {

                        pingValues = new ArrayList<Float>();

                        if (mChart != null) {
                            mChart.clearValues();
                            ping_avg.setText("");
                            ping_result.setText("");
                        }

                        switch (spinner.getSelectedItem().toString()) {
                            case "NA":
                                new PingTask(context, IP_NA).execute();
                                break;
                            case "EUW":
                                new PingTask(context, IP_EUW).execute();
                                break;
                            case "EUNE":
                                new PingTask(context, IP_EUNE).execute();
                                break;
                            case "LAN":
                                new PingTask(context, IP_LAN).execute();
                                break;
                            case "BR":
                                new PingTask(context, IP_BR).execute();
                                break;
                            case "OCE":
                                new PingTask(context, IP_OCE).execute();
                                break;
                        }

                        clicked = true;
                        checkPing_button.setText("STOP CHECK");

                        spinner.setEnabled(false);
                        spinner.setClickable(false);
                    } else {
                        clicked = false;
                        checkPing_button.setText("CHECK");

                        spinner.setEnabled(true);
                        spinner.setClickable(true);
                    }
                } else {
                    Toast.makeText(context, "Select a server.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        ping_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(context)
                        .setTitleText(getString(R.string.info_title))
                        .setContentText(getString(R.string.info_text))
                        .show();
            }
        });



        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void createGraph(ArrayList<Float> values){
        tvX = (TextView) findViewById(R.id.tvXMax);
        tvY = (TextView) findViewById(R.id.tvYMax);

        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);

        mSeekBarX.setProgress(45);
        mSeekBarY.setProgress(100);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("Error loading data.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        mChart.getAxisRight().setEnabled(false);

        setPingData(pingValues);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutBounce);
    }


    private void setPingData(ArrayList<Float> values){

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < values.size(); i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < values.size(); i++) {
            yVals.add(new Entry(values.get(i), i));
        }

        LineDataSet set1;

        Log.d("ACTUALIZAANDO", "DATOS");


        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {

            Log.d("ACTUALIZAANDO", "DATOS BIEN");

            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setFillColor(Color.RED);
            lineData.addEntry(new Entry(values.get(values.size()-1), values.size()-1), 0);

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(12);
            mChart.moveViewTo(lineData.getXValCount()-10, 50f, YAxis.AxisDependency.LEFT);

        } else {
            set1 = new LineDataSet(yVals, "Ping values");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);


            set1.setFillColor(Color.RED);
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1);

            // create a data object with the datasets
            lineData = new LineData(xVals, dataSets);

            // set data
            mChart.setData(lineData);
        }
    }


    public static String ping(String host) throws IOException, InterruptedException {
        pingError = null;
        StringBuffer echo = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("/system/bin/ping -c 1 " + host);
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line = "";

            while ((line = buffer.readLine()) != null) {
                echo.append(line + "\n");
            }

            pingValues.add(Float.valueOf(getPingStats(echo.toString())));
            return echo.toString();
        } else if (exit == 1) {
            pingError = "failed, exit = 1";
        } else {
            pingError = "error, exit = 2";
        }
        return pingError;
    }

    public static String getPingStats(String s) {
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            s = s.substring(start + 8, end);
            String stats[] = s.split("/");
            return stats[1];
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
        } else {
            pingError = "unknown error in getPingStats";
        }
        return pingError;
    }

    private class PingTask extends AsyncTask<String, String, String> {
        private String url;
        private Context context;
        private ProgressDialog pDialog;

        public PingTask(Context context, String url){
            this.url = url;
            this.context = context;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("ASYNKTASK", "STARTING PING !!!!!!");
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "Error";

            try {
                while (clicked) {
                    result = ping(url);
                    publishProgress(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(String... result){

            Log.d("ASYNKTASK", result[0]);
            String text = getPingStats(result[0]);

            if(pingError == null) {
                float value = Float.parseFloat(text);
                ping_value.setText(text + " ms");

                if (value <= NORMAL_PING) {
                    ping_value.setTextColor(Color.parseColor("#006400"));
                } else if (value <= HIGH_PING) {
                    ping_value.setTextColor(Color.parseColor("#EE7600"));
                } else {
                    ping_value.setTextColor(Color.parseColor("#990000"));

                }
            }else{
                cancel(true);
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(context, "No data received from the servers.", Toast.LENGTH_SHORT).show();

            ping_value.setText("No data");

            clicked = false;
            checkPing_button.setText("CHECK");

            spinner.setEnabled(true);
            spinner.setClickable(true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (pingValues != null && pingValues.size() > 0){

                float sum = 0;
                for (float d : pingValues)
                    sum += d;
                float mean = sum / pingValues.size();

                ping_avg.setText("Average : " + String.valueOf(mean) + " ms");
                if (mean <= NORMAL_PING) {
                    ping_avg.setTextColor(Color.parseColor("#006400"));
                    ping_result.setTextColor(Color.parseColor("#006400"));
                    ping_result.setText("STABLE");
                } else if (mean <= HIGH_PING) {
                    ping_avg.setTextColor(Color.parseColor("#EE7600"));
                    ping_result.setTextColor(Color.parseColor("#EE7600"));
                    ping_result.setText("INSTABLE");
                } else {
                    ping_avg.setTextColor(Color.parseColor("#990000"));
                    ping_result.setTextColor(Color.parseColor("#990000"));
                    ping_result.setText("LAGGING");
                }

                createGraph(pingValues);
            }else{
                Toast.makeText(context, "No data received from the servers.", Toast.LENGTH_SHORT).show();
            }
        }

    }


    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
