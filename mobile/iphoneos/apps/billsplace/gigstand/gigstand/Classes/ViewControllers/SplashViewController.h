//
//  SplashViewController.h
//  gigstand
//
//  Created by bill donner on 4/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SplashViewController : UIViewController {
    
    NSString                *rightbuttonlabel; // allow access from subclasses
@private
    

    UIWebView               *contentView_;
    NSUInteger               depth_;
    NSString                *leftbuttonlabel;
    
    
    NSString                *titlelabel;
    NSString                *verticalHTML;
    NSString                *horizontalHTML;
    SEL                     leftaction;
    SEL                     rightaction;
    id                      target;
	
	
	
	
	
}

@property (nonatomic, retain, readonly) UIWebView *contentView;

- (id) initWithVerticalHTML: (NSString *)vhtml horitzontalHTML: (NSString *)hhtml title: (NSString *)title 
            leftButtonOrNil:(NSString *)lb rightButtonOrNil:(NSString *)rb
                     target: (id) obj leftaction: (SEL) lselector  rightaction: (SEL) rselector;

- (void) injectJavaScript: (NSString *) jsString;


- (void) refresh;


@end
