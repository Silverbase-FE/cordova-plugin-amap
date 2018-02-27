
#import "CDVAMap4Yxt.h"
#import "MAMutablePolyline.h"
#import "MAMutablePolylineRenderer.h"

static NSString* const USER_DEFAULT_KEY = @"locations";
static NSString* const SPEED_KEY = @"speed";
static NSString* const ACCURACY_KEY = @"accuracy";
static NSString* const LATITUDE_KEY = @"latitude";
static NSString* const LONGITUDE_KEY = @"longitude";
static NSString* const CREATED_AT_KEY = @"timestamp";
static NSString* const IN_BACKGROUND_KEY = @"inBackground";
static NSString* const MAX_LENGTH_KEY = @"maxLength";
static NSString* const INTERVAL_KEY = @"interval";

static int const MAX_LENGTH = 10;

@implementation CDVAMap4Yxt
{
    NSMutableArray * _tracking;
    CFTimeInterval _duration;
}

//readValueFrom mainBundle
-(NSString *)getAMapApiKey{
    return [[[NSBundle mainBundle] infoDictionary] objectForKey:@"AMapApiKey"];
}

//init map Config
-(void) initMapConfig{
    [AMapServices sharedServices].apiKey = [self getAMapApiKey];
}

-(void) initLocationConfig{
    [AMapLocationServices sharedServices].apiKey = [self getAMapApiKey];
}

- (MAAnnotationView *)mapView:(MAMapView *)mapView viewForAnnotation:(id<MAAnnotation>)annotation
{
    if ([annotation isKindOfClass:[MAPointAnnotation class]])
    {
        static NSString *pointReuseIndetifier = @"pointReuseIndetifier";
        MAPinAnnotationView *annotationView = (MAPinAnnotationView*)[mapView dequeueReusableAnnotationViewWithIdentifier:pointReuseIndetifier];
        if (annotationView == nil)
        {
            annotationView = [[MAPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:pointReuseIndetifier];
        }
        
        annotationView.canShowCallout               = YES;
        annotationView.animatesDrop                 = YES;
        annotationView.draggable                    = YES;
        annotationView.rightCalloutAccessoryView    = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
        annotationView.pinColor                     = [self.annotations indexOfObject:annotation] % 3;
        
        return annotationView;
    }
    
    return nil;
}

- (MAOverlayPathRenderer *)mapView:(MAMapView *)mapView rendererForOverlay:(id<MAOverlay>)overlay
{
    
    if ([overlay isKindOfClass:[MAMutablePolyline class]])
    {
        MAMutablePolylineRenderer *renderer = [[MAMutablePolylineRenderer alloc] initWithOverlay:overlay];
        renderer.lineWidth = 4.0f;
        
        renderer.strokeColor = [UIColor redColor];
        self.render = renderer;
        
        return renderer;
    }
    
    return nil;
}

//关闭地图
-(void) hideMap:(CDVInvokedUrlCommand*)command{
    if (self._mapView != nil) {
        [self._mapView removeFromSuperview];
        NSLog(@"remove From Superview");
    }
    if (self.headerView != nil) {
        [self.headerView removeFromSuperview];
    }
    NSString* okStr = @"ok";
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:okStr];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

//展示地图
-(void) showMap:(CDVInvokedUrlCommand*)command{
    [self initMapConfig];
    [self initMapView];
    self.headerView.title = command.arguments[2];
    NSString* coordinates = [command.arguments objectAtIndex:0];
    NSString* tips        = [command.arguments objectAtIndex:1];
    
    if (coordinates.length && tips.length) {
        [self setAnnotations:coordinates andTips:tips];
    }
}

//我的轨迹
-(void) traceMap:(CDVInvokedUrlCommand*)command{
    [self initMapConfig];
    [self initMapView];
    [self initOverlay];
    
    NSString* coordinates = [command.arguments objectAtIndex:0];
    
    if (coordinates.length) {
        [self initRouter:coordinates];
    }
    
    [self._mapView addOverlay:self.mutablePolyline];
}

//获取当前位置
-(void) getCurrentPosition:(CDVInvokedUrlCommand*)command{

    if (!self.curLocationManager) {
        [self initLocationConfig];
        self.curLocationManager = [[AMapLocationManager alloc] init];
        [self.curLocationManager setDesiredAccuracy:kCLLocationAccuracyNearestTenMeters];
    }
    
    [self.commandDelegate runInBackground:^{
        [self.curLocationManager requestLocationWithReGeocode:YES completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
            CDVPluginResult* pluginResult = nil;
            if (error)
            {
                
                if (error.code == AMapLocationErrorLocateFailed)
                {
                    NSString *errorCode = [NSString stringWithFormat: @"%ld", (long)error.code];
                    NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:
                                          errorCode,@"errorCode",
                                          error.localizedDescription,@"errorInfo",
                                          nil];
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:dict];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                }
            }else{
                if (regeocode)
                {
                    NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:regeocode.province,@"provinceName",
                                          regeocode.city,@"cityName",
                                          regeocode.citycode,@"cityCode",
                                          regeocode.district,@"districtName",
                                          regeocode.township,@"roadName",
                                          @(location.coordinate.latitude),@"latitude",
                                          @(location.coordinate.longitude),@"longitude",
                                          nil];
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                }
            }
        }];
    }];
}

