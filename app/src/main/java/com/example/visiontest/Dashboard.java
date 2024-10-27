package com.example.visiontest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

public class Dashboard extends AppCompatActivity {

    CardView drillCard, practice , progressCard;

    CardView settings;
    TextView progress;
    FirebaseDatabase database;
    // create child of user id and store object of user profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        String UID = getIntent().getStringExtra("userID");
        Boolean ttsEnabled = getIntent().getBooleanExtra("ttsEnabled",true);



        practice = findViewById(R.id.drillCard);
        drillCard = findViewById(R.id.workoutCard);
        settings = findViewById(R.id.settingCard);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, Settings.class);
                startActivity(intent);
            }
        });

        progress =findViewById(R.id.Progress);
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, viewProgress.class);
                intent.putExtra("userID", UID);
                startActivity(intent);
            }
        });

        practice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, camDisplay.class);
                intent.putExtra("userID", UID);
                intent.putExtra("ttsEnabled",ttsEnabled);

                startActivity(intent);

            }
        });


        drillCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, drills.class);
                intent.putExtra("userID", UID);
                startActivity(intent);
            }
        });

    }


}