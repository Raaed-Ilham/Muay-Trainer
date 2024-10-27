package com.example.visiontest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class viewProgress extends AppCompatActivity {

    private FirebaseAuth authCurrent;

    DatabaseReference progressRef;

    Date currentDate;

    Query lastSevenDatesQuery;
    String userID,currentDateStr;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

    String dateNum= dateFormat.format(new Date());

    TextView dutchDrillstxt,jabRepsTxt, crossRepsTxt;


    ArrayList<BarEntry>stackedbarDataset = new ArrayList<>();
    ArrayList<Integer>Jabs = new ArrayList<Integer>();
    ArrayList<Integer>Cross = new ArrayList<Integer>();
    ArrayList<Integer>Dutch_drills = new ArrayList<Integer>();

    //String []days = new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};

    ArrayList<String>days = new ArrayList<>();

    int dayCount = 0;
    int totalJabs =0;
    int totalCross =0;
    int totalDrills = 0;
    int[]colorClassArray =new int[]{Color.BLUE,Color.CYAN,Color.RED};

    BarChart progressChart;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        progressChart = findViewById(R.id.progressChart);



        authCurrent = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authCurrent.getCurrentUser();
        userID = firebaseUser.getUid();
        currentDate = new Date();
        currentDateStr = currentDate.toString();
        currentDateStr = currentDateStr.substring(0,10);
        dutchDrillstxt = findViewById(R.id.DutchDrillstxt);
        jabRepsTxt = findViewById(R.id.jabRepstxt);
        crossRepsTxt =findViewById(R.id.crossRepstxt);



     //   progressRef = FirebaseDatabase.getInstance().getReference("Progress").child(userID).child(currentDateStr);




        Spinner spinnerProgress = findViewById(R.id.spinnerProgress);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.progress_options_array, android.R.layout.simple_spinner_item);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerProgress.setAdapter(adapter);

        spinnerProgress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();

                // Do something with the selected value
                // For example, you can use it in a switch statement


                //Loop through all values to get total progress per timeslot (punches,drills etc)
                //create arrays for each timeslot (maybe 2d)
                //create graph

                switch (selectedValue) {
                    case "View progress for today":
                        // Handle view progress by day
                        Log.d("spinner", "return by day");
                        progressRef = FirebaseDatabase.getInstance().getReference("Progress").child(userID);
                        // Create a query to retrieve the data for the last seven dates
                     lastSevenDatesQuery = progressRef.orderByKey().endAt(dateNum).limitToLast(1);
                        //   Query lastSevenDatesQuery = progressRef.child(currentDateStr).orderByChild("date").endAt(dateNum).limitToLast(7);


// Attach a ValueEventListener to the query
                        lastSevenDatesQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    days.clear();
                                    // Iterate through the dataSnapshot to retrieve the data for each date
                                    for (DataSnapshot progressSnap : dataSnapshot.getChildren()) {

                                        // Extract the data for each date and populate your bar chart
                                        String date = progressSnap.getKey();
                                        Log.d("date",date);
                                        days.add(date);
                                        // Assuming each date object holds dutchdrills, jab, and cross children
                                        Progress progress = progressSnap.getValue(Progress.class);
                                        int jabnum = progress.getJab();
                                        int crossnum= progress.getCross();
                                        int drillsnum = progress.getDutchDrills();

                                        totalJabs = totalJabs + jabnum;
                                        totalCross = totalCross + crossnum;
                                        totalDrills = totalDrills + drillsnum;

                                        jabRepsTxt.setText("Total Jab reps: " + String.valueOf(totalJabs));
                                        crossRepsTxt.setText(" Total Cross repetitions: " + String.valueOf(totalCross));
                                        dutchDrillstxt.setText("Total dutch drill reps: "+ String.valueOf(totalDrills));


                                        Log.d("Jabs", String.valueOf(jabnum));
                                        Log.d("cross's", String.valueOf(crossnum));
                                        Log.d("dutchdrils", String.valueOf(drillsnum));

                                        stackedbarDataset.add(new BarEntry(dayCount,new float[]{jabnum,jabnum+crossnum,jabnum+crossnum+drillsnum}));

                                        dayCount++;
                                    }

                                    totalJabs = 0;
                                    totalCross = 0;
                                    totalDrills = 0;

                                    BarDataSet barDataSet = new BarDataSet(stackedbarDataset,"progress");
                                    barDataSet.setColors(colorClassArray);
                                    BarData barData = new BarData(barDataSet);
                                    progressChart.setData(barData);
                                    XAxis xAxis = progressChart.getXAxis();
                                    xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
                                    xAxis.setCenterAxisLabels(true);
                                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                    xAxis.setGranularity(1);
                                    xAxis.setGranularityEnabled(true);
                                    progressChart.setVisibleXRangeMaximum(7);
                                    progressChart.getXAxis().setAxisMinimum(0);
                                    float Barspace = 0.08f;
                                    float Groupspace = 0.44f;
                                    barData.setBarWidth(0.10f);
                                    progressChart.getXAxis().setAxisMaximum(0+progressChart.getBarData().getGroupWidth(Groupspace,Barspace)*7);



                                } }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });






                        break;
                    case "View progress over last week":
                        // Handle view progress by week
                        Log.d("spinner", "return by week");
                        progressRef = FirebaseDatabase.getInstance().getReference("Progress").child(userID);
                        // Create a query to retrieve the data for the last seven dates
                       lastSevenDatesQuery = progressRef.orderByKey().endAt(dateNum).limitToLast(7);
                    //   Query lastSevenDatesQuery = progressRef.child(currentDateStr).orderByChild("date").endAt(dateNum).limitToLast(7);


