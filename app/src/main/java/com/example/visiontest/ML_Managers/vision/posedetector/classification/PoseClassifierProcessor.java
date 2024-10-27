/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.visiontest.ML_Managers.vision.posedetector.classification;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.visiontest.Progress;
import com.google.common.base.Preconditions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import com.example.visiontest.positionLog;
import com.example.visiontest.positionStorage;
import com.example.visiontest.evaluationFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
  private static final String TAG = "PoseClassifierProcessor";
  private static final String POSE_SAMPLES_FILE = "pose/training_full.csv";

  // Specify classes for which we want rep counting.
  // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
  // for your pose samples.
  private static final String CROSS = "cross";
  private static final String JAB = "jab";

DatabaseReference databaseDetails;

  private static final String[] POSE_CLASSES = {
    CROSS, JAB
  };

  private final boolean isStreamMode;

  private EMASmoothing emaSmoothing;
  private List<RepetitionCounter> repCounters;
  private PoseClassifier poseClassifier;
  private String lastRepResult;

  String selected_pose,dateStr;

  positionLog positionLog;





  private positionStorage positionStorage = com.example.visiontest.positionStorage.getInstance();

    evaluationFunctions  evaluationFunctions;



  public ArrayList <PoseLandmark> localandmarks;

  Activity activity;

  Date currentDate = new Date();


  SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

  //String Date= DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(currentDate);
  String Date= dateFormat.format(currentDate);




  // int dateInt = Integer.parseInt(Date);


  DatabaseReference usersRef;

  public String output, userID;

  public int dutchDrillReps = 0;


  int x=0;
   int currentReps,currentJabs,currentCross;

  TextView txtdisplay;


  @WorkerThread
  public PoseClassifierProcessor(Context context, boolean isStreamMode, Activity activity, String selected_pose, String userID) {

    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    this.activity = activity;
    this.selected_pose = selected_pose;
    this.userID=userID;
    this.isStreamMode = isStreamMode;




    if (isStreamMode) {
      emaSmoothing = new EMASmoothing();
      repCounters = new ArrayList<>();
      lastRepResult = "";

      // Instantiate positionLog in the UI thread or another thread with a prepared Looper

    }
    loadPoseSamples(context);

    usersRef = FirebaseDatabase.getInstance().getReference("Progress");
    dateStr = currentDate.toString();
    dateStr = dateStr.substring(0,10);

    DatabaseReference readRepsRef = usersRef.child(userID).child(Date);

    readRepsRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) { // Check if the snapshot exists
          Progress progress = snapshot.getValue(Progress.class);
          if (progress != null) { // Check if the progress object is not null
            currentJabs = progress.getJab();
            currentCross = progress.getCross();
            Log.d("current jab", String.valueOf(currentJabs));
          } else {
            Log.d("DataSnapshot", "Progress object is null");
          }
        } else {
          Log.d("DataSnapshot", "Snapshot does not exist");
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d("DatabaseError", error.getMessage());

      }
    });

  }

  private void loadPoseSamples(Context context) {
    List<PoseSample> poseSamples = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(context.getAssets().open(POSE_SAMPLES_FILE)));
      String csvLine = reader.readLine();
      while (csvLine != null) {
        // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
        PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
        if (poseSample != null) {
          poseSamples.add(poseSample);
        }
        csvLine = reader.readLine();
      }
    } catch (IOException e) {
      Log.e(TAG, "Error when loading pose samples.\n" + e);
    }
    poseClassifier = new PoseClassifier(poseSamples);
    if (isStreamMode) {
      for (String className : POSE_CLASSES) {
        repCounters.add(new RepetitionCounter(className));
        //speed of technique, current time in ms

      }
    }
  }

  /**
   * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
   * classification results.
   *
   * <p>Currently it returns up to 2 strings as following:
   * 0: PoseClass : X reps
   * 1: PoseClass : [0.0-1.0] confidence
   */
  @WorkerThread

  public List<String> getPoseResult(Pose pose) {
    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    List<String> result = new ArrayList<>();
    ClassificationResult classification = poseClassifier.classify(pose);

    // Update {@link RepetitionCounter}s if {@code isStreamMode}.
    if (isStreamMode) {
      // Feed pose to smoothing even if no pose found.
      classification = emaSmoothing.getSmoothedResult(classification);

      // Return early without updating repCounter if no pose found.
      if (pose.getAllPoseLandmarks().isEmpty()) {
        result.add(lastRepResult);
        return result;
      }

  // code below commented out due to performance issues with graphing functions

    //  float rightWristX = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().x;
      //  float rightWristY = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().y;

      //Log.d("test rightwirstX vals:", String.valueOf(rightWristX));
      //Log.d("test rightwirstX vals:", String.valueOf(rightWristX));

      Log.d("classifier UID: ",userID);

      //positionStorage.getRightWristPositionX().add(rightWristX);
      //positionStorage.getRightWristPositionY().add(rightWristY);
       localandmarks = new ArrayList<>(pose.getAllPoseLandmarks());
       evaluationFunctions = com.example.visiontest.evaluationFunctions.getInstance(activity,selected_pose);
       evaluationFunctions.setCurrentUserLandmarks(localandmarks);



    /* for (int i=0; i<positionStorage.getRightWristPositionX().size();i++){
         output =  String.valueOf(positionStorage.getRightWristPositionX().get(i))+" "+ output + " " ;
        //Log.d("output", output);

      } */


      for (RepetitionCounter repCounter : repCounters) {
        int repsBefore = repCounter.getNumRepeats();
        int repsAfter = repCounter.addClassificationResult(classification);
        if (repsAfter > repsBefore) {



           lastRepResult = String.format(
              Locale.US, "%s : %d reps", repCounter.getClassName(), repsAfter);

           Log.d("date in str2", Date);

          DatabaseReference readRepsRef = usersRef.child(userID).child(Date).child(repCounter.getClassName());

          if (repCounter.getClassName().equals("jab"))
          {usersRef.child(userID).child(Date).child(repCounter.getClassName()).setValue(repsAfter+currentJabs);
           // usersRef.child(userID).child(Date).child("date").setValue(Date);
          }
          else {
            usersRef.child(userID).child(Date).child(repCounter.getClassName()).setValue(repsAfter+currentCross);
           // usersRef.child(userID).child(Date).child("date").setValue(Date);

          }

          // Define an array of size 10





          /*

          dutchDrills[x] = repCounter.getClassName();
          x++;
          if (dutchDrills[0].equals("jab") && dutchDrills[1].equals("cross")) {
            dutchDrillReps++;
            Log.d("dutch reps", String.valueOf(dutchDrillReps));
          }

          Arrays.fill(dutchDrills, null);
    */

          break;
        }
      }
      result.add(lastRepResult);
    }

    // Add maxConfidence class of current frame to result if pose is found.
    if (!pose.getAllPoseLandmarks().isEmpty()) {
      String maxConfidenceClass = classification.getMaxConfidenceClass();
      String maxConfidenceClassResult = String.format(
          Locale.US,
          "%s : %.2f confidence",
          maxConfidenceClass,
          classification.getClassConfidence(maxConfidenceClass)
              / poseClassifier.confidenceRange());
      result.add(maxConfidenceClassResult);
    }

    return result;
  }

}
