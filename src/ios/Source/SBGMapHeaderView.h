//
//  SBGMapHeaderView.h
//
//  Created by UP on 2017/7/5.
//  Copyright © 2017年 Silver Base Group Holdings Limited. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SBGMapHeaderView : UIView

@property (nonatomic, strong) NSString *title;

@property (nonatomic, copy) void(^backCallBack)(void);
@end
