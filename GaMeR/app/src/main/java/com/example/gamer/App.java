package com.example.gamer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class App extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";
    public static final String CHANNEL_4_ID = "channel4";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_1_ID,
                "Channel 1",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel1.setDescription("This is Channel 1");

        NotificationChannel channel2 = new NotificationChannel(
                CHANNEL_2_ID,
                "Channel 2",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel2.setDescription("This is Channel 2");
        NotificationChannel channel3 = new NotificationChannel(
                CHANNEL_3_ID,
                "Channel 3",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel3.setDescription("This is Channel 3");
        NotificationChannel channel4 = new NotificationChannel(
                CHANNEL_4_ID,
                "Channel 4",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel4.setDescription("This is Channel 4");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
        manager.createNotificationChannel(channel2);
        manager.createNotificationChannel(channel3);
        manager.createNotificationChannel(channel4);
    }
}
