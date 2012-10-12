//
//  AllSetListsController.h
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AllSetListsController.h"

@interface AllSetListsController : UIViewController <UIActionSheetDelegate>{
	@private
	UIActionSheet *toass;
	UIImageView     *logoView;
	NSMutableArray *listItems;
	NSString *name;
	UITableView *tableView;
	BOOL canEdit;
	BOOL canReorder; // canEdit must be YES for this to matter
	NSUInteger tag;
	NSTimer *aTimer;	
}

@end
