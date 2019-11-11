package com.example.demo3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.banjo.ewilson.ardistance.common.helpers.DisplayRotationHelper;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
//import com.google.ar.core.example.java.augmentedimage.rendering.AugmentedImageRenderer;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mapbox.core.utils.TextUtils;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.banjo.ewilson.ardistance.common.helpers.CameraPermissionHelper;
import com.banjo.ewilson.ardistance.common.helpers.DisplayRotationHelper;
import com.banjo.ewilson.ardistance.common.helpers.FullScreenHelper;
import com.banjo.ewilson.ardistance.common.helpers.SnackbarHelper;
import com.banjo.ewilson.ardistance.common.helpers.TapHelper;
import com.banjo.ewilson.ardistance.common.rendering.BackgroundRenderer;
import com.banjo.ewilson.ardistance.common.rendering.ObjectRenderer;
import com.banjo.ewilson.ardistance.common.rendering.PlaneRenderer;
import com.banjo.ewilson.ardistance.common.rendering.PointCloudRenderer;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import android.hardware.SensorEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CAMERA;

import com.google.ar.core.Anchor;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GLSurfaceView.Renderer {
    private MapboxMap mapboxMap1;
    private MapView mapView;
    String style="mapbox://styles/82836225/cjyaadi4u04b81ds8josl2vi"; //預設樣式
    //sensor---------------------//
    private SensorManager sensorManager; //sensor manager
    private float[] mGravity = new float[3];//陀螺
    private float[] mGeomagnetic = new float[3];//三軸
    private float timestamp; //每秒 tamp
    private static final float NS2S = 1.0f / 1000000000.0f;//地球重力基準值
    float anglez = 0, angley = 0, AngleY = 0, AngleZ = 0; //Y,Z軸角度
    float accY, accZ; //Ｙ,Z加速度值
    float[] angle = new float[3]; //陀螺儀角度計算
    float[] angleStep = new float[3];
    double angleyz;//旋轉角度
    float yCoeff, zCoeff; // 加速度/10後的基準值
    private float azimuth = 0f; //這是方位，非常重要。
    private float currentAzimuth = 0f;//方位
    float Rotation[] = new float[9]; //加速度
    float[] degree = new float[3]; //xyz
    private float currentDegree = 0f;//角度
    LatLng[] StepPath = new LatLng[2];//
    //sensor---------------------//
    private int Requestcode = 100; //none means just for requestPermission
    //------------變數宣告--------//
    private Activity mainactivity;
    Timer timer = new Timer(true);
    private TextView txt_dis;
    private  LinearLayout linearLayout;
    private  View view;
    private  String startlocation ;
    private TextView txt_movedis;
    private  TextView stepcount;
    private  TextView stepdistance;
    private  ImageView imageView;
    private  Button btn_finsh;
    private Button scan_btn;
    private String qrcodedug="true";
    private  boolean startroute=false;
    private  String  startblocation = "";
    private  boolean navistart = false;
    private boolean shouldConfigureSession = false;
    private  boolean loadcheck = true;
    private  boolean clickcheck = false;
    private String scanContent = "";
    private Spinner mSpinner;
    private String bug;
    private String left;
    private String right;
    private  int ss=0; //走幾步判斷有沒有偏離路徑 以3步配合每秒檢測
    private ConstraintLayout constraintLayout;
    private Button btn_start;
    private  String armovecheck= "0";
    //------------變數宣告---ㄦ----//
    //path point-----------------//
    int count1 = 0, count2 = 0, countCheck = 0; //起始點跟終點轉換成int
    String PATH;            //選擇路徑
    double distance2 = 0;   //計算終點成為count2 所用到函數
    double LOWD2 = 100000;  //基準值為了不被超過
    double LOWD3 = 100000;
    double Actualdistance ; //經緯度距離(m)
    LatLng PointArrayUP[];  //路徑所有的點
    private Marker start;   //起始點
    private Marker point;   //終點
    private Pose pose1 ;
    private Pose pose2 ;
    LatLng StartP,StartP2, EndP,EndP2;    //起始點 終點 經緯度
    LatLng blocation;
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
    int routePcheck = -1;   //check 判斷路徑
    private final boolean useSingleImage = false;
    Vector<LatLng> v =new Vector<LatLng>(); //不懂
    HashMap<Integer, LatLng> map = new HashMap<Integer, LatLng>(); //所有路徑陣列且按照Hash方式排列
    HashMap<String, Double> mapdistance = new HashMap<String, Double>();  //不知道要幹嘛
    /////////// 管院
    private static final LatLng M618 = new LatLng(24.98738088163, 121.54823161628);
    private static final LatLng M615 = new LatLng(24.98730118264, 121.5481621635);
    private static final LatLng Femaletoilet = new LatLng(24.98723136526, 121.54774546606);
    private static final LatLng Maletoilet = new LatLng(24.98725468802, 121.54791221823);
    /////////// 舍我樓
    private static final LatLng SEntrance = new LatLng( 24.98969110435,121.54592964887);
  //  private static final LatLng Maletoilet = new LatLng(24.98725468802, 121.54791221823);

    //path------------------------//
    String MStep ="5";  //移動基準值
    private int step = 0;   //步數
    private double oriValue = 0;  //原始值
    private double lstValue = 0;  //上次的值
    private double curValue = 0;  //當前值
    private boolean motiveState = true;   //運動狀態
    private boolean markerYes = false;   //標記
    String check = "0"; //地圖檢查點
    double xx , yy ; //移動位置
    double angleALL = 0;
    int countD = 0;
    int MemberStep = 0; //紀錄目前幾步
    double StepLong = 0.000007; //移動距離角度值
    double AllStep = 0;//總步伐
    double trangle ;//轉彎判斷
    double angleAllabs=0;
    double angleZabs ;
    DecimalFormat foots = new DecimalFormat("0.00"); //單位換算
    double foot = 0;//還需要幾步到達目的地
    private final Map<Integer, Pair<AugmentedImage, Anchor>> augmentedImageMap = new HashMap<>();//ar 儲存圖片
    private GLSurfaceView surfaceView; //平面
    private boolean installRequested;  //狀態

    private Session session; //重要 ar核心 是ar運行開始的關鍵 主要是讓ar 有一個獨立會話
    private final com.banjo.ewilson.ardistance.common.helpers.SnackbarHelper messageSnackbarHelper = new com.banjo.ewilson.ardistance.common.helpers.SnackbarHelper(); //ARCore的套件
    private com.banjo.ewilson.ardistance.common.helpers.DisplayRotationHelper displayRotationHelper;
    private com.banjo.ewilson.ardistance.common.helpers.TapHelper tapHelper; //手指觸控螢幕
   // private final AugmentedImageRenderer augmentedImageRenderer = new AugmentedImageRenderer();


    private final com.banjo.ewilson.ardistance.common.rendering.BackgroundRenderer backgroundRenderer = new com.banjo.ewilson.ardistance.common.rendering.BackgroundRenderer();
    private final com.banjo.ewilson.ardistance.common.rendering.ObjectRenderer virtualObject = new com.banjo.ewilson.ardistance.common.rendering.ObjectRenderer();
    private final com.banjo.ewilson.ardistance.common.rendering.PlaneRenderer planeRenderer = new com.banjo.ewilson.ardistance.common.rendering.PlaneRenderer();
    private final com.banjo.ewilson.ardistance.common.rendering.PointCloudRenderer pointCloudRenderer = new com.banjo.ewilson.ardistance.common.rendering.PointCloudRenderer();


    // Temporary matrix allocated here to reduce number of allocations for each frame.
    //此處分配的臨時矩陣用於減少每幀的分配數量。
    private final float[] anchorMatrix = new float[16]; // ar matrix

    // Anchors created from taps used for object placing.

    private final ArrayList<Anchor> anchors = new ArrayList<>();// AR core point Array
    private Handler handler = new Handler();//防止因為時間線程而導致stop
    private static final LatLng CheckpointA = new LatLng(24.98731645596, 121.54824469205);
    private static final LatLng CheckpointB = new LatLng(24.98726978379, 121.54799250156);
    private static final LatLng CheckpointC = new LatLng(24.98724716065, 121.54787025872);
    private static final LatLng RCheckpointA = new LatLng(24.98970994546, 121.54632728635);
    private static final LatLng SCheckpointA = new LatLng(24.98992479329, 121.54635117529);
    private static final LatLng SCheckpointB = new LatLng(24.98993770991, 121.54658377259);
    int[] imgId={R.drawable.ic_maneuver_depart,R.drawable.ic_maneuver_depart_left,R.drawable.ic_maneuver_depart_right}; // img array

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiODI4MzYyMjUiLCJhIjoiY2p1ZmlyODZ4MGR6bDQzbHA2encxaXhydCJ9.NW6EQXxNywZ14Vr9D9VoHA");
        setContentView(R.layout.activity_main);
        txt_dis = (TextView)findViewById(R.id.txt_distace);
        txt_movedis= (TextView)findViewById(R.id.txt_line);;
        handler.removeCallbacks(updateTimer);
        handler.postDelayed(updateTimer, 1000);
        LayoutInflater inflater = getLayoutInflater();
        view=inflater.inflate(R.layout.fragment_indoormap,null);
        stepcount = (TextView) view.findViewById(R.id.textView3);
        stepdistance = (TextView) view.findViewById(R.id.textView4);
        linearLayout=(LinearLayout) findViewById(R.id.linearLayout);
        btn_finsh=(Button) view.findViewById(R.id.btn_finsh);
        imageView = (ImageView) view.findViewById(R.id.imageView4);
        String[] countries =getResources().getStringArray(R.array.search);
        surfaceView = findViewById(R.id.surfaceview);
        AlertDialog.Builder bdr = new AlertDialog.Builder(this);
        bdr.setMessage("請先選取地圖和抓取平面在進行導航")
                .setTitle("提醒")
                .setIcon(android.R.drawable.btn_star_big_on)
                .setPositiveButton("關閉訊息",null);
        bdr.setCancelable(true);
        bdr.show();
        //displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
        //tapHelper = new TapHelper(/*context=*/ this);
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries);
        btn_finsh=(Button)view.findViewById(R.id.btn_finsh);
        btn_start=(Button)findViewById(R.id.btn_start);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
      //  ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", "地圖載入中...", true); //dialog show
      //  AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this); //build dialog
      //  View mView = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_spinner, null); // dialog 中 show spinner
        mSpinner = (Spinner)findViewById(R.id.spinner); // 宣告 spinner
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
                        startblocation = "M";
                        clickcheck=false;
                        pathclear();
                        clear();
                        loadPath();
                        break;
                    case 2:
                        style = "mapbox://styles/82836225/cjvw5bu3f0jzu1cpg82699h61";
                        PATH = "M3path.geojson";
                        pathN = 16;
                        startblocation = "M";
                        clickcheck=false;
                        clear();
                        pathclear();
                        loadPath();
                        break;
                    case 3:
                        style = "mapbox://styles/82836225/cjvmr4mp94au51cn43p6fc0lb";
                        PATH = "S5R5path.geojson";
                        pathN = 21;
                        startblocation = "S";
                        clickcheck=false;
                        clear();
                        pathclear();
                        loadPath();
                        break;
                }
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                     //   PATH = "map1.geojson";
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
                                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                                Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);
                                if(loadcheck == true) {
                                    if (point == null) {
                                        point = mapboxMap1.addMarker(new MarkerOptions().position(origin).icon(icon)
                                                .title("終點"));
                                    } else {
                                        mapboxMap1.removeMarker(point);
                                        point = null;
                                        point = mapboxMap1.addMarker(new MarkerOptions().position(origin).icon(icon)
                                                .title("終點"));
                                    }
                                }
                                clickcheck=true;
                                EndP = new LatLng(origin);
                                return true;
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
              //  snackbar();
                startroute=true;
                navistart=true;
                loadcheck = false;
                linearLayout.addView(view);
                btn_start.setVisibility(View.INVISIBLE);
                scan_btn.setVisibility(View.INVISIBLE);
                mSpinner.setVisibility(View.INVISIBLE);
                stepdistance.setText("距離："+String.valueOf(Actualdistance)+"公尺");
                foot = Actualdistance/0.70;
                String footz = foots.format(foot);
                stepcount.setText("步數："+footz+"步");

            }
        });
        btn_finsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishbutton();
            }
        });
        tapHelper = new com.banjo.ewilson.ardistance.common.helpers.TapHelper(/*context=*/ this);
        displayRotationHelper = new com.banjo.ewilson.ardistance.common.helpers.DisplayRotationHelper(/*context=*/ this);
        // Set up tap listener.
        // Set up renderer.
        surfaceView.setOnTouchListener(tapHelper);
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setWillNotDraw(false);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        installRequested = false;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//請求權限～
        checkPermission();
    }
    private void checkPermission()
        {
         if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
         {
             ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION,CAMERA,BODY_SENSORS},200);
         }
        }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
       if(requestCode==200)
       {
           if(grantResults[0]==PackageManager.PERMISSION_GRANTED){}
           if(grantResults[1]==PackageManager.PERMISSION_GRANTED){}
           if(grantResults[2]==PackageManager.PERMISSION_GRANTED){}
       }
       else
       {
           Toast.makeText(this,"需要權限允許才能運作",Toast.LENGTH_SHORT).show();
       }
    }
    //QRCODE
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.navistart2);
        if (scanningResult != null) {
            if(scanningResult.getContents() != null) {
                scanContent = scanningResult.getContents();
                //  String scanFormat = scanningResult.getFormatName();
                qrcodedug="false";
                //startlocation = scanContent;
                switch (scanContent) {
                    case "M618":
                        start = mapboxMap1.addMarker(new MarkerOptions().position(M618).title("起點").icon(icon));
                        startlocation="M618";
                        break;
                    case "M617":
                        start = mapboxMap1.addMarker(new MarkerOptions().position(M615).title("起點").icon(icon));
                        break;
                    case "Maletoilet":
                        start = mapboxMap1.addMarker(new MarkerOptions().position(Maletoilet).title("起點").icon(icon));
                        break;
                    case "Femaletoilet":
                        start = mapboxMap1.addMarker(new MarkerOptions().position(Femaletoilet).title("起點").icon(icon));
                    case "SEntrance":
                        start = mapboxMap1.addMarker(new MarkerOptions().position(SEntrance).title("起點").icon(icon));
                        startlocation="SEntrance";
                        break;
                    case "SExit":
              //        start = mapboxMap1.addMarker(new MarkerOptions().position(SExit).title("起點").icon(icon));
                        startlocation="SExit";
                        break;
                }
                if(clickcheck==true) {
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
                    // starp2 記錄起點
                    StartP2 = StartP;

                    findCheapestPath(count1, count2, w1);

                    routePP = new LatLng[result.size()];
                    for (int i = 0; i < result.size(); i++) {
                        routePP[i] = map.get(result.get(i));
                    }
                    poly = mapboxMap1.addPolyline(new PolylineOptions()
                            .add(routePP)
                            .color(Color.RED)
                            .width(5));
                    Actualdistance = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP.getLatitude(), EndP.getLongitude());
                    txt_dis.setText(String.valueOf(Actualdistance));
                    markerYes = true;
                    btn_start.setVisibility(View.VISIBLE);
                }
            }
      //      scan_format.setText(scanFormat);
        } else {
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, intent);
        }

}
  public void snackbar()
  {
      constraintLayout= findViewById(R.id.constraint);

      Snackbar snackbar = Snackbar.make(constraintLayout,startlocation+"\n"+"前往",Snackbar.LENGTH_INDEFINITE)
              .setAction("結束", new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      Snackbar snackbar1 = Snackbar.make(constraintLayout,"結束導航",Snackbar.LENGTH_SHORT);
                      snackbar1.show();
                      startroute=false;
                      navistart=false;
                      markerYes = false;
                      loadcheck = true;
                      linearLayout.removeAllViews();
                     // timer.cancel();
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
   public void finishbutton()
   {
       startroute=false;
       navistart=false;
       markerYes = false;
       clickcheck=false;
       loadcheck = true;
       start.remove();
       point.remove();
       poly.remove();
       btn_start.setVisibility(View.INVISIBLE);
       scan_btn.setVisibility(View.VISIBLE);
       mSpinner.setVisibility(View.VISIBLE);
       Toast.makeText(MainActivity.this, "導航結束" , Toast.LENGTH_SHORT).show();
       linearLayout.removeAllViews();
       clear();
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
            Log.e("json:", "讀取成功");
            // Parse JSON
            JSONObject json = new JSONObject(sb.toString());

            JSONArray features = json.getJSONArray("features");
            for (int i = 0; i < pathN; i++) {
                JSONObject feature = features.getJSONObject(i);

                JSONObject geometry = feature.getJSONObject("geometry");
                if (geometry != null) {
                    Log.e("geometry", "讀取成功");
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
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!com.banjo.ewilson.ardistance.common.helpers.CameraPermissionHelper.hasCameraPermission(this)) {
                    com.banjo.ewilson.ardistance.common.helpers.CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);
                // 對應不同情況給予不同的訊息。
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                return;
            }
            shouldConfigureSession = true;
        }
        if (shouldConfigureSession) {
            configureSession();
            shouldConfigureSession = false;
        }
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();

        messageSnackbarHelper.showMessage(this, "Searching for surfaces...");
        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorManager.SENSOR_DELAY_GAME);
        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_GAME);
        sensorManager .registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        sensorManager.unregisterListener(this);
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
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
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.navistart2);
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
                            armovecheck = "1";
                            //檢測到一次峰值
                            oriValue = curValue;
                            //start.remove();

                            //===========


                            if (angleALL != 0) { //加總陀螺儀的角度
                                if (angleyz > 15 ) {  //大於10才判定有旋轉 //陀螺儀算出來的角度
                                    angleZabs = Math.abs(angleyz);
                                    angleAllabs = angleAllabs +angleZabs;
                                    angleALL = angleALL + angleyz;
                                    if (angleAllabs > 3000) {
                                    }
                                } else {
                                }
                                xx = (Math.cos(Math.toRadians(angleALL)));
                                yy = (Math.sin(Math.toRadians(angleALL)));
                                Log.e("angleall:", String.valueOf(angleALL));
                             //   angleALL=0;
                            } else {
                                xx = (Math.cos(Math.toRadians(degree[0]))); //直走 這邊要修改不是等於degree[0] 應該要等於上一次的angeALL 因為每次的degree[0]會改變
                                yy = (Math.sin(Math.toRadians(degree[0])));
                                angleALL = degree[0];
                                Log.e("degree:", String.valueOf(angleALL));
                            }

                            //計算下一點定位位置
                            angleALL = degree[0]; //這個是因為要讓他重新算出使點，每走一部，就把他得到的compass初始化，不會讓yz一直累積（加上y,z之前的angleALL）
                            MemberStep++; //記錄步伐 會重置
                            AllStep++;//總步伐
                            double X = start.getPosition().getLatitude() + StepLong * xx; //約75公分
                            double Y = start.getPosition().getLongitude() + StepLong * yy;
                            StepPath[0] = start.getPosition();
                            CheckMove();
                            if (markerYes) {
                                if (start!=null)
                                {
                                    start.remove();
                                    start = mapboxMap1.addMarker(new MarkerOptions().position(new LatLng(X, Y)).title("You").icon(icon));
                                    Log.e("移動座標", String.valueOf(X)+"--"+String.valueOf(Y));
                                    ss++;;
                                    StartP = new LatLng(start.getPosition().getLatitude(), start.getPosition().getLongitude());
                                    Log.e("ss:", String.valueOf(ss));
                                    Log.e("step:", String.valueOf(MemberStep));
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
                                    FinishD();
                                }

                            }

                        }
                    }
                }
            }
        }
    }
    public void FinishD() {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("抵達目的地 !")
                .setIcon(R.drawable.ic_finish)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                start.remove();
                                point.remove();
                                poly.remove();
                                markerYes = false;
                                loadcheck = true;
                                startroute=false;
                                navistart=false;
                                clickcheck=false;
                                btn_start.setVisibility(View.INVISIBLE);
                                scan_btn.setVisibility(View.VISIBLE);
                                mSpinner.setVisibility(View.VISIBLE);
                            }

                        }).show();
    }

    //方位旋轉
    private void updateCameraBearing(MapboxMap mMap, float bearing){
        mapboxMap1 = mMap;
        if(startroute==false)
        {
           if(startblocation=="M") {
               CameraPosition currentPlaceM = new CameraPosition.Builder()
                       .target(new LatLng(24.987366, 121.548034))//管院
                       .bearing(bearing).zoom(18.6f).tilt(40).build();
               mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlaceM), 2000);
           }
           else if(startblocation=="S")
            {
                CameraPosition currentPlaceS = new CameraPosition.Builder()
                         .target(new LatLng(24.989765,121.546310))//舍我
                        .bearing(bearing).zoom(18.6f).tilt(40).build();
                mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlaceS), 2000);
            }
        }
        else {
            if(startblocation=="M") {
                CameraPosition currentPlaceM2 = new CameraPosition.Builder()
                        .target(new LatLng(start.getPosition()))//管院
                        // .target(new LatLng(24.987419, 121.548190))//舍我
                        .bearing(bearing).zoom(20).tilt(60).build();
                mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlaceM2), 2000);
            }
            else if(startblocation=="S") {
                CameraPosition currentPlaceS2 = new CameraPosition.Builder()
                        .target(new LatLng(start.getPosition()))//舍我
                        .bearing(bearing).zoom(20).tilt(60).build();
                mapboxMap1.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlaceS2), 2000);
            }
        }
   //     Log.e("degree:", String.valueOf(bearing));
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
   //     Log.e("角度:", String.valueOf(angleyz));

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
    public void clear() {
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
    //使用timer持續搜尋
    private Runnable updateTimer = new Runnable() {
        public void run() {
        //    Log.e("Timer:", String.valueOf("TimerOK"));
            if(navistart == true) {
                CheckUserRoad();
                bug();
            }
            handler.postDelayed(this, 1000);
        }
    };

    public void CheckUserRoad() {
        double Dcheck = 0;
        int OtherP = 0;
        for (int i = 0; i < poly.getPoints().size(); i++) {
            Dcheck = start.getPosition().distanceTo(poly.getPoints().get(i));
            if (Dcheck <= LOWD3) {
                countCheck = i;
                LOWD3 = Dcheck;
            }
        }
        LOWD3 = 10000; //設10000只是故意設一個比較大的數字防止超過
        if (start.getPosition().distanceTo(poly.getPoints().get(countCheck)) > 5|| ss>3) {
            for (int i = 0; i < map.size(); i++) {
                distance2 = start.getPosition().distanceTo(map.get(i));
                if (distance2 <= LOWD2) {
                    OtherP = i;
                    LOWD2 = distance2;
                    ss=0;
                }
            //    Log.e("checkload:", String.valueOf(countCheck));
            }

            LOWD2 = 10000;
            if (ArrayC) {
                result.clear();
            }
       //     Log.e("checkload:", String.valueOf(OtherP));
            findCheapestPath(OtherP, count2, w1);
            routePP = new LatLng[result.size()];
            for (int i = 0; i < result.size(); i++) {
                routePP[i] = map.get(result.get(i));
            }
            if (routePcheck != OtherP) {
                mapboxMap1.removePolyline(poly);
                poly = mapboxMap1.addPolyline(new PolylineOptions()
                        .add(routePP)
                        .color(Color.RED)
                        .width(5));
                routePcheck = OtherP;
            }
            Actualdistance = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP.getLatitude(), EndP.getLongitude());
            stepdistance.setText("距離："+String.valueOf(Actualdistance)+"公尺");
        }
    }
    private void  pathclear()
    {
        try {
            InputStream inputStream = getAssets().open(PATH);
            inputStream.close();
            inputStream.reset();
        }
        catch (Exception exception){}
    }
    public void CheckMove() {
        if(startlocation=="M618") {
            Log.e("trangle:", check);
            if(check=="0") {
                EndP2= new LatLng(CheckpointA);
                trangle = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP2.getLatitude(), EndP2.getLongitude());
                Log.e("trangle:", String.valueOf(trangle));
                RightorLeft();
                check="1";
            }
        }
        if(startlocation=="SEntrance") {
            Log.e("trangle:", check);
            if(check=="0") {
                EndP2= new LatLng(RCheckpointA);
                trangle = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP2.getLatitude(), EndP2.getLongitude());
                Log.e("trangle:", String.valueOf(trangle));
                left="yes";
                RightorLeft();
                check="1";
            }
            if(check=="1")
            {
                EndP2= new LatLng(SCheckpointA);
                trangle = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP2.getLatitude(), EndP2.getLongitude());
                Log.e("trangle:", String.valueOf(trangle));
                right="yes";
                RightorLeft();
                check="2";
            }
            if(check=="2")
            {
                EndP2= new LatLng(SCheckpointB);
                trangle = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP2.getLatitude(), EndP2.getLongitude());
                Log.e("trangle:", String.valueOf(trangle));
                right="yes";
                RightorLeft();
            }
        }
    }
    public void bug()
    {
        if (bug == "1") {

            IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
            Icon icon = iconFactory.fromResource(R.drawable.navistart2);
            start.remove();
            start = mapboxMap1.addMarker(new MarkerOptions().position(new LatLng(blocation)).title("起點").icon(icon));
        }
        bug = "0" ;
    }
    public void RightorLeft()
    {
        if (trangle<=2&&right=="yes")
        {
            imageView.setImageResource(imgId[2]);
        }
        else{ imageView.setImageResource(imgId[0]);}
        if(trangle<=2&&left=="yes")
        {
            imageView.setImageResource(imgId[1]);
        }
        else{ imageView.setImageResource(imgId[0]);}
    }
    @Override //畫面改變？或是移動中改變。
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        com.banjo.ewilson.ardistance.common.helpers.FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }


    //創造出平面
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ this);
            planeRenderer.createOnGlThread(/*context=*/ this, "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(/*context=*/ this);

            virtualObject.createOnGlThread(/*context=*/ this, "models/waypoint.obj", "models/waypoint_color2.png");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);


        } catch (IOException e) {
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        // 通知ARCORE任務，當視野改變的時候，透視矩陣和畫面背景能夠被適合的調整校正。
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            // 從ARSESSION去獲得當前的幀數，當組態被設定到UPDATEMODE.BLOCKING時，會有類似匯流排功能去調整相機畫面
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.
            if(qrcodedug=="false"&& navistart==true)
            {
                anchors.add(session.createAnchor(
                        frame.getCamera().getPose()
                                .compose(Pose.makeTranslation(0, 0, -1f))
                                .extractTranslation()));
                pose1 = anchors.get(0).getPose();
                qrcodedug="true";
            }
            MotionEvent tap = tapHelper.poll();
            if (armovecheck== "1" && camera.getTrackingState() == TrackingState.TRACKING) {
                armovecheck = "0";
              /*  for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    // Creates an anchor if a plane or an oriented point was hit.
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof com.google.ar.core.Point
                            && ((Point) trackable).getOrientationMode()
                            == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        }*/
                        anchors.add(session.createAnchor(
                                frame.getCamera().getPose()
                                        .compose(Pose.makeTranslation(0, 0, -1f))
                                        .extractTranslation()));

                        pose1 = anchors.get(0).getPose();
                        pose2 = anchors.get(1).getPose();
                        Log.e("ARtestpose1:", String.valueOf(pose1));
                        Log.e("ARtestpose2:", String.valueOf(pose2));
                        double distanceMeters = getDistance(pose1, pose2);
                        if(distanceMeters>0.9)
                        {
                            distanceMeters=0.7;
                        }
                        Log.e("ARdistance:", String.valueOf(distanceMeters));
                        double distanceFeet = (double) (distanceMeters / 0.3048);
                        StepLong = distanceMeters * 0.00000900900901 ;
                        foot = (Actualdistance/ distanceMeters);
                        stepdistance.setText("距離："+String.valueOf(Actualdistance)+"公尺");
                        String footz = foots.format(foot);
                        stepcount.setText("步數："+footz+"步");

                double distanceInches = (double) (distanceFeet * 12);
                        double ygedis = (distanceFeet * 30.5 + distanceInches * 2.5)/2 ; // 30.5 and 2.5 are 「cm」
                        String result = "";
                        if(distanceFeet < 1) {
                            //result = format.format(distanceInches).toString() + ": in";
                            result = String.valueOf((int)ygedis) + "cm" ;
                            printResult(result);
                        }
                        if(distanceFeet >= 1){
                            distanceInches = distanceInches % 12;
                            // result = String.valueOf((int)distanceFeet) + " ft : " + format.format(distanceInches).toString() + " in";
                            result = String.valueOf((int)ygedis);
                            printResult(result);
                            Log.e("AR:", String.valueOf(distanceMeters));
                        }
                if (anchors.size() == 2) {
                    anchors.remove(0);
                }
                     //   break;
                  //  }
                //}

            }
            //Log.v("PLLLLLL",updatedAugmentedImages.toString());

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }


            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);
            drawAugmentedImages(frame, projmtx, viewmtx, colorCorrectionRgba);

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();

            // Check if we detected at least one plane. If so, hide the loading message.
            if (messageSnackbarHelper.isShowing()) {
                for (Plane plane : session.getAllTrackables(Plane.class)) {
                    if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {
                        messageSnackbarHelper.hide(this);
                        break;
                    }
                }
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
        }
    }

    //紀錄觸控點1,2的x,y,z座標，藉由計算出座標差，在轉換成現實中的距離。
    private double getDistance(Pose pose0, Pose pose1){
        float distanceX = pose0.tx() - pose1.tx();
        float distanceY = pose0.ty() - pose1.ty();
        float distanceZ = pose0.tz() - pose1.tz();
        return Math.sqrt(distanceX * distanceX + distanceZ * distanceZ + distanceY * distanceY);
    }

    //輸出結果
    private void printResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_movedis.setText(result);
            }
        });
    }
    private void drawAugmentedImages(
            Frame frame, float[] projmtx, float[] viewmtx, float[] colorCorrectionRgba) {
        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        // Iterate to update augmentedImageMap, remove elements we cannot draw.
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED://靜止圖片 但人在動
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    if(augmentedImage.getIndex() == 1) {
                        Log.e("pausedR:", "pausedRok");
                        bug = "1" ;
                        check="1";
                        blocation = new LatLng(24.98970994546, 121.54632728635);
                        NaviDo();
                        break;
                    }
                    if(augmentedImage.getIndex() == 0){
                        Log.e("pausedS:", "pausedSok");
                        bug = "1" ;
                        check="1";
                        blocation = new LatLng(24.98992479329, 121.54635117529);
                        NaviDo();
                        break;
                    }
                    String text = String.format("Detected Image %d", augmentedImage.getIndex());
                    messageSnackbarHelper.showMessage(this, text);
                    break;
                case TRACKING://追蹤
                    // Have to switch to UI Thread to update View.
                    // Create a new anchor for newly found images.
                    break;
                case STOPPED://停止
                    augmentedImageMap.remove(augmentedImage.getIndex());
                    break;
            }
        }
    }
    public void NaviDo() {
        //歸零避免累加
        angley = 0;
        anglez = 0;
        angle[1] = 0;
        angle[2] = 0;
        MemberStep = 0;
        EndP2 = new LatLng(blocation);
        StartP = new LatLng(EndP2);
        Actualdistance = GetDistance(StartP.getLatitude(), StartP.getLongitude(), EndP.getLatitude(), EndP.getLongitude());
        stepdistance.setText("距離："+String.valueOf(Actualdistance)+"公尺");

    }
    private void configureSession() {
        Config config = new Config(session);
       // config.setFocusMode(Config.FocusMode.AUTO);
        if (!setupAugmentedImageDatabase(config)) {
            messageSnackbarHelper.showError(this, "Could not setup augmented image database");
        }
        session.configure(config);
    }
    
    private boolean setupAugmentedImageDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        // There are two ways to configure an AugmentedImageDatabase:
        // 1. Add Bitmap to DB directly
        // 2. Load a pre-built AugmentedImageDatabase
        // Option 2) has
        // * shorter setup time
        // * doesn't require images to be packaged in apk.
        if (useSingleImage) {
            Bitmap augmentedImageBitmap = loadAugmentedImageBitmap();
            if (augmentedImageBitmap == null) {
                return false;
            }
            augmentedImageDatabase = new AugmentedImageDatabase(session);
            augmentedImageDatabase.addImage("70", augmentedImageBitmap);
           // augmentedImageDatabase.addImage("default", augmentedImageBitmap);
            // If the physical size of the image is known, you can instead use:
            // augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
            // This will improve the initial detection speed. ARCore will still actively estimate the
            // physical size of the image as it is viewed from multiple viewpoints.
        } else {
            // This is an alternative way to initialize an AugmentedImageDatabase instance,
            // load a pre-existing augmented image database.
            try (InputStream is = getAssets().open("img.imgdb")) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e("database", "IO exception loading augmented image database.", e);
                return false;
            }
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }
    private Bitmap loadAugmentedImageBitmap() {
        try (
                InputStream is = getAssets().open("trun_right.jpg")) {
                return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
                Log.e("ImageBit", "IO exception loading augmented image bitmap.", e);
        }
                return null;
    }
}


