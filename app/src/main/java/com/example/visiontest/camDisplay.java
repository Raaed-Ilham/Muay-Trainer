package com.example.visiontest;

import static com.google.mlkit.vision.pose.PoseDetectorOptionsBase.CPU;
import static com.google.mlkit.vision.pose.PoseDetectorOptionsBase.CPU_GPU;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.speech.tts.TextToSpeech;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.widget.TextView;
import com.example.visiontest.evaluationFunctions;

import java.io.IOException;
import java.util.Locale;

import com.example.visiontest.ML_Managers.vision.CameraSource;
import com.example.visiontest.ML_Managers.vision.CameraSourcePreview;
import com.example.visiontest.ML_Managers.vision.GraphicOverlay;
import com.example.visiontest.ML_Managers.vision.posedetector.PoseDetectorProcessor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;


public class camDisplay extends AppCompatActivity  {

    private static final String TAG = "camDisplayActivity";
    private static final int REQUEST_CAMERA = 1001;


    private Handler handler;
    private final int Interval_MS=300;

    protected CameraSource cameraSource;
     CameraSourcePreview cameraDisplay;



     evaluationFunctions evaluationFunctions;
    private TextToSpeech textToSpeech;
    String selected_pose = " ";
    String currentFeedback;
    String evalFeedback;

   Boolean ttsEnabled;



     ImageView camflip;

     TextView feedback;

     TextView jabSelect;
     TextView crossSelect;

    String userID;

    private FirebaseAuth authCurrent;





    GraphicOverlay graphicOverlay;







    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_display);
        authCurrent = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authCurrent.getCurrentUser();
       // userID = getIntent().getStringExtra("userID");
        userID = firebaseUser.getUid();
     //put if to make sure not null or chane to boolean
        ttsEnabled = getIntent().getBooleanExtra("ttsEnabled",true);
        Log.d("tts enable: ", String.valueOf(ttsEnabled));

    if (ttsEnabled) {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language for TextToSpeech
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported");
                    }
                } else {
                    Log.e(TAG, "Initialization failed");
                }
            }
        });
    }




        feedback =findViewById(R.id.txtFeedback);
        jabSelect = findViewById(R.id.jabposeselect);
        crossSelect =findViewById(R.id.crossPoseSelect);

        crossSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_pose = "cross";
                Log.d("te",selected_pose);
                startFeedbackUpdate(selected_pose);
            }
        });



        jabSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_pose = "jab";
                Log.d("te",selected_pose);
                startFeedbackUpdate(selected_pose);
            }
        });


        cameraDisplay = findViewById(R.id.camera_display);
        graphicOverlay = findViewById(R.id.graphic_overlay);


        camflip= findViewById(R.id.camFlip);
        camflip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Set facing");
                if (cameraSource != null) {
                    int currentface = cameraSource.getCameraFacing();
                    if(currentface==CameraSource.CAMERA_FACING_FRONT)
                    {cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);}
                    else if (currentface==CameraSource.CAMERA_FACING_BACK){
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                    }
                }
                cameraDisplay.stop();
                startCameraSource();
            }
        });

        requestCameraPermission();
        initSource();
        startCameraSource();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
          //  initSource();
          //  startCameraSource();
        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initSource();

        }
    }



           // issue with function being called simultaneously when another move is selected-results in flickering feedback
    private void startFeedbackUpdate(String given_pose) {
         if (handler == null) {
            handler = new Handler();
        }else {
             handler.removeCallbacksAndMessages(null);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update feedback text based on pose data
                evaluationFunctions = com.example.visiontest.evaluationFunctions.getInstance(camDisplay.this,given_pose);
                evalFeedback = evaluationFunctions.getFeedback(given_pose);
                feedback.setText(evalFeedback);
                if ((textToSpeech != null) && (!evalFeedback.equals(currentFeedback)) ) {
                    // Speak out the feedback

                    textToSpeech.speak(evaluationFunctions.getFeedback(given_pose), TextToSpeech.QUEUE_FLUSH, null, null);
                    currentFeedback = evalFeedback;
                }
                // Schedule the next execution
                handler.postDelayed(this, Interval_MS);
            }
        }, Interval_MS);
    }


    // Pre-defined Camera Functions
    private void initSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
       setProcessor(selected_pose);
        startCameraSource();
    }

    protected void setProcessor(String selected_pose) {

        AccuratePoseDetectorOptions.Builder builder =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE);

        builder.setPreferredHardwareConfigs(CPU_GPU);

        AccuratePoseDetectorOptions options = builder.build();

        PoseDetectorProcessor poseDetectorProcessor = new PoseDetectorProcessor(selected_pose,userID,this, camDisplay.this,options, true, true, true,true,true);
        cameraSource.setMachineLearningFrameProcessor(poseDetectorProcessor);
    }






    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (cameraDisplay == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                //callback.onCameraSourceStarted();
                cameraDisplay.start(cameraSource, graphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    //@Override
   /* public void onCameraSourceStarted() {
        evaluationFunctions = com.example.visiontest.evaluationFunctions.getInstance(camDisplay.this,selected_pose);
        feedback.setText(evaluationFunctions.getFeedback());
    } */
}