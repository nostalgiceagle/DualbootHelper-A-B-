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
import java.io.IOException;
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

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            // Load values from files
            setDefaultFromFile("slotakey", "/cache/dualboot/database/slota.txt");
            setDefaultFromFile("slotbkey", "/cache/dualboot/database/slotb.txt");
        }

        private void setDefaultFromFile(String preferenceKey, String filePath) {
            EditTextPreference preference = findPreference(preferenceKey);
            if (preference == null) {
                Log.w(TAG, "Preference with key " + preferenceKey + " not found.");
                return;
            }

            // Check if the value is already set in SharedPreferences
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            if (!sharedPreferences.contains(preferenceKey)) {
                // Load value from the file
                String fileValue = readFileContent(filePath);
                if (fileValue != null) {
                    // Save the value to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(preferenceKey, fileValue);
                    editor.apply();

                    // Update the preference's value
                    preference.setText(fileValue);
                }
            }

            // Set the summary provider for real-time updates
            preference.setSummaryProvider(p -> {
                String value = preference.getText();
                return value != null ? value : getString(R.string.unavailable); // Fallback value
            });
        }

        private String readFileContent(String filePath) {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.w(TAG, "File " + filePath + " does not exist.");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString().trim(); // Remove trailing newlines
            } catch (IOException e) {
                Log.e(TAG, "Error reading file " + filePath, e);
                return null;
            }
        }
    }
}
