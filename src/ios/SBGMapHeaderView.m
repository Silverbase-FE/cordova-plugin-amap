
#import "SBGMapHeaderView.h"

@interface SBGMapHeaderView ()
@property (nonatomic, strong) UILabel *titleLab;
@end

@implementation SBGMapHeaderView

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self createViews];
    }
    return self;
}

- (void)createViews {
    UIButton *backBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self addSubview:backBtn];
    backBtn.frame = CGRectMake(0, 20, 35, 44);
    [backBtn addTarget:self action:@selector(backAction:) forControlEvents:UIControlEventTouchUpInside];
    
//    NSString * path = [[[NSBundle mainBundle] pathForResource:@"AMap" ofType:@"bundle"] stringByAppendingPathComponent:@"images/naviBackIcon.png"];
    UIImage *img = [UIImage imageNamed:@"naviBackIcon"];
    [backBtn setImage:img forState:UIControlStateNormal];
}

- (void)backAction:(id)sender {
    if (self.backCallBack) {
        self.backCallBack();
    }
}

#pragma mark - ==============set & get==============

- (void)setTitle:(NSString *)title {
    _title = title;
    self.titleLab.text = _title;

    [self.titleLab sizeToFit];
    self.titleLab.center = self.center;
    CGRect titleFrame = CGRectMake(0, 20, 0, 44);
    titleFrame.origin.x = CGRectGetMinX(self.titleLab.frame);
    titleFrame.size.width = CGRectGetWidth(self.titleLab.frame);
    self.titleLab.frame = titleFrame;
}

- (UILabel *)titleLab {
    if (_titleLab) {
        return _titleLab;
    }
    _titleLab = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 30, 30)];
    _titleLab.textColor = [UIColor whiteColor];
    _titleLab.font = [UIFont systemFontOfSize:18.];
    [self addSubview:_titleLab];
    return _titleLab;
}
@end
