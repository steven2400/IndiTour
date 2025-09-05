package com.example.inditour;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is logged in → go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Not logged in → go to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // close LauncherActivity
    }
}
