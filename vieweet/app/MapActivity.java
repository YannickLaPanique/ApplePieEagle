package com.vieweet.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pixeet.app.camera360.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MapActivity extends Fragment implements OnMapReadyCallback{

    GoogleMap mMap;
    double lat = 51.508174;
    double lng = -0.075962;

    @BindView(R.id.lbl_back_map)
    TextView txtBack;

    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.activity_map, container, false);
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null){
            lat = extras.getDouble("lat");
            lng = extras.getDouble("lng");
        }

        unbinder = ButterKnife.bind(this, view);

        assert getFragmentManager() != null;
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map2);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override public void onDestroyView(){
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(10, 60, 0, 0);
        LatLng london = new LatLng(lat, lng);
        mMap.addMarker(
                new MarkerOptions().position(london)
                .title("London"));
        CameraPosition mPosition =
                new CameraPosition.Builder()
                .target(london)
                .zoom(14)
                .bearing(90)
                .tilt(30)
                .build();

        googleMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(mPosition)
        );
    }

    public MapActivity(){
        //empty constructor
    }

    public static MapActivity newInstance(Double lat, Double lng){
        MapActivity mapActivity = new MapActivity();

        Bundle args = new Bundle();
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        mapActivity.setArguments(args);

        return mapActivity;
    }
}
