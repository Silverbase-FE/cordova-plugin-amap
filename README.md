## cordova Amap plugin for iOS and Android

This plugin is a thin wrapper for [Amap Maps Android API](https://lbs.amap.com/api/android-sdk/summary/) and [Amap Maps SDK for iOS](https://lbs.amap.com/api/ios-sdk/summary/).
Both [PhoneGap](http://phonegap.com/) and [Apache Cordova](http://cordova.apache.org/) are supported.

-----

### Quick install

*npm (current stable 2.0.1)*
```bash
$> cordova plugin add cordova-plugin-amap --variable API_KEY_FOR_ANDROID="YOUR_ANDROID_API_KEY_IS_HERE" --variable API_KEY_FOR_IOS="YOUR_IOS_API_KEY_IS_HERE"
```

*Github (current master, potentially unstable)*
```bash
$> cordova plugin add https://github.com/Silverbase-FE/cordova-plugin-amap --variable API_KEY_FOR_ANDROID="YOUR_ANDROID_API_KEY_IS_HERE" --variable API_KEY_FOR_IOS="YOUR_IOS_API_KEY_IS_HERE"
```

If you re-install the plugin, please always remove the plugin first, then remove the SDK

```bash
$> cordova plugin rm cordova-plugin-amap
$> cordova plugin add cordova-plugin-amap --variable API_KEY_FOR_ANDROID="YOUR_ANDROID_API_KEY_IS_HERE" --variable API_KEY_FOR_IOS="YOUR_IOS_API_KEY_IS_HERE"
```

The SDK-Plugin won't be uninstalled automatically and you will stuck on an old version.

### [API Reference](https://github.com/Silverbase-FE/cordova-plugin-amap/blob/master/www/amap.js)
* getCurrentPosition

<pre>
// Get current address for once
// 获取当前地址

if (typeof AMapPlugin != 'undefined') {
    AMapPlugin.getCurrentPosition(function (data) {
      // success
      console.log(data);
    }, function (err) {
      // fail
      console.log(err);
    })
}
</pre>

* startUpdatePosition
* readUpdatePosition

<pre>
// Turn on continuous positioning
// 开启持续定位

// Read continuous positioning data
// 读取持续定位数据
if (typeof AMapPlugin != 'undefined') {
  console.log(AMapPlugin.startUpdatePosition);
  AMapPlugin.startUpdatePosition(function (data) {
    if (data == 200) {
    	AMapPlugin.readUpdatePosition(function(data) {
    		// success
	     console.log(data);
	   }, function(err) {
	   		// fail
	     alert("err" + JSON.stringify(err));
	   });
    } else {
    	// fail
    	console.log(data);
    }
  }, function(err) {
    alert("err" + JSON.stringify(err));
  });  
}
</pre>

* stopUpdatePosition

<pre>
// Stop update position
// 停止定位
</pre>

* showMap

<pre>
// Show Map
// 展示地图
</pre>

* hideMap

<pre>
// Hide Map
// 隐藏地图
</pre>

* traceMap

<pre>
// Trace Map
// 轨迹地图
</pre>

### Android
![](https://raw.githubusercontent.com/Silverbase-FE/testApp-plugin-amap/master/screenshots/Screenshot_2018-08-01-00-45-23-372_com.amap.plugi.png)

### iOS
![](https://upload-images.jianshu.io/upload_images/1876100-bf972cf0e66c9586.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

### 地图插件总结
* 做过很多项目都有使用地图的场景，原生app，混合app，H5页面，有基于gps信号定位的，基于ip定位的，基于基站定位的。比如：

`cordova-plugin-geolocation`
> Common sources of location information include Global Positioning System (GPS) and location inferred from network signals such as IP address, RFID, WiFi and Bluetooth MAC addresses, and GSM/CDMA cell IDs. There is no guarantee that the API returns the device's actual location.

在实际的使用过程中，误差在1km左右，并没有描述的这么精确。而且在一些gps有问题的手机上，这个是不会有返回的，比如小米4。

H5定位，根据ip，这个误差就不提了。

目前采用的cordova调用高德地图sdk的方式，采用GPS定位，基站定位，混合定位三种定位模式，精度还算比较理想，误差在10m左右。获取的经纬度信息后可选择调用，经纬度地理信息转换接口，就可以清楚的看到当前的位置信息。这是一种较好的高精度定位解决方案。

* 遇到的坑

1、Cordova Android 7.0.0后生成的Platform Android目录结构有变更。[官方说明文档](https://cordova.apache.org/announcements/2017/12/04/cordova-android-7.0.0.html)

>With this release, we have changed the default project structure for Android projects. People who currently use the CLI and treat everything in the platforms directory as a build artifact should not notice a difference.

2、Android 6.0及以上版本 动态权限问题

Android 6.0及以上版本调用原生功能，需要加入动态权限。示例代码如下图所示，加在生成的  [MainActivity.java](https://github.com/Silverbase-FE/testApp-plugin-amap/blob/master/platforms/android/app/src/main/java/com/amap/plugin/MainActivity.java) 文件中。

<pre>
public class GpsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSON_REQUESTCODE = 0;
    private static final int SETTING_REQUESTCODE = 1;
    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck) {
            checkPermissions(needPermissions);
        }
    }


    /**
     * 检查权限
     *
     * @param permissions
     * @since 2.5.0
     */
    private void checkPermissions(String... permissions) {
        //当前系统大于等于6.0就去动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needRequestPermissonList = findDeniedPermissions(permissions);
            if (null != needRequestPermissonList
                    && needRequestPermissonList.size() > 0) {
                ActivityCompat.requestPermissions(this,
                        needRequestPermissonList.toArray(
                                new String[needRequestPermissonList.size()]),
                        PERMISSON_REQUESTCODE);
            }
        }
    }


    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }


    /**
     * 检测是否所有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }


    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("温馨提示");
        builder.setMessage("现在去设置界面配置权限吗?");

        // 拒绝, 退出应用
        builder.setNegativeButton("拒绝",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton("好的",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }


    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SETTING_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_REQUESTCODE) {
            checkPermissions(needPermissions);
        }
    }

}
</pre>

3、关于高德地图key，SHA1

1、包名(PackageName)要唯一

2、每次打包需要是发布版安全码SHA1的签名包,不然会报错。[如何获取发布版安全码SHA1](http://lbs.amap.com/faq/top/hot-questions/249)

### demo code repository
[https://github.com/Silverbase-FE/testApp-plugin-amap 点击前往](https://github.com/Silverbase-FE/testApp-plugin-amap)

-----
开发调试插件，也是花费了大量的业余时间，如果对您有帮助，点个star吧！

由于能力有限，如有纰漏，欢迎多多交流！



