package com.example.demo3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.IconCompat;
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
import com.mapbox.core.utils.TextUtils;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
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


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
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
    private  TextView txt_dis;
    private Button scan_btn;
    private String scanContent = "";
    private Marker point;
    private Button load;
    private Button btn_start;
    int count1 = 0, count2 = 0;
    double distance2 = 0;
    double LOWD2 = 100000;
    double Actualdistance ;
    LatLng PointArrayUP[];
    Marker start;
    LatLng StartP, EndP;
    int latlngIndex = 0;
    Polyline poly;
    private LatLng[] routePP;
    boolean route = true, ArrayC = false;
    private static double[][] w1;
    ArrayList<Integer> result = new ArrayList<>();
    private double[][]dist;
    String PATH;
    private int[][]path;
    private static final  double INF = Double.POSITIVE_INFINITY;
    ArrayList<LatLng> Pts = new ArrayList<>();
    int pathN;
    int preKey,nowKey;
    Vector<LatLng> v =new Vector<LatLng>();

    HashMap<Integer, LatLng> map = new HashMap<Integer, LatLng>();
    HashMap<String, Double> mapdistance = new HashMap<String, Double>();
    private static final LatLng M618 = new LatLng(24.98738088163, 121.54823161628);
    private static final LatLng M615 = new LatLng(24.98730118264, 121.5481621635);


    //sensor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiODI4MzYyMjUiLCJhIjoiY2p1ZmlyODZ4MGR6bDQzbHA2encxaXhydCJ9.NW6EQXxNywZ14Vr9D9VoHA");
        setContentView(R.layout.activity_main);
        txt_dis = (TextView)findViewById(R.id.txt_distace);
        load=(Button)findViewById(R.id.load_btn);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                PATH="map1.geojson";
                pathN=3;
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/82836225/cjyaadi4u04b81ds8josl2vij"),
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
                loadPath();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);//必須到這把設定的值丟給animateCamera
                mapboxMap1.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng origin) {

                        if (point == null) {
                            point = mapboxMap1.addMarker(new MarkerOptions().position(origin));
                        } else {
                            mapboxMap1.removeMarker(point);
                            point = null;
                            point = mapboxMap1.addMarker(new MarkerOptions().position(origin));
                        }
                        EndP = new LatLng(origin);
                        return true;
                    }
                });
                load.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (poly != null) {
                            mapboxMap1.removePolyline(poly);
                        }
                        if (ArrayC) {
                            result.clear();
                        }
                        //設定起點終點後並劃出最短路徑
                        for (int i = 0; i < map.size(); i++) {
                            if (map.get(i).equals(start.getPosition())) count1 = i;
                        }
                        Log.e("count1", String.valueOf(count1));
                        for (int i = 0; i < map.size(); i++) {
                            distance2 = point.getPosition().distanceTo(map.get(i));
                            if (distance2 <= LOWD2) {
                                count2 = i;
                                LOWD2 = distance2;
                            }
                        }
                        Log.e("count2:", String.valueOf(count2));
                        LOWD2 = 10000;
                        //      destination = mapboxMap1.addMarker(new MarkerOptions().position((map.get(count2))).title("終點"));
                        StartP = new LatLng(start.getPosition().getLatitude(), start.getPosition().getLongitude());

                        findCheapestPath(count1, count2, w1);

                        routePP = new LatLng[result.size()];
                        for (int i = 0; i < result.size(); i++) {
                            routePP[i] = map.get(result.get(i));
                        }
                        poly = mapboxMap1.addPolyline(new PolylineOptions()
                                .add(routePP)
                                .color(Color.RED)
                                .width(5));
                        //  setTimerTask();
                        Actualdistance = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP.getLatitude(), EndP.getLongitude());
                        txt_dis.setText(String.valueOf(Actualdistance));
                    }
                });
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//請求權限～
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，這邊要取得。
            //以下動作是向使用者取得權限。
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA, BODY_SENSORS}, Requestcode);
        } else {
            //取得完畢，進行下一步動作。
        }
    }

    //QRCODE
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
  //      IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
  //     Icon icon = iconFactory.fromResource(R.drawable.mapbox_compass_icon);
        if (scanningResult != null) {
            scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            scan_content.setText(scanContent);
            scan_format.setText(scanFormat);
        } else {
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, intent);
        }
        switch (scanContent) {
            case "M618":
                start = mapboxMap1.addMarker(new MarkerOptions().position(M618).title("起點"));
                break;
            case "M617":
                start = mapboxMap1.addMarker(new MarkerOptions().position(M615).title("起點"));
                break;
        }
       //  setTimerTask();

}

   private void loadPath() {
        try {
            Log.e("陣列:", "進");
            // Load GeoJSON file
            InputStream inputStream = getAssets().open(PATH);
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp ;
            while ((cp = rd.read()) != -1) {
                sb.append((char)cp);
      //          Log.e("json:", sb.toString());
            }
            inputStream.close();
            Log.e("json:", "3");
            // Parse JSON
            JSONObject json = new JSONObject(sb.toString());

            JSONArray features = json.getJSONArray("features");
            Log.e("json5:", "5");
            for (int i = 0; i < pathN; i++) {
                Log.e("json2:", "3");
                JSONObject feature = features.getJSONObject(i);

                JSONObject geometry = feature.getJSONObject("geometry");
                if (geometry != null) {
                    Log.e("geometry", "3");
                    String type = geometry.getString("type");

                    // Our GeoJSON only has one feature: a line string
                    if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

                        // Get the Coordinates
                        JSONArray coords = geometry.getJSONArray("coordinates");

                        for (int lc = 0; lc < coords.length(); lc++) {
                            JSONArray coord = coords.getJSONArray(lc);
                            LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                            Pts.add(latLng);
                            Log.e("所有陣列:", latLng.toString());
                            int checkpoint = 0; //判定每個點是否存在
                            for (int z = 0; z < latlngIndex; z++) //判斷經緯度在HASHMAP裡是否出現過如果有checkpoint++
                            {
                                LatLng tmp = map.get(z);
                                if (tmp.getLatitude() == latLng.getLatitude()) {
                                    if (tmp.getLongitude() == latLng.getLongitude()) {
                                        checkpoint++;
                                    }
                                }
                            }
                            if (checkpoint == 0)//如果不存在創建一個新的KEY放進map
                            {
                                map.put(latlngIndex, latLng);
                                latlngIndex++;
                            }
                            if (lc != 0) {
                                JSONArray precoord = coords.getJSONArray(lc - 1);
                                LatLng preLatLng = new LatLng(precoord.getDouble(1), precoord.getDouble(0));
                                //float[] results = new float[1];
                                //Location.distanceBetween(preLatLng.getLatitude(), preLatLng.getLongitude(), latLng.getLatitude(), latLng.getLongitude(),results);
                                double ddd = preLatLng.distanceTo(latLng);///

                                for (int s : map.keySet()) {
                                    if (map.get(s).equals(preLatLng)) {
                                        preKey = s;
                                    } else if (map.get(s).equals(latLng)) {
                                        nowKey = s;
                                    }
                                }
                                String keyString = preKey + "," + nowKey;
                            //    Log.e("距離3:",results.toString());
                               mapdistance.put(keyString, ddd);
                            }
                            Pts.add(latLng);
                            Log.e("陣列:", Pts.toString());
                            //  Log.e("前點:",String.valueOf( mapdistance.get(preLatLng)));
                            Log.e("當點:", String.valueOf(map));
                        }

                    }
                }
            }
            StringBuilder a = new StringBuilder();
            w1 = new double[map.size()][map.size()];
            for (int m = 0; m < map.size(); m++) {
                for (int n = 0; n < map.size(); n++) {
                    w1[m][n] = Double.POSITIVE_INFINITY;
                }
            }
            String[] key;
            for (String s : mapdistance.keySet()) {
                key = s.split(",");
                w1[Integer.parseInt(key[0])][Integer.parseInt(key[1])] = mapdistance.get(s);
                w1[Integer.parseInt(key[1])][Integer.parseInt(key[0])] = mapdistance.get(s);
                Log.e("各個點:", String.valueOf(w1) );
            }
            for (int p = 0; p < map.size(); p++) {
                for (int l = 0; l < map.size(); l++) {
                    a.append(w1[p][l]).append(",");
                }
            }
            LatLng PointArray[] = new LatLng[Pts.size()];
            for (int c = 0; c < Pts.size(); c++) {
                PointArray[c] = Pts.get(c);
            }
            for (LatLng ele : PointArray) {
                if (!v.contains(ele)) {
                    v.add(ele);
                }
            }
            PointArrayUP = new LatLng[v.size()];
            for (int i = 0; i < v.size(); i++) {
                PointArrayUP[i] = v.get(i);
                Log.e("個點陣列:", PointArrayUP[i].toString());
            }
            dist = new double[w1.length][w1.length];
            path = new int[w1.length][w1.length];
        } catch (Exception exception) {
          //  Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
        }
    }
    public void findCheapestPath(int begin, int end, double[][] matrix) {
        floyd(matrix);
        result.add(begin);
        findPath(begin, end);
        result.add(end);
        ArrayC = true;
    }

    //最短路徑規劃找出所有點
    public void findPath(int i, int j) {
        int k = path[i][j];
        if (k == -1) return;
        findPath(i, k);   //遞歸
        result.add(k);
        findPath(k, j);
    }
    //佛洛伊德演算法(求兩點最短路徑)
    public void floyd(double[][] matrix) {

        int size = matrix.length;
        //initialize dist and path
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                path[i][j] = -1;
                dist[i][j] = matrix[i][j];
            }
        }
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (dist[i][k] != INF &&
                            dist[k][j] != INF &&
                            dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        path[i][j] = k;
                    }
                }
            }
        }

    }

    //QRCODE宣告
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    //傳感器-------地圖旋轉
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
    //方位旋轉
    private void updateCameraBearing(MapboxMap mMap, float bearing){
        mapboxMap1 = mMap;
        CameraPosition currentPlace = new CameraPosition.Builder()
                // .target(new LatLng(24.987366, 121.548034))//管院
               // .target(new LatLng(24.987419, 121.548190))//舍我
                .bearing(bearing).zoom(18.6f).tilt(40).build();
        mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace),2000);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
    //計算該緯度上的精度長度
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //計算經緯度實際距離
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
        double EARTH_RADIUS = 6378137;
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}