//开始后台持续定位
-(void) startUpdatePosition:(CDVInvokedUrlCommand*)command{
    
    self.minSpeed   = 2;
    self.minFilter  = 50;
    self.minInteval = 10;
    self.distanceFilter = self.minFilter;
    
    if (![self locationServicesEnabled]) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"请开启手机的GPS定位功能"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    
    [self initLocationConfig];
    if (self.locationManager) {
        self.locationManager = nil;
    }
    
    [self clearLocations];
    
    self.locationManager = [[AMapLocationManager alloc]init];
    self.locationManager.delegate = self;
    //一次还不错的定位，偏差在100米以内，耗时在3s左右 kCLLocationAccuracyHundredMeters];
    //精度很高的一次定位，偏差在10米以内，耗时在10s左右 kCLLocationAccuracyBest];
    [self.locationManager setDesiredAccuracy:kCLLocationAccuracyNearestTenMeters];
    
    //定位超时时间，最低2s，此处设置为11s
    //self.locationManager.locationTimeout =11;
    //逆地理请求超时时间，最低2s，此处设置为12s
    //self.locationManager.reGeocodeTimeout = 12;
    //设置允许后台定位参数，保持不会被系统挂起
    [self.locationManager setPausesLocationUpdatesAutomatically:NO];
    [self.locationManager setAllowsBackgroundLocationUpdates:YES];//iOS9(含)以上系统需设置
    isStart = YES;
    
    self.locationManager.distanceFilter = self.distanceFilter;
    [self.locationManager startUpdatingLocation];
}


//判断是否开启了GPS
-(Boolean) locationServicesEnabled {
    if (([CLLocationManager locationServicesEnabled]) && ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusAuthorizedAlways)) {
        NSLog(@"手机gps定位已经开启");
        return true;
    } else {
        NSLog(@"手机gps定位未开启");
        return false;
    }
}

//停止后台持续定位
-(void) stopUpdatePosition:(CDVInvokedUrlCommand*)command{
    if(self.locationManager){
        [self.locationManager stopUpdatingLocation];
    }
    isStart = NO;
}

//读取持续定位数据
-(void) readUpdatePosition:(CDVInvokedUrlCommand*)command{
    
    NSArray* array = [self getLocations];
    NSLog(@"get array in read: %@", array);
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:array];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [self clearLocations];
}

- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location
{
    [self adjustDistanceFilter:location];
    if (location.horizontalAccuracy < 200 && isStart) {
        if (lat!=location.coordinate.latitude && lng!=location.coordinate.longitude) {
            //            NSLog(@"put into:{lat:%e; lon:%e; accuracy:%e; speed:%e}", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy, location.speed);
            //            NSTimeZone* sourceTimeZone = [NSTimeZone timeZoneWithName:@"Asia/Shanghai"];
            //            NSInteger interval = [sourceTimeZone secondsFromGMTForDate:location.timestamp];
            //            NSLog(@"interval: %ld", (long)interval);
            
            //            NSDate *localeDate = [location.timestamp  dateByAddingTimeInterval: interval];
            //            NSLog(@"localeDate: %@", localeDate);
            
            NSString *timeSp = [NSString stringWithFormat:@"%ld", (long)[location.timestamp timeIntervalSince1970]];
            if (location.speed <= 0 && lat>0) {
                CLLocation *before=[[CLLocation alloc] initWithLatitude:lat longitude:lng];
                CLLocation *current=[[CLLocation alloc] initWithLatitude:location.coordinate.latitude longitude:location.coordinate.longitude];
                CLLocationDistance meters=[current distanceFromLocation:before];
                
                if (meters < 10) {
                    NSLog(@"before location: %f,%f, distance:%f", lat, lng, meters);
                    return;
                }
            }
            lat = location.coordinate.latitude;
            lng = location.coordinate.longitude;
            
            struct Yxtlocation loc = {location.coordinate.latitude, location.coordinate.longitude, location.speed,location.horizontalAccuracy,(long)timeSp};
            [self putLocation:loc];
        }
    }
    
    
}

