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

    public class SettingsFragment extends PreferenceFragmentCompat {

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            // Load values from files or create the files if missing
            initializePreferenceFromFile("slotakey", "/cache/dualboot/database/slotA.txt", "slotakey");
            initializePreferenceFromFile("slotbkey", "/cache/dualboot/database/slotB.txt", "slotbkey");
        }

        private void initializePreferenceFromFile(String preferenceKey, String filePath, String defaultContent) {
            EditTextPreference preference = findPreference(preferenceKey);
            if (preference == null) {
                Log.w(TAG, "Preference with key " + preferenceKey + " not found.");
                return;
            }

            // Check if the value is already set in SharedPreferences
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            if (!sharedPreferences.contains(preferenceKey)) {
                // Load value from the file or create the file if it doesn't exist
                String fileValue = ensureFileExistsAndRead(filePath, defaultContent);

                // Save the value to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(preferenceKey, fileValue);
                editor.apply();

                // Update the preference's value
                preference.setText(fileValue);
            }

            // Set the summary provider for real-time updates
            preference.setSummaryProvider(p -> {
                String value = preference.getText();
                return value != null ? value : getString(R.string.unavailable); // Fallback value
            });
        }

        private String ensureFileExistsAndRead(String filePath, String defaultContent) {
            File file = new File(filePath);
            if (!file.exists()) {
                // Create the file and write default content
                try {
                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                        file.getParentFile().mkdirs(); // Create parent directories if needed
                    }
                    if (file.createNewFile()) {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(defaultContent.getBytes());
                        }
                    }
                    Log.d(TAG, "Created file at " + filePath + " with default content: " + defaultContent);
                } catch (IOException e) {
                    Log.e(TAG, "Error creating file at " + filePath, e);
                    return defaultContent; // Return default content as a fallback
                }
            }

            // Read and return the file content
            return readFileContent(filePath);
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
                    content.append(line).append("\n");
                }
                return content.toString().trim(); // Remove trailing newlines
            } catch (IOException e) {
                Log.e(TAG, "Error reading file " + filePath, e);
                return null;
            }
        }
    }
}
