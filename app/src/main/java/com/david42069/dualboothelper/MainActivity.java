package com.david42069.dualboothelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.ActivityUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.topjohnwu.superuser.Shell;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import android.view.View;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AlertDialog;

import android.os.CountDownTimer;
import android.view.LayoutInflater;

import dev.oneuiproject.oneui.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        // Update slot cards to reflect any changes
        updateSlotCardView(R.id.slota_txt, sharedPreferences.getString("slotakey", getString(R.string.unavailable)));
        updateSlotCardView(R.id.slotb_txt, sharedPreferences.getString("slotbkey", getString(R.string.unavailable)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public static class RootChecker {
        private static boolean isRootAvailable = false;

        public static boolean isRootAvailable() {
            return isRootAvailable;
        }

        public static void checkRoot() {
            // Check for root access
            isRootAvailable = checkForRoot();
        }

        private static boolean checkForRoot() {
            try {
                // Execute the command to check for root access
                String output = Shell.cmd("id -u").exec().getOut().get(0);
                return "0".equals(output);
            } catch (Exception e) {
                // Handle exceptions (e.g., security exceptions)
                e.printStackTrace();
            }
            return false;
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (sharedPreferences, key) -> {
                if (key.equals("slotakey")) {
                    updateSlotCardView(R.id.slota_txt, sharedPreferences.getString(key, getString(R.string.unavailable)));
                } else if (key.equals("slotbkey")) {
                    updateSlotCardView(R.id.slotb_txt, sharedPreferences.getString(key, getString(R.string.unavailable)));
                }
            };

    private void updateSlotCardView(int cardViewId, String slotValue) {
        CardView slotCardView = findViewById(cardViewId);
        if (slotCardView != null) {
            if (slotValue != null) {
                slotValue = slotValue.replace("##UNAVAILABLE##", getString(R.string.unavailable));
            }

            slotCardView.setSummaryText(slotValue != null && !slotValue.trim().isEmpty() ? slotValue : getString(R.string.unavailable));
        }
    }

    private static String getStatusFilePath(Context context) {
        return new File(context.getFilesDir(), "status.txt").getPath();
    }

    public static String getSlotAFilePath(Context context) {
        return new File(context.getFilesDir(), "slota.txt").getPath();
    }

    public static String getSlotBFilePath(Context context) {
        return new File(context.getFilesDir(), "slotb.txt").getPath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootChecker.checkRoot();
        setContentView(R.layout.activity_main);
        Shell.getShell(shell -> {});
        ToolbarLayout toolbarLayout = findViewById(R.id.home);
        // Check root
        if (RootChecker.isRootAvailable()) {
            Shell.getShell(shell -> {});
            deleteFilesIfExist();
            updateStatusCardView();
            cp(R.raw.parted, "parted");
            cp(R.raw.jq, "jq");
            cp(R.raw.slotatwrp, "slota.zip");
            cp(R.raw.slotbtwrp, "slotb.zip");

            // Ensure preferences are initialized with file values on first launch
            initializePreferencesFromFile();


            // Update UI with the latest values
            updateSlotCardView(R.id.slota_txt, getPreferenceValue("slotakey", getString(R.string.unavailable)));
            updateSlotCardView(R.id.slotb_txt, getPreferenceValue("slotbkey", getString(R.string.unavailable)));
        } else {
            CardView statusCV = findViewById(R.id.status);
            statusCV.setSummaryText(getString(R.string.sudo_access));
            Log.e("MainActivity", "No root! Proceeding in safe mode" );
        }
        // Perform normal tasks
        setupCardViewWithConfirmation(R.id.reboot_a, R.string.reboot_a, "R.raw.switcha");
        setupCardViewWithConfirmation(R.id.reboot_b, R.string.reboot_b, "R.raw.switchb");
        setupCardViewWithConfirmation(R.id.rec_a, R.string.recovery_a, "R.raw.switchar");
        setupCardViewWithConfirmation(R.id.rec_b, R.string.recovery_b, "R.raw.switchbr");
        setupCardViewWithConfirmation(R.id.bootloader, R.string.dl_mode, "R.raw.download");
        setupCardViewWithConfirmation(R.id.poweroff, R.string.poweroff, "R.raw.shutdown");
    }

    // Helper function to read preference value with fallback
    private String getPreferenceValue(String key, String fallback) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, fallback);
    }

    // Initialize preferences from file on first launch
    private void initializePreferencesFromFile() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.contains("slotakey")) {
            String slotAValue = readValueFromFile("slota.txt");
            sharedPreferences.edit().putString("slotakey", slotAValue != null ? slotAValue : getString(R.string.unavailable)).apply();
        }

        if (!sharedPreferences.contains("slotbkey")) {
            String slotBValue = readValueFromFile("slotb.txt");
            sharedPreferences.edit().putString("slotbkey", slotBValue != null ? slotBValue : getString(R.string.unavailable)).apply();
        }
    }

    // Read a value from a file
    private String readValueFromFile(String fileName) {
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading file: " + fileName, e);
            }
        }
        return null;
    }

    private void registerPreferenceChangeListener() {
        preferenceChangeListener = (sharedPreferences, key) -> {
            if ("slotakey".equals(key)) {
                String updatedValue = sharedPreferences.getString(key, getString(R.string.unavailable));
                updateSlotCardView(R.id.slota_txt, updatedValue);
            } else if ("slotbkey".equals(key)) {
                String updatedValue = sharedPreferences.getString(key, getString(R.string.unavailable));
                updateSlotCardView(R.id.slotb_txt, updatedValue);
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void deleteFilesIfExist() {
        // Define the file paths using the current context
        String[] filePaths = {
                getStatusFilePath(this),
                getSlotAFilePath(this),
                getSlotBFilePath(this)
        };

        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d("MainActivity", "Deleted file: " + path);
                } else {
                    Log.e("MainActivity", "Failed to delete file: " + path);
                }
            }
        }
    }

    private void cp(int resourceId, String fileName) {
        try (InputStream in = getResources().openRawResource(resourceId);
             OutputStream out = new FileOutputStream(new File(getFilesDir(), fileName))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("FileCopyError", "Error copying file " + fileName, e);
        }
    }

    private void updateStatusCardView() {
        Shell.cmd(getResources().openRawResource(R.raw.updatedata)).exec();
        File statusFile = new File(getStatusFilePath(this));
        String textToDisplay;

        if (statusFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(statusFile))) {
                StringBuilder statusText = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.replace("##NOT_INSTALLED##", getString(R.string.not_installed))
                            .replace("##INSTALLED_V5##", getString(R.string.installed_v5))
                            .replace("##INSTALLED_V4##", getString(R.string.installed_v4))
                            .replace("##UNAVAILABLE##", getString(R.string.unavailable))
                            .replace("##SUPER_PARTITION##", getString(R.string.super_partition))
                            .replace("##NORMAL_NAMING##", getString(R.string.normal_naming))
                            .replace("##CAPS_NAMING##", getString(R.string.caps_naming))
                            .replace("##UFS_SDA##", getString(R.string.ufs_sda))
                            .replace("##EMMC_SDC##", getString(R.string.emmc_sdc))
                            .replace("##EMMC_MMCBLK0##", getString(R.string.emmc_mmcblk0));

                    // Format each line as a list item with a bullet point
                    if (!line.trim().isEmpty()) {
                        statusText.append("- ").append(line.trim()).append("\n");
                    }
                }

                // Remove any trailing newline, if present
                if (statusText.length() > 0 && statusText.charAt(statusText.length() - 1) == '\n') {
                    statusText.setLength(statusText.length() - 1);
                }

                textToDisplay = statusText.toString().trim().isEmpty() ? getString(R.string.sudo_access) : statusText.toString();
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading status.txt", e);
                textToDisplay = getString(R.string.sudo_access);  // Placeholder if reading fails
            }
        } else {
            textToDisplay = getString(R.string.sudo_access);  // Placeholder if file does not exist
        }

        CardView statusCardView = findViewById(R.id.status);
        statusCardView.setSummaryText(textToDisplay);
    }

