package com.vieweet.app.Network;

import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;
import com.vieweet.app.Database.Album;
import com.vieweet.app.Database.Picture;

public class ServerResponse {

    @SerializedName("result")
    public int result;

    @SerializedName("response")
    public ResponseInfo responseInfo;

    @SerializedName("errmess")
    public String errMess;

    public class ResponseInfo{

        @SerializedName("user_id")
        public String userID;

        @SerializedName("session_id")
        public String sessionID;

        @SerializedName("user")
        public UserInfo userInfo;

        @SerializedName("albums")
        public Album[] albums;

        @SerializedName("pictures")
        public Picture[] pictures;

        @SerializedName("data")
        public DataInfo dataInfo;
    }

    public class UserInfo{

        @SerializedName("USER_PRO_LEVEL")
        int userProLevel;

        @SerializedName("TOUR_CONSO")
        int tourConso;

        @SerializedName("TOUR_LIMIT")
        int tourLimit;

        @SerializedName("IMAGE_CONSO")
        int imageConso;

        @SerializedName("IMAGE_LIMIT")
        int imageLimit;

        @SerializedName("STORAGE_CONSO")
        int storageConso;

        @SerializedName("STORAGE_LIMIT")
        int storageLimit;

        @SerializedName("TOUR_PANO_LIMIT")
        int tourPanoLimit;

        @SerializedName("ENABLE_CAMERA")
        int enableCamera;

        public void parseSettings(SharedPreferences preferences){
            try{
                SharedPreferences.Editor mEditor = preferences.edit();

                mEditor.putInt("USER_PRO_LEVEL", userProLevel);
                mEditor.putInt("TOUR_CONSO", tourConso);
                mEditor.putInt("TOUR_LIMIT", tourLimit);
                mEditor.putInt("IMAGE_CONSO", imageConso);
                mEditor.putInt("IMAGE_LIMIT", imageLimit);
                mEditor.putInt("STORAGE_CONSO", storageConso);
                mEditor.putInt("STORAGE_LIMIT", storageLimit);
                mEditor.putInt("TOUR_PANO_LIMIT", tourPanoLimit);
                mEditor.putInt("ENABLE_CAMERA", enableCamera);

                mEditor.apply();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class DataInfo {

        @SerializedName("todo")
        public String todo;

        @SerializedName("album_id")
        public String albumID;
    }
}
