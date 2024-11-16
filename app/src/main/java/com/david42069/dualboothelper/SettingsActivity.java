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

            // Dynamically set default values for slotA and slotB preferences
            setupPreference("slotakey", "slota.txt");
            setupPreference("slotbkey", "slotb.txt");
        }

        private void setupPreference(String preferenceKey, String defaultValueFileName) {
            EditTextPreference preference = findPreference(preferenceKey);
            if (preference != null) {
                // Set the initial value from shared preferences or file
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String currentValue = sharedPreferences.getString(preferenceKey, null);

                if (currentValue == null || currentValue.isEmpty()) {
                    // Read the default value from the file if preference is not set
                    String filePath = new File(requireContext().getFilesDir(), defaultValueFileName).getPath();
                    currentValue = readFileContent(filePath, getString(R.string.unavailable));
                    preference.setText(currentValue);
                    sharedPreferences.edit().putString(preferenceKey, currentValue).apply();
                }

                // Update slot text in real-time when the preference changes
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    // Save the updated value
                    sharedPreferences.edit().putString(preferenceKey, newValue.toString()).apply();

                    // Notify MainActivity to update the slot
                    notifySlotUpdate(preferenceKey, newValue.toString());
                    return true; // Allow the value to update
                });
            }
        }

        private String readFileContent(String filePath, String fallbackValue) {
            File file = new File(filePath);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    return content.toString().trim();
                } catch (IOException e) {
                    Log.e("SettingsFragment", "Error reading file: " + filePath, e);
                }
            }
            return fallbackValue;
        }

        private void notifySlotUpdate(String preferenceKey, String updatedValue) {
            // Communicate the change to MainActivity
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if ("slotakey".equals(preferenceKey)) {
                    mainActivity.updateSlotCardView(R.id.slota_txt, updatedValue);
                } else if ("slotbkey".equals(preferenceKey)) {
                    mainActivity.updateSlotCardView(R.id.slotb_txt, updatedValue);
                }
            }
        }
    }
}