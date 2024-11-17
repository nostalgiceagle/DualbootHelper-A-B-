package com.david42069.dualboothelper;

import static androidx.core.content.ContentProviderCompat.requireContext;

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

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String CONFIG_FILE = "config.prop";


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            // Dynamically initialize preferences
            initializePreferenceFromFile("slotakey", "slota.txt", getString(R.string.unavailable));
            initializePreferenceFromFile("slotbkey", "slotb.txt", getString(R.string.unavailable));
        }

        private void initializePreferenceFromFile(String key, String fileName, String fallbackValue) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String currentValue = sharedPreferences.getString(key, null);

            if (currentValue == null) {
                // Read from the file
                String fileValue = readValueFromFile(fileName);

                // Determine the value to set
                String valueToSet = (fileValue == null || fileValue.trim().isEmpty()) ? fallbackValue : fileValue;

                // Save to SharedPreferences
                sharedPreferences.edit().putString(key, valueToSet).apply();

                // Update UI
                EditTextPreference preference = findPreference(key);
                if (preference != null) {
                    preference.setText(valueToSet);
                }
            }
        }

        private String readValueFromFile(String fileName) {
            File file = new File(requireContext().getFilesDir(), fileName);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    return reader.readLine();
                } catch (IOException e) {
                    Log.e("SettingsActivity", "Error reading file: " + fileName, e);
                }
            }
            return null;
        }
    }
}