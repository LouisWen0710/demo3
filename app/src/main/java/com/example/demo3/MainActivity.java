package com.example.demo3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
//import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;


import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private MapboxMap mapboxMap1;
    private MapView mapView;
    //sensor
    private SensorManager sensorManager;
    private Sensor mSensorG;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f; //這是方位，非常重要。
    private float currentAzimuth = 0f;
    private int Requestcode = 100; //none means just for requestPermission
    private Activity mainactivity;
    private TextView scan_content;
    private TextView scan_format;
    private Button scan_btn;


    //sensor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiODI4MzYyMjUiLCJhIjoiY2p1ZmlyODZ4MGR6bDQzbHA2encxaXhydCJ9.NW6EQXxNywZ14Vr9D9VoHA");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/82836225/cjvw5bu3f0jzu1cpg82699h61"),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                //do nothing
                            }
                        });
                mapboxMap1 = mapboxMap; //在MapView上做一個Mapbox物件
                mapboxMap.setMinZoomPreference(10.5);
                mapboxMap.setMaxZoomPreference(23);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(24.987419, 121.548190))//世新大學舍我樓院經緯度
                        .zoom(20)//載入時的大小
                        .bearing(170)//將地圖呈現時的傾斜度設置為平行
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),1000);//必須到這把設定的值丟給animateCamera
            }
        });

        init_view();
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(mainactivity);
                scanIntegrator.initiateScan();
            }
        });


        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//請求權限～
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission != PackageManager.PERMISSION_GRANTED){
            //未取得權限，這邊要取得。
            //以下動作是向使用者取得權限。
            ActivityCompat.requestPermissions(this,new String[] {ACCESS_FINE_LOCATION, CAMERA, BODY_SENSORS}, Requestcode);
        }else{
            //取得完畢，進行下一步動作。
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(scanningResult!=null){
            String scanContent=scanningResult.getContents();
            String scanFormat=scanningResult.getFormatName();
            scan_content.setText(scanContent);
            scan_format.setText(scanFormat);
        }else{
            Toast.makeText(getApplicationContext(),"nothing",Toast.LENGTH_SHORT).show();
        }
    }
    private void init_view(){
        this.scan_content=(TextView)findViewById(R.id.textView);
        this.scan_format=(TextView)findViewById(R.id.textView2);
        this.mainactivity=this;
        this.scan_btn = (Button)findViewById(R.id.QR);
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorManager.SENSOR_DELAY_GAME);
        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        sensorManager.unregisterListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        //  navigation.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f; //alpha值是用來過濾一些雜訊，降低誤差值。
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
            }
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
                Animation anim = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                currentAzimuth = azimuth;
                updateCameraBearing(mapboxMap1, currentAzimuth);

            }
        }
    }
    private void updateCameraBearing(MapboxMap mMap, float bearing){
        mapboxMap1 = mMap;
        CameraPosition currentPlace = new CameraPosition.Builder()
                 .target(new LatLng(24.987366, 121.548014))//管院
               // .target(new LatLng(24.987419, 121.548190))//舍我
                .bearing(bearing).zoom(17.5f).build();
        mapboxMap1.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
}

