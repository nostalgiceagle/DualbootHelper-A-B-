package com.david42069.dualboothelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.ActivityUtils;

import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import androidx.preference.PreferenceManager;

import android.os.CountDownTimer;
import android.view.LayoutInflater;

import dev.oneuiproject.oneui.widget.CardView;

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);
        Shell.getShell(shell -> {});
        deleteFilesIfExist();
        cp(R.raw.parted, "parted");
        cp(R.raw.jq, "jq");
        ToolbarLayout toolbarLayout = findViewById(R.id.home);
        updateStatusCardView();
        updateSlotCardView(R.id.slota_txt, getSlotAFilePath(this));
        updateSlotCardView(R.id.slotb_txt, getSlotBFilePath(this));
        // Listen for preference changes
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if ("slotakey".equals(key)) {
                updateSlotCardView(R.id.slota_txt, "slotakey");
            } else if ("slotbkey".equals(key)) {
                updateSlotCardView(R.id.slotb_txt, "slotbkey");
            }
        });

        setupCardViewWithConfirmation(R.id.reboot_a, R.string.reboot_a, "R.raw.switcha");
        setupCardViewWithConfirmation(R.id.reboot_b, R.string.reboot_b, "R.raw.switchb");
        setupCardViewWithConfirmation(R.id.rec_a, R.string.recovery_a, "R.raw.switchar");
        setupCardViewWithConfirmation(R.id.rec_b, R.string.recovery_b, "R.raw.switchbr");
        setupCardViewWithConfirmation(R.id.bootloader, R.string.dl_mode, "R.raw.download");
        setupCardViewWithConfirmation(R.id.poweroff, R.string.poweroff, "R.raw.shutdown");
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

    private void updateSlotCardView(int cardViewId, String input) {
        String textToDisplay;

        // Determine if the input is a key or a file path
        if (input.equals("slotakey") || input.equals("slotbkey")) {
            // Handle it as a key (read from config.prop or fallback to default slotN.txt)
            String configPath = new File(getFilesDir(), "config.prop").getPath();
            textToDisplay = readConfigFile(configPath, input);

            if (textToDisplay == null) {
                // If config.prop doesn't have the value, read from the corresponding default slot file
                String filePath = getDefaultSlotFilePath(input);
                textToDisplay = readAndReplacePlaceholders(filePath, getString(R.string.unavailable));
            }
        } else {
            // Handle it as a file path
            textToDisplay = readAndReplacePlaceholders(input, getString(R.string.unavailable));
        }

        // Update the CardView
        CardView slotCardView = findViewById(cardViewId);
        slotCardView.setSummaryText(textToDisplay);
    }

    private String readAndReplacePlaceholders(String filePath, String defaultText) {
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    // Replace any placeholder with the appropriate value
                    line = line.replace("##UNAVAILABLE##", getString(R.string.unavailable));
                    content.append(line).append("\n");
                }

                // Trim trailing newlines and check for empty content
                String finalContent = content.toString().trim();
                return finalContent.isEmpty() ? defaultText : finalContent;
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading file: " + filePath, e);
            }
        }

        return defaultText; // Return default if the file does not exist or reading fails
    }

    private String readFileContent(String filePath, String defaultText) {
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }

                return content.toString().trim().isEmpty() ? defaultText : content.toString().trim();
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading file: " + filePath, e);
            }
        }

        return defaultText; // Return default if the file does not exist or reading fails
    }

    private String getDefaultSlotFilePath(String slotKey) {
        String fileName;
        switch (slotKey) {
            case "slotakey":
                fileName = "slota.txt";
                break;
            case "slotbkey":
                fileName = "slotb.txt";
                break;
            default:
                throw new IllegalArgumentException("Invalid slot key: " + slotKey);
        }
        return new File(getFilesDir(), fileName).getPath();
    }

    private String readConfigFile(String configPath, String key) {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            return null; // Config file does not exist
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming the format is key=value
                String[] parts = line.split("=", 2);
                if (parts.length == 2 && parts[0].trim().equals(key)) {
                    return parts[1].trim(); // Return the value for the key
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading config.prop", e);
        }
        return null; // Key not found
    }


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
        // Check for SU access using 'id -u'
        boolean hasSuAccess = Shell.cmd("id -u").exec().getOut().contains("0");

        if (!hasSuAccess) {
            // Show a dialog informing the user about missing SU access
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sudo_access_title)) // Use a title like "Permission Denied"
                    .setMessage(getString(R.string.sudo_access)) // Message about needing superuser access
                    .setPositiveButton(getString(R.string.dialog_ok), null) // OK button that does nothing
                    .create()
                    .show();
            return; // Exit the method since SU access is required
        }

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
        if (item.getItemId() == R.id.menu_app_info) {
            ActivityUtils.startPopOverActivity(this,
                    new Intent(this, AboutActivity.class),
                    null,
                    ActivityUtils.POP_OVER_POSITION_RIGHT | ActivityUtils.POP_OVER_POSITION_TOP);
            return true;
        } else if (item.getItemId() == R.id.menu_settings) { // Add this case for settings
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }
}