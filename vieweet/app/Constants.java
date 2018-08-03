package com.vieweet.app;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
    public static final String DATABASE_NAME = "app360db";
    public static final int DATABASE_VERSION = 2;

    public static final String PREFERENCES_KEY = "app360";
    public static final String APP_FONT = "fonts/gillsans.ttf";

    public static final String CACHE_FOLDER = "Vieweet";

    public static final String APP_PARTNER = "VIEWEET";
    public static final String APP_VERSION = "3.05";

    public static final String URL_AMAZON_S3 = "https://s3.amazonaws.com/vieweet360/panoramas";
    public static final String URL_API = "https://api.vieweet.com/v1.0";
    public static final String URL_SERVER = "https://mobileapp.vieweet.com";
    public static final String URL_VIEWER_PANO = "https://vieweet.com/pano/";
    public static final String URL_VIEWER_TOUR = "https://vieweet.com/tour/";
    public static final String URL_HELP = "/web/help";
    public static final String URL_PROFILE = "/web/profile";
    public static final String URL_DATA = "/service/mobile_data";
    public static final String URL_REGISTRATION = "/service/mobile_register";
    public static final String URL_SIGN_IN = "/service/mobile_login";
    public static final String URL_SUBSCRIBE = "/web/subscribe";
    public static final String URL_EDIT_TOUR = "/web/tour";
    public static final String URL_EDIT_PANORAMA = "/web/pano";
    public static final String URL_UPLOAD = "/service/mobile_upload";
    public static final String URL_PROGRESSION = "/service/mobile_progress";
    public static final String URL_OPERATION = "/service/mobile_update";
    public static final String URL_INIT = "/service/mobile_init";

    public static final String USER_AGENT = "User-Agent: Android-Secure-Agent-1.0";

    public static String prepareInfoUrl(Context ctx, String url) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
        String user_id = prefs.getString("user_id", "");
        String session_id = prefs.getString("session_id", "");
        String device_id = prefs.getString("device_id", "");
        String device_type = prefs.getString("device_type", "");
        String device_manufacturer = prefs.getString("device_manufacturer", "");
        String device_model = prefs.getString("device_model", "");
        String partner_ref = prefs.getString("partner_ref", "");
        String app_version = prefs.getString("app_version", "");
        String app_lang = prefs.getString("lang", "");
        boolean isAuthenticated = session_id.length() > 0;
        return String.format(
                "%s?partner_ref=%s&lang=%s&auth=%s&user_id=%s&session_id=%s&device_id=%s&device_ref=%s&device_manufacturer=%s&device_model=%s&app_type=ANDROID&app_version=%s",
                url, partner_ref, app_lang, isAuthenticated ? "Y" : "N", user_id, session_id, device_id,
                device_type, device_manufacturer, device_model, app_version);
    }

    public static final String getPanoUrl(String code) {
        return String.format("%s%s", URL_VIEWER_PANO, code);
    }

    public static final String getTourUrl(String code) {
        return String.format("%s%s", URL_VIEWER_TOUR, code);
    }

    public static String getStorageUrl(String uid, String pid, String fileExtension) {
        return String.format("%s/%s/%s.%s", URL_AMAZON_S3, uid, pid, fileExtension);
    }

    // Camera Type
    public static final String IS_VIEWEET_LENS_USED = "IS_VIEWEET_LENS_USED";
    public static final String IS_THETA_LENS_USED = "IS_THETA_LENS_USED";

    //

    public static final String ALBUM = "ALBUM";
    public static final String PICTURES = "PICTURES";
    public static final String PICTURE = "PICTURE";
    public static final String HOTSPOT = "HOTSPOT";
    public static final String THUMBNAIL = "THUMBNAIL";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
}
