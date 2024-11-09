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
import android.os.Bundle;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


public class MainActivity extends AppCompatActivity {
    private String scriptContent;
            TextView status;
            TextView slota;
            TextView slotb;
    // updatedata.sh logic stuff - translation
    private void updateStatus() {
        // Fetch translated strings
        String notInstalledText = getString(R.string.not_installed);
        String installedTextV5 = getString(R.string.installed_v5);
        String installedTextV4 = getString(R.string.installed_v4);
        String unavailableText = getString(R.string.unavailable);
        String superPartitionText = getString(R.string.super_partition);
        String normalNamingText = getString(R.string.normal_naming);
        String capsNamingText = getString(R.string.caps_naming);
        String ufsSdaText = getString(R.string.ufs_sda);
        String emmcSdcText = getString(R.string.emmc_sdc);
        String emmcMmcblk0Text = getString(R.string.emmc_mmcblk0);

        // Load script content with translations injected
        scriptContent = ScriptToString(
            R.raw.updatedata,
            notInstalledText, installedTextV5, installedTextV4, unavailableText,
            superPartitionText, normalNamingText, capsNamingText,
            ufsSdaText, emmcSdcText, emmcMmcblk0Text
        );

        // Execute the script
        executeScript(scriptContent);
    }

    // COpy logic
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
    // Execute logic
    public void executeScript(String script) {
        try {
            // Use the full command with `su` and the desired script
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", script});

            // Capture the output
            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();

            // Read output for debugging
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));

            String line;
            while ((line = stdoutReader.readLine()) != null) {
                Log.d("ScriptOutput", line);
            }
            while ((line = stderrReader.readLine()) != null) {
                Log.e("ScriptError", line);
            }

            // Wait for the process to complete
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            Log.e("ScriptError", "Error executing script", e);
        }
    }

    // Script to string logic
    private String ScriptToString(
            int resourceId, String notInstalledText, String installedTextV5, String installedTextV4, String unavailableText,
            String superPartitionText, String normalNamingText, String capsNamingText,
            String ufsSdaText, String emmcSdcText, String emmcMmcblk0Text) {

        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder script = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                // Replace placeholders with translated strings
                line = line.replace("##NOT_INSTALLED##", notInstalledText)
                           .replace("##INSTALLED_V5##", installedTextV5)
                           .replace("##INSTALLED_V4##", installedTextV4)
                           .replace("##UNAVAILABLE##", unavailableText)
                           .replace("##SUPER_PARTITION##", superPartitionText)
                           .replace("##NORMAL_NAMING##", normalNamingText)
                           .replace("##CAPS_NAMING##", capsNamingText)
                           .replace("##UFS_SDA##", ufsSdaText)
                           .replace("##EMMC_SDC##", emmcSdcText)
                           .replace("##EMMC_MMCBLK0##", emmcMmcblk0Text);
                script.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            Log.e("ScriptToString", "Error reading script file", e);
        }

        return script.toString();
    }

    // Read script logic
    private String readScriptFromRaw(int resourceId) {
        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder scriptContent = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                scriptContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            Log.e("ScriptError", "Error reading script from raw resource", e);
        }
        return scriptContent.toString();
    }
    // Action on MaKing TOAST and DO
    private void mktoastdo(@StringRes int promptResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    
        String scriptContent;
        // because of translation, scripttostring requries manu arg. do this for now.
        switch (promptResId) {
            case R.string.reboot_a:
                scriptContent = ScriptToString(R.raw.switcha, "", "", "", "", "", "", "", "", "", "");
                break;
            case R.string.reboot_b:
                scriptContent = ScriptToString(R.raw.switchb, "", "", "", "", "", "", "", "", "", "");
                break;
            case R.string.recovery_a:
                scriptContent = ScriptToString(R.raw.switchar, "", "", "", "", "", "", "", "", "", "");
                break;
            case R.string.recovery_b:
                scriptContent = ScriptToString(R.raw.switchbr, "", "", "", "", "", "", "", "", "", "");
                break;
            case R.string.dl_mode:
                scriptContent = ScriptToString(R.raw.download, "", "", "", "", "", "", "", "", "", "");
                break;
            case R.string.poweroff:
                scriptContent = ScriptToString(R.raw.shutdown, "", "", "", "", "", "", "", "", "", "");
                break;
            default:
                scriptContent = ""; // or handle any unexpected case
                break;
        }
    
        if (!scriptContent.isEmpty()) {
            executeScript(scriptContent);
        }
    }

    public void confirmreboot(@StringRes int promptResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getString(promptResId) + "?";
        String message = getString(R.string.dialog_confirm);
        String positiveButton = getString(R.string.dialog_yes);
        String negativeButton = getString(R.string.dialog_no);

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, (dialog, which) -> mktoastdo(promptResId))
            .setNegativeButton(negativeButton, (dialog, which) -> {});
    
        AlertDialog alert = builder.create();
        alert.show();
    }



    public String readFileFromInternalStorage(String filetostr) {
		String fileContents = "";
		try {
			File file = new File(getFilesDir(), filetostr);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			StringBuilder stringBuilder = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}

			reader.close();
			fis.close();

			fileContents = stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContents;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cp(R.raw.parted, "parted");
		cp(R.raw.jq, "jq");
        findViewById(R.id.reboot_a).setOnClickListener(v -> confirmreboot(R.string.reboot_a));
        findViewById(R.id.reboot_b).setOnClickListener(v -> confirmreboot(R.string.reboot_b));
        findViewById(R.id.rec_a).setOnClickListener(v -> confirmreboot(R.string.recovery_a));
        findViewById(R.id.rec_b).setOnClickListener(v -> confirmreboot(R.string.recovery_b));
        findViewById(R.id.bootloader).setOnClickListener(v -> confirmreboot(R.string.dl_mode));
        findViewById(R.id.poweroff).setOnClickListener(v -> confirmreboot(R.string.poweroff));

        ToolbarLayout toolbarLayout = findViewById(R.id.home);
        toolbarLayout.setNavigationButtonAsBack();
        status = findViewById(R.id.status);
        slota = findViewById(R.id.slota_txt);
        slotb = findViewById(R.id.slotb_txt);
        scriptContent = ScriptToString(R.raw.updatedata);
		executeScript(scriptContent);
		CharSequence statusstring = readFileFromInternalStorage("status.txt");
		CharSequence slotastring = readFileFromInternalStorage("slota.txt");
		CharSequence slotbstring = readFileFromInternalStorage("slotb.txt");
		status.setText(statusstring);
		slota.setText(slotastring);
        slotb.setText(slotbstring);
		if (statusstring == "") {
			status.setText(R.string.sudo_access);
		}
		if (slotbstring == "") {
			slotb.setText(R.string.unavailable);
		}
		if (slotastring == "") {
			slota.setText(R.string.unavailable);
		}
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