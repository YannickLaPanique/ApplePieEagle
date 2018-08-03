package com.vieweet.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pixeet.app.camera360.R;
import com.theta360.sample.v2.network.GLJpegView;
import com.theta360.sample.v2.network.HttpConnector;
import com.theta360.sample.v2.view.MJpegInputStream;
import com.theta360.sample.v2.view.MJpegView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LivePreviewActivity extends Activity {

    @BindView(R.id.btn_capture)
    ImageButton btnCapture;
    @BindView(R.id.btn_cancel_capture) Button btnCancel;
    @BindView(R.id.button_import_capture) Button btn360;
    @BindView(R.id.surfaceView)
    MJpegView mJpegView;
    @BindView(R.id.txtCameraStatus)
    TextView txtCameraStatus;
    @BindView(R.id.canvas)
    RelativeLayout canvas;

    String cameraIpAddress;

    ShowLiveViewTask livePreviewTask;

    GLJpegView mGLJpegView;

    Boolean isJpegView = true;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_camera_ricotheta);
        ButterKnife.bind(this);

        txtCameraStatus.setText(getString(R.string.feed_connecting));
        btnCapture.setEnabled(false);

        cameraIpAddress = getString(R.string.theta_ip_address);

        livePreviewTask = new ShowLiveViewTask();
        livePreviewTask.execute();

        btn360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start360View();
            }
        });
        btn360.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJpegView.play();

        if (livePreviewTask != null) {
            livePreviewTask.cancel(true);
            livePreviewTask = new ShowLiveViewTask();
            livePreviewTask.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mJpegView.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (livePreviewTask != null){
            livePreviewTask.cancel(true);
        }
    }

    private void changeCameraStatus(final int resID) {
        runOnUiThread(new Runnable() {
            public void run() {
                txtCameraStatus.setText(resID);
            }
        });
    }

    private class ShowLiveViewTask extends AsyncTask<Void, Void, MJpegInputStream>{
        public ShowLiveViewTask(){}

        @Override
        protected MJpegInputStream doInBackground(Void... voids) {
            MJpegInputStream mJis = null;
            try{
                HttpConnector camera = new HttpConnector(cameraIpAddress);
                InputStream is = camera.getLivePreview();
                mJis = new MJpegInputStream(is);
                changeCameraStatus(R.string.feed_connecting);

            } catch (JSONException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException ex) {
                changeCameraStatus(R.string.text_camera_notconnected);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LivePreviewActivity.this)
                                .setTitle("Connection to Theta Camera Needed")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .setPositiveButton("Connect to Theta",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                            }
                                        })
                                .show();
                    }
                });
            }
            changeCameraStatus(R.string.feed_connected);
            return mJis;
        }

        @Override
        protected void onPostExecute(MJpegInputStream mJpegInputStream) {
            if (isJpegView) {
                if (mJpegInputStream != null) {
                    mJpegView.setSource(mJpegInputStream);
                    btn360.setClickable(true);
                }
            } else {
                if (mGLJpegView != null){
                    mGLJpegView.setSource(mJpegInputStream);
                }
            }
        }
    }

    public void start360View(){
        mJpegView.stopPlay();
        mGLJpegView = new GLJpegView(this);
        canvas.addView(mGLJpegView,
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));
    }
}
