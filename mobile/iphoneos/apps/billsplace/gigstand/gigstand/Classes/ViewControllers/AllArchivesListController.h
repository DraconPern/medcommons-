//
//  AllArchivesListController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AllArchivesListController.h"
@interface AllArchivesListController :  UIViewController {
	
	NSMutableArray *listItems;
	NSString *name;
	NSString *plist;
	UITableView *tableView;
	BOOL canEdit;
	BOOL canReorder; // canEdit must be YES for this to matter
	NSUInteger tag;
	
	UIImageView     *logoView;
}

@end
