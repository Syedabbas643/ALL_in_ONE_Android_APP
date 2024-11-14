package com.example.gamer.http;

import static com.example.gamer.App.CHANNEL_3_ID;
import static com.example.gamer.App.CHANNEL_4_ID;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Environment;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.example.gamer.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
public class Filedownloader {
    Queue<DownloadInfo> downloadQueue;
    Context context;
    DownloadQueueManager queueManager;
    private static Filedownloader instance;
    int file1;
    int file2;
    private PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder notificationBuilder1,notificationBuilder2,notificationBuilder3;
    private NotificationManager notificationManager;
    private NotificationCompat.BigTextStyle bigTextStyle1,bigTextStyle2,bigTextStyle3;

    public Filedownloader(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        downloadQueue = new LinkedList<>();
        FileDownloader.setup(context);

        queueManager = new DownloadQueueManager();
        queueManager.setQueueChangeListener(new DownloadQueueManager.QueueChangeListener() {
            @Override
            public void onQueueChanged(int queueSize) {
                updateNotificationqeue(queueSize);
            }
        });

    }
    public static Filedownloader getInstance(Context context) {
        if (instance == null) {
            instance = new Filedownloader(context);
        }
        return instance;
    }

    public void main(String url, String filename, int parts) {
        if (file1 == 0){
            startDownload1(url,filename,parts);
        }else if (file2 == 0) {
            startDownload2(url,filename,parts);
        }else {
            DownloadInfo newTask = new DownloadInfo(url, filename);
            queueManager.addDownloadTask(newTask);
            Toast.makeText(context, "Added to the download queue", Toast.LENGTH_LONG).show();
        }
    }
    class DownloadInfo {
        private String url;
        private String fileName;

        public DownloadInfo(String url, String fileName) {
            this.url = url;
            this.fileName = fileName;
        }