/**
 *  规则: 如果速度小于minSpeed m/s 则把触发范围设定为minFilter m
 *  否则将触发范围设定为minSpeed*minInteval
 *  此时若速度变化超过10% 则更新当前的触发范围(这里限制是因为不能不停的设置distanceFilter,
 *  否则uploadLocation会不停被触发)
 */
- (void)adjustDistanceFilter:(CLLocation*)location
{
    if ( location.speed < self.minSpeed )
    {
        if ( fabs(self.distanceFilter-self.minFilter) > 0.1f )
        {
            self.distanceFilter = self.minFilter;
            self.locationManager.distanceFilter = self.distanceFilter;
        }
    }
    else
    {
        CGFloat lastSpeed = self.distanceFilter/self.minInteval;
        
        if ( (fabs(lastSpeed-location.speed)/lastSpeed > 0.1f) || (lastSpeed < 0) )
        {
            CGFloat newSpeed  = (int)(location.speed+0.5f);
            CGFloat newFilter = newSpeed*self.minInteval;
            
            self.distanceFilter = newFilter;
            self.locationManager.distanceFilter = self.distanceFilter;
        }
    }
    
}

- (NSMutableArray*)getLocations {
    NSUserDefaults* userDefaults = [NSUserDefaults standardUserDefaults];
    NSArray* array = [userDefaults objectForKey:USER_DEFAULT_KEY];
    NSMutableArray* mutableArray = nil;
    if(array != nil){
        mutableArray = [NSMutableArray arrayWithArray:array];
    }else{
        mutableArray = [[NSMutableArray alloc] init];
    }
    return mutableArray;
}

- (void) setLocations:(NSMutableArray*)locations{
    NSUserDefaults* userDefaults = [NSUserDefaults standardUserDefaults];
    NSArray * array = [NSArray arrayWithArray:locations];
    [userDefaults setObject:array forKey:USER_DEFAULT_KEY];
}

- (NSString*)dictionaryToJson:(NSDictionary*)dictionary{
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:NSJSONWritingPrettyPrinted error:nil];
    return [[NSString alloc] initWithBytes:[jsonData bytes] length:[jsonData length] encoding:NSUTF8StringEncoding];
}

//暂存持续定位数据
- (void) putLocation:(struct Yxtlocation) location{
    //is in background
    UIApplicationState appState = UIApplicationStateActive;
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(applicationState)]) {
        appState = [UIApplication sharedApplication].applicationState;
    }
    BOOL inBackground = appState != UIApplicationStateActive;
    
    NSMutableArray* locations = [self getLocations];
    NSDictionary* dictionary = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:inBackground], IN_BACKGROUND_KEY, location.timestamp, CREATED_AT_KEY, [NSNumber numberWithDouble: location.latitude], LATITUDE_KEY, [NSNumber numberWithDouble: location.longitude],LONGITUDE_KEY, [NSNumber numberWithDouble: location.speed],SPEED_KEY,[NSNumber numberWithDouble: location.accuracy],ACCURACY_KEY,nil];
    //    NSLog(@"dictionary %@", dictionary);
    [locations addObject:[self dictionaryToJson:dictionary]];
    if([locations count] > MAX_LENGTH){
        [locations removeObjectAtIndex:0];
    }
    [self setLocations:locations];
}

- (void) clearLocations{
    NSUserDefaults* userDefault = [NSUserDefaults standardUserDefaults];
    [userDefault removeObjectForKey: USER_DEFAULT_KEY];
}

- (void)mapView:(MAMapView *)mapView didUpdateUserLocation:(MAUserLocation *)userLocation updatingLocation:(BOOL)updatingLocation
{
    if (!updatingLocation)
    {
        return;
    }
    
    if (userLocation.location.horizontalAccuracy < 80 && userLocation.location.horizontalAccuracy > 0)
    {
        
        [self.mutablePolyline appendPoint: MAMapPointForCoordinate(userLocation.location.coordinate)];
        
        [self._mapView setCenterCoordinate:userLocation.location.coordinate animated:YES];
        
        //        [self.render invalidatePath];
    }
    //    [self.statusView showStatusWith:userLocation.location];
}

