
#import "MAMutablePolyline.h"

@interface MAMutablePolyline()

@property (nonatomic, readwrite) CLLocationCoordinate2D coordinate;

@property (nonatomic, readwrite) MAMapRect boundingMapRect;

@end


@implementation MAMutablePolyline

#pragma mark - interface

- (MAMapRect)showRect
{
    return self.boundingMapRect;
}

- (void)updatePoints:(NSArray *)points
{
    _pointArray = [NSMutableArray arrayWithArray:points];
    [self calculateBoundingMapRect];
}

- (void)appendPoint:(MAMapPoint)point
{
    [_pointArray addObject:[NSValue valueWithMAMapPoint:point]];
    [self calculateBoundingMapRect];
}

#pragma mark - Helper

- (MAMapPoint)mapPointForPointAt:(NSUInteger)index
{
    NSValue *value = [self.pointArray objectAtIndex:index];
    return [value MAMapPointValue];
}

- (void)calculateBoundingMapRect
{
    if (_pointArray.count > 0)
    {
        CGFloat minX = 0;
        CGFloat minY = 0;
        CGFloat maxX = 0;
        CGFloat maxY = 0;
        
        int index = 0;
        for (NSValue *value in _pointArray)
        {
            if (index == 0)
            {
                MAMapPoint point0 = [value MAMapPointValue];
                minX = point0.x;
                minY = point0.y;
                maxX = minX;
                maxY = minY;
            }
            else
            {
                MAMapPoint point = [value MAMapPointValue];
                
                if (point.x < minX)
                {
                    minX = point.x;
                }
                
                if (point.x > maxX)
                {
                    maxX = point.x;
                }
                
                if (point.y < minY)
                {
                    minY = point.y;
                }
                
                if (point.y > maxY)
                {
                    maxY = point.y;
                }
            }
            ++index;
        }
        _boundingMapRect = MAMapRectMake(minX, minY, fabs(maxX - minX), fabs(maxY - minY));
    }
}

#pragma mark - MAOverlay

- (MAMapRect)boundingMapRect
{
    return _boundingMapRect;
}

- (CLLocationCoordinate2D)coordinate
{
    return MACoordinateForMapPoint(MAMapPointMake(MAMapRectGetMidX(_boundingMapRect), MAMapRectGetMidY(_boundingMapRect)));
}

#pragma mark - Life Cycle

- (instancetype)initWithPoints:(NSArray *)nsvaluePoints
{
    if (self = [super init])
    {
        [self updatePoints:nsvaluePoints];
    }
    
    return self;
}

@end
