package com.david42069.dualboothelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.ActivityUtils;

import android.app.Activity;
import com.topjohnwu.superuser.Shell;
import android.os.Bundle;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import android.view.View;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import android.graphics.Color;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import android.os.CountDownTimer;
import android.net.Uri;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.LayoutInflater;
import androidx.annotation.StringRes;
import dev.oneuiproject.oneui.widget.ui.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static final String STATUS_FILE_PATH = "/data/data/com.david42069.dualboothelper/files/status.txt";
    private static final String SLOT_A_FILE_PATH = "/data/data/com.david42069.dualboothelper/files/slota.txt";
    private static final String SLOT_B_FILE_PATH = "/data/data/com.david42069.dualboothelper/files/slotb.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shell.getShell(shell -> {});
        ToolbarLayout toolbarLayout = findViewById(R.id.home);
        updateStatusCardView();
        updateSlotCardView(R.id.slota_txt, SLOT_A_FILE_PATH);
        updateSlotCardView(R.id.slotb_txt, SLOT_B_FILE_PATH);

        setupButtonWithConfirmation(R.id.reboot_a, R.string.reboot_a, "switcha.sh");
        setupButtonWithConfirmation(R.id.reboot_b, R.string.reboot_b, "switchb.sh");
        setupButtonWithConfirmation(R.id.rec_a, R.string.recovery_a, "switchar.sh");
        setupButtonWithConfirmation(R.id.rec_b, R.string.recovery_b, "switchbr.sh");
        setupButtonWithConfirmation(R.id.bootloader, R.string.dl_mode, "download.sh");
        setupButtonWithConfirmation(R.id.poweroff, R.string.poweroff, "shutdown.sh");
    }

    private void updateStatusCardView() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(STATUS_FILE_PATH)))) {
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

                statusText.append(line).append("\n");
            }

            CardView statusCardView = findViewById(R.id.status);
            statusCardView.setSummaryText(statusText.toString().trim());
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading status.txt", e);
        }
    }

    private void updateSlotCardView(int cardViewId, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            StringBuilder slotText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                slotText.append(line).append("\n");
            }

            CardView slotCardView = findViewById(cardViewId);
            slotCardView.setSummaryText(slotText.toString().trim());
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading " + filePath, e);
        }
    }

    private void setupButtonWithConfirmation(int buttonId, int promptResId, String scriptFile) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> showConfirmationDialog(promptResId, scriptFile));
    }
    private void showConfirmationDialog(int promptResId, String scriptFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getString(promptResId) + "?";
        String message = getString(R.string.dialog_confirm);
        String positiveButton = getString(R.string.dialog_yes);
        String negativeButton = getString(R.string.dialog_no);

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, (dialog, which) -> {
                showLoadingDialog();
                executeShellCommand(scriptFile);
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

    private void executeShellCommand(String scriptFile) {
        Shell.cmd("/data/data/com.david42069.dualboothelper/files/" + scriptFile)
            .submit(result -> {
                if (result.isSuccess()) {
                    Log.i("MainActivity", "Execution successful for script: " + scriptFile);
                } else {
                    Log.e("MainActivity", "Execution failed for script: " + scriptFile);
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
        }
        return false;
    }
}