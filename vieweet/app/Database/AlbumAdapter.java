package com.vieweet.app.Database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixeet.app.camera360.R;
import com.squareup.picasso.Picasso;
import com.vieweet.app.Constants;
import com.vieweet.app.ManageToursActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumAdapter extends ArrayAdapter<Album>{

    private int resource;

    public AlbumAdapter(@NonNull Context context,int resource,  List<Album> albums) {
        super(context, resource, albums);
        this.resource = resource;

    }

    @Override
    public int getItemViewType(int position){
        return  1;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        Album album = getItem(position);

        assert album != null;
        String subTitle = album.count > 1 ? String.valueOf(album.count) + " panoramas"
                : (album.count > 0 ? String.valueOf(album.count) + " panorama" : "No panorama");
        String title = (album.name.length() > 0 ? album.name : "No name");

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.btnDelete.setTag(position);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                ManageToursActivity albumsActivity = ManageToursActivity.getMyActivity();
                albumsActivity.showRemoveBar(getItem(index));
            }
        });

        holder.subTitleView.setText(subTitle);
        holder.titleView.setText(title);

        if (album.pictureID.length() > 0) {
            String urlThumb = Constants.getStorageUrl(album.userID, album.pictureID, "thumb21.jpg");
            Picasso.with(getContext())
                    .load(urlThumb)
                    .into(holder.thumbImage);

        }else {
            holder.thumbImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_screen_bg));
            holder.thumbImage.setImageResource(R.drawable.ic_launcher);
        }

        return convertView;


    }

    static class ViewHolder {
        @BindView(R.id.btn_delete)
        ImageButton btnDelete;
        @BindView(R.id.rowSubTitle)
        TextView subTitleView;
        @BindView(R.id.rowTitle)
        TextView titleView;
        @BindView(R.id.thumbImage)
        ImageView thumbImage;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
