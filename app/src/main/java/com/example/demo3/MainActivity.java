package com.example.demo3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private MapboxMap mapboxMap1;
    private MapView mapView;
    String style="mapbox://styles/82836225/cjyaadi4u04b81ds8josl2vi"; //預設樣式
    //sensor---------------------//
    private SensorManager sensorManager; //sensor manager
    private Sensor mSensorG;
    private float[] mGravity = new float[3];//陀螺
    private float[] mGeomagnetic = new float[3];//三軸
    private float timestamp; //每秒 tamp
    private static final float NS2S = 1.0f / 1000000000.0f;//基準值
    float anglez = 0, angley = 0, AngleY = 0, AngleZ = 0; //Y,Z軸角度
    float accY, accZ; //Ｙ,Z加速度值
    float[] angle = new float[3]; //陀螺儀角度計算
    float[] angleStep = new float[3];
    double angleyz;//旋轉角度
    float yCoeff, zCoeff; // 加速度/10後的基準值
    private float azimuth = 0f; //這是方位，非常重要。
    private float currentAzimuth = 0f;//方位
    //sensor---------------------//
    private int Requestcode = 100; //none means just for requestPermission
    //------------變數宣告--------//
    private Activity mainactivity;
    private TextView scan_content;
    private TextView scan_format;
    private TextView txt_dis;
    private Button scan_btn;
    private  boolean startroute=false;
    private  boolean navistart = false;
    private String scanContent = "";
    private Button load;
    private String line="直線";
    private  int ss=0;
    private ConstraintLayout constraintLayout;
    private Button btn_start;
    //------------變數宣告---ㄦ----//
    //path point-----------------//
    int count1 = 0, count2 = 0; //起始點跟終點轉換成int
    String PATH;            //選擇路徑
    double distance2 = 0;   //計算終點成為count2 所用到函數
    double LOWD2 = 100000;  //基準值為了不被超過
    double Actualdistance ; //經緯度距離(m)
    LatLng PointArrayUP[];  //路徑所有的點
    private Marker start;   //起始點
    private Marker point;   //終點
    LatLng StartP, EndP;    //起始點 終點 經緯度
    int latlngIndex = 0;    //check point
    Polyline poly;          //畫線
    private LatLng[] routePP;//route陣列
    boolean route = true, ArrayC = false; //判斷陣列
    private static double[][] w1; //相鄰矩陣
    ArrayList<Integer> result = new ArrayList<>(); //負責最短路徑所有點的陣列值
    private double[][]dist; //矩陣長度 佛洛伊德用
    private int[][]path;    //矩陣長度 佛洛伊德用
    private static final  double INF = Double.POSITIVE_INFINITY;
    ArrayList<LatLng> Pts = new ArrayList<>(); //所有路徑的陣列值 未排列
    int pathN;              //json的路徑 每多一條則要增加1
    int preKey,nowKey;      //前一個key, 目前的key
    Vector<LatLng> v =new Vector<LatLng>(); //不懂
    HashMap<Integer, LatLng> map = new HashMap<Integer, LatLng>(); //所有路徑陣列且按照Hash方式排列
    HashMap<String, Double> mapdistance = new HashMap<String, Double>();  //不知道要幹嘛
    private static final LatLng M618 = new LatLng(24.98738088163, 121.54823161628);
    private static final LatLng M615 = new LatLng(24.98730118264, 121.5481621635);
    //path------------------------//
    String MStep ="2";
    private int step = 0;   //步數
    private double oriValue = 0;  //原始值
    private double lstValue = 0;  //上次的值
    private double curValue = 0;  //當前值
    private boolean motiveState = true;   //運動狀態
    private boolean markerYes = false;   //標記
    double xx , yy ;
    double angleALL = 0;
    int countD = 0;
    float Rotation[] = new float[9];
    float[] degree = new float[3];
    private float currentDegree = 0f;
    LatLng[] StepPath = new LatLng[2];
    double MemberStep = 0;
    double StepLong = 0.000007;
    double AllStep = 0;
    double foot = 75;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiODI4MzYyMjUiLCJhIjoiY2p1ZmlyODZ4MGR6bDQzbHA2encxaXhydCJ9.NW6EQXxNywZ14Vr9D9VoHA");
        setContentView(R.layout.activity_main);
        txt_dis = (TextView)findViewById(R.id.txt_distace);
        load=(Button)findViewById(R.id.load_btn);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("導航");
        btn_start=(Button)findViewById(R.id.btn_start);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
      //  ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", "地圖載入中...", true); //dialog show
      //  AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this); //build dialog
      //  View mView = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_spinner, null); // dialog 中 show spinner
        final Spinner mSpinner = (Spinner)findViewById(R.id.spinner); // 宣告 spinner
        //下面是 spinner 去接  spinner 內容的方法
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.map));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner 接 adapter 資料
        mSpinner.setAdapter(adapter);
        //spinner選擇，顯示，並讀選被選擇的position
        // 讀取被選擇的順序
        // 從[0]開始
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        style = "mapbox://styles/82836225/cjyaadi4u04b81ds8josl2vij";//mapbox style網址
                        PATH = "M6Path.geojson";//路徑檔案
                        pathN = 32;
                        clear();
                        loadPath();
                        break;
                    case 2:
                        style = "mapbox://styles/82836225/cjvw5bu3f0jzu1cpg82699h61";
                        PATH = "M3path.geojson";
                        pathN = 16;
                        clear();
                        loadPath();
                        break;
                }
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                     //   PATH = "map1.geojson";
                     //   pathN = 3;
                        mapboxMap.setStyle(new Style.Builder().fromUri(style),
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
                                .zoom(19)//載入時的大小
                                .bearing(170)//將地圖呈現時的傾斜度設置為平行
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);//必須到這把設定的值丟給animateCamera
                        mapboxMap.setMinZoomPreference(18);
                        mapboxMap.setMaxZoomPreference(20);
                        mapboxMap1.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng origin) {
                         //       IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                         //       Icon icon = iconFactory.fromResource(R.drawable.th);
                                if (point == null) {
                                    point = mapboxMap1.addMarker(new MarkerOptions().position(origin)//.icon(icon)
                                             .title("終點"));
                                } else {
                                    mapboxMap1.removeMarker(point);
                                    point = null;
                                    point = mapboxMap1.addMarker(new MarkerOptions().position(origin)//.icon(icon)
                                            .title("終點"));
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
                                markerYes = true;
                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar();
                startroute=true;
                navistart=true;
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
    //    IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
    //    Icon icon = iconFactory.fromResource(R.drawable.th);
        if (scanningResult != null) {
            scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            scan_content.setText(scanContent);
      //      scan_format.setText(scanFormat);
        } else {
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, intent);
        }
        switch (scanContent) {
            case "M618":
                start = mapboxMap1.addMarker(new MarkerOptions().position(M618).title("起點")//.icon(icon)
                );
                break;
            case "M617":
                start = mapboxMap1.addMarker(new MarkerOptions().position(M615).title("起點")//.icon(icon)
                );
                break;
        }
       //  setTimerTask();
}
  public void snackbar()
  {
      constraintLayout= findViewById(R.id.constraint);

      Snackbar snackbar = Snackbar.make(constraintLayout,"距離："+String.valueOf(Actualdistance)+"公尺"+" "+"目前："+line+"\n"+String.valueOf(foot)+"腳步",Snackbar.LENGTH_INDEFINITE)
              .setAction("結束", new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      Snackbar snackbar1 = Snackbar.make(constraintLayout,"結束導航",Snackbar.LENGTH_SHORT);
                      snackbar1.show();
                      startroute=false;
                      navistart=false;
                      clear();
                  }
              }).setActionTextColor(Color.RED);
      View snackView =snackbar.getView();
      snackView.setBackgroundColor(Color.WHITE);
      TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
      textView.setTextColor(Color.BLACK);
      textView.setTextSize(20);
      snackbar.show();
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
      //  this.scan_format=(TextView)findViewById(R.id.textView2);
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
        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_GAME);
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
                accY = event.values[1];
                accZ = event.values[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
            }
            if (mGravity != null && mGeomagnetic != null) {

                SensorManager.getRotationMatrix(Rotation, null, mGravity,
                        mGeomagnetic);
                SensorManager.getOrientation(Rotation, degree);

                degree[0] = (float) Math.toDegrees(degree[0]);

                // currentDegree-初始角度,-degree逆時針旋轉結束角度
                RotateAnimation ra = new RotateAnimation(currentDegree, -degree[0],
                        Animation.RELATIVE_TO_SELF, 0.5f, // x座標
                        Animation.RELATIVE_TO_SELF, 0.5f); // y座
                currentDegree = -degree[0];
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if(navistart==true) {
                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;
                        angle[0] += event.values[0] * dT;
                        angle[1] += event.values[1] * dT;
                        angle[2] += event.values[2] * dT;
                        angleStep[0] += event.values[0] * dT;
                        angleStep[1] += event.values[1] * dT;
                        angleStep[2] += event.values[2] * dT;
                    }
                    angley = (float) Math.toDegrees(angle[1]) % 360;
                    anglez = (float) Math.toDegrees(angle[2]) % 360;
                    AngleY = (float) Math.toDegrees(angleStep[1]) % 360;
                    AngleZ = (float) Math.toDegrees(angleStep[2]) % 360;
                    timestamp = event.timestamp;
                }
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
        if(navistart == true) {

            //步數判斷
            YZangle();
            if (point != null && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                double range = Double.valueOf(MStep);   //設定一個精準度範圍
                float[] value = event.values;
                curValue = magnitude(value[0], value[1], value[2]);   //計算當前的加速度向量
                //向上波加速的狀態
                if (motiveState) {
                    if (curValue >= lstValue) lstValue = curValue;
                    else {
                        //檢測到一次峰值
                        if (Math.abs(curValue - lstValue) > range) {
                            oriValue = curValue;
                            motiveState = false;
                        }
                    }
                }
                //向下波加速的狀態
                if (!motiveState) {
                    if (curValue <= lstValue) lstValue = curValue;
                    else {
                        if (Math.abs(curValue - lstValue) > range) {
                            //檢測到一次峰值
                            oriValue = curValue;
                            //start.remove();

                            //===========


                            if (angleALL != 0) { //加總陀螺儀的角度
                                if (angleyz > 15) {  //大於10才判定有旋轉 //陀螺儀算出來的角度
                                    angleALL = angleALL + angleyz;
                                    if (angleALL > 5000) {
                                        line = "轉彎";
                                    }
                                } else {
                                    line = "直線";
                                }
                                xx = (Math.cos(Math.toRadians(angleALL)));
                                yy = (Math.sin(Math.toRadians(angleALL)));
                                angleALL=0;
                            } else {
                                xx = (Math.cos(Math.toRadians(degree[0]))); //直走 這邊要修改不是等於degree[0] 應該要等於上一次的angeALL 因為每次的degree[0]會改變
                                yy = (Math.sin(Math.toRadians(degree[0])));
                                angleALL = degree[0];
                                Log.e("degree:", String.valueOf(degree));
                            }

                            //計算下一點定位位置
                            angleALL = degree[0]; //這個是因為要讓他重新算出使點，每走一部，就把他得到的compass初始化，不會讓yz一直累積（加上y,z之前的angleALL）
                            MemberStep++;
                            AllStep++;
                            double X = start.getPosition().getLatitude() + StepLong * xx; //約75公分
                            double Y = start.getPosition().getLongitude() + StepLong * yy;
                            StepPath[0] = start.getPosition();

                            if (markerYes) {
                                //    stepCount.setText(String.valueOf(AllStep));
                                //   stepLong.setText(String.valueOf(foot));
                                if (start!=null)
                                {
                                    start.remove();
                                    start = mapboxMap1.addMarker(new MarkerOptions().position(new LatLng(X, Y)).title("You"));
                                    Log.e("移動座標", String.valueOf(X)+"--"+String.valueOf(Y));
                                    ss++;
                                    StartP = new LatLng(start.getPosition().getLatitude(), start.getPosition().getLongitude());
                                    Log.e("ss:", String.valueOf(ss));
                                }

                            }

                            StepPath[1] = start.getPosition();
                            motiveState = true;
                            angleStep[1] = 0;
                            angleStep[2] = 0;
                            if (point != null) {
                                if (start.getPosition().distanceTo(point.getPosition()) < 3) {
                                    countD++;
                                    if (countD < 2) {
                                    }
                                    //FinishD();
                                }

                            }

                        }
                    }
                }
            }
            if(ss>10)
            {
                NaviDo();
                ss=0;
            }
        }
    }

    public void NaviDo() {
        //歸零避免累加
        angley = 0;
        anglez = 0;
        angle[1] = 0;
        angle[2] = 0;
    //    EndP = new LatLng(blocation);
        Actualdistance = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP.getLatitude(), EndP.getLongitude());
        StepLong = (Actualdistance / MemberStep) * 0.00000900900901;//因為是公尺所以*0.00000900900901代表一度
        foot = (Actualdistance / MemberStep);
        MemberStep = 0;
        txt_dis.setText(String.valueOf(Actualdistance));
 //       StartP = new LatLng(EndP);
