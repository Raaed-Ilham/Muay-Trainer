package com.example.visiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.example.visiontest.ML_Managers.vision.posedetector.classification.PoseClassifierProcessor;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;




    public class positionLog extends AppCompatActivity {

        public String test;

        public TextView textView;
        PoseClassifierProcessor poseClassifierProcessor;

        positionStorage positionStorage;

        LineChart mplinechart;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_position_log);
            textView = findViewById(R.id.txtPositionLog);
            mplinechart = findViewById(R.id.line_chart);

            positionStorage = positionStorage.getInstance(); // Get the singleton instance


            LineDataSet lineDataSet = new LineDataSet(dataValues(),"right wrist dataset");
            ArrayList<ILineDataSet>dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);
            LineData data =new LineData(dataSets);
            mplinechart.setData(data);
            mplinechart.invalidate();

            for (int i = 0; i < positionStorage.getRightWristPositionX().size(); i++) {
                test = " " + test + " " + String.valueOf(positionStorage.getRightWristPositionX().get(i));
                Log.d("position storage print", test);
            }

            //textView.setText("Right wrist position: "+ test);



        }

        private ArrayList<Entry>dataValues() {
             ArrayList<Entry> dataVals = new ArrayList<Entry>();

            for (int i = 0; i < positionStorage.getRightWristPositionX().size(); i++) {
               dataVals.add(new Entry(positionStorage.getRightWristPositionX().get(i),positionStorage.getRightWristPositionY().get(i)));


            }

            return dataVals;
        }



        //end of class bracket
    }

