package com.example.gamer.Yogi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gamer.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import com.example.gamer.http.Filedownloader;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView titleTExtView,l720p,l480p;
    private String link720p,link480p;
    public Button ybt720;
    Filedownloader down1;
    public  Button ybt480;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageView = findViewById(R.id.imageView);
        titleTExtView = findViewById(R.id.textView);
        l720p = findViewById(R.id.l720p);
        l480p = findViewById(R.id.l480p);
        ybt720 = findViewById(R.id.ybt720);
        ybt480 = findViewById(R.id.ybt480);
        down1 = Filedownloader.getInstance(this);


        titleTExtView.setText(getIntent().getStringExtra("title"));
        Picasso.get().load(getIntent().getStringExtra("image")).into(imageView);
        Content content = new Content();
        content.execute();

        ybt720.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:yogiLockTag");
                wakeLock.acquire(30*60*1000L /*30 minutes*/);

                String url = link720p;
                String fileName1 = url.substring(url.lastIndexOf("/") + 1);
                String fileName = fileName1.replace("%20", " ");

                down1.main(url,fileName,18);
                //Intent shareIntent = new Intent(Intent.ACTION_SEND);
                //shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, link720p);

                //Intent chooser = Intent.createChooser(shareIntent, "Share link");

                //try {
                    //if (chooser.resolveActivity(getPackageManager()) != null) {
                        //startActivity(chooser);
                    //} else {
                        // Handle the case where no activity can handle the intent
                    //}
                //} catch (ActivityNotFoundException e) {
                    //e.printStackTrace();
                //}
            }
        });
        ybt480.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:yogiLockTag");
                wakeLock.acquire(30*60*1000L /*30 minutes*/);

                String url = link480p;
                String fileName1 = url.substring(url.lastIndexOf("/") + 1);
                String fileName = fileName1.replace("%20", " ");

                down1.main(url,fileName,18);

                //Intent shareIntent = new Intent(Intent.ACTION_SEND);
                //shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, link720p);

                //Intent chooser = Intent.createChooser(shareIntent, "Share link");

                //try {
                    //if (chooser.resolveActivity(getPackageManager()) != null) {
                        //startActivity(chooser);
                    //} else {
                        // Handle the case where no activity can handle the intent
                    //}
                //} catch (ActivityNotFoundException e) {
                    //e.printStackTrace();
                //}
            }
        });
    }
    private class Content extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            l720p.setText(link720p);
            l480p.setText(link480p);


        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                
                String detailUrl = getIntent().getStringExtra("detailUrl");
                System.out.println(detailUrl);
                org.jsoup.nodes.Document document1 = Jsoup.connect(detailUrl).get();
                String elements1 = document1.getElementsByTag("iframe").get(0).attr("src");
                System.out.println(elements1);
                String url2 = elements1.replace("embed7","embed");
                org.jsoup.nodes.Document document2 = Jsoup.connect(url2).userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1").get();

                // for 720p
                Element elements2 = document2.getElementsByClass("download_links").get(0);
                link720p = elements2.attr("href");

                // for 480p
                Element elements3 = document2.getElementsByClass("download_links").get(1);
                link480p = elements3.attr("href");

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
}