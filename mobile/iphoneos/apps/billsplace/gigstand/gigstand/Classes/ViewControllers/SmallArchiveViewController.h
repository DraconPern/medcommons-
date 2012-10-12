//
//  SongsViewController.h
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GeneralListViewController.h"

@interface SmallArchiveViewController : UIViewController<UITableViewDataSource, UITableViewDelegate> {
	NSString *archive;
	NSMutableArray *listItems;
	NSString *listName;
	
	UITableView *tableView;
	
}

-(id) initWithArchive:(NSString *)archive;

-(id) initWithArchiveReversed: (NSString *)archiv;

@end
