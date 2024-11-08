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

public class MainActivity extends AppCompatActivity {
    private String scriptContent;
            TextView status;
            TextView slota;
            TextView slotb;
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
    private String ScriptToString(int resourceId) {
        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder script = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
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
    private void mktoastdo(String prompt) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Rebooting... Please kindly wait.");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if (prompt.equals("Reboot to Slot A")) {
            String scriptContent = ScriptToString(R.raw.switcha);
            executeScript(scriptContent);
        } else if (prompt.equals("Reboot to Slot B")) {
            String scriptContent = ScriptToString(R.raw.switchb);
            executeScript(scriptContent);
        } else if (prompt.equals("Reboot to Recovery Slot A")) {
            String scriptContent = ScriptToString(R.raw.switchar);
            executeScript(scriptContent);
        } else if (prompt.equals("Reboot to Recovery Slot B")) {
            String scriptContent = ScriptToString(R.raw.switchbr);
            executeScript(scriptContent);
        } else if (prompt.equals("Reboot to Download Mode")) {
            String scriptContent = ScriptToString(R.raw.download);
            executeScript(scriptContent);
        } else if (prompt.equals("Shut Down")) {
            String scriptContent = ScriptToString(R.raw.shutdown);
            executeScript(scriptContent);
        }
        // Continue with other conditions as needed
    }

    public void confirmreboot(String prompt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(prompt + "?")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> mktoastdo(prompt))
            .setNegativeButton("Cancel", (dialog, which) -> {
            });
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
        findViewById(R.id.reboot_a).setOnClickListener(v -> confirmreboot("Reboot to Slot A"));
        findViewById(R.id.reboot_b).setOnClickListener(v -> confirmreboot("Reboot to Slot B"));
        findViewById(R.id.rec_a).setOnClickListener(v -> confirmreboot("Reboot to Recovery Slot A"));
        findViewById(R.id.rec_b).setOnClickListener(v -> confirmreboot("Reboot to Recovery Slot B"));
        findViewById(R.id.bootloader).setOnClickListener(v -> confirmreboot("Reboot to Download Mode"));
        findViewById(R.id.poweroff).setOnClickListener(v -> confirmreboot("Shut Down"));

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
			status.setText("Please grant superuser access to gather info about the device");
		}
		if (slotbstring == "") {
			slotb.setText("UNKNOWN");
		}
		if (slotastring == "") {
			slota.setText("UNKNOWN");
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