package com.vieweet.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.pixeet.app.camera360.R;
import com.theta360.sample.v2.glview.GLPhotoView;
import com.theta360.sample.v2.model.Photo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CaptureViewActivity extends Activity {

    @BindView(R.id.btnUsePreview) Button btnUse;
    @BindView(R.id.btnCancelPreview) Button btnCancel;
    @BindView(R.id.thetaCapturePreview)
    GLPhotoView mGLPhotoView;
    @BindView(R.id.loading_photo_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.linearLayoutButtonsPreview)
    LinearLayout layout;

    Photo mTexture;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_preview_capture_theta);
        ButterKnife.bind(this);

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
