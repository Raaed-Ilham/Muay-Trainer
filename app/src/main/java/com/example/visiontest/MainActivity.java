package com.example.visiontest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";






    Button loginbtn;
    TextView signup,forgotPassword,workoutPlans;

    EditText Email;
    EditText Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        Email = findViewById(R.id.login_email);
        Password = findViewById(R.id.login_password);





        signup = findViewById(R.id.signupRedirectText);
       signup.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this, register.class);
               startActivity(intent);
           }
       });


        loginbtn = findViewById(R.id.login_button);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(Email.getText());
                String password = String.valueOf(Password.getText());
                if (email == null || email.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your email",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (password == null || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your password.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {signIn(email,password);}
            }
        });


    }

   // @Override
   /* public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
           Intent intent = new Intent(MainActivity.this, Dashboard.class);
           startActivity(intent);
        }

    } */


    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userID = user.getUid();
                            Intent intent = new Intent(MainActivity.this, Dashboard.class);
                            intent.putExtra("userID", userID);
                            createProfileEntry(userID,email);
                            //pass UID thru intent data
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Log.d(TAG,"sign in failed");
                            Toast.makeText(MainActivity.this, "Authentication failed.Invalid Credentials",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private void createProfileEntry(String userId, String email) {
        // Get a reference to the users node in the database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Create a new user object
        UserProfile userProfile = new UserProfile(email, "Default username", "Default profile picture URL");

        // Set the user object under the user's ID
        usersRef.child(userId).setValue(userProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("signup", "User profile created successfully");
                        } else {
                            Log.e("signup", "Error creating user profile", task.getException());
                        }
                    }
                });
    }


}