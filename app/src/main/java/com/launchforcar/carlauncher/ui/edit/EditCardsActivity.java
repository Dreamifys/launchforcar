package com.launchforcar.carlauncher.ui.edit;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.data.local.LauncherPreferences;
import com.launchforcar.carlauncher.databinding.ActivityEditCardsBinding;

public class EditCardsActivity extends AppCompatActivity {

    private ActivityEditCardsBinding binding;
    private LauncherPreferences launcherPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCardsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        launcherPreferences = new LauncherPreferences(this);

        binding.backButton.setOnClickListener(v -> finish());
        bindState();
        bindListeners();
    }

    private void bindState() {
        binding.musicCardCheckbox.setChecked(launcherPreferences.isMusicCardVisible());
        binding.contactCardCheckbox.setChecked(launcherPreferences.isContactCardVisible());
        binding.statsCardCheckbox.setChecked(launcherPreferences.isStatsCardVisible());
    }

    private void bindListeners() {
        binding.musicCardCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launcherPreferences.setMusicCardVisible(isChecked);
            }
        });

        binding.contactCardCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launcherPreferences.setContactCardVisible(isChecked);
            }
        });

        binding.statsCardCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launcherPreferences.setStatsCardVisible(isChecked);
            }
        });
    }
}