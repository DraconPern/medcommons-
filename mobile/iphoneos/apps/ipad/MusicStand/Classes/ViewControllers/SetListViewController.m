    //
//  SetListViewController.m
//  MusicStand
//
//  Created by bill donner on 10/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SetListViewController.h"



#import <MobileCoreServices/MobileCoreServices.h>

//#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>
#import "AppDelegate.h"
#import "AsyncImageView.h"
//#import "DictionaryAdditions.h"
#import "DocumentsManager.h"
//#import "MCDocumentTableViewCell.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "OneTuneViewController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "SettingsViewController.h"
#import "TitleNode.h"


#pragma mark -
#pragma mark Public Class SetListViewController
#pragma mark -
#pragma mark Internal Constants


//
// Table sections:
//


enum
{
    SECTION_COUNT = 1  // MUST be kept in display order ...
	
    //
	
};

@interface SetListViewController () <UITableViewDataSource, UITableViewDelegate>
@end

@implementation SetListViewController




#pragma mark Overridden UIViewController Methods



- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
}
										  
-(id) initWithList: (NSMutableArray *)section name: (NSString *) namxe;
{
	self = [super init];
	if (self)
	{
	self->sections = [section retain];
		self->name = [namxe  retain];
		
	}
	return self;
}

- (void) loadView
{
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
                                                         style: UITableViewStylePlain]
                            autorelease];
	
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
    tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	// autorelease];
	
	// add a button to push to actual view controller
	//self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Settings" style:UIBarButtonItemStyleDone target: self action:@selector(pushToSettings)] autorelease];
	self.navigationItem.title = self->name;
	// now put up the initial view and then scan for files to add
	//	[NSTimer scheduledTimerWithTimeInterval: 2.0f target:self selector:@selector(periodicNewFilePoller) userInfo:nil repeats:NO];
    
    self.view = tmpView;
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    [(UITableView *) self.view flashScrollIndicators];
}

- (void) viewDidLoad
{
    [super viewDidLoad];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];
	
	
    UITableView *tabView = (UITableView *) self.view;
	
	
	[tabView reloadData];
	
	
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];
	
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	[self->name release];
    [self->sections release];

	[super dealloc];
}



#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}



#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles from the collation.
 */



- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"ZipCell1";
	NSUInteger section = idxPath.section;
    NSUInteger row = idxPath.row;
	
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier1];
	
    if (!cell)
    {
		
		cell = [[[UITableViewCell alloc]
				 initWithStyle: UITableViewCellStyleSubtitle reuseIdentifier: CellIdentifier1]
				autorelease ];
		
    }
	
    //[
    // Reset cell properties to default:
    //
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;
	
	
	if (section < [self->sections count]) // this is always 1
	{
		//MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		
		
		RefNode *tn = [ self->sections objectAtIndex:row];
		
		if (!tn)
		{
			NSLog(@"cant find TitleNode found for item at row %d", tn);
			return nil;
		}
		
		else 
		{
			//NSLog (@"found TitleNode for item at row %d dict %@", row, tn);
		}
		
		
		// look it up again
		
		TitleNode *tn2 = [[DataManager sharedInstance].titlesDictionary objectForKey:tn.title ];
		if (!tn2)
		{
			NSLog(@"cant find TitleNode2for %@", tn.title);
			return nil;
		}
		
		NSEnumerator *enumerator = [tn2.variants keyEnumerator];
		id key;
		//		NSString *path;
		NSMutableString *namex=[NSMutableString stringWithString:@""]; 
		NSUInteger variants = 0;
		while ((key = [enumerator nextObject])) {
			//up to the first slash to get the archive part of the filepath;
			NSString *s = [key stringByDeletingLastPathComponent];
			NSUInteger arcidx=[DataManager indexFromArchiveName:s];
			//NSLog (@"Looked up %@ got %d",s,arcidx);//////////999999//////////////////////////////////////////////
			NSString	*sn = [DataManager shortNameFromArchiveIndex:arcidx];
			namex = (NSMutableString *)[ namex stringByAppendingFormat: @" %@ ",sn];
			variants++;
		}
		//path = [tn2.variants objectForKey:name];
		cell.textLabel.text = tn2.title;			
		cell.detailTextLabel.text = namex;
		//if (variants>1) NSLog (@"multiple versions of single title %@",tn2.title);
		
	}
	else
		cell = nil;
	
	
    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	
	//section should always be 1
	
	return [self->sections count];
	
}


#pragma mark UITableViewDelegate Methods

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return 60.0f;
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// section should always be 1
	
	
	NSUInteger count = [self->sections  count];
	
	if (idxPath.row < count)
	{
		
		
		RefNode *tn = [ self->sections  objectAtIndex:idxPath.row];
		if (!tn)
		{
			NSLog(@"no refNodes found for item at row %d", idxPath.row);
			return;
		}
		// look it up again
		
		TitleNode *tn2 = [[DataManager sharedInstance].titlesDictionary objectForKey:tn.title ];
		if (!tn2)
		{
			NSLog(@"cant find TitleNode2for %@", tn.title);
			return;
		}
		
		for (NSString *path in tn2.variants) // only executed for the first variant
			
			if ([[tn2.variants objectForKey:path] unsignedIntValue] == 0) 
				
			{
				
				
				NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],path];
				NSURL    *docURL = [NSURL fileURLWithPath: url
											  isDirectory: NO];
				
				NSLog (@"=>recentssview  %@",url);
				
				OneTuneViewController *wvc = [[[OneTuneViewController alloc]
											   initWithURL: docURL andWithTitle: tn.title andWithShortPath: path andWithBackLabel:self->name]
											  autorelease];
				wvc.title = tn.title  ;
				
				UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
				
				[self presentModalViewController:nav
				 //[self.navigationController pushViewController: wvc
										animated: YES];
				
				break;
			}
	}
}

- (void) tableView: (UITableView *) tabView
   willDisplayCell: (UITableViewCell *) cell
 forRowAtIndexPath: (NSIndexPath *) idxPath
{
    //
    // Apple docs say to do this here rather than at cell creation time ...
    //
    cell.backgroundColor = [UIColor whiteColor];
}

@end

