package com.webstormtest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiyaofang
 * @date 2018-02-08
 */

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
