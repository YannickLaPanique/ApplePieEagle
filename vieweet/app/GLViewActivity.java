package com.vieweet.app;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.pixeet.app.camera360.R;
import com.squareup.picasso.Picasso;
import com.theta360.sample.v2.glview.GLPhotoView;
import com.theta360.sample.v2.model.Photo;
import com.theta360.sample.v2.model.RotateInertia;
import com.vieweet.app.Database.Hotspot;
import com.vieweet.app.Database.MyDatabase;
import com.vieweet.app.Database.Picture;
import com.vieweet.app.Database.Status;
import com.vieweet.app.Glide.GlideApp;
import com.vieweet.app.Glide.ProgressTarget;
import com.vieweet.app.Network.HttpClient;
import com.vieweet.app.Network.ServerResponse;
import com.vieweet.app.Network.ServiceGenerator;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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

public class GLViewActivity extends FragmentActivity implements OnMapReadyCallback{

    @BindView(R.id.top_bar)
    RelativeLayout topBar;
    @BindView(R.id.bottom_bar)
    RelativeLayout bottomBar;

    @BindView(R.id.photo_image) GLPhotoView mGLPhotoView;

    RotateInertia mRotateInertia = RotateInertia.INERTIA_0;
    Photo mTexture;

    @BindView(R.id.lbl_edit)
    TextView lblEdit;
    @BindView(R.id.btn_edit)
    Button btnEdit;
    @BindView(R.id.lbl_title)
    TextView lblTitle;
    @BindView(R.id.lbl_back)
    TextView lblBack;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.btn_previous)
    Button btnPrevious;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.btn_info_nav)
    Button btnInfoNav;

    @BindView(R.id.loading_photo_progress_bar) ProgressBar mProgressBar;

    @BindView(R.id.remove_bar) TableLayout layoutRemove;
    @BindView(R.id.btn_cancel_remove) Button btnCancelRemove;
    @BindView(R.id.btn_validate_remove) Button btnValidateRemove;

    @BindView(R.id.top_map) RelativeLayout top_map;
    GoogleMap mMap;

    ArrayList<Picture> pictures;
    List<Hotspot> hotspots;
    Hotspot selectedHotspot;
    Picture picture;
    int index;

    MyDatabase myDatabase;

    static GLViewActivity myActivity;

    public static GLViewActivity getMyActivity(){
        return myActivity;
    }

    PopupWindow popupWindow;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        myActivity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_viewer360);

        ButterKnife.bind(this);

        pictures = new ArrayList<>();
        Intent intent = getIntent();
        String[] picturesData = intent.getStringArrayExtra(Constants.PICTURES);
        for (String data : picturesData) {
            pictures.add(new Picture(data));
        }

        index = intent.getIntExtra("index", 0);
        if (index ==1) {
            bottomBar.setVisibility(View.GONE);
        }
        btnInfoNav.setText((index + 1) + "/" + pictures.size());
        picture = pictures.get(index);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        lblEdit.setTypeface(tf);
        btnEdit.setTypeface(tf);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionsMenu();
            }
        });

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        btnPrevious.setTypeface(tf);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index -= 1;
                if (index == -1){
                    index = pictures.size() - 1;
                }
                picture = pictures.get(index);
                loadPicture();
            }
        });
        btnNext.setTypeface(tf);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index += 1;
                if (index == pictures.size()){
                    index = 0;
                }
                picture = pictures.get(index);
                loadPicture();
            }
        });

        byte[] byteThumbnail = intent.getByteArrayExtra(Constants.THUMBNAIL);
        ByteArrayInputStream inputStreamThumbnail = new ByteArrayInputStream(byteThumbnail);
        Drawable thumbnail = BitmapDrawable.createFromStream(inputStreamThumbnail, null);

        Photo _thumbnail = new Photo(((BitmapDrawable)thumbnail).getBitmap());

        mGLPhotoView.setTexture(_thumbnail);
        mGLPhotoView.setmRotateInertia(mRotateInertia);

        myDatabase = Room.databaseBuilder(this, MyDatabase.class, Constants.DATABASE_NAME)
                .build();

        loadPicture();

        initRemoveBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myActivity = null;

        myDatabase.close();

        if (popupWindow != null){
            popupWindow.dismiss();
        }
    }

    /**
     * onResume Method
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLPhotoView.onResume();

        if (null != mTexture) {
            if (null != mGLPhotoView) {
                mGLPhotoView.setTexture(mTexture);
            }
        }
    }

    /**
     * onPause Method
     */
    @Override
    protected void onPause() {
        this.mGLPhotoView.onPause();
        super.onPause();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.map2, MapActivity.newInstance(picture.latitude, picture.longitude));
                transaction.commit();
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.viewer360, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_share:
                openNewEmail(Constants.getPanoUrl(picture.code));
                return true;
            case R.id.menu_move:

                return true;
            case R.id.menu_edition:
                startPictureEdition();
                return true;
            case R.id.menu_delete:
                //
                Intent i = getIntent();
                i.putExtra("source", "VIEWER");
                i.putExtra("picture_id", picture.pictureID);
                i.putExtra("todo", "delete_picture");
                setResult(RESULT_OK, i);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initRemoveBar() {
        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);
        btnCancelRemove.setTypeface(tf);
        btnValidateRemove.setTypeface(tf);
        // Confirm Remove
        btnCancelRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                layoutRemove.setVisibility(View.GONE);
                top_map.setVisibility(View.VISIBLE);
            }
        });

        btnValidateRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Delete Online
                mProgressBar.setVisibility(View.VISIBLE);
                try {
                    SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
                    RequestBody userID = new FormBody.Builder()
                            .add("user_id", prefs.getString("user_id", ""))
                            .build();
                    RequestBody hotspotID = new FormBody.Builder()
                            .add("hotspot_id", selectedHotspot.hotspotID)
                            .build();
                    RequestBody todo = new FormBody.Builder()
                            .add("todo", "delete_hotspot")
                            .build();

                    final MultipartBody data = new MultipartBody.Builder()
                            .addPart(userID)
                            .addPart(hotspotID)
                            .addPart(todo)
                            .build();

                    String errorMessage = updateHotspot(data);

                    if (errorMessage != null) {
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                    mProgressBar.setVisibility(View.GONE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        layoutRemove.setVisibility(View.GONE);
    }

    private void openNewEmail(String body) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    private void startPictureEdition() {
        String pictureData = picture.serverJSON();

        Intent launcher = new Intent(this, WebActivity.class);


        String url = Constants.prepareInfoUrl(getApplicationContext(),
                Constants.URL_SERVER + Constants.URL_EDIT_PANORAMA);
        launcher.putExtra("url", url);
        launcher.putExtra("mode", "pano");
        launcher.putExtra("id", picture.pictureID);
        try {
            launcher.putExtra("post", pictureData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startActivityForResult(launcher, 0);
    }

    public void loadHotspot(final Hotspot hotspot){
        if (hotspot.type == 4) {
            // URL
            String url = hotspot.destID;
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        } else if (hotspot.type == 3) {
            // Video
            String url = Constants.getStorageUrl(picture.userID, hotspot.destID, "hotspot.m4v");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        } else if (hotspot.type == 2) {
            // PDF
            String url = Constants.getStorageUrl(picture.userID, hotspot.destID, "hotspot.pdf");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        } else if (hotspot.type == 1) {
            // Photo
            String url = Constants.getStorageUrl(picture.userID, hotspot.destID, "hotspot.png");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        } else {
            // Pano
            Completable.fromAction(new Action() {
                @Override
                public void run() {
                    Picture[] pcts = myDatabase.myDao().getPictures(hotspot.destID);
                    if (pcts != null && pcts.length > 0) {
                        picture = pcts[0];
                        loadPicture();
                    }
                    else {
                        myDatabase.myDao().DeleteHotspot(hotspot);
                        loadPicture();
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public void loadPicture(){
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                hotspots = myDatabase.myDao().getHotspotList(picture.pictureID);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadPhoto();
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void loadPhoto(){
        String urlImage = Constants.getStorageUrl(picture.userID, picture.pictureID, "equi.jpg");

        ProgressTarget<String, Bitmap> target = new MyProgressTarget<>(
                new SimpleTarget<Bitmap>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (popupWindow != null) popupWindow.dismiss();
                        Bitmap bitmap = drawHotspot(resource);
                        mTexture = new Photo(bitmap);
                        mGLPhotoView.setTexture(mTexture);
                        mGLPhotoView.setHotspots(hotspots);
                        btnInfoNav.setText((index + 1) + "/" + pictures.size());
                    }
                }, mProgressBar, getApplicationContext());
        target.setModel(urlImage);


        GlideApp.with(this)
                .asBitmap()
                .load(urlImage)
                .into(target);
    }

    public void selectionPicture(final Double theta, final Double phi){
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        LinearLayout layout = new LinearLayout(this);

        for (final Picture pct : pictures) {
            if (!pct.pictureID.equals(picture.pictureID)){
                LinearLayout view = (LinearLayout) (LayoutInflater.from(this).inflate(R.layout.image_selection, null));

                TextView title = view.findViewById(R.id.title);
                ImageView image = view.findViewById(R.id.image);

                title.setText(pct.name);
                String urlThumb = Constants.getStorageUrl(pct.userID, pct.pictureID, "thumb21.jpg");
                Picasso.with(getApplicationContext())
                        .load(urlThumb)
                        .into(image);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Hotspot hotspot = new Hotspot(picture.pictureID, pct.pictureID, theta, phi);
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() {
                                myDatabase.myDao().InsertHotspot(hotspot);
                                hotspots.add(hotspot);
                                selectedHotspot = hotspot;
                                SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
                                RequestBody userID = new FormBody.Builder()
                                        .add("user_id", prefs.getString("user_id", ""))
                                        .build();
                                RequestBody hotspotID = new FormBody.Builder()
                                        .add("hotspot_id", hotspot.hotspotID)
                                        .build();
                                RequestBody sourceID = new FormBody.Builder()
                                        .add("source_id", hotspot.sourceID)
                                        .build();
                                RequestBody dest_id = new FormBody.Builder()
                                        .add("dest_id", hotspot.destID)
                                        .build();
                                RequestBody theta = new FormBody.Builder()
                                        .add("theta", hotspot.theta.toString())
                                        .build();
                                RequestBody phi = new FormBody.Builder()
                                        .add("phi", hotspot.phi.toString())
                                        .build();
                                RequestBody todo = new FormBody.Builder()
                                        .add("todo", "insert_hotspot")
                                        .build();

                                final MultipartBody data = new MultipartBody.Builder()
                                        .addPart(userID)
                                        .addPart(hotspotID)
                                        .addPart(sourceID)
                                        .addPart(dest_id)
                                        .addPart(theta)
                                        .addPart(phi)
                                        .addPart(todo)
                                        .build();

                                updateHotspot(data);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadPhoto();
                                    }
                                });
                            }
                        }).subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                });
                layout.addView(view);
            }
        }

        scrollView.addView(layout);
        RelativeLayout relativeLayout =  new RelativeLayout(this);
        relativeLayout.addView(scrollView);
        if (popupWindow !=  null && popupWindow.isShowing()) popupWindow.dismiss();
        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(relativeLayout);
        popupWindow.showAtLocation(mGLPhotoView, Gravity.BOTTOM, 0, 0);
    }

    private Bitmap drawHotspot(Bitmap bitmap){

        Drawable hotspotDrawable = getDrawable(R.drawable.hotspot3d);
        assert hotspotDrawable != null;
        Bitmap hotspotImage = ((BitmapDrawable)hotspotDrawable).getBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int hsptWidth = hotspotImage.getWidth() / 2;
        int hsptHeight =  hotspotImage.getHeight() / 2;

        Bitmap bmOverlay = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        canvas.drawBitmap(bitmap, new Matrix(), null);

        for (Hotspot hotspot : hotspots) {
            int x = (int) (width/2 + (hotspot.theta * width / 360));
            int y = (int) (height/2 - (hotspot.phi * height / 180));

            Rect dst1 = new Rect(x - hsptWidth/2, y - hsptHeight/2, x + hsptWidth/2, y + hsptHeight/2);
            Rect dst2 = new Rect(x - hsptWidth/2 - width, y - hsptHeight/2, x + hsptWidth/2 - width, y + hsptHeight/2);
            Rect dst3 = new Rect(x - hsptWidth/2 + width, y - hsptHeight/2, x + hsptWidth/2 + width, y + hsptHeight/2);

            canvas.drawBitmap(hotspotImage, null, dst1, null);
            canvas.drawBitmap(hotspotImage, null, dst2, null);
            canvas.drawBitmap(hotspotImage, null, dst3, null);
        }

        return bmOverlay;
    }

    private String updateHotspot(RequestBody data){
        String url = Constants.prepareInfoUrl(this,
                Constants.URL_OPERATION);

        HttpClient client = ServiceGenerator.createService(HttpClient.class);
        Call<ServerResponse> call = client.UpdateTask(url, data);

        final String[] errorMessage = new String[1];

         call.enqueue(new Callback<ServerResponse>() {
             @Override
             public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                 ServerResponse serverResponse = response.body();
                 if (serverResponse != null) {
                     int result = serverResponse.result;
                     if (result == 200){
                         ServerResponse.ResponseInfo responseInfo = serverResponse.responseInfo;
                         if (responseInfo != null){
                             ServerResponse.DataInfo dataInfo = responseInfo.dataInfo;
                             if (dataInfo != null) {
                                 String todo = dataInfo.todo;
                                 if (todo.equalsIgnoreCase("delete_album")){
                                     myDatabase.myDao().DeleteHotspot(selectedHotspot);
                                     loadPicture();
                                 } else if (todo.equalsIgnoreCase("insert_hotspot")){
                                     selectedHotspot.status = Status.ONLINE;
                                     myDatabase.myDao().UpdateHotspot(selectedHotspot);
                                 }
                             }
                         }
                     } else {
                         errorMessage[0] = serverResponse.errMess;
                         if (errorMessage[0] == null || errorMessage[0].length() == 0) {
                             errorMessage[0] = getString(R.string.General_ErrNotAvailable);
                         }
                     }
                 }
             }

             @Override
             public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                 t.printStackTrace();
             }
         });
         return errorMessage[0];
    }
}

class MyProgressTarget<Z> extends ProgressTarget<String, Z> {

    private final ProgressBar progress;

    MyProgressTarget(Target<Z> target, ProgressBar progress, Context context) {
        super(target, context);
        this.progress = progress;
    }

    @Override public float getGranualityPercentage() {
        return 0.1f; // this matches the format string for #text below
    }

    @Override protected void onConnecting() {
        progress.setIndeterminate(true);
        progress.setVisibility(View.VISIBLE);
    }

    @Override protected void onDownloading(long bytesRead, long expectedLength) {
        progress.setIndeterminate(false);
        progress.setProgress((int)(100 * bytesRead / expectedLength));
    }

    @Override protected void onDownloaded() {
        progress.setIndeterminate(true);
    }
    @Override protected void onDelivered() {
        progress.setVisibility(View.GONE);
    }
}