//    public void notifySlotUpdate(String preferenceKey, String updatedValue) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//
//        // Update the preference value
//        sharedPreferences.edit().putString(preferenceKey, updatedValue).apply();
//
//        // Update the corresponding card view
//        if ("slotakey".equals(preferenceKey)) {
//            updateSlotCardView(R.id.slota_txt, preferenceKey);
//        } else if ("slotbkey".equals(preferenceKey)) {
//            updateSlotCardView(R.id.slotb_txt, preferenceKey);
//        }
//    }

    private void setupCardViewWithConfirmation(int cardViewId, int promptResId, String scriptFile) {
        CardView cardView = findViewById(cardViewId);
        cardView.setOnClickListener(v -> showConfirmationDialog(promptResId, scriptFile));
    }

    public void executescriptstr(String scriptFile){
        new CountDownTimer(100,100){
            @Override
            public void onTick(long p1){

            }
            @Override
            public void onFinish(){
                executeShellCommand(scriptFile);
            }
        }.start();
    }

    private void showConfirmationDialog(int promptResId, String scriptFile) {

        if (!RootChecker.isRootAvailable()) {
            // Show a dialog informing the user about missing SU access
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sudo_access_title)) // Use a title like "Permission Denied"
                    .setMessage(getString(R.string.sudo_access)) // Message about needing superuser access
                    .setPositiveButton(getString(R.string.dialog_ok), null) // OK button that does nothing
                    .create()
                    .show();
            return; // Exit the method since SU access is required
        } else {
            // If SU access is granted, proceed with the normal confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String title = getString(promptResId) + "?";
            String message = getString(R.string.dialog_confirm);
            String positiveButton = getString(R.string.dialog_yes);
            String negativeButton = getString(R.string.dialog_no);

            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveButton, (dialog, which) -> {
                        showLoadingDialog();
                        executescriptstr(scriptFile);
                    })
                    .setNegativeButton(negativeButton, null);

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void executeShellCommand(String scriptFile) {
        executorService.execute(() -> {
            try {
                // Run the shell command in a background thread
                Shell.cmd(getResources().openRawResource(getResources().getIdentifier(scriptFile.replace("R.raw.", ""), "raw", getPackageName()))).exec();
            } catch (Exception e) {
                Log.e("MainActivity", "Error executing shell command", e);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                // Navigate to Settings Activity
                ActivityUtils.startPopOverActivity(this,
                        new Intent(this, SettingsActivity.class),
                        null,
                        ActivityUtils.POP_OVER_POSITION_RIGHT | ActivityUtils.POP_OVER_POSITION_TOP);
                return true;
            case R.id.about_app:
                // Navigate to About Activity
                ActivityUtils.startPopOverActivity(this,
                        new Intent(this, AboutActivity.class),
                        null,
                        ActivityUtils.POP_OVER_POSITION_RIGHT | ActivityUtils.POP_OVER_POSITION_TOP);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister listener to prevent memory leaks
        if (preferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }
}