package com.vieweet.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.pixeet.app.camera360.R;
import com.vieweet.app.Database.Album;
import com.vieweet.app.Database.MyDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewTourActivity extends Activity {

    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.lbl_back)
    TextView lblBack;
    @BindView(R.id.lbl_title)
    TextView lblTitle;
    @BindView(R.id.btn_validate)
    Button btnValidate;
    @BindView(R.id.bar_progression)
    ProgressBar mProgressBar;
    @BindView(R.id.txt_name)
    EditText txtName;

    Album album;
    MyDatabase myDatabase;

    protected Location mLastKnownLocation;
    private AddressResultReceiver mResultReceiver;
    private LocationRequest mLocationRequest;
    String mAddressOutput;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_new_tour);
        ButterKnife.bind(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        mFusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);

        txtName.setTypeface(tf);
        txtName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (txtName.getText().toString().length() > 0) {
                    btnValidate.setText(R.string.CreateTour_Next);
                } else {
                    btnValidate.setText(R.string.CreateTour_Skip);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreatePictureActivity.class);
                startActivity(intent);
            }
        });

        lblBack.setTypeface(tf);
        btnBack.setTypeface(tf);
        lblTitle.setTypeface(tf);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mResultReceiver = new AddressResultReceiver(new Handler());

        fetchAddress();
    }

    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                mAddressOutput = "";
            }
            displayAddressOutput();
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastKnownLocation);
        startService(intent);
    }

    private void displayAddressOutput() {
        txtName.setText(mAddressOutput);
    }

    private void fetchAddress() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        fetchAddress();
                    }
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult){
                            if (locationResult == null || locationResult.getLocations().size() == 0 ) {
                                return;
                            }
                            mLastKnownLocation = locationResult.getLocations().get(0);
                            if (mLastKnownLocation == null) {
                                return;
                            }
                            if (!Geocoder.isPresent()) {
                                return;
                            }
                            startIntentService();
                        }
                    }, null);
                } else {
                    Toast.makeText(this, "Autofill of the address disabled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
