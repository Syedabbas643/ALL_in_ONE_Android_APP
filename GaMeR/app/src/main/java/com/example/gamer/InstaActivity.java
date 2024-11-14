package com.example.gamer;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.databinding.Observable;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Observable;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class InstaActivity extends AppCompatActivity {
    private ProgressDialog dialog;
    public static String BASE_URL = null;
    public Button instabtn;
    public EditText instaurl;
    private ProgressBar progressBar;
    private ProgressBar pbLoading;
    private TextView tvDownloadStatus;

    private boolean downloading = false;
    private boolean updating = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String processId = "MyDlProcess";
    private PowerManager.WakeLock wakeLock;

    private static final String TAG = InstaActivity.class.getSimpleName();


    private final Function3<Float, Long, String, Unit> callback = new Function3<Float, Long, String, Unit>() {
        @Override
        public Unit invoke(Float progress, Long o2, String line) {
            runOnUiThread(() -> {
                        progressBar.setProgress((int) progress.floatValue());
                        tvDownloadStatus.setText(line);
                    }
            );
            return Unit.INSTANCE;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta);
        dialog = new ProgressDialog(this);
        dialog.setMessage("please wait..");
        dialog.setCancelable(false);
        instabtn = findViewById(R.id.instabtn);
        instaurl = findViewById(R.id.instaurl);
        progressBar = findViewById(R.id.progress_bar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        pbLoading = findViewById(R.id.pb_status);
        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
        } catch (YoutubeDLException e) {
            Log.e("ytdlp", "failed to initialize youtubedl-android", e);
        }

        instabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (updating) {
                    Toast.makeText(InstaActivity.this, "Update is already in progress!", Toast.LENGTH_LONG).show();
                    return;
                }

                updating = true;
                progressBar.setVisibility(View.VISIBLE);
                Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(InstaActivity.this, YoutubeDL.UpdateChannel._STABLE))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(status -> {
                            progressBar.setVisibility(View.GONE);
                            switch (status) {
                                case DONE:
                                    Toast.makeText(InstaActivity.this, "Update successful " + YoutubeDL.getInstance().versionName(InstaActivity.this), Toast.LENGTH_LONG).show();
                                    downloadStart();
                                    break;
                                case ALREADY_UP_TO_DATE:
                                    Toast.makeText(InstaActivity.this, "Already up to date " + YoutubeDL.getInstance().versionName(InstaActivity.this), Toast.LENGTH_LONG).show();
                                    downloadStart();
                                    break;
                                default:
                                    Toast.makeText(InstaActivity.this, status.toString(), Toast.LENGTH_LONG).show();
                                    break;
                            }
                            updating = false;
                        }, e -> {
                            if (BuildConfig.DEBUG) Log.e(TAG, "failed to update", e);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(InstaActivity.this, "update failed", Toast.LENGTH_LONG).show();
                            updating = false;
                        });
                compositeDisposable.add(disposable);
            }
        });

    }


    private void downloadStart() {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:saverLockTag");
            wakeLock.acquire(30*60*1000L /*30 minutes*/);

            if (TextUtils.isEmpty(instaurl.getText().toString())) {
                instaurl.setError("ENter valid url");
                return;
            }
            File gamerDir = new File(Environment.getExternalStorageDirectory(), "GaMeR");
            if (!gamerDir.exists()) {
                gamerDir.mkdirs();
            }
            File videosDir = new File(gamerDir, "All video Saver");
            if (!videosDir.exists()) {
                videosDir.mkdirs();
            }

            YoutubeDLRequest request = new YoutubeDLRequest(instaurl.getText().toString());
            request.addOption("-o", videosDir.getAbsolutePath() + "/%(title)s.%(ext)s");
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");

            showStart();

            downloading = true;
            Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,callback))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(youtubeDLResponse -> {
                        pbLoading.setVisibility(View.GONE);
                        progressBar.setProgress(100);
                        tvDownloadStatus.setText("Download complete");
                        Toast.makeText(InstaActivity.this, "download successful", Toast.LENGTH_LONG).show();
                        downloading = false;
                    }, e -> {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(InstaActivity.this, "download failed", Toast.LENGTH_LONG).show();
                        downloading = false;
                    });
            compositeDisposable.add(disposable);
        }
        private void showStart() {
            tvDownloadStatus.setText("Download started");
            progressBar.setProgress(0);
            pbLoading.setVisibility(View.VISIBLE);

    }
    }

