package com.yxt.cordova;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.MapView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AMapPlugin extends CordovaPlugin implements AMapLocationListener {

    private static final String GET_LOCATION_ACTION   = "getCurrentPosition";
    private static final String START_LOCATION_ACTION = "startUpdatePosition";
    private static final String READ_LOCATION_ACTION  = "readUpdatePosition";
    private static final String STOP_LOCATION_ACTION  = "stopUpdatePosition";

    private static final String SHOW_MAP_ACTION       = "showMap";
    private static final String HIDE_MAP_ACTION       = "hideMap";
    private static final String TRACE_MAP_ACTION      = "traceMap";

    private static final String TAG = AMapPlugin.class.getSimpleName();

    private AMapLocationClient curLocationClient = null;
    private AMapLocationClientOption curLocationOption = null;
    private CallbackContext curCallbackContext;//当前位置返回

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private CallbackContext mainCallbackContext;
    private CallbackContext cCtx;

    //上一次有效的经纬度
    private double lastLat = 0;
    private double lastLng = 0;

    private int minSpeed   = 2; //最小速度
    private float minFilter= 50; //最小过滤距离
    private int minInteval = 10; //最小间隔时间
    private float distanceFilter = 0; //最小过滤距离

    private static final double EARTH_RADIUS = 6378.137;

    protected ViewGroup root; // original Cordova layout
    protected RelativeLayout main; // new layout to support map
    protected MapView mapView;

    private static Context mContext;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mContext = cordova.getActivity().getApplicationContext();
        main     = new RelativeLayout(cordova.getActivity().getApplicationContext());
        distanceFilter = minFilter;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        cCtx = callbackContext;
        Log.d(TAG, " action code:" + action);
        Log.d(TAG, " action result:" + TRACE_MAP_ACTION.equals(action));

        if (SHOW_MAP_ACTION.equals(action)) {
            String coordinates = (String)args.get(0);
            String tips        = (String)args.get(1);
            String title       = (String)args.get(2);

            try {
                //下面两句最关键，利用intent启动新的Activity
                Intent intent = new Intent(cordova.getActivity(), MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("coordinates", coordinates);
                bundle.putString("tips", tips);
                bundle.putString("title", title);
                intent.putExtras(bundle);
                this.cordova.startActivityForResult(this, intent, 200);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else if(HIDE_MAP_ACTION.equals(action)) {
            if (mapView != null) {
                mapView.onDestroy();
            }
            cCtx.success(200);
            return true;
        }else if(TRACE_MAP_ACTION.equals(action)) {
            String coordinates = (String)args.get(0);
            String title       = (String)args.get(1);
            try {
                //下面两句最关键，利用intent启动新的Activity
                Intent intent = new Intent(cordova.getActivity(), TraceActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("coordinates", coordinates);
                bundle.putString("title", title);
                intent.putExtras(bundle);
                this.cordova.startActivityForResult(this, intent, 200);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else if (GET_LOCATION_ACTION.equals(action)) {
            this.curCallbackContext = callbackContext;
            //单次定位
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            curCallbackContext.sendPluginResult(pluginResult);
            if (curLocationClient == null) {
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (curLocationOption == null) {
                            curLocationOption = new AMapLocationClientOption();
                            curLocationClient = new AMapLocationClient(mContext);
                        }

                        curLocationOption.setOnceLocation(true); //是否单次定位
                        curLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//                        curLocationOption.setInterval(minInteval * 1000); //设置发起定位请求的时间间隔
//                        curLocationOption.setGpsFirst(false); //优先返回GPS定位
//                        curLocationOption.setNeedAddress(false); // 可选，设置是否返回逆地理地址信息。默认是true
//                        curLocationOption.setHttpTimeOut(11 * 1000);//设置联网超时时间 30s
//                        curLocationOption.setWifiScan(true); // 可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
//                        curLocationOption.setMockEnable(false);
//                        curLocationOption.setSensorEnable(false); // 可选，设置是否使用传感器。默认是false
//                        curLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true

                        curLocationClient.setLocationOption(curLocationOption);
                        curLocationClient.setLocationListener(AMapPlugin.this);
                        curLocationClient.startLocation();
                    }
                });
            }
            return true;
        }else if(READ_LOCATION_ACTION.equals(action)) {
            setCallbackContext(callbackContext);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            mainCallbackContext.sendPluginResult(pluginResult);

            if (locationClient == null) {
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (locationOption == null) {
                            locationOption = new AMapLocationClientOption();
                        }
                        locationOption.setInterval(minInteval * 1000);
                        locationOption.setNeedAddress(false);
                        locationOption.setOnceLocation(false);
                        locationOption.setGpsFirst(false);
                        locationOption.setHttpTimeOut(11 * 1000); //网络定位11S 超时
                        locationOption.setWifiActiveScan(true);
                        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                        locationOption.setMockEnable(false);

                        locationClient = new AMapLocationClient(mContext);
                        locationClient.setLocationOption(locationOption);
                        locationClient.setLocationListener(AMapPlugin.this);
                        locationClient.startLocation();
                    }
                });
            }
            return true;
        }else if(START_LOCATION_ACTION.equals(action)) {
            setCallbackContext(callbackContext);
            PluginResult pluginResult = null;
            if (isOPen(mContext)) {
                pluginResult = new PluginResult(PluginResult.Status.OK, 200);
            } else {
                pluginResult = new PluginResult(PluginResult.Status.ERROR, "请开启手机的GPS定位功能");
            }
            //判断GPS定位是否开启
            pluginResult.setKeepCallback(false);
            mainCallbackContext.sendPluginResult(pluginResult);
            return true;
        }else if(STOP_LOCATION_ACTION.equals(action)) {
            setCallbackContext(callbackContext);
            if (locationClient != null) {
                if(locationClient.isStarted()){
                    locationClient.stopLocation();
                }
                locationClient.onDestroy();
                locationClient = null;
            }
            // mainCallbackContext.success(200);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, 200);
            pluginResult.setKeepCallback(false);
            mainCallbackContext.sendPluginResult(pluginResult);
            return true;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, PluginResult.Status.INVALID_ACTION.toString());
        pluginResult.setKeepCallback(false);
        mainCallbackContext.sendPluginResult(pluginResult);
        return false;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stopLocation();
    }

    public CallbackContext getCallbackContext() {
        return mainCallbackContext;
    }

    public void setCallbackContext(CallbackContext callbackContext) {
        mainCallbackContext = callbackContext;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){

        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, " request code" + requestCode);
        Log.d(TAG, " result code" + resultCode);

        if(resultCode==Activity.RESULT_OK){
            cCtx.success("success");
        } else {
            cCtx.success("fail");
        }
    }

    // 定位监听
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        try {
            if (null != amapLocation) {
                JSONObject resultObject = new JSONObject();
                //返回的状态码
                int statusCode = amapLocation.getErrorCode();
                if(statusCode==0){
                    resultObject.put("latitude", amapLocation.getLatitude());
                    resultObject.put("longitude", amapLocation.getLongitude());
                    resultObject.put("speed", amapLocation.getSpeed());       //当GPS定位时有效
                    resultObject.put("provider", amapLocation.getProvider()); //定位提供者: lbs:高德网络定位 gps: gps定位
                    resultObject.put("accuracy",amapLocation.getAccuracy());  //精度

                    Log.d(TAG, amapLocation.toStr(3));
                    Log.d(TAG, "speed:" + amapLocation.getSpeed());

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObject);
                    if (curLocationOption!=null && curLocationOption.isOnceLocation()) {
                        pluginResult.setKeepCallback(false);
                        curLocationOption = null;
                        curLocationClient = null;
                        curCallbackContext.sendPluginResult(pluginResult);
                    } else {
                        pluginResult.setKeepCallback(true);
                        //精度大于200的坐标抛弃
                        if ((double)amapLocation.getAccuracy() > 200) {
                            resultObject = null;
                            pluginResult = null;

                            resultObject = new JSONObject();
                            resultObject.put("errorCode", 13);
                            resultObject.put("errorInfo", "The Accuracy is more than 200");
                            pluginResult = new PluginResult(PluginResult.Status.NO_RESULT, resultObject);
                            pluginResult.setKeepCallback(true);
                            mainCallbackContext.sendPluginResult(pluginResult);
                            return;
                        }

                        //2次连续坐标点相同 抛弃
                        if (lastLat == amapLocation.getLatitude() && lastLng == amapLocation.getLongitude()) {
                            resultObject = null;
                            pluginResult = null;

                            resultObject = new JSONObject();
                            resultObject.put("errorCode", 13);
                            resultObject.put("errorInfo", "The Accuracy is equal the last location");
                            pluginResult = new PluginResult(PluginResult.Status.NO_RESULT, resultObject);
                            pluginResult.setKeepCallback(true);
                            mainCallbackContext.sendPluginResult(pluginResult);
                            return;
                        }
                        //根据速度设置过滤距离
                        adjustDistanceFilter(amapLocation);

                        if (lastLat > 0 && amapLocation.getSpeed()<=0) {
                            //计算与上一个有效点之间的距离
                            float[] results=new float[1];
                            Location.distanceBetween(lastLat,lastLng,amapLocation.getLatitude(), amapLocation.getLongitude(),results);
//                            Log.i(LOCATION_TAG, results[0]+"");
//                            Log.i(LOCATION_TAG, "lastlat: " + lastLat + " lastLng: " + lastLng);
//                            Log.i(LOCATION_TAG, "distanceFilter: " + distanceFilter);
//                            Log.i(LOCATION_TAG, "speed: " + amapLocation.getSpeed());
                            if (results[0] < 10) {
                                resultObject = null;
                                pluginResult = null;

                                resultObject = new JSONObject();
                                resultObject.put("errorCode", 13);
                                resultObject.put("errorInfo", "The distance is smaller than 100m");
                                pluginResult = new PluginResult(PluginResult.Status.NO_RESULT, resultObject);
                                pluginResult.setKeepCallback(true);
                                mainCallbackContext.sendPluginResult(pluginResult);
                                return;
                            }
                        }


                        lastLat = amapLocation.getLatitude();
                        lastLng = amapLocation.getLongitude();

                        mainCallbackContext.sendPluginResult(pluginResult);
                    }
                }else{
                    //错误编码
                    resultObject.put("errorCode",amapLocation.getErrorCode());
                    //错误信息
                    resultObject.put("errorInfo",amapLocation.getErrorInfo());

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, resultObject);
                    if (!locationOption.isOnceLocation()) {
                        pluginResult.setKeepCallback(true);
                    } else {
                        pluginResult.setKeepCallback(false);
                    }
                    mainCallbackContext.sendPluginResult(pluginResult);
                }
            }else{
                JSONObject resultObject = new JSONObject();
                //错误编码
                resultObject.put("errorCode",amapLocation.getErrorCode());
                //错误信息
                resultObject.put("errorInfo",amapLocation.getErrorInfo());
                mainCallbackContext.error(resultObject);
            }
        }catch (Exception ex){
            mainCallbackContext.error(ex.getMessage());
            String errMsg = ex.getMessage();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, errMsg);
            pluginResult.setKeepCallback(false);
            mainCallbackContext.sendPluginResult(pluginResult);
            stopLocation();
        }
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        if (locationClient != null) {
            if(locationClient.isStarted()){
                locationClient.stopLocation();
            }
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    //根据速度设置过滤距离
    private void adjustDistanceFilter(AMapLocation amapLocation) {
        float speed = amapLocation.getSpeed();

        if (speed < minSpeed) {
            if (Math.abs(distanceFilter - minFilter) > 0.1f) {
                distanceFilter = minFilter;
            }
        } else {
            float lastSpeed = distanceFilter / minInteval;
            if ( (Math.abs(lastSpeed - speed)/lastSpeed > 0.1f) || (lastSpeed <= 0)) {
                float newSpeed = (speed + 0.5f);
                distanceFilter = newSpeed * minInteval;
            }
        }
    }
}
