package com.launchforcar.carlauncher.ui.settings;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.data.local.LauncherPreferences;
import com.launchforcar.carlauncher.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private LauncherPreferences launcherPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        launcherPreferences = new LauncherPreferences(this);

        binding.backButton.setOnClickListener(v -> finish());
        bindState();
        bindListeners();
    }

    private void bindState() {
        binding.autoNightCheckbox.setChecked(launcherPreferences.isAutoNightEnabled());
        binding.sceneAutomationCheckbox.setChecked(launcherPreferences.isSceneAutomationEnabled());
    }

    private void bindListeners() {
        binding.autoNightCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launcherPreferences.setAutoNightEnabled(isChecked);
            }
        });

        binding.sceneAutomationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launcherPreferences.setSceneAutomationEnabled(isChecked);
            }
        });
    }
}