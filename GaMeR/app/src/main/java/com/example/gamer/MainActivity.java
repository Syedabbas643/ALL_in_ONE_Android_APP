package com.example.gamer;


import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;
import android.Manifest;

import com.example.gamer.Yogi.YogimainActivity;
import com.example.gamer.fitgirl.fitmainActivity;
import com.example.gamer.http.ftpActivity;


public class MainActivity extends AppCompatActivity {
    RelativeLayout ytm;
    RelativeLayout yog;
    RelativeLayout inst;
    RelativeLayout Rocker;
    RelativeLayout ftp;
    RelativeLayout cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ytm = findViewById(R.id.ytm);
        yog = findViewById(R.id.Yog);
        inst = findViewById(R.id.aboutmain);
        Rocker = findViewById(R.id.rockersmain);
        ftp = findViewById(R.id.ftp);
        cal = findViewById(R.id.calender);
        //if (!checkPermission()){
            //showPermissionDialog();
        //}


        ytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent =new Intent(MainActivity.this,yt.class);
                 startActivity(intent);
            }
        });
        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this,calActivity.class);
                startActivity(intent);
            }
        });
        yog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this, YogimainActivity.class);
                startActivity(intent2);
            }
        });
        inst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(MainActivity.this, InstaActivity.class);
                startActivity(intent3);
            }
        });
        Rocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent4 = new Intent(MainActivity.this, fitmainActivity.class);
                startActivity(intent4);
            }
        });
        ftp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Wi-Fi is enabled
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.isWifiEnabled();

                // Check if location services are enabled
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                // Check if both Wi-Fi and location services are enabled
                if (isWifiEnabled) {
                    // Start the FTP activity
                    Intent intent = new Intent(MainActivity.this, ftpActivity.class);
                    startActivity(intent);
                } else {
                    // Prompt the user to enable Wi-Fi and/or location services
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivity(panelIntent);
                }
            }
        });


    }
}


