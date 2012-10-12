//
//  SongsViewController.h
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
@class TuneInfo;
@interface TuneInfoController : UIViewController<UITableViewDataSource, UITableViewDelegate> {
	
	NSArray *listItems;
	NSString *tuneTitle;
	TuneInfo *tuneInfo;
	UITableView *tableView;
	
	
}

-(id) initWithTune : (NSString *) tune;
@end
