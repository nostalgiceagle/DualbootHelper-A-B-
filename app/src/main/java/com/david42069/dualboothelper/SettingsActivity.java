package com.david42069.dualboothelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.preference.EditTextPreference;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // A layout that hosts the fragment
        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setNavigationButtonAsBack();

        // Load the preferences fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String CONFIG_FILE = "config.prop";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String value = sharedPreferences.getString(key, null);
            if (value != null) {
                writeConfigFile(key, value);
            }
        }

        private void writeConfigFile(String key, String value) {
            File configFile = new File(requireContext().getFilesDir(), CONFIG_FILE);
            try {
                // Read existing config into memory
                Map<String, String> config = readConfigFile(configFile);

                // Update the key-value pair
                config.put(key, value);

                // Write back the updated configuration
                try (FileWriter writer = new FileWriter(configFile)) {
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                    }
                }
            } catch (IOException e) {
                Log.e("SettingsFragment", "Error writing to config.prop", e);
            }
        }

        private Map<String, String> readConfigFile(File configFile) throws IOException {
            Map<String, String> config = new HashMap<>();
            if (configFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            config.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }
            return config;
        }
    }
}