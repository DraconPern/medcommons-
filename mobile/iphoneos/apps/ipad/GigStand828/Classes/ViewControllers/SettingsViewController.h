//
//  SettingsViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
@class ProgressString;
@interface SettingsViewController : UIViewController
{
@private

	NSTimer *aTimer;
	
	
    UIActivityIndicatorView *activityIndicator;
							   
	
	//ProgressString *progressString;
	
	
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
