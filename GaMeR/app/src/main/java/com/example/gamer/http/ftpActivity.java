package com.example.gamer.http;

import static android.os.Build.VERSION.SDK_INT;
import static com.example.gamer.App.CHANNEL_1_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.gamer.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


public class ftpActivity extends AppCompatActivity {
    public static final String TAG = "HotspotManager";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private boolean isWifiP2pEnabled = false;
    private boolean isHotspotEnabled = false;
    private int connectedDeviceCount = 0;
    private PowerManager.WakeLock wakeLock;
    private MyHttpServerTask serverTask;
    EditText ip,port;
    SharedPreferences share2;
    String ipaddr;
    String po;
    private NotificationManagerCompat notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        TextView textView = findViewById(R.id.textViewLog);
        ip = findViewById(R.id.ip);
        enableLocationSettings();
        port = findViewById(R.id.port);
        share2 = getSharedPreferences("fitshare",MODE_PRIVATE);
        ip.setText(share2.getString("ip","192.168.49.1"));
        port.setText(share2.getString("port","8080"));
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setMovementMethod(new ScrollingMovementMethod());

        notificationManager = NotificationManagerCompat.from(this);

        serverTask = new MyHttpServerTask();

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction("STOP_SERVER_ACTION");



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!initP2p()) {
                finish();
            }
        }
    }
    protected void enableLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10)
                .setFastestInterval(5)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {
                    // startUpdatingLocation(...);
                })
                .addOnFailureListener(this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(ftpActivity.this,3);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }

    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver( this);
        ContextCompat.registerReceiver(this,receiver,intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    /* unregister the broadcast receiver */
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private boolean initP2p() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        manager = (WifiP2pManager) getApplicationContext().getSystemService(WIFI_P2P_SERVICE);
        if (manager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        channel = manager.initialize(this, getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void onButtonStopTapped(View view) {

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    outputLog("hotspot stopped\n");
                    isHotspotEnabled = false;
                    connectedDeviceCount = 0;
                    notificationManager.cancel(1);
                    //return null;
                }

                @Override
                public void onFailure(int i) {
                    outputLog("hotspot failed to stop. reason: " + String.valueOf(i) + "\n");
                }
            });
        }
        try {
            if (serverTask!= null){
                serverTask.cancel(true);
            }
        }catch (Exception e){
            outputLog("server already stoped");
        }
        outputLog("----SERVER STOPED----\n");
    }

    public void onButtonSendTapped(View view) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:MyWakesendTag");
        wakeLock.acquire(30*60*1000L /*30 minutes*/);

        List<String> REQUIRED_SSIDS = new ArrayList<String>() {{
            add("GaMeR");
            add("GaMeR-5G");
            add("AndroidWifi");
            add("DIRECT-hs-myhotspot");
            add("DIRECT-hs-myhotspot1");
            add("DIRECT-hs-myhotspot2");
            add("DIRECT-hs-myhotspot3");
        }};

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSsid = wifiInfo.getSSID();
        if (currentSsid != null) {
            currentSsid = currentSsid.replace("\"", ""); // Remove surrounding quotes
        }

        if (REQUIRED_SSIDS.contains(currentSsid)) {
            ipaddr = ip.getText().toString();
            po = port.getText().toString();
            SharedPreferences.Editor editor = share2.edit();
            editor.putString("ip",ipaddr);
            editor.putString("port",po);
            editor.apply();
            Intent intent = new Intent(ftpActivity.this, WhatsappActivity.class);
            String httpurl = "http://"+ipaddr+":"+po+"/";
            intent.putExtra("httpurl",httpurl);
            startActivity(intent);
        } else {
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivity(panelIntent);
            }else {
                Toast.makeText(this, "Please connect to WiFi", Toast.LENGTH_LONG).show();
            }
        }
    }
    public String getIpAddress(Context context) {
        String ipAddress = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null) {
                        // Check if connected to a Wi-Fi hotspot
                        if (isHotspotConnected(context)) {
                            // Retrieve the hotspot IP address
                            ipAddress = getHotspotIpAddress();
                        } else {
                            // Retrieve the Wi-Fi IP address
                            ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                        }
                    }
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // Handle mobile network connection IP address retrieval if needed
                }
            }
        }
        return ipAddress;
    }

    private boolean isHotspotConnected(Context context) {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                        if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                            return true;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getHotspotIpAddress() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces(); networkInterfaceEnum.hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaceEnum.nextElement();
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    for (Enumeration<InetAddress> ipAddressEnum = networkInterface.getInetAddresses(); ipAddressEnum.hasMoreElements(); ) {
                        InetAddress inetAddress = ipAddressEnum.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class MyHttpServerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
                try {
                    MyjettyServer.main(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            return null;
        }
        protected void onCancelled() {
            try {
                MyjettyServer.stop();
            } catch (Exception ignored) {

            }
        }
    }

    @SuppressLint("MissingPermission")
    public void onButtonReceiveTapped(View view) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:MyWakeLockTag");
        wakeLock.acquire(30*60*1000L /*30 minutes*/);
        String des = getIpAddress(this);

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            receivewithstart(des);
        }else {
            receiveonly(des);
        }
    }
    private void outputLog(String msg){
        TextView textViewLog = findViewById(R.id.textViewLog);
        textViewLog.append("  " + msg);
    }

    @SuppressLint("MissingPermission")
    void receiveonly(String hot){
        serverTask = new MyHttpServerTask();
        serverTask.execute();
        String des = getIpAddress(this);
        if (des.length()<2){
            des = hot;
        }
        String descrip = "Server Started : "+ des;
        Intent activityIntent = new Intent(this, ftpActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent stopIntent = new Intent(this, WiFiDirectBroadcastReceiver.class);
        stopIntent.setAction("STOP_SERVER_ACTION");
        stopIntent.putExtra("STOP_SERVER_EXTRA", true);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.baseline_wifi_protected_setup_24)
                .setContentTitle(descrip)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setColor(Color.BLUE)
                .setOngoing(true)
                .addAction(R.drawable.baseline_wifi_protected_setup_24, "Stop Server", stopPendingIntent)
                .build();

        notificationManager.notify(1, notification);
    }

    @SuppressLint("MissingPermission")
    private void receivewithstart(String hot){
        if (!isWifiP2pEnabled) {
            outputLog("error: cannot start hotspot. WifiP2p is not enabled\n");
            return;
        }

        EditText editText = findViewById(R.id.editSSID);
        String ssid = "DIRECT-hs-" + editText.getText().toString();
        EditText editText1 = findViewById(R.id.editPassword);
        String password = editText1.getText().toString();
        int band = WifiP2pConfig.GROUP_OWNER_BAND_5GHZ;
        if (((RadioButton) findViewById(R.id.radioButton2G)).isChecked()) {
            band = WifiP2pConfig.GROUP_OWNER_BAND_2GHZ;
        } else if (((RadioButton) findViewById(R.id.radioButton5G)).isChecked()) {
            band = WifiP2pConfig.GROUP_OWNER_BAND_5GHZ;
        }

        int finalBand = band;


        if (SDK_INT >= Build.VERSION_CODES.Q) {
            this.manager.createGroup(this.channel, new WifiP2pConfig.Builder().setNetworkName(ssid).setPassphrase(password).enablePersistentMode(false).setGroupOperatingBand(finalBand).build(), new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    ftpActivity.this.outputLog("----SERVER STARTED----\n");
                    boolean unused = ftpActivity.this.isHotspotEnabled = true;
                    ftpActivity.this.outputLog("--------------- Hotspot Info ---------------\n");
                    ftpActivity.this.outputLog("SSID: " + ssid + "\n");
                    ftpActivity.this.outputLog("Password: " + password + "\n");
                    ftpActivity.this.outputLog("Band: " + (finalBand == 1 ? "2.4" : "5") + "GHz\n");
                    ftpActivity.this.outputLog("---------------------------------------------------\n");
                    //receiveonly(hot);
                }

                public void onFailure(int i) {
                    ftpActivity.this.outputLog("hotspot failed to start. reason: " + String.valueOf(i) + "\n");
                }
            });
        }
    }

}