// Attach a ValueEventListener to the query
                        lastSevenDatesQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                days.clear();
                                // Iterate through the dataSnapshot to retrieve the data for each date
                                for (DataSnapshot progressSnap : dataSnapshot.getChildren()) {

                                    // Extract the data for each date and populate your bar chart
                                    String date = progressSnap.getKey();
                                    Log.d("date",date);
                                    days.add(date);
                                    // Assuming each date object holds dutchdrills, jab, and cross children
                                    Progress progress = progressSnap.getValue(Progress.class);
                                    int jabnum = progress.getJab();
                                    int crossnum= progress.getCross();
                                    int drillsnum = progress.getDutchDrills();

                                    totalJabs = totalJabs + jabnum;
                                    totalCross = totalCross + crossnum;
                                    totalDrills = totalDrills + drillsnum;

                                    jabRepsTxt.setText("Total Jabs: " + String.valueOf(totalJabs));


                                    Log.d("Jabs", String.valueOf(jabnum));
                                    Log.d("cross's", String.valueOf(crossnum));
                                    Log.d("dutchdrils", String.valueOf(drillsnum));

                                   stackedbarDataset.add(new BarEntry(dayCount,new float[]{jabnum,jabnum+crossnum,jabnum+crossnum+drillsnum}));

                                    dayCount++;
                                }
                                    totalJabs = 0;
                                    totalCross = 0;
                                    totalDrills = 0;


                                BarDataSet barDataSet = new BarDataSet(stackedbarDataset,"progress");
                                barDataSet.setColors(colorClassArray);
                                BarData barData = new BarData(barDataSet);
                                progressChart.setData(barData);
                                XAxis xAxis = progressChart.getXAxis();
                                xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
                                xAxis.setCenterAxisLabels(true);
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setGranularity(1);
                                xAxis.setGranularityEnabled(true);
                                progressChart.setVisibleXRangeMaximum(7);
                                progressChart.getXAxis().setAxisMinimum(0);
                                float Barspace = 0.08f;
                                float Groupspace = 0.44f;
                                barData.setBarWidth(0.10f);
                                progressChart.getXAxis().setAxisMaximum(0+progressChart.getBarData().getGroupWidth(Groupspace,Barspace)*7);




                            } }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });

                        break;
                    case "View progress over last month":
                        // Handle view progress by month
                        Log.d("spinner", "return by month");

                    //    progressRef = FirebaseDatabase.getInstance().getReference("Progress").child(userID);
                        // Create a query to retrieve the data for the last seven dates
                  //       lastSevenDatesQuery = progressRef.orderByKey().endAt(currentDateStr).limitToLast(28);

// Attach a ValueEventListener to the query
                  /*      lastSevenDatesQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                days.clear();
                                // Iterate through the dataSnapshot to retrieve the data for each date
                                for (DataSnapshot progressSnap : dataSnapshot.getChildren()) {

                                    // Extract the data for each date and populate your bar chart
                                    String date = progressSnap.getKey();
                                    Log.d("date",date);
                                    days.add(date);
                                    // Assuming each date object holds dutchdrills, jab, and cross children
                                    Progress progress = progressSnap.getValue(Progress.class);
                                    int jabnum = progress.getJab();
                                    int crossnum= progress.getCross();
                                    int drillsnum = progress.getDutchDrills();
                                    Log.d("Jabs", String.valueOf(jabnum));
                                    Log.d("cross's", String.valueOf(crossnum));
                                    Log.d("dutchdrils", String.valueOf(drillsnum));
                                    stackedbarDataset.add(new BarEntry(dayCount,new float[]{jabnum,jabnum+crossnum,jabnum+crossnum+drillsnum}));

                                    dayCount++;
                                }


                                BarDataSet barDataSet = new BarDataSet(stackedbarDataset,"progress");
                                barDataSet.setColors(colorClassArray);
                                BarData barData = new BarData(barDataSet);
                                progressChart.setData(barData);
                                XAxis xAxis = progressChart.getXAxis();
                                xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
                                xAxis.setCenterAxisLabels(true);
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setGranularity(1);
                                xAxis.setGranularityEnabled(true);
                                progressChart.setVisibleXRangeMaximum(7);
                                progressChart.getXAxis().setAxisMinimum(0);
                                float Barspace = 0.08f;
                                float Groupspace = 0.44f;
                                barData.setBarWidth(0.10f);
                                progressChart.getXAxis().setAxisMaximum(0+progressChart.getBarData().getGroupWidth(Groupspace,Barspace)*7);



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });
*/


                        break;
                    default:
                        // Handle default case
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




    }











}