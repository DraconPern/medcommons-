//
//  SetListViewController.h
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SetListViewController.h"

#import <MessageUI/MFMailComposeViewController.h>


@interface SetListViewController : UIViewController 
<UITableViewDataSource, UITableViewDelegate,
MFMailComposeViewControllerDelegate,UIActionSheetDelegate,
UIPrintInteractionControllerDelegate> 
{
	BOOL canShare;
	UIActionSheet *toass;
	MFMailComposeViewController* controller;
	NSMutableArray *listItems;
	NSString *name;
	NSString *plist;
	UITableView *tableView;
	BOOL canEdit;
	BOOL canReorder; // canEdit must be YES for this to matter
	NSUInteger tag;
	
	NSTimer *aTimer;
	
}

-(NSUInteger ) itemCount;


-(void) reloadListItems : (NSArray *) newItems;
-(void) setBarButtonItems;
-(void) leaveEditMode;
-(void) emailPressed;
-(id) initWithPlist:(NSString *)path  name:(NSString *) namex edit:(BOOL) editx;
-(void) enterEditMode;

-(void) leaveEditMode;
@end
