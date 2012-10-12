//
//  ArchiveListViewController.h
//  MusicStand
//
//  Created by bill donner on 10/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GeneralSetListViewController.h"


@interface ArchiveListViewController : UIViewController {
	NSArray *listItems;
	NSString *name;	
	UITableView *tableView;

}

-(id) initWithArchive:(NSString * )archivex  listItems:(NSArray *) archiveItems;
@end