//        blocation = new LatLng(blocation.getLatitude(),blocation.getLongitude());
 //       start = mapboxMap1.addMarker(new MarkerOptions().position(new LatLng(blocation)));

    }

    //方位旋轉
    private void updateCameraBearing(MapboxMap mMap, float bearing){
        mapboxMap1 = mMap;
        if(startroute==false)
        {
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(24.987366, 121.548034))//管院
                    // .target(new LatLng(24.987419, 121.548190))//舍我
                    .bearing(bearing).zoom(18.6f).tilt(40).build();
            mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace),2000);
        }
        else {
            CameraPosition currentPlace1 = new CameraPosition.Builder()
                    .target(new LatLng(start.getPosition()))//管院
                    // .target(new LatLng(24.987419, 121.548190))//舍我
                    .bearing(bearing).zoom(20).tilt(60).build();
            mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace1), 2000);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
    //角度判別
    public void YZangle() {
        yCoeff = accY / 10;
        zCoeff = accZ / 10;
     //   Log.e("角", angley + "," + anglez);
        angleyz = (angley * yCoeff) + (anglez * zCoeff);
        Log.e("角度:", String.valueOf(angleyz));

    }
    //計算該緯度上的精度長度
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public double magnitude(float x, float y, float z) {
        double magnitude = 0;
        magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude;
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
    public  void clear() {
        if (poly != null) {
            mapboxMap1.removePolyline(poly);
        }
        if (ArrayC) {
            result.clear();
        }
        if (point != null) {
            mapboxMap1.removeMarker(point);
            point = null;
        }
        if (start != null) {
            mapboxMap1.removeMarker(start);
            start = null;
        }
    }
}

