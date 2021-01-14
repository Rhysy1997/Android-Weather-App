package com.example.weatherandhygiene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {

    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    EditText editText_email;
    EditText editText_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar_login);
        progressBar.setVisibility(View.INVISIBLE);

        //animated gradient background
        ConstraintLayout constraintLayout = findViewById(R.id.layoutLogin);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    public void loginUser(View view) {
        //getting email and password from edit texts
        String email = ((EditText) findViewById(R.id.editText_log_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.editText_log_password)).getText().toString();

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
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            //display some message here
                            Toast.makeText(LoginActivity.this, "Login Successful",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            finish();
                        } else {
                            //display some message here
                            Toast.makeText(LoginActivity.this, "Login Error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void goRegister(View view){
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    public void skipLogin(View view){
        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
    }
}
