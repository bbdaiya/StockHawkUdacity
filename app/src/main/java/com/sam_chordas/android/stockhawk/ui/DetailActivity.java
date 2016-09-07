package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class DetailActivity extends Activity {
    public final String LOG = DetailActivity.class.getSimpleName();
    int maxPrice, minPrice;
    private LineChartView lineChartView;
    private LineSet lineSet;
    private ArrayList<String> labels = new ArrayList<String>();
    private ArrayList<Float> values = new ArrayList<Float>();
    private String company_name;
    TextView company_text_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String ticker = intent.getStringExtra("ticker");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        company_text_view = (TextView)findViewById(R.id.company_name);

        FetchData fetchData = new FetchData(ticker);
        fetchData.execute();
        initializeChart();


    }
    public void initializeChart(){
        lineSet = new LineSet();

        lineChartView = (LineChartView)findViewById(R.id.linechart);
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.line_color));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dpToPx(1));

        lineChartView.setBorderSpacing(1)
                .setLabelsColor(getResources().getColor(R.color.labels))
                .setBorderSpacing(dpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, paint);


        lineSet.setColor(getResources().getColor(R.color.line_set_color))
                .setDotsStrokeColor(getResources().getColor(R.color.line_set_color))
                .setDotsColor(Color.WHITE)
                .setDotsRadius(10)
                .setDotsStrokeThickness(dpToPx(2));
        lineChartView.setXLabels(AxisController.LabelPosition.NONE);
        lineChartView.setYLabels(AxisController.LabelPosition.NONE);
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    public void refreshChart(){
        findMinMax();
        addPointsToLineSet();
        lineChartView.addData(lineSet);
        lineChartView.show();
    }

    public void addPointsToLineSet(){
        for (int i = 0 ; i < values.size(); i++){
            lineSet.addPoint(labels.get(i), values.get(i));
        }
    }
    public void findMinMax() {
        maxPrice = Math.round(Collections.max(values));
        minPrice = Math.round(Collections.min(values));

        lineChartView.setAxisBorderValues(minPrice - 50, maxPrice + 50);
    }
    class FetchData extends AsyncTask<Void, Void, Void> {

        private String ticker;

        FetchData(String ticker) {
            this.ticker = ticker;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonp = null;
            String chartJson = null;

            try {
                final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
                final String TICKER = ticker;


                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(TICKER)
                        .appendPath("/chartdata;type=quote;range=1m/json")
                        .build();

                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                    Log.v(LOG, url.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                //Read Input Stream
                InputStream is = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (is == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");           //for making debugging easy we add newline
                }
                if (buffer.length() == 0) {
                    //Stream is empty
                    return null;
                }

                jsonp = buffer.toString();
                chartJson = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
                getChartData(chartJson);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            return null;
        }

        public void getChartData(String chartJson) throws JSONException {
            final String SERIES = "series";
            final String DATE = "Date";
            final String CLOSE = "close";
            final String COMPANY_NAME = "Company-Name";
            final String META = "meta";
            JSONObject chart_data = new JSONObject(chartJson);
            JSONArray seriesResult = chart_data.getJSONArray(SERIES);
            JSONObject meta_info = chart_data.getJSONObject(META);
            company_name = meta_info.getString(COMPANY_NAME);

            for (int i = 0; i < seriesResult.length(); i++) {
                JSONObject eachObject = seriesResult.getJSONObject(i);
                Log.v(LOG,"Date: "+String.valueOf(eachObject.getInt(DATE)));
                labels.add(String.valueOf(eachObject.getInt(DATE)));
                values.add((float) eachObject.getDouble(CLOSE));

            }


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v(LOG, "labrl: "+labels.get(0));
            company_text_view.setText(company_name);
            refreshChart();
        }

    }
}
