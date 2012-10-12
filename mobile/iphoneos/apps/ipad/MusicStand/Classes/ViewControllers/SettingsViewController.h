//
//  SettingsViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SettingsViewController : UIViewController
{
@private

    NSCalendar      *calendar_;
    NSDateFormatter *dateFormatter_;
    NSMutableArray *documents_;
	UITextView *textView;
	UIAlertView *av;
	UIAlertView *zipalert;
	NSUInteger archive_import_count;
	NSUInteger setlist_import_count;
	NSUInteger lastzipcount;
	BOOL insideImport;

}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;



@end
