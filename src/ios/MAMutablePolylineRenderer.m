
#import "MAMutablePolylineRenderer.h"
#import "MAMutablePolyline.h"

@implementation MAMutablePolylineRenderer

- (void)createPath
{
    CGMutablePathRef path = CGPathCreateMutable();

    MAMutablePolyline *overlay = self.overlay;
    
    if (overlay.pointArray.count > 0)
    {
        CGPoint point = [self pointForMapPoint:[overlay mapPointForPointAt:0]];
        CGPathMoveToPoint(path, nil, point.x,point.y);
    }
    
    for (int i = 1; i < overlay.pointArray.count; i++)
    {
        CGPoint point = [self pointForMapPoint:[overlay mapPointForPointAt:i]];
        CGPathAddLineToPoint(path, nil, point.x, point.y);
    }
    
//    self.path = path;
}

- (void)fillPath:(CGPathRef)path inContext:(CGContextRef)context
{
    return;
}
@end


