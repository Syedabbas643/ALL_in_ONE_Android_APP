package com.example.gamer.fitgirl;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import com.example.gamer.R;
import com.example.gamer.http.Filedownloader;


public class fitmainActivity extends AppCompatActivity {

    Button page;
    EditText pageno;
    SharedPreferences share2;
    String pagenum;
    Filedownloader down1;
    private PowerManager.WakeLock wakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitmain);
        page = findViewById(R.id.page);
        pageno = findViewById(R.id.pageno);
        down1 = Filedownloader.getInstance(this);

        page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "server:downloadLockTag");
                wakeLock.acquire(30*60*1000L /*30 minutes*/);

                pagenum = pageno.getText().toString();
                share2 = getSharedPreferences("fitshare", MODE_PRIVATE);
                SharedPreferences.Editor editor = share2.edit();
                editor.putString("pagenum", pagenum);
                editor.apply();

                String url = pagenum;
                String fileName1 = url.substring(url.lastIndexOf("/") + 1);
                String fileName = fileName1.replace("%20", " ");

                if (((RadioButton) findViewById(R.id.part4)).isChecked()) {
                    down1.main(url, fileName, 1);

                } else if (((RadioButton) findViewById(R.id.part6)).isChecked()) {
                    down1.main(url, fileName, 4);

                } else if (((RadioButton) findViewById(R.id.part8)).isChecked()) {
                    down1.main(url, fileName, 8);

                } else if (((RadioButton) findViewById(R.id.part12)).isChecked()) {
                    down1.main(url, fileName, 12);

                } else if (((RadioButton) findViewById(R.id.part18)).isChecked()) {
                    down1.main(url, fileName, 18);

                }
            }
    });
    }

}

