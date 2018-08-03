package com.vieweet.app.Database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pixeet.app.camera360.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vieweet.app.Constants;
import com.vieweet.app.ManagePicturesActivity;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PictureAdapter extends ArrayAdapter<Picture> {
    private int resource;

    public PictureAdapter(@NonNull Context context, int resource, List<Picture> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @NonNull
    @Override public View getView(int position, View view, @NonNull ViewGroup parent){

        final ViewHolder holder;

        Picture picture = getItem(position);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        assert picture != null;
        String subTitle = sdf.format(picture.dateCreated);
        String title = picture.name.length() > 0 ? picture.name : "No name";

        if (view != null){
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(getContext()).inflate(resource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.subTitleView.setText(subTitle);
        holder.titleView.setText(title);

        holder.btnDelete.setTag(position);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                ManagePicturesActivity picturesActivity = ManagePicturesActivity.getMyActivity();
                picturesActivity.showRemoveBar(getItem(index));
            }
        });
        int progression = 0;
        holder.btnDelete.setVisibility(View.VISIBLE);
        if (picture.status == Status.PROCESSED) {
            progression = 0;
            holder.thumbImage.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.color_screen_bg));
            holder.viewStatus.setVisibility(View.GONE);
        } else if (picture.status == Status.ONLINE) {
            holder.thumbImage.setBackgroundColor(Color.BLACK);
            holder.viewStatus.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.color_status_process));
            holder.viewStatus.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);
        } else if (picture.status == Status.LOCAL) {
            holder.thumbImage.setBackgroundColor(Color.BLACK);
            holder.viewStatus.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.color_status_upload));
            holder.viewStatus.setVisibility(View.VISIBLE);
        } else {
            progression = 100;
            holder.thumbImage.setBackgroundColor(Color.BLACK);
            holder.viewStatus.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.color_status_draft));
            holder.viewStatus.setVisibility(View.VISIBLE);
        }
        RelativeLayout.LayoutParams thumbParams =
                (RelativeLayout.LayoutParams) holder.thumbImage.getLayoutParams();
        RelativeLayout.LayoutParams viewParams =
                (RelativeLayout.LayoutParams) holder.viewStatus.getLayoutParams();
        viewParams.width = thumbParams.width * progression / 100;
        holder.viewStatus.setLayoutParams(viewParams);

        String urlThumb = Constants.getStorageUrl(picture.userID, picture.pictureID, "thumb21.jpg");
        Picasso.with(getContext())
                .load(urlThumb)
                .into(holder.thumbImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });
        return view;
    }

    class ViewHolder{

        @BindView(R.id.rowSubTitle) TextView subTitleView;
        @BindView(R.id.rowTitle) TextView titleView;
        @BindView(R.id.thumbImage)
        ImageView thumbImage;
        @BindView(R.id.btn_delete)
        ImageButton btnDelete;
        @BindView(R.id.view_status) View viewStatus;
        @BindView(R.id.bar_progression)
        ProgressBar mProgressBar;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
