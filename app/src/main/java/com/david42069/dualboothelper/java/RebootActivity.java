package com.david42069.dualboothelper;

// from OneUI Sample app. Credits to everyone who contributed in making the app.

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.david42069.dualboothelper.BuildConfig;
import com.david42069.dualboothelper.R;
import com.david42069.dualboothelper.databinding.ActivityAboutBinding;
import com.david42069.dualboothelper.databinding.ActivityAboutContentBinding;

import dev.oneuiproject.oneui.utils.ViewUtils;
import dev.oneuiproject.oneui.utils.internal.ToolbarLayoutUtils;
import dev.oneuiproject.oneui.widget.Toast;

public class RebootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot);
    }

    @Override
    public void onBackPressed() {
        // Override the back button to do nothing
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
    }
}
