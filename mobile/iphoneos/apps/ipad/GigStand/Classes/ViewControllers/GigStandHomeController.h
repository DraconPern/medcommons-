//
//  GigStandHomeController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GigStandHomeController : UIViewController
{

	NSMutableArray *listItems;
	NSArray *alistItems;
	UIView *mainTableView;
    UIWindow *deviceWindow;
	UITextView *consoleTextView;
	UIWindow *externalWindow;
	NSArray *screenModes;
	UIScreen *externalScreen;
	
	NSTimer *aTimer;
	
	//@private
	
	//UIView *tVView;
}

@property (nonatomic, retain) IBOutlet UIWindow *deviceWindow;
@property (nonatomic, retain) IBOutlet UITextView *consoleTextView;
@property (nonatomic, retain) IBOutlet UIWindow *externalWindow;

- (void)log:(NSString *)msg;

@end
