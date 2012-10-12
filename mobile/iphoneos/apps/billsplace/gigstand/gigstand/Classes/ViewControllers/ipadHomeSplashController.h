//
//  ipadHomeSplashController.h
//  gigstand
//
//  Created by bill donner on 4/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SplashViewController.h"


@interface ipadHomeSplashController : UIViewController <UIPopoverControllerDelegate,UIScrollViewDelegate> {
  
@private
    
    UIPopoverController *popoverController;
    NSTimer *aTimer;
    NSTimer *checkTimer;
    
    UIPageControl *pageControl;

    UIView *landscapeView;
    UIView *portraitView;
    
    UIView *landscapeSnapshotView;
    UIView *portraitSnapshotView;
    
    UIScrollView *scrollView;
    
    NSString *rightbuttonlabel; // allow access from subclasses
    NSString *leftbuttonlabel;
    NSString *titlelabel;
    BOOL first;
    
}


-(void) popOff;
-(void) pushToController :(UIViewController *) uivc; // called from menuviewcontroller

@end
