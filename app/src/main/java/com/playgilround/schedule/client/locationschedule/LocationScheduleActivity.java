package com.playgilround.schedule.client.locationschedule;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.locationschedule.model.SearchLocationResult;
import com.playgilround.schedule.client.model.LocationInfo;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.playgilround.schedule.client.addschedule.AddScheduleActivity.LOCATION_OK;

/**
 * 19-01-01
 * 위치 관련 Activity
 */
public class LocationScheduleActivity extends AppCompatActivity implements OnMapReadyCallback,
        MaterialSearchBar.OnSearchActionListener, LocationScheduleContract.View {

    static final String TAG = LocationScheduleActivity.class.getSimpleName();

    public static final String LOCATION_CURRENT = "current";
    public static final String LOCATION_DESTINATION = "destination";

    public static final String INTENT_EXTRA_LOCATION = "location";
    public static final String INTENT_EXTRA_LATITUDE = "latitude";
    public static final String INTENT_EXTRA_LONGITUDE = "longitude";

    @BindView(R.id.searchBar)
    MaterialSearchBar searchBar;
    double latitude; // 위도
    double longitude; // 경도

    double intentLatitude;
    double intentLongitude;

    // 현재 SearchBar 에 적힌 텍스트 확인.
    String strSearchBar;

    private GoogleMap mMap;

    ProgressDialog progress;

    /**
     * Material Search Bar 검색 버튼 클릭 시,
     * 2번 검색되는 버그가 있어 flag 추가 (isSearch)
     * 검색 버튼 클릭 시,
     * SearchBar 텍스트가 이전과 같을 경우
     * 검색이 안되는 버그가 있어 flag 추가 (isInit).
     */
    private boolean isInit = false;
    private boolean isSearch = true;

    private LocationScheduleContract.Presenter mPresenter;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            finishLocation();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        intentLatitude = intent.getDoubleExtra("latitude", 0);
        intentLongitude = intent.getDoubleExtra("longitude", 0);

        new LocationSchedulePresenter(this, this);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);

            progress = new ProgressDialog(this);
            progress.setCanceledOnTouchOutside(false);
            progress.setTitle(getString(R.string.text_location));
            progress.setMessage(getString(R.string.text_find_current_location));
            progress.show();
        } catch (SecurityException e) {
            Log.e(TAG, "Set location manager error - " + e.toString());
            // e.printStackTrace();

            // Crash 에러 등록 코드
            // CrashlyticsCore.getInstance().logException(e);
        }

        //Material Search Bar 관련 작업
        searchBar.setHint(getString(R.string.text_search_location));
        searchBar.setSpeechMode(false);

        searchBar.setOnSearchActionListener(this);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                isInit = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                strSearchBar = editable.toString();
            }
        });
    }

    //위치 주변 원 표시 작업
    protected CircleOptions showCircleAround(LatLng aroundMap) {
        return new CircleOptions().center(aroundMap)
                .radius(500)     //반지름 단위 : m
                .strokeWidth(0f) //선 없음
                .fillColor(getResources().getColor(R.color.color_map_background));
    }

    //MarkerOption 설정 작업
    protected MarkerOptions showMarkerDisplay(String title, String snippet, LatLng latLng, BitmapDescriptor icon) {
        return new MarkerOptions().title(title).snippet(snippet).position(latLng).icon(icon);
    }

    //SearchBar Clicked
    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                break;

            case MaterialSearchBar.BUTTON_SPEECH:
                break;
        }
    }

    //SearchBar inputText changed
    @Override
    public void onSearchStateChanged(boolean enabled) {

    }

    //SearchBar SearchButton Click
    @Override
    public void onSearchConfirmed(CharSequence text) {
        if (isSearch) {
            if (isInit) {
                mPresenter.onSearchConfirmed(text);
                isSearch = false;
            }
        } else {
            isSearch = true;
        }
    }

    // 검색 결과 성공 시
    @Override
    public void mapSearchResultComplete(SearchLocationResult result) {
        MarkerOptions mOption = showMarkerDisplay(result.getTitle(), result.getSnippet(), result.getSearchResultLocation(),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addCircle(showCircleAround(result.getSearchResultLocation()));
        mMap.addMarker(mOption);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result.getSearchResultLocation(), result.getZoomLevel()));

        mMap.addPolyline(new PolylineOptions().add(result.getCurrentLocation(), result.getSearchResultLocation()).width(5).color(Color.RED));

    }

    // 검색 결과 실패 시
    @Override
    public void mapSearchResultError() {
        Toast.makeText(this, getString(R.string.toast_error_msg_find_location), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mPresenter.setMapDisplay(latitude, longitude, intentLatitude, intentLongitude);
    }

    @Override
    public void setMapMarker(LatLng curMap, LatLng destMap) {
        progress.cancel();
        MarkerOptions currentMarker = showMarkerDisplay(getString(R.string.text_my_location), getString(R.string.text_my_location), curMap, null);

        if (destMap.latitude != 0.0) {
            MarkerOptions destMarker = showMarkerDisplay(getString(R.string.text_destination), getString(R.string.text_destination), destMap, null);
            mMap.addMarker(destMarker);
            mMap.addCircle(showCircleAround(destMap));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destMap, 15));
        }
        mMap.addMarker(currentMarker);
        mMap.addCircle(showCircleAround(curMap));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curMap, 15));

        int zoomLevel = mPresenter.setMapZoomLevel(curMap, destMap);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
        mMap.addPolyline(new PolylineOptions().add(curMap, destMap).width(5).color(Color.RED));
    }

    //위도 경도 탐색완료.
    private void finishLocation() {
        // Todo:: Android X에 맞춰서 코드 정리 할 필요가 있음.
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    @OnClick(R.id.tvCancel)
    void onCancelClick() {
        finish();
    }

    @OnClick(R.id.tvConfirm)
    void onConfirmClick() {
        //Search Bar 텍스트와, 지정된 Location이 같을 때만 finish
        ArrayList<LocationInfo> arrLocationInfo = mPresenter.getLocationInfo();

        double searchLatitude = arrLocationInfo.get(0).latitude;
        double searchLongitude = arrLocationInfo.get(0).longitude;
        String searchLocation = arrLocationInfo.get(0).location;

        if (strSearchBar == null || searchLocation == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_null_location), Toast.LENGTH_LONG).show();
        } else if (strSearchBar.equals(searchLocation)) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_LOCATION, searchLocation);
            intent.putExtra(INTENT_EXTRA_LATITUDE, searchLatitude);
            intent.putExtra(INTENT_EXTRA_LONGITUDE, searchLongitude);
            setResult(LOCATION_OK, intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_check_location), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setPresenter(LocationScheduleContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
