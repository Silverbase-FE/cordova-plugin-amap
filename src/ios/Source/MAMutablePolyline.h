//
//  MAMutablePolyline.h
//
//  Created by UP on 2017/7/5.
//  Copyright © 2017年 Silver Base Group Holdings Limited. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MAMapKit/MAOverlay.h>

@interface MAMutablePolyline : NSObject<MAOverlay>

/* save MAMapPoints by NSValue */
@property (nonatomic, strong) NSMutableArray *pointArray;

- (instancetype)initWithPoints:(NSArray *)nsvaluePoints;

- (MAMapRect)showRect;

- (MAMapPoint)mapPointForPointAt:(NSUInteger)index;

- (void)updatePoints:(NSArray *)points;

- (void)appendPoint:(MAMapPoint)point;

@end
