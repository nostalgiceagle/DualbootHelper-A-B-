package com.david42069.dualboothelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
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

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            // Set default values dynamically based on MainActivity data
            EditTextPreference slotAPref = findPreference("slotakey");
            EditTextPreference slotBPref = findPreference("slotbkey");

            if (slotAPref != null) {
                slotAPref.setDefaultValue(MainActivity.getSlotAFilePath(requireContext())); // Dynamic default
                slotAPref.setSummaryProvider(preference -> {
                    String value = slotAPref.getText();
                    return value != null ? value : MainActivity.getSlotAFilePath(requireContext());
                });
            }

            if (slotBPref != null) {
                slotBPref.setDefaultValue(MainActivity.getSlotBFilePath(requireContext())); // Dynamic default
                slotBPref.setSummaryProvider(preference -> {
                    String value = slotBPref.getText();
                    return value != null ? value : MainActivity.getSlotBFilePath(requireContext());
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // Register listener for preference changes
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Unregister listener to avoid memory leaks
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("slotakey".equals(key)) {
                String value = sharedPreferences.getString(key, "");
                saveToFile("/cache/dualboot/database/slota.txt", value);
            } else if ("slotbkey".equals(key)) {
                String value = sharedPreferences.getString(key, "");
                saveToFile("/cache/dualboot/database/slotb.txt", value);
            }
        }

        private void saveToFile(String filePath, String content) {
            File file = new File(filePath);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes());
                Log.d(TAG, "Saved content to file: " + filePath);
            } catch (IOException e) {
                Log.e(TAG, "Failed to save content to file: " + filePath, e);
            }
        }
    }
}
