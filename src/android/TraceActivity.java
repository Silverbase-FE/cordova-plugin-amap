package com.yxt.cordova;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.plugin.R;

// import com.silverbase.salesmanage.R;
// import com.silverbase.salesmanage.MainActivity;

public class TraceActivity extends Activity implements LocationSource,
        AMapLocationListener {

    private AMap                      aMap;
    private MapView                   mMapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient        mlocationClient;
    private AMapLocationClientOption  mLocationOption;

    private String coordinates = null;
    private String tips = null;
    private String title = null;

    private Marker marker2;// 有跳动效果的marker对象
    private LatLng latlng = new LatLng(36.061, 103.834);
    private TextView mTvTitilName;
    private TextView mTvTitilBack;
    private String backColor;
    private RelativeLayout mRlTitil;

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }

        ActionBar actionBar = getActionBar();
        actionBar.hide();
//        getActionBar().setDisplayOptions(
//                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE,
//                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
//
//        TextView tv = new TextView(this);
//        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        tv.setGravity(Gravity.CENTER);
//
//        actionBar.setCustomView(tv,
//                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
//        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        setResult(RESULT_OK);
        this.finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace_activity);
        mTvTitilName = (TextView) findViewById(R.id.title_name);
        mTvTitilBack = (TextView) findViewById(R.id.title_back);
        mRlTitil = (RelativeLayout) findViewById(R.id.title_rl);

        Bundle bundle = this.getIntent().getExtras();
        coordinates = bundle.getString("coordinates");
        tips = bundle.getString("tips");
        title = bundle.getString("title");
        backColor = bundle.getString("backColor");
        if (title != null ) {
            setTitle(title);
            mTvTitilName.setText(title);
        }
        backColor = backColor==null?"#000000":backColor;
        mRlTitil.setBackgroundColor(Color.parseColor(backColor));

        if (title != null ) {
            setTitle(title);
        }
        mTvTitilBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mMapView.onCreate(savedInstanceState);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.layout.menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        //缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));// 设置小蓝点的图标

//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
//         myLocationStyle.anchor(int,int)//设置小蓝点的锚点
//        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        addPolylineToMap();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Intent myIntent = new Intent();
//            myIntent = new Intent(TraceActivity.this, MainActivity.class);
//            startActivity(myIntent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 在地图上添加折现路径
     */
    private void addPolylineToMap() {

        String[] coArr   = coordinates.split(";");
        PolylineOptions pline = new PolylineOptions();

        if (coArr.length >= 1) {
            for(int i =0;i< coArr.length;i++) {
                String[] t = coArr[i].split(",");
                if (t[0].equals("")) {
                    continue;
                }
                pline.add(new LatLng(Double.valueOf(t[0]),Double.valueOf(t[1])));
            }
        }
        pline.width(5).color(Color.RED).setDottedLine(true);
        aMap.addPolyline(pline);
    }
}
