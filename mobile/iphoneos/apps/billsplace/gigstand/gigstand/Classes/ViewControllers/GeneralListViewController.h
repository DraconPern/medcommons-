//
//  GeneralListViewController.h
//  MusicStand
//
//  Created by bill donner on 10/18/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>


@interface GeneralListViewController : UIViewController {
	
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
