//
//  TestFullScreenView.m
//  MedPad
//
//  Created by william donner on 4/8/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TestFullScreenView.h"
#import "AppDelegate.h"
#import "DetailViewController.h"
#import "DataManager.h"

@implementation TestFullScreenView


-(void) testFullScreenView

{

    NSString *janeh = @"https://healthurl.medcommons.net/router/currentccr?a=1013062431111407&aa=1117658438174637&g=3642665a324059089d0b989286eb9a9ced046e54&t=&m=&c=&auth=c3faf3705e0e98c0984d1f86b96a789f3dba2638&at=c3faf3705e0e98c0984d1f86b96a789f3dba2638";

    DataManager *dm = [DataManager sharedInstance];
    //-(void) displayFullScreenWebView: (NSString *)urlpart  backcolor: (UIColor *)bc title: (NSString *)titl

    [dm.detailController displayFullScreenWebView:janeh backcolor: [UIColor blackColor]
                                            title:@"Not quite new Jane"];
}

@end
