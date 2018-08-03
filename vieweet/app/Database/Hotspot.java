package com.vieweet.app.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;


@Entity(tableName = "HOTSPOT")
public class Hotspot {

    @PrimaryKey
    @SerializedName("ID")
    @ColumnInfo(name = "HOTSPOT_ID")
    @NonNull
    public String hotspotID;

    @SerializedName("SOURCE_ID")
    @ColumnInfo(name = "SOURCE_ID")
    public String sourceID;

    @SerializedName("DID")
    @ColumnInfo(name = "DEST_ID")
    public String destID;

    @SerializedName("THETA")
    @ColumnInfo(name = "THETA")
    public Double theta;

    @SerializedName("PHI")
    @ColumnInfo(name = "PHI")
    public Double phi;

    @SerializedName("TYPE")
    @ColumnInfo(name = "HOTSPOT_TYPE")
    public int type;

    @ColumnInfo(name = "HOTSPOT_STATUS")
    public Status status;

    Hotspot(){
        hotspotID = UUID.randomUUID().toString();
        status = Status.LOCAL;
    }

    @Ignore
    public Hotspot(String sourceID, String destID, Double theta, Double phi){
        super();
        this.sourceID = sourceID;
        this.destID = destID;
        type = 0;
        this.theta = theta;
        this.phi = phi;
    }
}
