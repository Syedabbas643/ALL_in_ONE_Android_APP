package com.example.gamer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class calActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set a WebViewClient to open links within the WebView
        webView.setWebViewClient(new WebViewClient());

        // Set a WebChromeClient to handle progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                // Update the progress bar based on the loading progress
                if (newProgress == 100) {
                    // Hide the progress bar when the page is fully loaded
                    progressBar.setVisibility(android.view.View.GONE);
                } else {
                    // Show the progress bar while the page is loading
                    progressBar.setVisibility(android.view.View.VISIBLE);
                }
            }
        });

        // Load your desired URL
        webView.loadUrl("https://syedabbas.onrender.com");
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            // If there is a page to navigate back to, go back in the WebView's history
            webView.goBack();
        } else {
            // If there is no page to navigate back to, perform your custom action
            // For example, you can close the WebView or finish the activity
            super.onBackPressed(); // This will close the activity
        }
    }
}