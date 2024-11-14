package com.example.gamer;

import static com.example.gamer.App.CHANNEL_1_ID;
import static com.example.gamer.App.CHANNEL_2_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.BuildConfig;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class yt extends AppCompatActivity {
    private ProgressDialog dialog;
    public static String BASE_URL = null;
    public Button btn,btn480,btn720,btnaudio;
    public EditText url;
    private ProgressBar progressBar;
    private ProgressBar pbLoading;
    private TextView tvDownloadStatus;

    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String processId = "MyDlProcess";
    private NotificationManagerCompat notificationManager;

    private NotificationCompat.Builder notification;
    private boolean updating = false;
    final int progressmax = 100;
    private PowerManager.WakeLock wakeLock;

    private static final String TAG = yt.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yt);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:ytLockTag");
        wakeLock.acquire(30*60*1000L /*30 minutes*/);

        dialog = new ProgressDialog(this);
        dialog.setMessage("please wait..");
        dialog.setCancelable(false);
        btn = findViewById(R.id.btn);
        url = findViewById(R.id.edit_text);
        notificationManager = NotificationManagerCompat.from(this);

        progressBar = findViewById(R.id.progress_bar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        pbLoading = findViewById(R.id.pb_status);
        btn480 = findViewById(R.id.btn480);
        btn720 = findViewById(R.id.btn720);
        btnaudio = findViewById(R.id.btnaudio);
        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
        } catch (YoutubeDLException e) {
            Log.e("ytdlp", "failed to initialize youtubedl-android", e);
        }

        final Function3<Float, Long, String, Unit> callback = new Function3<Float, Long, String, Unit>() {
            @SuppressLint("MissingPermission")
            @Override
            public Unit invoke(Float progress, Long o2, String line) {
                runOnUiThread(() -> {
                            progressBar.setProgress((int) progress.floatValue());
                            tvDownloadStatus.setText(line);
                            notification.setContentText(line);
                            notification.setProgress(progressmax,(int) progress.floatValue(),false);
                            notificationManager.notify(2,notification.build());
                        }
                );
                return Unit.INSTANCE;
            }
        };

        btn480.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(url.getText().toString())) {
                    url.setError("ENter valid url");
                    return;
                }
                File gamerDir = new File(Environment.getExternalStorageDirectory(), "GaMeR");
                if (!gamerDir.exists()) {
                    gamerDir.mkdirs();
                }
                File videosDir = new File(gamerDir, "YT videos");
                if (!videosDir.exists()) {
                    videosDir.mkdirs();
                }

                YoutubeDLRequest request = new YoutubeDLRequest(url.getText().toString());
                request.addOption("-o", videosDir.getAbsolutePath() + "/%(title)s.%(ext)s");
                request.addOption("-f", "best[height<480]/best");

                showStart();

                downloading = true;
                @SuppressLint("MissingPermission") Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,callback))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(youtubeDLResponse -> {
                            pbLoading.setVisibility(View.GONE);
                            progressBar.setProgress(100);
                            tvDownloadStatus.setText("Download complete");
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download successful", Toast.LENGTH_LONG).show();
                            downloading = false;
                        }, e -> {
                            pbLoading.setVisibility(View.GONE);
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download failed", Toast.LENGTH_LONG).show();
                            downloading = false;
                        });compositeDisposable.add(disposable);
            }

        });
        btnaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(url.getText().toString())) {
                    url.setError("ENter valid url");
                    return;
                }
                File gamerDir = new File(Environment.getExternalStorageDirectory(), "GaMeR");
                if (!gamerDir.exists()) {
                    gamerDir.mkdirs();
                }
                File videosDir = new File(gamerDir, "Songs");
                if (!videosDir.exists()) {
                    videosDir.mkdirs();
                }

                YoutubeDLRequest request = new YoutubeDLRequest(url.getText().toString());
                request.addOption("-o", videosDir.getAbsolutePath() + "/%(title)s.%(ext)s");
                request.addOption("-f", "m4a/bestaudio/best");

                showStart();

                downloading = true;
                @SuppressLint("MissingPermission") Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,callback))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(youtubeDLResponse -> {
                            pbLoading.setVisibility(View.GONE);
                            progressBar.setProgress(100);
                            tvDownloadStatus.setText("Download complete");
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download successful", Toast.LENGTH_LONG).show();
                            downloading = false;
                        }, e -> {
                            pbLoading.setVisibility(View.GONE);
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download failed", Toast.LENGTH_LONG).show();
                            downloading = false;
                        });compositeDisposable.add(disposable);
            }

        });
        btn720.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(url.getText().toString())) {
                    url.setError("ENter valid url");
                    return;
                }
                File gamerDir = new File(Environment.getExternalStorageDirectory(), "GaMeR");
                if (!gamerDir.exists()) {
                    gamerDir.mkdirs();
                }
                File videosDir = new File(gamerDir, "YT videos");
                if (!videosDir.exists()) {
                    videosDir.mkdirs();
                }

                YoutubeDLRequest request = new YoutubeDLRequest(url.getText().toString());
                request.addOption("-o", videosDir.getAbsolutePath() + "/%(title)s.%(ext)s");
                request.addOption("-f", "best[height<1080]/best");

                showStart();

                downloading = true;
                @SuppressLint("MissingPermission") Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,callback))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(youtubeDLResponse -> {
                            pbLoading.setVisibility(View.GONE);
                            progressBar.setProgress(100);
                            tvDownloadStatus.setText("Download complete");
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download successful", Toast.LENGTH_LONG).show();
                            downloading = false;
                        }, e -> {
                            pbLoading.setVisibility(View.GONE);
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download failed", Toast.LENGTH_LONG).show();
                            downloading = false;
                        });compositeDisposable.add(disposable);
            }

        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(url.getText().toString())) {
                    url.setError("ENter valid url");
                    return;
                }
                File gamerDir = new File(Environment.getExternalStorageDirectory(), "GaMeR");
                if (!gamerDir.exists()) {
                    gamerDir.mkdirs();
                }
                File videosDir = new File(gamerDir, "YT videos");
                if (!videosDir.exists()) {
                    videosDir.mkdirs();
                }

                YoutubeDLRequest request = new YoutubeDLRequest(url.getText().toString());
                request.addOption("-o", videosDir.getAbsolutePath() + "/%(title)s.%(ext)s");
                request.addOption("-f", "bestvideo+bestaudio/best[ext=mp4]/best");

                showStart();

                downloading = true;
                @SuppressLint("MissingPermission") Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,callback))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(youtubeDLResponse -> {
                            pbLoading.setVisibility(View.GONE);
                            progressBar.setProgress(100);
                            tvDownloadStatus.setText("Download complete");
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download successful", Toast.LENGTH_LONG).show();
                            downloading = false;
                        }, e -> {
                            pbLoading.setVisibility(View.GONE);
                            notificationManager.cancel(2);
                            Toast.makeText(yt.this, "download failed", Toast.LENGTH_LONG).show();
                            downloading = false;
                        });compositeDisposable.add(disposable);


            }

        });

    }
    private void showStart() {
        tvDownloadStatus.setText("Download started");
        progressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
        notification = new NotificationCompat.Builder(this,CHANNEL_2_ID)
                .setSmallIcon(R.drawable.baseline_wifi_protected_setup_24)
                .setContentTitle("download")
                .setContentText("downloading")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(progressmax,0,false);
    }
    private void updateYoutubeDL(YoutubeDL.UpdateChannel updateChannel) {
        if (updating) {
            Toast.makeText(yt.this, "Update is already in progress!", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
        progressBar.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(this, updateChannel))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    progressBar.setVisibility(View.GONE);
                    switch (status) {
                        case DONE:
                            Toast.makeText(yt.this, "Update successful " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            Toast.makeText(yt.this, "Already up to date " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(yt.this, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                    updating = false;
                }, e -> {
                    if (BuildConfig.DEBUG) Log.e(TAG, "failed to update", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(yt.this, "update failed", Toast.LENGTH_LONG).show();
                    updating = false;
                });
        compositeDisposable.add(disposable);
    }

    public void updatedl(View view) {
        updateYoutubeDL(YoutubeDL.UpdateChannel._STABLE);
    }
}
