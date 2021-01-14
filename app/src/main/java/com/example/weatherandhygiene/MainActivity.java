package com.example.weatherandhygiene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar_Userreg);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void registerUser(View view) {
        //getting email and password from edit texts
        String email = ((EditText) findViewById(R.id.editText_userEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.editText_password)).getText().toString();

        //checking if email and password are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }
        //if email and password are empty, display progress bar
        progressBar.setVisibility(View.VISIBLE);
        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            //display some message here
                            Toast.makeText(MainActivity.this, "Successfully Registered",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            //display some message here
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(MainActivity.this, "Failed Registration" + e.getMessage()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void goLogin(View view){
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }


}