        public String getUrl() {
            return url;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public class DownloadQueueManager {
        private Queue<DownloadInfo> downloadQueue;
        private DownloadQueueManager.QueueChangeListener listener;

        public DownloadQueueManager() {
            downloadQueue = new LinkedList<>();
        }

        public void setQueueChangeListener(DownloadQueueManager.QueueChangeListener listener) {
            this.listener = listener;
        }

        public void addDownloadTask(DownloadInfo task) {
            downloadQueue.add(task);
            if (listener != null) {
                listener.onQueueChanged(downloadQueue.size());
            }
        }

        public DownloadInfo removeNextTask() {
            DownloadInfo nextTask = downloadQueue.poll();
            if (listener != null) {
                listener.onQueueChanged(downloadQueue.size());
            }
            return nextTask;
        }


        public interface QueueChangeListener {
            void onQueueChanged(int queueSize);
        }
    }

    private void startDownload1(String url, String filename, int parts) {
        file1 = 1;

        notificationBuilder1 = new NotificationCompat.Builder(context, CHANNEL_3_ID);
        bigTextStyle1 = new NotificationCompat.BigTextStyle();

        final long[] lastUpdateTime = {0L};
        final long[] lastDownloadedBytes = {0L};
        final int[] id = {0};

        File folder = new File(Environment.getExternalStorageDirectory(), "GaMeR");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GaMeR/"+filename;

        int id1 = FileDownloader.getImpl().create(url)
                .setPath(parentPath)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 11; Pixel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36")
                .setCallbackProgressMinInterval(1000)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        id[0] = task.getId();
                        initNotificationProgress(notificationManager,notificationBuilder1,bigTextStyle1,filename,1,id[0]);

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        int progress = (int) ((soFarBytes * 100) / totalBytes);
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastUpdateTime[0];
                        long downloadedBytes = soFarBytes - lastDownloadedBytes[0];

                        double speed = downloadedBytes / (elapsedTime / 1000.0); // Speed in bytes per second

                        lastUpdateTime[0] = currentTime;
                        lastDownloadedBytes[0] = soFarBytes;

                        String formattedSpeed = formatDownloadSpeedbysec(speed);
                        updateNotification(notificationManager,notificationBuilder1,bigTextStyle1,progress,formattedSpeed,formatDownloadSpeed(soFarBytes),formatDownloadSpeed(totalBytes),1);

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        completeNotificationProgress(notificationManager,notificationBuilder1,bigTextStyle1,1);
                        file1 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload1(nextTaskUrl, nextTaskFileName, 6);
                        }
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        completeNotificationProgress(notificationManager,notificationBuilder1,bigTextStyle1,1);
                        file1 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload1(nextTaskUrl, nextTaskFileName, 6);
                        }

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        completeNotificationProgress(notificationManager,notificationBuilder1,bigTextStyle1,1);
                        file1 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload1(nextTaskUrl, nextTaskFileName, 6);
                        }
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                })
                .start();

        System.out.println(id1);
    }
    private void startDownload2(String url, String filename, int parts){
        file2 = 1;

        notificationBuilder2 = new NotificationCompat.Builder(context, CHANNEL_4_ID);
        bigTextStyle2 = new NotificationCompat.BigTextStyle();
        final long[] lastUpdateTime = {0L};
        final long[] lastDownloadedBytes = {0L};
        final int[] id = {0};

        File folder = new File(Environment.getExternalStorageDirectory(), "GaMeR");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GaMeR/"+filename;

        FileDownloader.getImpl().create(url)
                .setPath(parentPath)
                .setCallbackProgressMinInterval(2000)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 11; Pixel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36")
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        id[0] = task.getId();
                        initNotificationProgress(notificationManager,notificationBuilder2,bigTextStyle2,filename,2,id[0]);


                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        int progress = (int) ((soFarBytes * 100) / totalBytes);
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastUpdateTime[0];
                        long downloadedBytes = soFarBytes - lastDownloadedBytes[0];

                        double speed = downloadedBytes / (elapsedTime / 1000.0); // Speed in bytes per second

                        lastUpdateTime[0] = currentTime;
                        lastDownloadedBytes[0] = soFarBytes;

                        String formattedSpeed = formatDownloadSpeedbysec(speed);
                        updateNotification(notificationManager,notificationBuilder2,bigTextStyle2,progress,formattedSpeed,formatDownloadSpeed(soFarBytes),formatDownloadSpeed(totalBytes),2);

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        completeNotificationProgress(notificationManager,notificationBuilder2,bigTextStyle2,2);
                        file2 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload2(nextTaskUrl, nextTaskFileName, 6);
                        }
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        completeNotificationProgress(notificationManager,notificationBuilder2,bigTextStyle2,2);
                        file2 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload2(nextTaskUrl, nextTaskFileName, 6);
                        }
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        completeNotificationProgress(notificationManager,notificationBuilder2,bigTextStyle2,2);
                        file2 = 0;
                        DownloadInfo nextTask = queueManager.removeNextTask();
                        if (nextTask != null) {
                            String nextTaskUrl = nextTask.getUrl();
                            String nextTaskFileName = nextTask.getFileName();
                            startDownload2(nextTaskUrl, nextTaskFileName, 6);
                        }

                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                })
                .start();
    }


    private String formatDownloadSpeed(long downloadSpeed) {
        double speed = downloadSpeed; // Convert to double for precise calculation

        if (speed < 1024) {
            return String.format("%.2f B", speed);
        } else if (speed < 1024 * 1024) {
            return String.format("%.2f KB", speed / 1024);
        } else if (speed < 1024 * 1024 * 1024){
            return String.format("%.2f MB", speed / (1024 * 1024));
        }else {
            return String.format("%.2f GB", speed / (1024 * 1024 *1024));
        }
    }
    private static String formatDownloadSpeedbysec(double speed1) {
         double speed = speed1*1.1 ;
        if (speed < 1024) {
            return String.format("%.2f B/s", speed);
        } else if (speed < 1024 * 1024) {
            return String.format("%.2f KB/s", speed / 1024);
        } else {
            return String.format("%.2f MB/s", speed / (1024 * 1024));
        }
    }

    private void updateNotification(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder, NotificationCompat.BigTextStyle bigTextStyle, int progress, String taskSpeed, String current, String total, int id) {
        if (notificationBuilder != null) {
            notificationBuilder.setProgress(100, progress, false);
            bigTextStyle.bigText(progress + "% || " + taskSpeed + " || " + current + "/" + total);
            notificationBuilder.setStyle(bigTextStyle);

            if (notificationManager != null) {
                boolean isNotificationActive = false;
                StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification notification : activeNotifications) {
                    if (notification.getId() == id) {
                        isNotificationActive = true;
                        break;
                    }
                }
                if (isNotificationActive) {
                    notificationManager.notify(id, notificationBuilder.build());
                }
            }
        }
    }
    private void initNotificationProgress(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder, NotificationCompat.BigTextStyle bigTextStyle,String filename,int id,int iddd) {
        String ids = String.valueOf(id);
        Intent cancelIntent = new Intent(context, YourCancelService.class);
        cancelIntent.setAction("CANCEL_DOWNLOAD_ACTION"+ids);
        if (id == 1){
            cancelIntent.putExtra("downloadid1",iddd);
        } else if (id == 2) {
            cancelIntent.putExtra("downloadid2",iddd);
        }
        PendingIntent pendingCancelIntent = PendingIntent.getService(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent dummyIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder
                .setContentTitle(filename)
                .setContentText("Downloading...")
                .setContentInfo("0%")
                .setContentIntent(dummyIntent)
                .addAction(R.drawable.baseline_cancel_24, "Cancel", pendingCancelIntent)
                .setSmallIcon(R.drawable.baseline_bolt_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setColor(Color.BLUE)
                .setProgress(100, 0, false);
        if (notificationManager != null) {
            notificationManager.notify(id, notificationBuilder.build());
        }
    }
    private void completeNotificationProgress(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder, NotificationCompat.BigTextStyle bigTextStyle,int id) {
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
        System.out.println("notify complete");
    }

    private void updateNotificationqeue(int queueSize) {
        String notificationText = "Pending tasks in queue: " + queueSize;
        System.out.println(notificationText);
        notificationBuilder3 = new NotificationCompat.Builder(context, CHANNEL_3_ID)
                .setContentTitle(notificationText)
                .setSmallIcon(R.drawable.baseline_bolt_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setColor(Color.BLUE);
        if (notificationManager != null&& queueSize > 0) {
            notificationManager.notify(5, notificationBuilder3.build());
        }else {
            notificationManager.cancel(5);
        }

    }
}
