package com.example.visiontest;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;


import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import com.example.visiontest.Calculations;

import javax.annotation.Nullable;

public class evaluationFunctions {

    private static evaluationFunctions instance ;




    public Activity activity;

    public ArrayList<PoseLandmark>currentUserLandmarks;

    TextView displayFeedback;



    String feedback = "display feedback";

    PoseLandmark coachLandmarkJab;

    ArrayList <Float> arrCoachlandmarkJab = new ArrayList<>();
    List<PointF3D> coachJabPoints = new ArrayList<>();

    public Calculations calculations = new Calculations();

    PointF3D coachRightWrist;
    PointF3D coachLeftWrist;
    PointF3D coachRightHip;
    PointF3D coachLeftHip;
    PointF3D coachLeftMouth;
    PointF3D coachRightMouth;
    PointF3D coachLeftShoulder;


    String selected_pose;
    Pose coachPoseJab;

    //change constructor to private if only one instance needed
    public evaluationFunctions(Activity activity,String selected_pose) {
        this.activity = activity;
        readData(activity);
        this.selected_pose = selected_pose;
    }

    public static evaluationFunctions getInstance(Activity activity, String selected_pose){
        if (instance == null) {
            instance = new evaluationFunctions(activity,selected_pose);
        }
        return instance;
    }
    public void setCurrentUserLandmarks(ArrayList<PoseLandmark> currentUserLandmarks) {
        this.currentUserLandmarks = currentUserLandmarks;
    }
    public String getFeedback(String selected_pose) {
        if (selected_pose.equals("jab"))
        {feedback = jab(selected_pose);}
        else if (selected_pose.equals("cross"))
        {
            feedback = cross(selected_pose);
        }

        return feedback;
    }
    public String jab (String selected_pose){
           //jab evaluation
             if (currentUserLandmarks != null) {
                 float currentRightWristX = currentUserLandmarks.get(PoseLandmark.RIGHT_WRIST).getPosition3D().getX();
                 float currentRightWristY = currentUserLandmarks.get(PoseLandmark.RIGHT_WRIST).getPosition3D().getY();
                 float currentRightWristZ = currentUserLandmarks.get(PoseLandmark.RIGHT_WRIST).getPosition3D().getZ();
                 float currentRightMouthX = currentUserLandmarks.get(PoseLandmark.RIGHT_MOUTH).getPosition3D().getX();
                 float currentRightMouthY = currentUserLandmarks.get(PoseLandmark.RIGHT_MOUTH).getPosition3D().getY();
                 float currentRightMouthZ = currentUserLandmarks.get(PoseLandmark.RIGHT_MOUTH).getPosition3D().getZ();

                 calculations.difference(currentRightWristX,currentRightWristY,currentRightWristZ,currentRightMouthX,currentRightMouthY,currentRightMouthZ);
                 Log.d("Difference X", String.valueOf(calculations.xDiff));
                 Log.d("Difference Y", String.valueOf(calculations.yDiff));
                 Log.d("Difference Z", String.valueOf(calculations.zDiff));

                 double jabAngle = calculations.getAngle(currentUserLandmarks.get(PoseLandmark.LEFT_WRIST),currentUserLandmarks.get(PoseLandmark.LEFT_SHOULDER),currentUserLandmarks.get(PoseLandmark.LEFT_HIP));
                 Log.d("Jab angle", String.valueOf(jabAngle));

                 String angleEval = " ";


                 if (calculations.xDiff>78f)
                 {feedback = " keep your right hand close to your face." ;}
                 else if (calculations.yDiff>60) {
                     feedback = "lift your right hand close to your face";
                 } else if (jabAngle<170) {
                     feedback = "raise your left hand slightly" ;
                 }  else if (jabAngle>190){
                     feedback = "lower your left hand slightly";
                 }
                 else {
                     feedback = "good form";
                 }
             }
        return feedback;
    }

