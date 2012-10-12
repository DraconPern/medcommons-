//
//  SamplesController.h
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SamplesController : UIViewController<UITableViewDataSource, UITableViewDelegate> {
	
	NSMutableArray *listItems;
	
	NSMutableArray *listImages;
	
	NSMutableArray *listItemsEnabled;

	BOOL canEdit;
	UITableView *tableView;
	
	
}

-(id) init;
@end
