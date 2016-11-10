package com.appindexingtest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * https://firebase.google.com/docs/app-indexing/android/activity?authuser=1
 */
public class AndroidActivity extends AppCompatActivity {
    private static final String TAG = AndroidActivity.class.getName();
    private static final Uri BASE_URL = Uri.parse("http://engineering.letsnurture.com/");
    private GoogleApiClient mClient;
    private Uri contentUri;
    private String blogName;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android);
        webView = (WebView) findViewById(R.id.webViewAndroid);
        webView.setWebViewClient(new MyWebViewClient());
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Toast.makeText(this,"just wait a moment, your page is loading..",Toast.LENGTH_LONG).show();
        onNewIntent(getIntent());
    }


    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            Log.d("Url:", data);
            int countMatch = data.split("/").length;
            if (countMatch == 2) {
                data = data.substring(0, data.length() - 1);
                blogName = data.substring(data.lastIndexOf("/") + 1);
            } else {
                blogName = data.substring(data.lastIndexOf("/"));
            }

            Log.d(TAG, BASE_URL + blogName);
            //contentUri = Uri.parse(BASE_URL + blogName);
            contentUri = Uri.parse(data);
            displayBlogInWebView(contentUri);
        }
    }

    private void displayBlogInWebView(Uri contentUri) {
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(contentUri.toString());
    }

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setName(blogName)
                .setDescription("LetsNurture Engineering Blog")
                .setUrl(contentUri)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
        //AppIndex.AppIndexApi.start(mClient, getAction());

        // Call the App Indexing API view method
        PendingResult<Status> result = AppIndex.AppIndexApi.start(mClient, getAction());

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error indexing the recipe view."
                            + status.toString());
                }
            }
        });
    }

    @Override
    public void onStop() {
        //AppIndex.AppIndexApi.end(mClient, getAction());
        // Call the App Indexing API view method
        PendingResult<Status> result = AppIndex.AppIndexApi.end(mClient, getAction());

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error indexing the recipe view."
                            + status.toString());
                }
            }
        });
        mClient.disconnect();
        super.onStop();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }
}