//初始化地图
- (void)initMapView
{
    if (self._mapView) {
        self._mapView = nil;
    }
    self.headerView = [[SBGMapHeaderView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.webView.bounds), 64)];
    self.headerView.title = @"定位";
    
    __weak CDVAMap4Yxt *weakSelf = self;
    [self.headerView setBackCallBack:^{
        [weakSelf hideMap:nil];
    }];
    self.headerView.backgroundColor = [UIColor colorWithRed:87/255.0 green:142/255.0 blue:220/255.0 alpha:1];
    [self.webView addSubview:self.headerView];
    self._mapView = [[MAMapView alloc] initWithFrame:CGRectMake(0, 64, CGRectGetWidth(self.webView.bounds), CGRectGetHeight(self.webView.bounds))];
    self._mapView.delegate = self;
    self._mapView.showsUserLocation = YES;
    self._mapView.distanceFilter = 10;
    self._mapView.desiredAccuracy = kCLLocationAccuracyBestForNavigation;
    self._mapView.pausesLocationUpdatesAutomatically = NO;
    
    [self._mapView setUserTrackingMode: MAUserTrackingModeFollow animated:YES];
    
    [self.webView addSubview:self._mapView];
    
    UIButton *locationBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self._mapView addSubview:locationBtn];
    locationBtn.backgroundColor = [UIColor whiteColor];
    [locationBtn addTarget:self action:@selector(scrollCenter:) forControlEvents:UIControlEventTouchUpInside];
    locationBtn.frame = CGRectMake(20, CGRectGetHeight(self._mapView.frame) - 128, 27.5, 27.5);
//    NSString * path = [[[NSBundle mainBundle] pathForResource:@"AMap" ofType:@"bundle"] stringByAppendingPathComponent:@"images/locationIcon.png"];
    UIImage *img = [UIImage imageNamed:@"locationIcon"];
    [locationBtn setImage:img forState:UIControlStateNormal];
}

- (void)initOverlay
{
    self.mutablePolyline = [[MAMutablePolyline alloc] initWithPoints:@[]];
}

- (void)setAnnotations:(NSString *)coordinates andTips: (NSString *)tips
{
    self.annotations = [NSMutableArray array];
    
    NSArray *coordinateslistItems = [coordinates componentsSeparatedByString:@";"];
    NSArray *tipsItems = [tips componentsSeparatedByString:@","];
    
    long len =[coordinateslistItems count];
    for(int i=0; i < len; i++) {
        NSString* item = [coordinateslistItems objectAtIndex:i];
        NSArray * tmp = [item componentsSeparatedByString:@","];
        
        CLLocationCoordinate2D coord = CLLocationCoordinate2DMake([[tmp objectAtIndex:0] doubleValue], [[tmp objectAtIndex:1] doubleValue]);
        
        MAPointAnnotation *a1 = [[MAPointAnnotation alloc] init];
        a1.coordinate = coord;
        a1.title      = [NSString stringWithFormat:@"%@", [tipsItems objectAtIndex:i]];
        [self.annotations addObject:a1];
    }
    
    [self._mapView addAnnotations:self.annotations];
    [self._mapView showAnnotations:self.annotations edgePadding:UIEdgeInsetsMake(20, 20, 20, 80) animated:YES];
}

- (void) initRouter:(NSString *)coordinates
{
    
    NSArray *coordinateslistItems = [coordinates componentsSeparatedByString:@";"];
    long len =[coordinateslistItems count];
    for(int i=0; i < len; i++) {
        NSString* item = [coordinateslistItems objectAtIndex:i];
        NSArray * tmp = [item componentsSeparatedByString:@","];
        
        CLLocationCoordinate2D coord = CLLocationCoordinate2DMake([[tmp objectAtIndex:0] doubleValue], [[tmp objectAtIndex:1] doubleValue]);
        if (i == 0) {
            MAPointAnnotation *a1 = [[MAPointAnnotation alloc] init];
            a1.coordinate = coord;
            a1.title      = [NSString stringWithFormat:@"%@", @"开始位置"];
            [self._mapView addAnnotation:a1];
        }
        
        [self.mutablePolyline appendPoint:MAMapPointForCoordinate(coord)];
        
    }
    
    //    NSLog(@"length is %@",[self.mutablePolyline ])
}

- (void)mapView:(MAMapView *)mapView  didChangeUserTrackingMode:(MAUserTrackingMode)mode animated:(BOOL)animated
{
    if (mode == MAUserTrackingModeNone)
    {
        // [self.locationBtn setImage:self.imageNotLocate forState:UIControlStateNormal];
    }
    else
    {
        // [self.locationBtn setImage:self.imageLocated forState:UIControlStateNormal];
        [self._mapView setZoomLevel:16 animated:YES];
    }
}

- (void)checkPermissions {
    BOOL locationEnabled = [CLLocationManager locationServicesEnabled];
    CLAuthorizationStatus authorizationStatus = [CLLocationManager authorizationStatus];
    if (authorizationStatus == kCLAuthorizationStatusRestricted) {
        //拒绝app定位
    }
    if (!locationEnabled) {
        //未开启定位服务
    }
    
}

- (void)scrollCenter:(id)sender {
    [self._mapView setCenterCoordinate:self._mapView.userLocation.location.coordinate animated:YES];
}
@end
