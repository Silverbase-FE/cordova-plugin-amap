//
//  CDVAMapLocation.h
//  Created by tomisacat on 16/1/8.
//
//

#import <Cordova/CDVPlugin.h>
#import <AMapFoundationKit/AMapFoundationKit.h>
#import <MAMapKit/MAMapKit.h>
#import <AMapLocationKit/AMapLocationKit.h>
#import "MAMutablePolylineRenderer.h"
#import "MAMutablePolyline.h"
#import "SBGMapHeaderView.h"

struct Yxtlocation {
    CLLocationDegrees latitude;
    CLLocationDegrees longitude;
    double speed;
    double accuracy;
    long timestamp;
};

@interface CDVAMap4Yxt : CDVPlugin <AMapLocationManagerDelegate, MAMapViewDelegate> {
	BOOL isStart;
    double lat;
    double lng;
}

@property (nonatomic, strong) AMapLocationManager *curLocationManager; //获取当前位置

@property (nonatomic, strong) AMapLocationManager *locationManager; //后台持续定位

@property (nonatomic, assign) CGFloat minSpeed;     //最小速度

@property (nonatomic, assign) CGFloat minFilter;    //最小范围

@property (nonatomic, assign) CGFloat minInteval;   //更新间隔

@property (nonatomic, assign) CGFloat distanceFilter;    //最小范围

@property (nonatomic, strong) MAMapView *_mapView; //地图view

@property (nonatomic, strong) NSMutableArray *annotations; //标注

@property (nonatomic, strong) MAMutablePolylineRenderer *render;

@property (nonatomic, strong) MAMutablePolyline *mutablePolyline;

@property (nonatomic, strong) SBGMapHeaderView *headerView;

-(void) getCurrentPosition:(CDVInvokedUrlCommand*)command;

-(void) startUpdatePosition:(CDVInvokedUrlCommand*)command;

-(void) readUpdatePosition:(CDVInvokedUrlCommand*)command;

-(void) stopUpdatePosition:(CDVInvokedUrlCommand*)command;

-(void) showMap:(CDVInvokedUrlCommand*)command;

-(void) hideMap:(CDVInvokedUrlCommand*)command;

-(void) traceMap:(CDVInvokedUrlCommand*)command;

// -(void)setAnnotations:(NSString*)coordinates andTips:(NSString*)tips;

@end
