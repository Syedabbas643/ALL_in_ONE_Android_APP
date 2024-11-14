package com.example.gamer.http;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.liulishuo.filedownloader.FileDownloader;

public class YourCancelService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileDownloader.setup(this);

        if ("CANCEL_DOWNLOAD_ACTION1".equals(intent.getAction())) {
            int downloadId1 = intent.getIntExtra("downloadid1", -1);
            if (downloadId1 != -1) {
                FileDownloader.getImpl().pause(downloadId1);
            }
        } else if ("CANCEL_DOWNLOAD_ACTION2".equals(intent.getAction())) {
            int downloadId2 = intent.getIntExtra("downloadid2", -1);
            if (downloadId2 != -1) {
                FileDownloader.getImpl().pause(downloadId2);
            }
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
