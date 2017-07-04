var cordova = require('cordova');
var exec = require('cordova/exec');
module.exports = {
    //获取当前地址
    getCurrentPosition: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "getCurrentPosition", []);
    },
    //开启持续定位
    startUpdatePosition: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "startUpdatePosition", []);
    },
    //读取持续定位数据
    readUpdatePosition: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "readUpdatePosition", []);
    },
    //停止定位
    stopUpdatePosition: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "stopUpdatePosition", []);
    },
    //展示地图
    showMap: function (successCallback, errorCallback, coordinates, tips, title) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "showMap", [coordinates, tips, title]);
    },
    //关闭展示的地图
    hideMap: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "hideMap", []);
    },
    //轨迹地图
    traceMap: function (successCallback, errorCallback, coordinates, title) {
        cordova.exec(successCallback, errorCallback, "AMapPlugin", "traceMap", [coordinates, title]);
    }
};