    public String cross (String selected_pose){
        if (currentUserLandmarks != null) {
            float currentLeftWristX = currentUserLandmarks.get(PoseLandmark.LEFT_WRIST).getPosition3D().getX();
            float currentLeftWristY = currentUserLandmarks.get(PoseLandmark.LEFT_WRIST).getPosition3D().getY();
            float currentLeftWristZ = currentUserLandmarks.get(PoseLandmark.LEFT_WRIST).getPosition3D().getZ();
            float currentLeftMouthX = currentUserLandmarks.get(PoseLandmark.LEFT_MOUTH).getPosition3D().getX();
            float currentLeftMouthY = currentUserLandmarks.get(PoseLandmark.LEFT_MOUTH).getPosition3D().getY();
            float currentLeftMouthZ = currentUserLandmarks.get(PoseLandmark.LEFT_MOUTH).getPosition3D().getZ();
           float LeftHeelConfidence = currentUserLandmarks.get(PoseLandmark.LEFT_HEEL).getInFrameLikelihood();


            calculations.difference(currentLeftWristX,currentLeftWristY,currentLeftWristZ,currentLeftMouthX,currentLeftMouthY,currentLeftMouthZ);
            Log.d("Difference X", String.valueOf(calculations.xDiff));
            Log.d("Difference Y", String.valueOf(calculations.yDiff));
            Log.d("Difference Z", String.valueOf(calculations.zDiff));

            double crossAngle = calculations.getAngle(currentUserLandmarks.get(PoseLandmark.RIGHT_WRIST),currentUserLandmarks.get(PoseLandmark.RIGHT_SHOULDER),currentUserLandmarks.get(PoseLandmark.RIGHT_HIP));
            double frontLegAngle = calculations.getAngle(currentUserLandmarks.get(PoseLandmark.LEFT_HIP),currentUserLandmarks.get(PoseLandmark.LEFT_KNEE),currentUserLandmarks.get(PoseLandmark.LEFT_HEEL));
            Log.d("cross angle", String.valueOf(crossAngle));
            Log.d("Front leg angle", String.valueOf(frontLegAngle));

            String angleEval = " ";
            String legEval=" ";

            if(LeftHeelConfidence < 0.2) {
                feedback = "Ensure all joints are visible";
            } else
            if (calculations.xDiff>78f)
            {feedback = "keep your left hand close to your face.";}
            else if (calculations.yDiff>60) {
                feedback = "lift your right hand close to your face";
            } else if (crossAngle<170) {
                feedback = "raise your right hand slightly";
            } else if (frontLegAngle>170) {
                legEval = "bend your knee slightly";
              feedback = legEval;}
            } else {
                feedback = "good form";
            }

        return feedback;
    }



//find ideal points from stored CSV file to compare to givenpoints
    public void readData(Activity activity) {
        InputStream inputStream = activity.getResources().openRawResource(R.raw.training_poses_images_out_avg);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

        try {
            // Read the file line by line
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line into individual data fields
                String[] data = line.split(",");
                StringBuilder logMsg = new StringBuilder();


                for (int i=0;i<data.length;i++){

                    if("avg_jab".equals(data[i])){

                        for (int j = i + 1; j < data.length - 2; j += 3) {
                            if (j + 2 < data.length) { // Ensure that there are enough elements remaining
                                coachJabPoints.add(PointF3D.from(
                                        Float.parseFloat(data[j]),
                                        Float.parseFloat(data[j + 1]),
                                        Float.parseFloat(data[j + 2])
                                ));

                               // arrCoachlandmarkJab.add(Float.parseFloat(data[j]));
                                Log.d("avg_jab values", String.valueOf(coachJabPoints.get(j/3).getX()));
                            }
                        }
                    }

                }


                // Process the data as needed
                // For example, you can access individual fields using data[index]
                // data[0] corresponds to the first field, data[1] to the second, and so on

                // Here, you can process the data or store it in a suitable data structure

                // For demonstration, let's print the data to the console
                for (String field : data) {
                    logMsg.append(field).append(" ");
                }
                Log.d("CSVReader", logMsg.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the BufferedReader to release resources
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
