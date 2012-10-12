//
//  WebPaneController.m
//  MedCommons
//
//  Created by bill donner on 12/28/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "WebPaneController.h"
#import "DataManager.h"
#import "SegmentMap.h"


@implementation WebPaneController


-(WebPaneController *) initWithURL: (NSString *)_urlx andWithTitle: (NSString *)_titley andWithFrame:(CGRect)_frame
{
    self = [super init];
    theurl = [_urlx copy]; // important  - else crashes
    panetitle = [_titley copy] ;

    NSLog (@"webpane webframe %f %f %f %f %@" , _frame.origin.x,_frame.origin.y,_frame.size.width ,_frame.size.height, _urlx);
    outerView = [[UIView alloc] initWithFrame:_frame];
    outerView.backgroundColor = [UIColor lightGrayColor];

    NSLog (@"outerview webframe %f %f %f %f %@" , outerView.frame.origin.x,outerView.frame.origin.y,outerView.frame.size.width ,outerView.frame.size.height, _urlx);


    //BREADCRUMBS_PUSH;

    return self;
}
-(void) dealloc
{
    //BREADCRUMBS_POP;
    [webView release];
    [outerView release];
    [theurl release];
    [panetitle release];
    [super dealloc];
}
- (void) loadView
{
    self.view = outerView;
    //NSLog(@"outerview2 frame center bounds is %@  %@",outerView.frame,outerView.bounds);

    NSLog (@"outerview webframe %f %f %f %f" , outerView.frame.origin.x,outerView.frame.origin.y,outerView.frame.size.width ,outerView.frame.size.height);

    CGRect webframe = CGRectMake(outerView.frame.origin.x+5.f,outerView.frame.origin.y+5.f,outerView.frame.size.width-10.f ,outerView.frame.size.height-10.f);
    webView = [[UIWebView alloc] initWithFrame:webframe];
    webView.scalesPageToFit=YES;
    webView.backgroundColor = [UIColor blueColor];
    //  [[[webView subviews] lastObject] setScrollingEnabled:NO];

    self.navigationItem.title = panetitle;
    self.navigationItem.hidesBackButton = YES; //NO;

    webView.autoresizingMask = //0;//
    UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;

    NSURL *url = [NSURL URLWithString:theurl];

    NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
    [webView loadRequest:requestObj];

    // copied in
    DataManager *dm =   [DataManager sharedInstance];
    //NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext];
    //  if (!scene) return ;// really should die here horribly



    self.navigationItem.rightBarButtonItem = dm.rightRootSM.segmentBarItem;

    self.navigationItem.leftBarButtonItem = dm.leftRootSM.segmentBarItem ;

    self.navigationController.toolbarHidden = NO;//
    self.navigationController.toolbar.barStyle = UIBarStyleBlack;

    //
    NSMutableArray *tbitems = [NSMutableArray array];
    //
    if (dm.bottomRootSM) // if a bottom was specified then add it
    {
        [tbitems addObject:dm.bottomRootSM.segmentBarItem];

        [self setToolbarItems:tbitems];

        //[tbitems release];
    }

    [self.view addSubview:webView];


}


@end

