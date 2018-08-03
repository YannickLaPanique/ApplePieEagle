package com.vieweet.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeet.app.camera360.R;
import com.vieweet.app.Network.HttpClient;
import com.vieweet.app.Network.ServerResponse;
import com.vieweet.app.Network.ServiceGenerator;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomepageActivity extends Activity {

    @BindView(R.id.bar_progression)
    ProgressBar mProgressBar;

    @BindView(R.id.btn_profile)
    Button btnProfile;
    @BindView(R.id.lbl_profile)
    TextView lblProfile;

    @BindView(R.id.row_create_tour)
    TableRow rowCreateTour;

    @BindView(R.id.lbl_create_tour)
    TextView lblCreateTour;

    @BindView(R.id.row_manage_tour)
    TableRow rowManageTour;
    @BindView(R.id.lbl_manage_tour)
    TextView lblManageTour;

    String userID;
    String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        lblProfile.setTypeface(tf);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMenuProfile();
            }
        });

        lblCreateTour.setTypeface(tf);
        rowCreateTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMenuCreateTour();
            }
        });

        lblManageTour.setTypeface(tf);
        rowManageTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMenuManageTour();
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (permissions[0].equals(Manifest.permission.READ_PHONE_STATE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPreferences();
                    updateUI();
                } else {
                    Toast.makeText(this, "The authentication to the server requires your phone device id.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initPreferences() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = prefs.edit();

        // Device ID
        String deviceId = prefs.getString("device_id", "");
        if (deviceId.length() == 0) {
            // Device ID & Info
            TelephonyManager tm =
                    (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mEditor.putString("device_id", tm.getDeviceId());
        }

        // User ID
        userID = prefs.getString("user_id", "");
        if (userID.length() == 0) {
            userID = UUID.randomUUID().toString();
            mEditor.putString("user_id", userID);
        }

        // Device Manufacturer
        String deviceManufacturer = prefs.getString("device_manufacturer", "");
        if (deviceManufacturer.length() == 0) {
            deviceManufacturer = Build.MANUFACTURER.toUpperCase();
            mEditor.putString("device_manufacturer", deviceManufacturer);
        }

        // Device Model
        String deviceModel = prefs.getString("device_model", "");
        if (deviceModel.length() == 0) {
            deviceModel = Build.MODEL.toUpperCase().replace(" ", "-");
            mEditor.putString("device_model", deviceModel);
        }

        // Device Type
        String deviceType = prefs.getString("device_type", "");
        if (deviceType.length() == 0) {
            deviceType = deviceManufacturer + "-" + deviceModel;
            mEditor.putString("device_type", deviceType);
        }

        // Lang
        String deviceLang = prefs.getString("lang", "");
        if (deviceLang.length() == 0) {
            deviceLang = "en";
            mEditor.putString("lang", deviceLang);
        }

        // Camera type defaults
        // By default, normal vieweet lens is used
        if (!prefs.contains(Constants.IS_VIEWEET_LENS_USED))
            mEditor.putBoolean(Constants.IS_VIEWEET_LENS_USED, true);

        if (!prefs.contains(Constants.IS_THETA_LENS_USED))
            mEditor.putBoolean(Constants.IS_THETA_LENS_USED, false);

        //
        mEditor.putString("partner_ref", Constants.APP_PARTNER);
        mEditor.putString("app_version", Constants.APP_VERSION);

        // Save Preferences
        mEditor.apply();

        // Get Session ID
        sessionID = prefs.getString("session_id", "");
    }


    public void initPrefs(){
        String url = Constants.prepareInfoUrl(getApplication(), Constants.URL_INIT);

        try{
            HttpClient client = ServiceGenerator.createService(HttpClient.class);
            Call<ServerResponse> call = client.InitTask(url);

            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null){
                        ServerResponse.ResponseInfo responseInfo = serverResponse.responseInfo;
                        if (responseInfo != null){
                            ServerResponse.UserInfo userInfo = responseInfo.userInfo;
                            userInfo.parseSettings(getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE));
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateUI() {
        if (sessionID.length() > 0) {
            // Authenticated
            lblProfile.setText("MY PROFILE");
        } else {
            // Not authenticated
            lblProfile.setText("SIGN IN");
        }
    }

    public void startLoginActivity() {
        Intent launcher = new Intent(this, LoginActivity.class);
        startActivityForResult(launcher, 0);
    }

    private void startMenuProfile() {
        if (sessionID.length() < 1) {
            startLoginActivity();
            return;
        }
        Intent launcher = new Intent(this, WebActivity.class);
        String fullUrl = Constants.prepareInfoUrl(getApplicationContext(),
                Constants.URL_SERVER + Constants.URL_PROFILE);
        launcher.putExtra("url", fullUrl);
        launcher.putExtra("mode", "profile");

        startActivityForResult(launcher, 0);
    }

    private void startMenuCreateTour() {
        // Check Auth
        if (sessionID.length() < 1) {
            startLoginActivity();
            return;
        }
        // Check Limit
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
        int tourLimit = prefs.getInt("TOUR_LIMIT", 0);
        int tourConso = prefs.getInt("TOUR_CONSO", 0);
        if (tourLimit != 0 && tourConso >= tourLimit) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.General_ErrTooManyTour),
                    Toast.LENGTH_LONG);
            t.show();
            return;
        }

        // Launch
        Intent launcher = new Intent(this, CreateNewTourActivity.class);
        startActivityForResult(launcher, 0);
    }

    private void startMenuManageTour() {
        if (sessionID.length() < 1) {
            startLoginActivity();
            return;
        }
        //
        Intent launcher = new Intent(this, ManageToursActivity.class);
        startActivityForResult(launcher, 0);
    }

    public void loginUser(String uId, String sId) {
        if (sId.length() == 0 || uId.length() == 0) return;

        userID = uId;
        sessionID = sId;

        //
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = prefs.edit();

        //
        mEditor.putString("user_id", userID);
        mEditor.putString("session_id", sessionID);
        mEditor.putBoolean("data_loaded", false);

        // Save Preferences
        mEditor.apply();

        //
    }

    public void logoutUser() {
        //
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = prefs.edit();

        //
        mEditor.putString("session_id", "");
        mEditor.putString("user_id", "");

        // Save Preferences
        mEditor.apply();

        //
        sessionID = "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String source = data.getExtras().getString("source");
            if (source.equals("SIGN_IN")) {
                int login = data.getExtras().getInt("login");
                if (login > 0) {
                    loginUser(data.getExtras().getString("user_id"),
                            data.getExtras().getString("session_id"));
                    initPrefs();
                    updateUI();
                }
            } else if (source.equals("PROFILE")) {
                int logout = data.getExtras().getInt("logout");
                if (logout > 0) {
                    logoutUser();
                    updateUI();
                }
            }

        }
    }
}
