//
//  AllSetListsController.h
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AllSetListsController.h"

@interface AllSetListsController : UIViewController <UIActionSheetDelegate>{
	
	UIActionSheet *toass;
  UIImageView     *logoView_;

	NSMutableArray *listItems;
	NSString *name;
	NSString *plist;
	UITableView *tableView;
	BOOL canEdit;
	BOOL canReorder; // canEdit must be YES for this to matter
	NSUInteger tag;
	
}

-(NSUInteger ) itemCount;

-(id) initWithList: (NSArray *)arr name: (NSString *) namxe   canEdit:(BOOL)cane canReorder:(BOOL)canr tag:(NSUInteger)tagg;
-(void) reloadListItems : (NSArray *) newItems;
-(void) setBarButtonItems;
-(void) leaveEditMode;

-(void) enterEditMode;
@end
