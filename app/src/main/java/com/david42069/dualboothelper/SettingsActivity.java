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
        ToolbarLayout toolbarLayout = findViewById(R.id.settings);
        toolbarLayout.setNavigationButtonAsBack();
        // Load the preferences fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences sharedPreferences;
        private Context mContext;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = getContext();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(getResources().getColor(R.color.oui_background_color, mContext.getTheme()));
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            registerPreferenceChangeListener();
        }

        private void registerPreferenceChangeListener() {
            sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                if ("slotakey".equals(key)) {
                    handlePreferenceChange("slotakey", "slota.txt");
                } else if ("slotbkey".equals(key)) {
                    handlePreferenceChange("slotbkey", "slotb.txt");
                }
            });
        }

        private void handlePreferenceChange(String key, String fileName) {
            String value = sharedPreferences.getString(key, "");
            if (value == null || value.trim().isEmpty()) {
                // Reset to the default value from the file or fallback
                String defaultValue = readValueFromFile(fileName);
                if (defaultValue == null || defaultValue.isEmpty()) {
                    defaultValue = getString(R.string.unavailable);
                }
                sharedPreferences.edit().putString(key, defaultValue).apply();
            }
        }

        private String readValueFromFile(String fileName) {
            File file = new File(requireContext().getFilesDir(), fileName);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    return reader.readLine();
                } catch (IOException e) {
                    Log.e("SettingsFragment", "Error reading file: " + fileName, e);
                }
            }
            return null;
        }
    }
}