package com.vieweet.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pixeet.app.camera360.R;

import org.json.JSONObject;

import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends Activity {
    @BindView(R.id.lbl_back) TextView lblBack;
    @BindView(R.id.btn_back) Button btnBack;
    @BindView(R.id.lbl_action) TextView lblAction;
    @BindView(R.id.btn_action) Button btnAction;
    @BindView(R.id.bar_progression) ProgressBar barProgression;
    @BindView(R.id.web_view) WebView wv;
    String mode;
    String sharingUrl = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        // Retrieve Parent Parameters
        Intent myIntent = getIntent(); // gets the previously created intent
        String url = myIntent.getStringExtra("url");
        mode = myIntent.getStringExtra("mode");

        // Get Album or Picture Data Code
        String jsonString = myIntent.getStringExtra("post");
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            if (mode.equals("pano")) {
                if (jsonObj.has("picture_code") && jsonObj.getString("picture_code").length() > 0) {
                    sharingUrl = Constants.getPanoUrl(jsonObj.getString("picture_code"));
                }
            } else if (mode.equals("tour")) {
                if (jsonObj.has("album_code") && jsonObj.getString("album_code").length() > 0) {
                    sharingUrl = Constants.getTourUrl(jsonObj.getString("album_code"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        this.setTitle("Web Browser");

        // Button Login
        lblBack.setTypeface(tf);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeScreen();
            }
        });

        //
        lblAction.setTypeface(tf);
        if ((mode.equals("pano") || mode.equals("tour")) && sharingUrl.length() > 0) {
            // My Profile
            lblAction.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
        } else if (mode.equals("profile")) {
            // My Profile
            lblAction.setText("HELP");
            lblAction.setVisibility(View.VISIBLE);
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startMenuHelp();
                }
            });
        } else {
            lblAction.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
        }

        // Web View
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        wv.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:") || url.startsWith("geo:") || url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                if (url.startsWith("http")) {
                    view.loadUrl(url);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override public void onPageFinished(WebView view, String url) {
                barProgression.setVisibility(View.GONE);
                view.loadUrl(
                        "javascript:console.log('MAGIC'+document.getElementsByTagName('html')[0].innerHTML);");
            }
        });

        // Load Web Page

        String postData ;
        if (url.length() > 0) {
            postData = myIntent.getStringExtra("post");
            if (postData != null) {
                try {
                    wv.postUrl(url,
                            ("data=" + URLEncoder.encode(postData, "utf-8")).getBytes());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                wv.loadUrl(url);
            }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void closeScreen() {
        finish();
    }

    public void startMenuHelp() {
        barProgression.setVisibility(View.VISIBLE);
        String url = Constants.prepareInfoUrl(getApplicationContext(),
                Constants.URL_SERVER + Constants.URL_HELP);
        wv.loadUrl(url);
    }

    private void openNewEmail(String subject, String body) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        //emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"abc@xyz.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public void logout() {
        Intent i = getIntent();
        i.putExtra("source", "PROFILE");
        i.putExtra("logout", 1);
        setResult(RESULT_OK, i);
        closeScreen();
    }

    public void updateAlbum(String jsonString) {
        Intent i = getIntent();
        i.putExtra("source", "ALBUM");
        i.putExtra("data", jsonString);
        setResult(RESULT_OK, i);
        closeScreen();
    }

    public void updatePicture(String jsonString) {
        Intent i = getIntent();
        i.putExtra("source", "PICTURE");
        i.putExtra("data", jsonString);
        setResult(RESULT_OK, i);
        closeScreen();
    }

    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void jsLogout() {
            logout();
        }

        @JavascriptInterface
        public void jsAlbum(String jsonString) {
            updateAlbum(jsonString);
        }

        @JavascriptInterface
        public void jsPicture(String jsonString) {
            updatePicture(jsonString);
        }
    }
}


