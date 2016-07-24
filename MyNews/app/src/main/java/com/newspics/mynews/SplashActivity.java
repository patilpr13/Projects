package com.newspics.mynews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NCPrefFile";
    Intent intent;
    TextView textView;
    TextView exceptionTextView;
    WebView webView;
    boolean hasErrors = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

        setContentView(R.layout.activity_splash);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        textView = (TextView)findViewById(R.id.textView2);

        //need to remove
        exceptionTextView = (TextView)findViewById(R.id.textView3);
        exceptionTextView.setMovementMethod(new ScrollingMovementMethod());
        exceptionTextView.setTextIsSelectable(true);

        webView = (WebView)findViewById(R.id.webView2);
        webView.setWebViewClient(new LoadingViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(getString(R.string.loading_page));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    private class LoadingViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            textView.setText(R.string.preparing_news);
            new InternetConnectionChecker().execute();
        }
    }

    private boolean isAlreadyLoaded(){
        String currentDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String storedDate = settings.getString("CURRENT_DATE", "");
        //return currentDate.equals(storedDate);
        return false;
    }

    private void setAlreadyLoaded(){
        String currentDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("CURRENT_DATE", currentDate);
        editor.commit();
    }

    private class ConfigReader extends AsyncTask<Void, Void, Boolean> {

        News news;

        public ConfigReader() {
            news = new News();
        }

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        @Override
        protected Boolean doInBackground(Void... params) {
           boolean result = false;
            //load config
            if(!isAlreadyLoaded()) {
                String configFile = getString(R.string.url)+"newsconfig.xml";
                news.loadNewsConfig(configFile, true);
            }
            else{
                String configFile = Environment.getExternalStorageDirectory().toString()
                        + "/news/newsconfig.xml";
                news.loadNewsConfig(configFile, false);
                result = true;
            }
            return result;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Boolean isAlreadyExists) {
            if(news.getNumberOfPages() > 0 ) { //Some pages available
                if(!isAlreadyExists) {
                    new Executor(news).execute();
                }
                else{
                    //finish this activity
                    finish();
                    MainActivity.setNewsObject(news);
                    // Start main activity
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }
            else{
                    hasErrors = true;
                    textView.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    //exceptionTextView.setText(R.string.nothing_available);
                    exceptionTextView.setText("Error in config download!! : " + getStackTrace(news.getException()));
            }

        }
    }

    private class Executor extends AsyncTask<Void, Void, Void> {

        News news = null;

        Executor(News news){
          this.news = news;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ExecutorService executor = Executors.newCachedThreadPool();
            File path = new File(Environment.getExternalStorageDirectory().toString()+"/news");
            path.mkdirs();

            DownLoaderTask configDownloadTask = new DownLoaderTask(-1, 9); // 9=>config file download
            executor.execute(configDownloadTask);
            List<Page> pages = news.getPages();
            for(int i =0; i < pages.size();i++){
                Page page = pages.get(i);
                DownLoaderTask imageDownloadTask = new DownLoaderTask(page.getId(), -1);// -1=>main images file download
                executor.execute(imageDownloadTask);
                if(page.getType() == 0 || page.getType() == 1){
                   DownLoaderTask dataDownloadTask = new DownLoaderTask(page.getId(), page.getType());
                   executor.execute(dataDownloadTask);
                }
            }

            executor.shutdown();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                    MainActivity.setNewsObject(news);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            });

            // Wait until all threads are finish
            while (!executor.isTerminated()) {
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.refreshPager(-1);// -1 => All download completed
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!hasErrors) {
                // All pages downloaded
                setAlreadyLoaded();
            }
        }
    }

    private class DownLoaderTask implements  Runnable{

        boolean isCompleted = false;
        String strId;
        int id;
        int type;
        String url = getString(R.string.url);
        String textFolder = getString(R.string.text_folder) + "/";
        String imageFolder = getString(R.string.image_folder) + "/";
        String videoFolder = getString(R.string.video_folder) + "/";
        String extension = null;
        DownLoaderTask(int id, int type){
           this.id  = id;
           this.strId = String.valueOf(id);
           this.type = type;
           switch (type){
               case 0:
                   extension = ".html";
                   url = url + textFolder + strId + extension;
                   break;
               case 1:
                   extension = ".html";
                   url = url+ videoFolder + strId + extension;
                   break;
               case 2:
                   url = null;
                   break;
               case 9:
                   strId = "newsconfig";
                   extension = ".xml";
                   url = url + strId + extension;
                   break;
               default:
                   extension = ".jpg";
                   url = url + imageFolder + strId + extension;
                   break;
           }
        }

        @Override
        public void run() {
            isCompleted =false;
            try {
                URL downLoadUrl = new URL(url);
                InputStream inputStream = downLoadUrl.openStream();
                OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory().toString()
                        + "/news/" + strId + extension);

                byte[] b = new byte[4096];
                int length;
                while ((length = inputStream.read(b)) >= 0) {
                    outputStream.write(b, 0, length);
                }
                inputStream.close();
                outputStream.close();
                isCompleted = true;

            } catch (final Exception e) {
                while(!isCompleted) {
                    run();
                }
            }
            finally {
                if (isCompleted){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(extension.equals(".jpg")) {
                                MainActivity.refreshPager(id);
                            }
                        }
                    });
                }
            }
        }
    }

    private String getStackTrace(final Throwable throwable) {
        String exception = "Unknown exception!!!";
        if(throwable != null) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            throwable.printStackTrace(pw);
            exception = sw.getBuffer().toString();
        }
        return exception;
    }


    private class InternetConnectionChecker extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
                     new ConfigReader().execute();
                 }
        }
    }
