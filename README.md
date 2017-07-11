## cordova Amap plugin for iOS and Android

This plugin is a thin wrapper for [Amap Maps Android API](https://lbs.amap.com/api/android-sdk/summary/) and [Amap Maps SDK for iOS](https://lbs.amap.com/api/ios-sdk/summary/).
Both [PhoneGap](http://phonegap.com/) and [Apache Cordova](http://cordova.apache.org/) are supported.

-----

### Quick install

*npm (current stable 1.0.3)*
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
$> cordova plugin rm com.amap.ios
$> cordova plugin add cordova-plugin-amap --variable API_KEY_FOR_ANDROID="YOUR_ANDROID_API_KEY_IS_HERE" --variable API_KEY_FOR_IOS="YOUR_IOS_API_KEY_IS_HERE"
```

The SDK-Plugin won't be uninstalled automatically and you will stuck on an old version.

-----

