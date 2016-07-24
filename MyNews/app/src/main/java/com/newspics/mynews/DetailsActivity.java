package com.newspics.mynews;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DetailsActivity extends AppCompatActivity {

    private  WebView webView;
    Intent intent;
    private static String currentCard;
    private static int newsType;
    static DetailsActivity detailsActivity;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        detailsActivity = this;
        setContentView(R.layout.activity_details);
        intent = new Intent(DetailsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new NewsWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        location = "file:///" + Environment.getExternalStorageDirectory().toString()+"/news";

        if(newsType == 1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        location = location + "/" + currentCard + ".html";
        webView.loadUrl(location);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });
        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new OnSwipeListener() {
            @Override
            public boolean onSwipe(Direction direction) {
                switch (direction) {
                    case right:
                        openMainActivity();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event))
                    return true;
                else
                    return false;
            }
        });
    }

    private void openMainActivity(){
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onBackPressed() {
        openMainActivity();
    }

    private class NewsWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
       }
    }



    /**
     * Set news type.
     * @param newsType news type.
     */
    public static void setNewsType(int newsType) {
        DetailsActivity.newsType = newsType;
    }

    /**
     * set current card.
     * @param currentCard the curent card.
     */
    public static void setCurrentCard(String currentCard) {
        DetailsActivity.currentCard = currentCard;
    }
}
