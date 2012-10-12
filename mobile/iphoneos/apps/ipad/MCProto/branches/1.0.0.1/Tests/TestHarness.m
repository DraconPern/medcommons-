//
//  TestHarness.m
//  MedPad
//
//  Created by bill donner on 4/3/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TestHarness.h"
#import "AppDelegate.h"
#import "DataManager.h"
#import "DetailViewController.h"

@implementation TestHarness
-(void) toplog: (NSString *) s
{
    //NSLog (@"top log -%@",s);
    [[DataManager sharedInstance].detailController showTopDetail:s];
}
-(void) bottomlog: (NSString *) s
{
    //NSLog (@"bottom log -%@",s);

    [[DataManager sharedInstance].detailController showBottomDetail:s];
}

@end
