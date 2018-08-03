package com.vieweet.app.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MyDao {
    @Insert
    public void InsertAlbum(Album album);

    @Insert
    public void InsertPicture(Picture picture);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void InsertHotspot(Hotspot hotspot);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void InsertAlbums(Album[] albums);

    @Insert
    public void InsertPictures(Picture[] pictures);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void InsertHotspots(Hotspot[] hotspots);

    @Update
    public void UpdateHotspot(Hotspot hotspot);

    @Delete
    public void DeleteAlbum(Album album);

    @Delete
    public void DeletePicture(Picture picture);

    @Delete
    public void DeleteHotspot(Hotspot hotspot);

    @Query("SELECT * FROM ALBUM WHERE ALBUM_ID= :albumId")
    public Album[] getAlbums(String albumId);

    @Query("SELECT COUNT(ALBUM_ID)FROM ALBUM")
    public int getAlbumCount();

    @Query("SELECT * FROM PICTURE WHERE ALBUM_ID = :albumID LIMIT 1")
    public Picture getAlbumPicture(String albumID);

    @Query("SELECT * FROM PICTURE WHERE ALBUM_ID = :albumID")
    public Picture[] getAlbumPictures(String albumID);

    @Query("SELECT * FROM PICTURE WHERE PICTURE_ID= :pictureID")
    public Picture[] getPictures(String pictureID);

    @Query("SELECT COUNT(PICTURE_ID) FROM PICTURE WHERE ALBUM_ID= :albumID")
    public int getAlbumPicturesCount(String albumID);

    @Query("DELETE FROM ALBUM WHERE ALBUM_STATUS > 0")
    public void purgeAlbums();

    @Query("DELETE FROM PICTURE WHERE PICTURE_STATUS > 4")
    public void purgePictures();

    @Query("DELETE FROM HOTSPOT WHERE HOTSPOT_STATUS > 4")
    public void purgeHotspots();

    @Query("SELECT * FROM ALBUM")
    public Album[] getAllAlbums();

    @Query("SELECT * FROM HOTSPOT WHERE SOURCE_ID = :sourceID")
    public List<Hotspot> getHotspotList(String sourceID);
}
