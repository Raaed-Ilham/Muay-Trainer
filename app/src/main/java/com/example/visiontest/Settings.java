package com.example.visiontest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.window.OnBackInvokedDispatcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;

public class Settings extends AppCompatActivity {

    ToggleButton ttstoggle;
    Boolean ttsEnabled ;
    EditText newEmail, newPassword;
    String newMail,newPass,userID;
    Button dashboardReturn , EditDetails;
    FirebaseUser firebaseUser;
    DatabaseReference usersRef;

    private FirebaseAuth authCurrent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ttstoggle =findViewById(R.id.toggleButton);
        dashboardReturn =findViewById(R.id.dashboard_return);
        EditDetails = findViewById(R.id.change_Details);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        authCurrent = FirebaseAuth.getInstance();
         firebaseUser = authCurrent.getCurrentUser();
        userID = firebaseUser.getUid();


        //newMail = newEmail.getText().toString();
     //   newPass = newPassword.getText().toString();


        EditDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPassword();
            }
        });

        ttstoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                { ttsEnabled = true;}
                else {ttsEnabled = false;}

            }
        });



        dashboardReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this,Dashboard.class);
                intent.putExtra("ttsEnabled",ttsEnabled);
                startActivity(intent);
            }
        });

    }

    private void ResetPassword() {

        authCurrent.sendPasswordResetEmail(firebaseUser.getEmail())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Settings.this, "Reset Password link has been sent to your registered Email", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Settings.this, "Error :- " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }


}