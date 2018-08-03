package com.vieweet.app;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeet.app.camera360.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vieweet.app.Database.Album;
import com.vieweet.app.Database.MyDatabase;
import com.vieweet.app.Database.Picture;
import com.vieweet.app.Database.PictureAdapter;
import com.vieweet.app.Database.Status;
import com.vieweet.app.Network.HttpClient;
import com.vieweet.app.Network.ServerResponse;
import com.vieweet.app.Network.ServiceGenerator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagePicturesActivity extends Activity {
    @BindView(R.id.list)
    ListView mListView;

    @BindView(R.id.lbl_back)
    TextView lblBack;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.lbl_title)
    TextView lblTitle;

    @BindView(R.id.remove_bar)
    TableLayout removeBar;

    @BindView(R.id.btn_cancel_remove)
    Button btnCancelRemove;
    @BindView(R.id.btn_validate_remove)
    Button btnValidateRemove;


    @BindView(R.id.bar_progression)
    ProgressBar mProgressBar;

    @BindView(R.id.layout_action)
    LinearLayout layoutAction;
    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.btn_cancel)
    Button btnCancel;

    @BindView(R.id.btn_edit)
    Button btnEdit;
    @BindView(R.id.lbl_edit)
    TextView lblEdit;

    Album album;
    ArrayList<Picture> pictureItems;
    PictureAdapter pictureAdapter;
    Picture selectedPicture;

    MyDatabase myDatabase;


    private static ManagePicturesActivity myActivity;

    public static ManagePicturesActivity getMyActivity(){
        return myActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        myActivity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pictures);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        album = new Album(intent.getStringExtra(Constants.ALBUM));

        Typeface tf = Typeface.createFromAsset(getAssets(),
                Constants.APP_FONT);

        lblTitle.setTypeface(tf);
        lblTitle.setText("");

        lblBack.setTypeface(tf);
        btnBack.setTypeface(tf);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAdd.setTypeface(tf);

        btnSend.setTypeface(tf);

        btnCancel.setTypeface(tf);

        btnEdit.setTypeface(tf);
        lblEdit.setTypeface(tf);

        btnCancelRemove.setTypeface(tf);
        btnValidateRemove.setTypeface(tf);
        btnCancelRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                removeBar.setVisibility(View.GONE);
            }
        });
        btnValidateRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePicture(selectedPicture);
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        pictureItems = new ArrayList<>();
        pictureAdapter = new PictureAdapter(this, R.layout.list_picture_item, pictureItems);
        mListView.setAdapter(pictureAdapter);

        myDatabase = Room.databaseBuilder(this, MyDatabase.class,
                Constants.DATABASE_NAME).build();

        populateList();

        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startViewer(i);
            }
        });

    }

    @Override protected void onDestroy(){
        super.onDestroy();
        myDatabase.close();
        myActivity = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.pictures, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_share:
                openNewEmail(Constants.getTourUrl(album.code));
                return true;
            case R.id.menu_edition:
                startAlbumEdition();
                return true;
            case R.id.menu_delete:
                Intent intent = getIntent();
                intent.putExtra("source", "PICTURES");
                intent.putExtra(Constants.ALBUM, album.toJson());
                intent.putExtra("todo", "delete_album");
                setResult(RESULT_OK, intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void populateList(){
        lblTitle.setText(album.name);
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                final Picture[] pictures = myDatabase.myDao().getAlbumPictures(album.albumID);
                pictureItems.addAll(Arrays.asList(pictures));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void openNewEmail(String body) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    private void startAlbumEdition() {
        Intent launcher = new Intent(this, WebActivity.class);

        String url = Constants.prepareInfoUrl(getApplicationContext(),
                Constants.URL_SERVER + Constants.URL_EDIT_TOUR);
        launcher.putExtra("url", url);
        launcher.putExtra("mode", "tour");
        launcher.putExtra("id", album.albumID);
        launcher.putExtra("id", album.toJson());

        startActivityForResult(launcher, 0);
    }

    private void startViewer(int index){
        final Intent launcher = new Intent(this, GLViewActivity.class);
        launcher.putExtra("index", index);
        String[] picturesData = new String[pictureItems.size()];
        for (int i = 0; i < pictureItems.size(); i++) {
            picturesData[i] = pictureItems.get(i).toJson();
        }
        launcher.putExtra(Constants.PICTURES, picturesData);
        final String urlThumb = Constants.getStorageUrl(pictureItems.get(index).userID,
                pictureItems.get(index).pictureID, "thumb21.jpg");
        Picasso.with(this)
                .load(urlThumb)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap thumbnail, Picasso.LoadedFrom from) {
                        if (thumbnail != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbnailImage = baos.toByteArray();
                            launcher.putExtra(Constants.THUMBNAIL, thumbnailImage);
                            startActivityForResult(launcher, 0);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    public void showRemoveBar(Picture picture){
        removeBar.setVisibility(View.VISIBLE);
        selectedPicture = picture;
    }

    public void removePicture(Picture picture){
        if (picture.status == Status.LOCAL){
            removeLocalPicture(picture);
        } else {
            deleteOnline(picture);
        }
    }

    private void deleteOnline(final Picture picture){
        String url = Constants.prepareInfoUrl(getApplicationContext(), Constants.URL_OPERATION);

        RequestBody userID = new FormBody.Builder()
                .add("user_id", picture.userID)
                .build();
        RequestBody albumID = new FormBody.Builder()
                .add("album_id", picture.pictureID)
                .build();
        RequestBody todo = new FormBody.Builder()
                .add("todo", "delete_picture")
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
                                if (todo.equalsIgnoreCase("delete_picture")){
                                    removeLocalPicture(picture);
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

    private void removeLocalPicture(final Picture picture){
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                myDatabase.myDao().DeletePicture(picture);
                populateList();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
