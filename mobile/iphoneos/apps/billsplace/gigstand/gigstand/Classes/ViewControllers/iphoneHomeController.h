//
//  iPadHomeController.h
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>

@interface iphoneHomeController : UIViewController
{
    @private
	NSMutableArray *listItems;
	NSArray *alistItems;
	UITableView *mainTableView;
    UIWindow *deviceWindow;
	UITextView *consoleTextView;
	UIWindow *externalWindow;
	NSArray *screenModes;
	UIScreen *externalScreen;
	
	NSTimer *aTimer;
    NSTimer *checkTimer;
    
    
    NSUInteger ltunecount;
    NSUInteger licount;
    NSUInteger aicount;
	
}

@property (nonatomic, retain) IBOutlet UIWindow *deviceWindow;
@property (nonatomic, retain) IBOutlet UITextView *consoleTextView;
@property (nonatomic, retain) IBOutlet UIWindow *externalWindow;

- (void)log:(NSString *)msg;

@end
