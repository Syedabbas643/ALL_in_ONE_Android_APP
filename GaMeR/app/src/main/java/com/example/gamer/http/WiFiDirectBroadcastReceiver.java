package com.example.gamer.http;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_P2P_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Looper.getMainLooper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.net.InetAddress;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private ftpActivity mActivity;
    private WifiP2pManager manager;
    SharedPreferences share2;
    private WifiP2pManager.Channel channel;
    private ftpActivity.MyHttpServerTask serverTask;
    private NotificationManagerCompat notificationManager;
    String hotspotIpAddress;

    public WiFiDirectBroadcastReceiver( ftpActivity ftpActivity) {
        this.mActivity = ftpActivity;
        share2 = mActivity.getSharedPreferences("fitshare",MODE_PRIVATE);
    }

    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
            int intExtra = intent.getIntExtra("wifi_p2p_state", -1);
            this.mActivity.setIsWifiP2pEnabled(intExtra == 2);
            Log.d(ftpActivity.TAG, "P2P state changed: " + intExtra);
        }else if (action.equals("STOP_SERVER_ACTION")) {
            serverTask = new ftpActivity.MyHttpServerTask();
            notificationManager = NotificationManagerCompat.from(context);

            if (SDK_INT >= Build.VERSION_CODES.Q) {
                manager = (WifiP2pManager) context.getSystemService(WIFI_P2P_SERVICE);
                channel = manager.initialize(context, getMainLooper(), null);
                manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        notificationManager.cancel(1);
                    }
                    @Override
                    public void onFailure(int i) {
                    }
                });
            }else {
                notificationManager.cancel(1);
            }
            try {
                if (serverTask!= null){
                    serverTask.cancel(true);
                }
            }catch (Exception ignored){

            }
        } else if (!"android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
            if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // Wi-Fi P2P connection is established, retrieve the IP address
                    WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                    InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                    hotspotIpAddress = groupOwnerAddress.getHostAddress();
                    mActivity.receiveonly(hotspotIpAddress);
                    System.out.println(hotspotIpAddress);
                }
        }else {
                "android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action);
            }
    }
}
}