package com.vieweet.app.Database;

import android.annotation.SuppressLint;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Entity(tableName = "PICTURE")
public class Picture {

    @SerializedName("AID")
    @ColumnInfo(name = "ALBUM_ID")
    public String albumID;

    @SerializedName("UID")
    @ColumnInfo(name = "USER_ID")
    public String userID;

    @PrimaryKey
    @SerializedName("ID")
    @ColumnInfo(name = "PICTURE_ID")
    @NonNull
    public String pictureID;

    @SerializedName("NAME")
    @ColumnInfo(name = "PICTURE_NAME")
    public String name;

    @SerializedName("CODE")
    @ColumnInfo(name = "PICTURE_READ_CODE")
    public String code;

    @SerializedName("HFOV")
    @ColumnInfo(name = "PICTURE_HFOV")
    public Double hfov;

    @SerializedName("VFOV")
    @ColumnInfo(name = "PICTURE_VFOV")
    public Double vfov;

    @SerializedName("COMPASS")
    @ColumnInfo(name = "PICTURE_YAW")
    public Double yaw;

    @SerializedName("LAT")
    @ColumnInfo(name = "PICTURE_LAT")
    public Double latitude;

    @SerializedName("LON")
    @ColumnInfo(name = "PICTURE_LON")
    public Double longitude;

    @SerializedName("STATUS")
    @ColumnInfo(name = "PICTURE_STATUS")
    public Status status;

    @SerializedName("CREATED")
    @ColumnInfo(name = "PICTURE_CREATED")
    public Date dateCreated;


    @SerializedName("UPDATED")
    @ColumnInfo(name = "PICTURE_UPDATED")
    public Date dateUpdated;

    @SerializedName("hotspots")
    @Ignore
    public Hotspot[] hotspots;

    @Ignore
    private String address;

    public Picture() {
        pictureID = UUID.randomUUID().toString();
    }

    public Picture(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            albumID = jsonObject.getString("albumID");
            pictureID = jsonObject.getString("pictureID");
            userID = jsonObject.getString("userID");
            name = jsonObject.getString("name");
            code = jsonObject.getString("code");
            hfov = jsonObject.getDouble("hfov");
            vfov = jsonObject.getDouble("vfov");
            yaw = jsonObject.getDouble("yaw");
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
            status = Converters.toStatus(jsonObject.getInt("status"));
            dateCreated = new Date(jsonObject.getLong("dateCreated"));
            dateUpdated = new Date(jsonObject.getLong("dateUpdated"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJson(){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("albumID", albumID);
            jsonObject.put("pictureID", pictureID);
            jsonObject.put("userID", userID);
            jsonObject.put("name", name);
            jsonObject.put("code", code);
            jsonObject.put("hfov", hfov);
            jsonObject.put("vfov", vfov);
            jsonObject.put("yaw", yaw);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("status", Converters.toStatusInt(status));
            jsonObject.put("dateCreated", dateCreated.getTime());
            jsonObject.put("dateUpdated", dateUpdated.getTime());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    public String serverJSON() {
        String result = "";
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", userID);
            object.put("picture_id", pictureID);
            object.put("album_id", albumID);
            object.put("picture_name", name);
            object.put("picture_lat", latitude);
            object.put("picture_lon", longitude);
            object.put("picture_hfov", hfov);
            object.put("picture_vfov", vfov);
            object.put("picture_min", 1);
            object.put("picture_count", 1);
            object.put("picture_projection", "");
            object.put("picture_info", "");
            object.put("picture_compass", yaw);
            object.put("picture_status", Converters.toStatusInt(status));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            object.put("picture_created", df.format(dateCreated));
            object.put("picture_updated", df.format(dateUpdated));
            object.put("picture_address", address);
            //
            result = object.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
