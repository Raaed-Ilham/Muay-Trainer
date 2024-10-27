package com.example.visiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class drills extends AppCompatActivity {

    Button completeDrillBtn;
    Date currentDate;
    private FirebaseAuth authCurrent;
    String userID,currentDateStr;
    int drillReps =0;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

    String dateNum= dateFormat.format(new Date());





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drills);
        authCurrent = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authCurrent.getCurrentUser();
        userID = firebaseUser.getUid();
        completeDrillBtn = findViewById(R.id.drillCompleteBtn);
        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference("Progress").child(userID);





        completeDrillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Progress progress = new Progress();
               // int drillReps = progressRef.child(userID).child(currentDateStr).get();
                progressRef.child(dateNum).child("DutchDrills").setValue(drillReps);
                drillReps++;

            }
        });

        WebView webView = findViewById(R.id.webView); // replace with your WebView ID
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://drive.google.com/file/d/1e9bg90NkmDkzQhNMpajnVVHTtIOoJGEo/view?usp=sharing");

    }
}