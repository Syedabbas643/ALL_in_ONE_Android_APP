package com.example.gamer.Yogi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gamer.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class yogi extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParseAdapter adapter;
    private ArrayList<ParseItem> parseItems = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yogi);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParseAdapter(parseItems, this);
        recyclerView.setAdapter(adapter);

        webscare wb = new webscare();
        wb.execute();

    }

    private class webscare extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(yogi.this, android.R.anim.fade_in));

        }
        @Override
        protected Void doInBackground(Void... voids) {
Document document = null;
        try {
            // for shared preference
            SharedPreferences share1 = getSharedPreferences("yogishare", MODE_PRIVATE);
            String domain = share1.getString("dom", "cash");
            String sname = share1.getString("sname", "");
            String pagenum = share1.getString("pagenum","");
            String yogiurl = null;
            if (sname.isEmpty() && pagenum.isEmpty()) {
                yogiurl = "https://tamilyogi." + domain + "/category/tamilyogi-bluray-movies/";
            } else if(pagenum.isEmpty()) {
                yogiurl = "https://tamilyogi." + domain + "/?s=" + sname;
            } else if (sname.isEmpty()) {
                yogiurl = "https://tamilyogi." + domain + "/category/tamilyogi-bluray-movies/page/"+pagenum;
            }else {
                Toast.makeText(yogi.this, "Dont Input search and page no together", Toast.LENGTH_LONG).show();
            }

            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("188.166.241.213", 8080));
            document = (Document) Jsoup.connect(yogiurl).get();

            Elements elements = document.getElementsByClass("post-thumbnail");
            for (Element ele : elements) {
                String title = ele.getElementsByTag("img").attr("alt").toString();
                String imgUrl = ele.getElementsByTag("img").attr("src");
                String detailUrl = ele.attr("href");
                parseItems.add(new ParseItem(imgUrl, title, detailUrl));
            }
        }catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(yogi.this, "Url Fetch error", Toast.LENGTH_LONG).show();
        }
        return null;
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(yogi.this, android.R.anim.fade_out));
            adapter.notifyDataSetChanged();

        }
    }

}