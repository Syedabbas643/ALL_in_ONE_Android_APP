package com.example.gamer;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class permissionActivity extends AppCompatActivity {

    private Button storagebutton;
    private Button notifybutton;
    private Button batterybutton;
    private Button locationbutton;
    private Handler handler;
    private boolean isHandlerRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        storagebutton = findViewById(R.id.storageButton);
        notifybutton = findViewById(R.id.notificationButton);
        batterybutton = findViewById(R.id.batteryOptimizationButton);
        locationbutton = findViewById(R.id.locationButton);

        handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isHandlerRunning) {
                    return;
                }
                checkStoragePermission();
                checkLocationPermission();
                checkNotificationPermission();
                checkBatteryOptimizationPermission();
                if (storagePermissionGranted() && locationPermissionGranted() && notificationPermissionGranted() && batteryOptimizationPermissionGranted()) {
                    isHandlerRunning = false;
                    moveToMainActivity();
                }
                handler.postDelayed(this, 3000);
            }
        });
    }
    private void moveToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back
    }

    private void checkNotificationPermission() {

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                setTickMark(notifybutton);
            } else {
                notifybutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestNotificationPermission();
                    }
                });
            }
        } else {
            setTickMark(notifybutton);
        }
    }

    private void checkStoragePermission() {
        if (SDK_INT >= Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager()) {
                setTickMark(storagebutton);
            }else {
                storagebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestStoragePermission();
                    }
                });
            }
        } else {
            int write = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);

            if (write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED){
                setTickMark(storagebutton);
            }else {
                storagebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestStoragePermission();
                    }
                });
            }
        }
    }

    private void checkBatteryOptimizationPermission() {
        if (isBatteryOptimizationDisabled()) {
            setTickMark(batterybutton);
        } else {
            batterybutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestBatteryOptimizationPermission();
                }
            });
        }
    }

    private void checkLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setTickMark(locationbutton);
        } else {
            locationbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestLocationPermission();
                }
            });
        }
    }

    public boolean notificationPermissionGranted() {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }else {
            return true;
        }
    }

    private boolean storagePermissionGranted() {
        if (SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean batteryOptimizationPermissionGranted() {
        return isBatteryOptimizationDisabled();
    }

    private boolean locationPermissionGranted() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestNotificationPermission() {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(permissionActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    public void requestStoragePermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2);
            }
        } else {
            ActivityCompat.requestPermissions(permissionActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE,}, 2);
        }
    }

    public void requestBatteryOptimizationPermission() {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivity(intent);
    }

    public void requestLocationPermission() {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(permissionActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES}, 3);
        }else {
            ActivityCompat.requestPermissions(permissionActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
    }

    private boolean isBatteryOptimizationDisabled() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    private void setTickMark(Button button) {
        // Set a tick mark or any other indicator as a drawable on the button.
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_bookmark_added_24, 0);
        button.setText("Permission Granted");
        button.setTextColor(Color.rgb(0,205,0));
        button.setOnClickListener(null);
        button.setBackgroundColor(Color.rgb(0,0,0));// Remove the OnClickListener.
    }

}