//
//  SBGMapHeaderView.h
//  salesManage
//
//  Created by silverbase on 2017/1/5.
//
//

#import <UIKit/UIKit.h>

@interface SBGMapHeaderView : UIView

@property (nonatomic, strong) NSString *title;

@property (nonatomic, copy) void(^backCallBack)(void);
@end
