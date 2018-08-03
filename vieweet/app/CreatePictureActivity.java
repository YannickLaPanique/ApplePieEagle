package com.vieweet.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pixeet.app.camera360.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreatePictureActivity extends Activity {

    @BindView(R.id.editPanoramaName)
    EditText editTextPanoramaName;
    @BindView(R.id.btnValidate)
    Button btnValidate;
    @BindView(R.id.textHardwareBack)
    TextView textBack;
    @BindView(R.id.textHardwareLabel) TextView textHardware;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_panorama_creation_theta);
        ButterKnife.bind(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LivePreviewActivity.class);
                startActivity(intent);
            }
        });
    }
}
