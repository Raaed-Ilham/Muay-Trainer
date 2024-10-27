package com.example.visiontest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {

    private FirebaseAuth registerAuth;

    Button registerBtn;

    EditText Email;

    EditText Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerAuth = FirebaseAuth.getInstance();

        registerBtn = findViewById(R.id.Signup_button);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.register_password);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = String.valueOf(Email.getText());
                String password = String.valueOf(Password.getText());



                createAccount(email,password);
            }
        });


    }
    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        registerAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            Log.d("signup", "createUserWithEmail:success");
                            Toast.makeText(register.this, "successfully registered.",
                                    Toast.LENGTH_LONG).show();
                            // Get the newly created user
                            FirebaseUser user = registerAuth.getCurrentUser();
                            Intent intent = new Intent(register.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign up fails, display a message to the user.
                            Log.w("signup", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(register.this, "registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END create_user_with_email]
    }


}