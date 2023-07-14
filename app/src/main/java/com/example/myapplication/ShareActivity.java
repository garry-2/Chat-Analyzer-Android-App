package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ShareActivity extends AppCompatActivity  {

    private String result = null;
    TextView textView,total_messages,total_words,total_links,total_media_messages;
    BarChart chart;
    BarChart busy_month_chart;
    LineChart daily_timeline_chart;
    LineChart monthly_timeline_chart;
    PieChart busy_users_chart,busy_days_chart,busy_months_chart;

    LottieAnimationView loaderAnimationView;
    LottieAnimationView networkAnimationView;
    ScrollView scrollView;
    LinearLayout splashLayout;
    LinearLayout splashLayout2;

    HorizontalBarChart horizontalBarChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        textView = findViewById(R.id.textView1);
        loaderAnimationView = findViewById(R.id.animationLoader);
        loaderAnimationView.playAnimation();
        scrollView = findViewById(R.id.scrollView);
        splashLayout = findViewById(R.id.splash_layout);
        splashLayout2 = findViewById(R.id.splash_layout2);

        networkAnimationView = findViewById(R.id.networkAnimation);
        networkAnimationView.playAnimation();

        if(ContextCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 5);
        }

        if(ContextCompat.checkSelfPermission(this,"android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Internet is not granted", Toast.LENGTH_SHORT).show();
        }

        Uri uri = null;

        //our intent action is send multiple then we always get a arraylist as a intent int this activity
        ArrayList<Parcelable> extraList = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        //our file uri is the first element of the received arraylist
        if (extraList != null && !extraList.isEmpty()) {
            Parcelable p = extraList.get(0);
            if (p instanceof Uri) {
                uri = (Uri) p;
            }
        }

        //if uri is null
        if (uri == null) {
            Log.d("Gaurav", "uri is null");
        }

        // defining our api endpoint.
        String API_ENDPOINT = "https://wp-chat-api.azurewebsites.net/";

        //getting name of the file which we received as an uri.
        String chatFileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                chatFileName = cursor.getString(nameIndex);
            }
        }

        if (chatFileName.contains("\"")) {
            chatFileName = chatFileName.substring(chatFileName.indexOf('"') + 1, chatFileName.lastIndexOf('"'));
        }
        if(chatFileName.length() > 14){
            setTitle(chatFileName.substring(9,chatFileName.length()-4));
        }
        else{
            setTitle(chatFileName);
        }

        //performing operations on our chat uri which we received
        if (uri != null) {

            try {
                //defining inputstream object to read the content of the uri.
                InputStream inputStream = getContentResolver().openInputStream(uri);
                OutputStream outputStream = openFileOutput(chatFileName, Context.MODE_PRIVATE);

                // Create a reader and writer for the input and output streams
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

                // Read the input file and write to the output file with UTF-8 encoding
                char[] buffer = new char[1024];
                int charsRead;
                while ((charsRead = inputStreamReader.read(buffer)) != -1) {
                    outputStreamWriter.write(buffer, 0, charsRead);
                }

                // Close the streams
                inputStreamReader.close();
                outputStreamWriter.close();
                inputStream.close();
                outputStream.close();

                // Confirm that the contents are written to the file in app storage
                File file = new File(getFilesDir(), chatFileName);

                StringBuilder stringBuilder = new StringBuilder();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    Log.d("Gaurav","Contents of file "+stringBuilder);
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.d("Gaurav","creating object of Chatloading");


            //performing operations to call the api using chatloading thread as this contains network operations.;



            ChatLoading cl = new ChatLoading(new ChatLoading.ApiCallback() {

                @Override
                public void onResponseReceived(String response) {

                    result = response;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                Log.d("Gaurav","inside run method !"+response);

                                splashLayout.setVisibility(View.GONE);
                                splashLayout2.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);

                                // Create a list of random colors
                                List<Integer> colors = new ArrayList<>();
                                for (int i = 0; i < 2; i++) {
                                    int r = (int) (Math.random() * 255);
                                    int g = (int) (Math.random() * 255);
                                    int b = (int) (Math.random() * 255);
                                    int color = Color.rgb(r, g, b);
                                    colors.add(color);
                                }

                                Log.d("Gaurav","Busy Days");
                                JSONObject jsonResponse = new JSONObject(response);

                                total_words = findViewById(R.id.textView_totalWords);
                                total_messages = findViewById(R.id.textView_totalMessages);
                                total_links = findViewById(R.id.textView_totalLinks);
                                total_media_messages = findViewById(R.id.textView_totalMediaMessages);

                                JSONObject statsObject = jsonResponse.getJSONObject("stats");
                                String  words = Integer.toString(statsObject.getInt("words"));
                                String messages = Integer.toString(statsObject.getInt("num_messages")) ;
                                String links = Integer.toString(statsObject.getInt("num_links"));
                                String media_messages = Integer.toString(statsObject.getInt("num_media_messages"));
                                Log.d("Gaurav","total words "+words+" messages "+messages);

                                total_words.setText(words);
                                total_messages.setText(messages);
                                total_links.setText(links);
                                total_media_messages.setText(media_messages);

//                                total_words.setText(statsObject.getInt("words"));
//                                total_messages.setText(statsObject.getInt("num_messages"));

                                JSONObject busyDayObject = jsonResponse.getJSONObject("busy_day");

                                //converting jsonojbect to a jsonarray
                                JSONArray busyDayValues = new JSONArray();
                                Iterator<String> busyDaykeys = busyDayObject.keys();
                                List<String> busy_day_labels = new ArrayList<>();
                                while(busyDaykeys.hasNext()) {
                                    String key = busyDaykeys.next();
                                    busy_day_labels.add(key);
                                    busyDayValues.put(busyDayObject.get(key));
                                }
                                ArrayList<BarEntry> entries = new ArrayList<>();
                                for(int i = 0;i<busyDayValues.length();i++){

                                    entries.add(new BarEntry(i,busyDayValues.getInt(i)));
                                }

                                // Next, create a BarDataSet with the data and customize its appearance
                                BarDataSet dataSet = new BarDataSet(entries, "Busy Days");
                                dataSet.setColors(colors);

                                BarData data = new BarData(dataSet);

                                // Finally, set up the chart with the BarData and customize its appearance
                                chart = findViewById(R.id.bar_chart); // assuming you have a BarChart view in your layout with the id "chart"

                                data.setBarWidth(0.9f);
                                chart.setData(data);


                                chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(busy_day_labels));
                                chart.setFitBars(true);
                                chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(busy_day_labels));
                                chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                //chart.getXAxis().setGranularity(1f);

                                chart.getXAxis().setGranularityEnabled(true);
                                chart.getAxisLeft().setGranularity(1f);
                                chart.getDescription().setEnabled(false);
                                chart.getXAxis().setEnabled(false);
                                //chart.getAxisRight().setEnabled(false);
                                chart.getLegend().setEnabled(false);
                                chart.invalidate();
                                chart.getXAxis().setDrawGridLines(true);
                                chart.setDragEnabled(false);
                                chart.setScaleEnabled(false);
                                chart.setDoubleTapToZoomEnabled(false);
                                chart.setPinchZoom(false);
                                chart.animateY(1000);

                                //pie chart for busy days
                                ArrayList<PieEntry> busyDaypieEntries = new ArrayList<>();
                                for(int i = 0;i< busyDayValues.length();i++){
                                    busyDaypieEntries.add(new PieEntry( (float) busyDayValues.getDouble(i),busy_day_labels.get(i)));
                                }
//
                                PieDataSet busyDaypieDataSet = new PieDataSet(busyDaypieEntries, "Active Users Pie Chart");
                                busyDaypieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                                busyDaypieDataSet.setValueTextColor(Color.BLACK);
                                busyDaypieDataSet.setValueTextSize(16);
//
//
                                PieData busyDaypiedata = new PieData(busyDaypieDataSet);
                                busy_days_chart = findViewById(R.id.busy_days_pie_chart);

                                busy_days_chart.setData(busyDaypiedata);
                                busy_days_chart.getDescription().setEnabled(false);
                                busy_days_chart.animateY(2000);

                                Log.d("Gaurav","Busy Months ");
                                // barchart for busy months
                               JSONObject busyMonthObject = jsonResponse.getJSONObject("busy_month");

                                //converting jsonojbect to a jsonarray
                                JSONArray busyMonthValues = new JSONArray();
                                List<String> busy_months_labels = new ArrayList<>();
                                Iterator<String> busyMonthkeys = busyMonthObject.keys();

                                while(busyMonthkeys.hasNext()) {
                                    String key = busyMonthkeys.next();
                                    busy_months_labels.add(key);
                                    busyMonthValues.put(busyMonthObject.get(key));
                                }

                                //Pie chart for busy months
                                ArrayList<PieEntry> busyMonthPieEntries = new ArrayList<>();
                                for(int i = 0;i< busyMonthValues.length();i++){
                                    busyMonthPieEntries.add(new PieEntry( (int) busyMonthValues.getInt(i),busy_months_labels.get(i)));
                                }

                                PieDataSet busyMonthPieDataSet = new PieDataSet(busyMonthPieEntries, "");
                                busyMonthPieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                                busyMonthPieDataSet.setValueTextColor(Color.BLACK);
                                busyMonthPieDataSet.setValueTextSize(16);

                                PieData busyMonthPieData = new PieData(busyMonthPieDataSet);
                                busy_months_chart = findViewById(R.id.busy_months_pie_chart);

                                busy_months_chart.setData(busyMonthPieData);
                                busy_months_chart.getDescription().setEnabled(false);
                                busy_months_chart.animateY(2000);

                                Log.d("Gaurav","Bar Graph for busy months");
                                ArrayList<BarEntry> busy_month_entries = new ArrayList<>();
                                for(int i = 0;i<busyMonthValues.length();i++){
                                    busy_month_entries.add(new BarEntry(i,busyMonthValues.getInt(i)));
                                }

                                BarDataSet busy_month_dataset = new BarDataSet(busy_month_entries, "Busy Months");
                                // set value text color based on theme
                                //busy_month_dataset.setValueTextColor(currentNightMode == Configuration.UI_MODE_NIGHT_YES ? Color.WHITE : Color.BLACK);
                                busy_month_dataset.setColors(colors);
//
                                busy_month_chart = findViewById(R.id.busy_month_bar_chart);
//
                                BarData busy_month_data = new BarData(busy_month_dataset);
//
                                busy_month_chart.setData(busy_month_data);
                                busy_month_chart.getDescription().setEnabled(false);
                                busy_month_chart.getXAxis().setEnabled(false);
                                busy_month_chart.getAxisRight().setEnabled(false);
                                busy_month_chart.getLegend().setEnabled(false);
                                busy_month_chart.getXAxis().setDrawGridLines(false);

                                    // disable zooming and dragging
                                busy_month_chart.setDragEnabled(false);
                                busy_month_chart.setScaleEnabled(false);
                                busy_month_chart.setDoubleTapToZoomEnabled(false);
                                busy_month_chart.setPinchZoom(false);

                                busy_month_chart.animateY(1000);
                                busy_month_chart.invalidate();

//                              // Daily Timeline
                                Log.d("Gaurav","Daily Timeline");
                                JSONObject dailyTimelineObject = jsonResponse.getJSONObject("daily_timeline");
                                JSONObject messageObject = dailyTimelineObject.getJSONObject("message");
                                JSONObject onlyDateObject = dailyTimelineObject.getJSONObject("only_date");

                                //converting jsonojbect to a jsonarray
                                JSONArray dailyTimelineMessageValues = new JSONArray();
                                Iterator<String> keys = messageObject.keys();
                                while(keys.hasNext()) {
                                    String key = keys.next();
                                    dailyTimelineMessageValues.put(messageObject.get(key));

                                }

                                JSONArray dailyTimelineDateValues = new JSONArray();
                                Iterator<String> keys2 = onlyDateObject.keys();
                                while(keys2.hasNext()) {
                                    String key = keys2.next();
                                    dailyTimelineDateValues.put(onlyDateObject.get(key));
                                }

                                ArrayList<Entry> yValues = new ArrayList<>();
                                ArrayList<String> xValues = new ArrayList<>();

                                //populate arraylist with x and y axis values
                                for(int i=0; i<dailyTimelineDateValues.length(); i++){
                                    String daily_timeline_date = dailyTimelineDateValues.getString(i);
                                    int daily_timeline_message = dailyTimelineMessageValues.getInt(i);
                                    xValues.add(daily_timeline_date);
                                    yValues.add(new Entry(i, daily_timeline_message));
                                    Log.d("Gaurav",""+daily_timeline_date+" "+daily_timeline_message);
                                }
//
                                //create line dataset with the arraylist of yValues and label
                                LineDataSet daily_timeline_dataSet = new LineDataSet(yValues, "Daily Timeline");
//
                                daily_timeline_dataSet.setColors(colors);
                                daily_timeline_chart = findViewById(R.id.daily_timeline);


                                daily_timeline_chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
                                LineData daily_timeline_data = new LineData();
                                daily_timeline_data.addDataSet(daily_timeline_dataSet);
//
                                //create chart and set data
                                daily_timeline_chart.setData(daily_timeline_data);
                                daily_timeline_chart.invalidate();
                                daily_timeline_chart.animateX(1000);
                                daily_timeline_chart.getXAxis().setEnabled(false);
                                daily_timeline_chart.getAxisRight().setEnabled(false);

                                Log.d("Gaurav","Montlhy Timeline");
                                JSONObject monthlyTimelineObject = jsonResponse.getJSONObject("timeline");
                                JSONObject monthlyMessageObject = monthlyTimelineObject.getJSONObject("message");
                                JSONObject monthlyObject = monthlyTimelineObject.getJSONObject("month");

                                //converting jsonojbect to a jsonarray
                                JSONArray monthlyTimelineMessageValues = new JSONArray();
                                Iterator<String> keys3 = monthlyMessageObject.keys();
                                while(keys3.hasNext()) {
                                    String key = keys3.next();
                                    monthlyTimelineMessageValues.put(monthlyMessageObject.get(key));

                                }

                                JSONArray monthlyValues = new JSONArray();
                                Iterator<String> keys4 = monthlyObject.keys();
                                while(keys4.hasNext()) {
                                    String key = keys4.next();
                                    monthlyValues.put(monthlyObject.get(key));
                                }

                                ArrayList<Entry> yValues_monthlyTimeline = new ArrayList<>();
                                ArrayList<String> xValues_monthlyTimeline = new ArrayList<>();

                                //populate arraylist with x and y axis values
                                for(int i=0; i<monthlyValues.length(); i++){
                                    String month = monthlyValues.getString(i);
                                    int monthly_messages = monthlyTimelineMessageValues.getInt(i);
                                    xValues_monthlyTimeline.add(month);
                                    yValues_monthlyTimeline.add(new Entry(i, monthly_messages));
                                    Log.d("Gaurav",""+month+" "+monthly_messages);
                                }

                                LineDataSet monthly_timeline_dataset = new LineDataSet(yValues_monthlyTimeline, "Monthly Timeline");
//
                                monthly_timeline_dataset.setColors(colors);
                                //create line data object with xValues and line dataset
                                LineData monthly_timeline_data = new LineData();
                                monthly_timeline_data.addDataSet(monthly_timeline_dataset);
//
                                //create chart and set data
                                monthly_timeline_chart = findViewById(R.id.monthly_timeline); //assuming you have a LineChart view with id "chart" in your layout
                                monthly_timeline_chart.setData(monthly_timeline_data);
                                monthly_timeline_chart.invalidate();
                                monthly_timeline_chart.animateX(1000);
                                monthly_timeline_chart.getXAxis().setEnabled(false);
                                monthly_timeline_chart.getAxisRight().setEnabled(false);

                                //piechart for most active users
                                Log.d("Gaurav","Most busy users Piechart");
                                JSONObject busyUsersObject = jsonResponse.getJSONObject("new_df");
                                JSONObject percentsObject = busyUsersObject.getJSONObject("count");
                                JSONObject usersObject = busyUsersObject.getJSONObject("percent");

                                JSONArray percentValues = new JSONArray();
                                Iterator<String> percentKey = percentsObject.keys();
                                int i1 = 0;
                                while(percentKey.hasNext()) {
                                    if(i1 == 10){
                                        break;
                                    }
                                    String key = percentKey.next();
                                    percentValues.put(percentsObject.get(key));
                                    i1++;

                                }

                                JSONArray usersValues = new JSONArray();
                                Iterator<String> countKey = usersObject.keys();
                                int i2 = 0;
                                while(countKey.hasNext()) {
                                    if(i2 == 10){
                                        break;
                                    }
                                    String key = countKey.next();
                                    usersValues.put(usersObject.get(key));
                                    i2++;

                                }

                                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                                for(int i = 0;i< usersValues.length();i++){
                                    pieEntries.add(new PieEntry( (float) percentValues.getDouble(i),usersValues.getString(i)));
                                }

                                PieDataSet pieDataSet = new PieDataSet(pieEntries, "Active Users Pie Chart");
                                pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                                pieDataSet.setValueTextColor(Color.BLACK);
                                pieDataSet.setValueTextSize(16);


                                PieData piedata = new PieData(pieDataSet);
                                busy_users_chart = findViewById(R.id.busy_users_pie_chart);

                                busy_users_chart.setData(piedata);
                                busy_users_chart.getDescription().setEnabled(false);
                                busy_users_chart.animateY(2000);


                                //most common words
                                Log.d("Gaurav","Common Words");
                                JSONObject commonWordsObject = jsonResponse.getJSONObject("most_common_words");
                                JSONObject wordsObject = commonWordsObject.getJSONObject("0");
                                JSONObject wordsFrequencyObject = commonWordsObject.getJSONObject("1");

                                ArrayList<BarEntry> common_words_entries = new ArrayList<>();

                                // Iterate over the words and counts to create BarEntry objects
                                for (int i = 0; i < wordsObject.length(); i++) {
                                    String word = wordsObject.getString(String.valueOf(i));
                                    int count = wordsFrequencyObject.getInt(String.valueOf(i));
                                    Log.d("Gaurav","common Words : "+word+" "+count);
                                    common_words_entries.add(new BarEntry(count, i, word));
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });

                }
            },API_ENDPOINT, chatFileName,getApplicationContext());
            //Log.d("Gaurav","Starting object of Chatloading");
            cl.start();

        }

    }

    public void showAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the UI to display the animation
                // Example: Display a progress bar or an animated view
                networkAnimationView.setVisibility(View.VISIBLE);
            }
        });
    }



}








