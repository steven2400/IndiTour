package com.example.inditour;

import android.content.Intent;
import android.os.Bundle;

import com.example.inditour.databinding.ActivityIntroBinding;

public class IntroActivity extends BaseActivity{

    ActivityIntroBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.introBtn.setOnClickListener(v -> startActivity(new Intent(IntroActivity.this, MainActivity.class)));
    }
}