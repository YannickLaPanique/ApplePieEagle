package com.vieweet.app.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "ALBUM")
public class Album {

    @PrimaryKey
    @SerializedName("ID")
    @ColumnInfo(name = "ALBUM_ID")
    @NonNull public String albumID;

    @SerializedName("UID")
    @ColumnInfo(name = "USER_ID")
    public String userID;

    @SerializedName("NAME")
    @ColumnInfo(name = "ALBUM_NAME")
    public String name;

    @SerializedName("CODE")
    @ColumnInfo(name = "ALBUM_READ_CODE")
    public String code;

    @SerializedName("STATUS")
    @ColumnInfo(name  = "ALBUM_STATUS")
    public Status status;

    @SerializedName("CREATED")
    @ColumnInfo(name = "ALBUM_CREATED")
    public Date dateCreated;

    @SerializedName("UPDATED")
    @ColumnInfo(name = "ALBUM_UPDATED")
    public Date dateUpdated;

    @Ignore
    public String pictureID;

    @Ignore
    public Status pictureStatus;

    @Ignore
    public int count;

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("albumID", albumID);
            jsonObject.put("userID", userID);
            jsonObject.put("name", name);
            jsonObject.put("code", code);
            jsonObject.put("status", Converters.toStatusInt(status));
            jsonObject.put("count", count);
            jsonObject.put("dateCreated", dateCreated.getTime());
            jsonObject.put("dateUpdated", dateUpdated.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public Album(){
        userID = UUID.randomUUID().toString();
    }

    public Album(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            albumID = jsonObject.getString("albumID");
            userID = jsonObject.getString("userID");
            name = jsonObject.getString("name");
            code = jsonObject.getString("code");
            status = Converters.toStatus(jsonObject.getInt("status"));
            count = jsonObject.getInt("count");
            dateCreated = new Date(jsonObject.getLong("dateCreated"));
            dateUpdated = new Date(jsonObject.getLong("dateUpdated"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
