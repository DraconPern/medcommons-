//
//  SettingsViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>

#import <MessageUI/MFMailComposeViewController.h>
@class ProgressString;
@interface SettingsViewController : UIViewController <MFMailComposeViewControllerDelegate>
{
@private

	NSTimer *aTimer;
	
	
    UIActivityIndicatorView *activityIndicator;
							   
	
	//ProgressString *progressString;
	
	
	MFMailComposeViewController* controller;
	NSUInteger archive_import_count;
	NSUInteger setlist_import_count;
	NSUInteger lastzipcount;
	BOOL insideImport;
	
	
	NSString *incoming; 
	NSString *iname; 
	BOOL displayIncomingInfo;
	
	UITableView *mainTableView;
	
	BOOL autostart;

}



- (id) initWithAutoStart:(BOOL)moveSamples;

@end
