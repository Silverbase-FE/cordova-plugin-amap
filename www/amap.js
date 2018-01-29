/*global cordova, module*/
var cordova = require('cordova');
var exec = require('cordova/exec');
var AMapPlugin = function(){};

//获取当前地址
AMapPlugin.getCurrentPosition = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AMapPlugin", "getCurrentPosition", []);
};

//开启持续定位
AMapPlugin.startUpdatePosition = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AMapPlugin", "startUpdatePosition", []);
};

//读取持续定位数据
AMapPlugin.readUpdatePosition = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AMapPlugin", "readUpdatePosition", []);
};

//停止定位
AMapPlugin.stopUpdatePosition = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AMapPlugin", "stopUpdatePosition", []);
};

//展示地图
AMapPlugin.showMap = function (successCallback, errorCallback, coordinates, tips, title) {
    exec(successCallback, errorCallback, "AMapPlugin", "showMap", [coordinates, tips, title]);
};

//关闭展示的地图
AMapPlugin.hideMap = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "AMapPlugin", "hideMap", []);
};

//轨迹地图
AMapPlugin.traceMap = function (successCallback, errorCallback, coordinates, title) {
    exec(successCallback, errorCallback, "AMapPlugin", "traceMap", [coordinates, title]);
};


module.exports = AMapPlugin;