package com.vieweet.app;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeet.app.camera360.R;
import com.vieweet.app.Database.Album;
import com.vieweet.app.Database.AlbumAdapter;
import com.vieweet.app.Database.Hotspot;
import com.vieweet.app.Database.MyDatabase;
import com.vieweet.app.Database.Picture;
import com.vieweet.app.Database.Status;
import com.vieweet.app.Network.HttpClient;
import com.vieweet.app.Network.ServerResponse;
import com.vieweet.app.Network.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageToursActivity extends Activity{

    @BindView(R.id.list)
    ListView mListView;

    @BindView(R.id.lbl_back)
    TextView lblBack;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.lbl_action)
    TextView lblAction;
    @BindView(R.id.btn_action)
    Button btnAction;

    @BindView(R.id.remove_bar)
    TableLayout removeBar;

    @BindView(R.id.btn_cancel_remove)
    Button btnCancelRemove;
    @BindView(R.id.btn_validate_remove)
    Button btnValidateRemove;
    Album selectedAlbum;

    @BindView(R.id.bar_progression)
    ProgressBar mProgressBar;

    MyDatabase myDatabase;

    List<Album> albumItems;
    AlbumAdapter albumAdapter;

    static ManageToursActivity myActivity;

    boolean isDataLoaded = false;

    public static ManageToursActivity getMyActivity() {
        return myActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        myActivity = this;
        super.onCreate(savedInstanceState);

        isDataLoaded = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)
                .getBoolean("data_loaded", false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_albums);
        ButterKnife.bind(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        btnCancelRemove.setTypeface(tf);
        btnCancelRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                removeBar.setVisibility(View.GONE);
            }
        });
        btnValidateRemove.setTypeface(tf);
        btnValidateRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAlbum(selectedAlbum);
            }
        });


        lblAction.setTypeface(tf);
        btnAction.setTypeface(tf);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDataLoaded = false;
                synchronizeDatabase();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        lblBack.setTypeface(tf);
        btnBack.setTypeface(tf);

        albumItems = new ArrayList<>();
        albumAdapter = new AlbumAdapter(this, R.layout.list_album_item, albumItems);
        mListView.setAdapter(albumAdapter);

        myDatabase = Room.databaseBuilder(getApplicationContext(),
                MyDatabase.class, Constants.DATABASE_NAME).build();

        synchronizeDatabase();

        mListView.setClickable(true);

        mListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Album album = albumItems.get(i);
                if (album.count > 0){
                    Intent intent = new Intent(getApplicationContext(), ManagePicturesActivity.class);
                    intent.putExtra(Constants.ALBUM, album.toJson());
                    startActivityForResult(intent, 0);
                }
            }
        });


    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String source = data.getExtras().getString("source");
            if (source != null && source.equals("PICTURES")) {
                if (data.getExtras().getString("todo").equalsIgnoreCase("delete_album")) {
                    Album album = new Album(data.getExtras().getString(Constants.ALBUM));
                    removeAlbum(album);
                } else {
                    populateList();
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myDatabase.close();
        myActivity = null;
    }

    public void populateList(){
        albumItems.clear();
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                Album[] albums;
                albums = myDatabase.myDao().getAllAlbums();
                for (final Album album : albums) {
                    String albumID = album.albumID;
                    album.count = myDatabase.myDao().getAlbumPicturesCount(albumID);
                    Picture picture = myDatabase.myDao().getAlbumPicture(albumID);
                    if (picture == null) album.pictureID = "";
                    else {
                        album.pictureID = picture.pictureID;
                        album.pictureStatus = picture.status;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumItems.add(album);
                            albumAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();

    }

    public void synchronizeDatabase(){
        if (isDataLoaded) {
            populateList();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);

        RequestBody md5Albums = RequestBody.create(MediaType.parse("string"), prefs.getString("MD5_ALBUMS", ""));
        RequestBody md5Pictures = RequestBody.create(MediaType.parse("string"), prefs.getString("MD5_PICTURES", ""));
        RequestBody md5Hotspots = RequestBody.create(MediaType.parse("string"), prefs.getString("MD5_HOTSPOTS", ""));
        RequestBody md5Videos = RequestBody.create(MediaType.parse("string"), prefs.getString("MD5_VIDEOS", ""));
        RequestBody md5Cameras = RequestBody.create(MediaType.parse("string"), prefs.getString("MD5_CAMERAS", ""));

        String url = Constants.prepareInfoUrl(getApplicationContext(), Constants.URL_DATA);

        try{
            HttpClient client = ServiceGenerator.createService(HttpClient.class);
            Call<ServerResponse> call = client.SynchronizeTask(url, md5Albums, md5Pictures, md5Videos, md5Hotspots, md5Cameras);

            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(@NonNull Call<ServerResponse> call, @NonNull final Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        int result = serverResponse.result;
                        if (result == 200){
                            final ServerResponse.ResponseInfo responseInfo = serverResponse.responseInfo;
                            if (responseInfo != null) {
                                ServerResponse.UserInfo userInfo = responseInfo.userInfo;
                                if (userInfo != null) {
                                    userInfo.parseSettings(getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE));
                                }
                                Completable.fromAction(new Action() {
                                    @Override
                                    public void run() {
                                        myDatabase.myDao().purgeAlbums();
                                        Album[] albums = responseInfo.albums;
                                        myDatabase.myDao().InsertAlbums(albums);

                                        myDatabase.myDao().purgePictures();
                                        Picture[] pictures = responseInfo.pictures;
                                        myDatabase.myDao().InsertPictures(pictures);

                                        myDatabase.myDao().purgeHotspots();
                                        for (Picture picture : pictures) {
                                            if (picture.hotspots != null) {
                                                for (Hotspot hotspot : picture.hotspots) {
                                                    hotspot.sourceID = picture.pictureID;
                                                    myDatabase.myDao().InsertHotspot(hotspot);
                                                }
                                            }
                                        }

                                        isDataLoaded = true;

                                        getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)
                                                .edit()
                                                .putBoolean("data_loaded", true)
                                                .apply();

                                        populateList();

                                    }
                                }).subscribeOn(Schedulers.io())
                                        .subscribe();
                            }
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

    private void removeAlbum(Album album){
        if (album.status == Status.LOCAL){
            removeLocalAlbum(album);
        }
        else {
            deleteOnline(album);
        }
    }

    private void removeLocalAlbum(final Album album){
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                myDatabase.myDao().DeleteAlbum(album);
                populateList();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void showRemoveBar(Album album){
        removeBar.setVisibility(View.VISIBLE);
        selectedAlbum = album;
    }

    private void deleteOnline(final Album album){
        String url = Constants.prepareInfoUrl(getApplicationContext(), Constants.URL_OPERATION);

        RequestBody userID = new FormBody.Builder()
                .add("user_id", album.userID)
                .build();
        RequestBody albumID = new FormBody.Builder()
                .add("album_id", album.albumID)
                .build();
        RequestBody todo = new FormBody.Builder()
                .add("todo", "delete_album")
                .build();

        final MultipartBody data = new MultipartBody.Builder()
                .addPart(userID)
                .addPart(albumID)
                .addPart(todo)
                .build();

        HttpClient client = ServiceGenerator.createService(HttpClient.class);
        Call<ServerResponse> call = client.UpdateTask(url, data);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null){
                    int result = serverResponse.result;
                    if (result == 200){
                        ServerResponse.ResponseInfo responseInfo = serverResponse.responseInfo;
                        if (responseInfo != null){
                            ServerResponse.DataInfo dataInfo = responseInfo.dataInfo;
                            if (dataInfo != null) {
                                String todo = dataInfo.todo;
                                if (todo.equalsIgnoreCase("delete_album")){
                                    removeLocalAlbum(album);
                                }
                            }
                        }
                    } else {
                        String errorMessage = serverResponse.errMess;
                        if (errorMessage == null || errorMessage.length() == 0) {
                            errorMessage = getString(R.string.General_ErrNotAvailable);
                        }
                        Toast.makeText(getApplicationContext(),
                                errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
