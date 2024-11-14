package com.example.gamer.Yogi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.gamer.R;

public class YogimainActivity extends AppCompatActivity {

    Button page;
    EditText pageno,dom,search;

    SharedPreferences share;

    String pagenum;
    String domain;
    String proxyv;

    String sname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yogimain);
        page = findViewById(R.id.page);
        pageno = findViewById(R.id.pageno);
        dom = findViewById(R.id.dom);
        search = findViewById(R.id.search);
        share = getSharedPreferences("yogishare",MODE_PRIVATE);
        dom.setText(share.getString("dom","cash"));



        page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagenum = pageno.getText().toString();
                domain = dom.getText().toString();
                sname = search.getText().toString();
                SharedPreferences.Editor editor = share.edit();
                editor.putString("pagenum",pagenum);
                editor.putString("dom",domain);
                editor.putString("proxy",proxyv);
                editor.putString("sname",sname);
                editor.apply();


                Intent intent2 = new Intent(YogimainActivity.this, yogi.class);
                startActivity(intent2);
            }
        });
    }
}