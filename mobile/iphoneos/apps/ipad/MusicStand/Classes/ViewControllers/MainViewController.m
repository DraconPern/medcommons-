//
//  MainViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>
#import "AppDelegate.h"
#import "AsyncImageView.h"
//#import "DictionaryAdditions.h"
#import "DocumentsManager.h"
#import "MCDocumentTableViewCell.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "OneTuneViewController.h"
#import "MainViewController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "SettingsViewController.h"
#import "TitleNode.h"


#pragma mark -
#pragma mark Public Class MainViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//


enum
{
    SECTION_COUNT = 27  // MUST be kept in display order ...
	
    //
	
};

@interface MainViewController () <UITableViewDataSource, UITableViewDelegate>

@end

@implementation MainViewController




#pragma mark Overridden UIViewController Methods
//
//- (void) periodicNewFilePoller
//{
//	
//    self.application.networkActivityIndicatorVisible = YES;
//	BOOL didsomething = [[DocumentsManager sharedInstance] decompressAllZipFilesInDocumentDirectory]; // shud return BOOL if anything was done
//	if (didsomething) {
//		[DataManager buildMasterIndex];
//		[self loadView]; // resets 
//	}
//	self.application.networkActivityIndicatorVisible = NO;
//	// check again in 10s
//	//[NSTimer scheduledTimerWithTimeInterval: 10.0f target:self selector:@selector(periodicNewFilePoller) userInfo:nil repeats:NO];
//	
//}


- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
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
	self.navigationItem.title = @"All Tunes";
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

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return [[[DataManager sharedInstance].collation sectionTitles] objectAtIndex:section];
}


- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView {
    return [[DataManager sharedInstance].collation sectionIndexTitles];
}


- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {
    return [[DataManager sharedInstance].collation sectionForSectionIndexTitleAtIndex:index];
}


- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 27;
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
	
    //
    // Reset cell properties to default:
    //
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;
	
	
	if (section < [ [DataManager sharedInstance].sectionsArray count])
	{
		//MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		NSArray *titleNodes = [ [DataManager sharedInstance].sectionsArray objectAtIndex:section];
		
		if (row <[titleNodes count])
		{
			
			TitleNode *tn = [titleNodes objectAtIndex: row];
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
			NSMutableString *name=[NSMutableString stringWithString:@""]; 
			NSUInteger variants = 0;
			while ((key = [enumerator nextObject])) {
				//up to the first slash to get the archive part of the filepath;
				NSString *s = [key stringByDeletingLastPathComponent];
				NSUInteger arcidx=[DataManager indexFromArchiveName:s];
				//NSLog (@"Looked up %@ got %d",s,arcidx);//////////999999//////////////////////////////////////////////
				NSString	*sn = [DataManager shortNameFromArchiveIndex:arcidx];
				name = (NSMutableString *)[ name stringByAppendingFormat: @" %@ ",sn];
				variants++;
			}
			//path = [tn2.variants objectForKey:name];
			cell.textLabel.text = tn2.title;			
			cell.detailTextLabel.text = name;
			//if (variants>1) NSLog (@"multiple versions of single title %@",tn2.title);
		}
		else cell = nil;
	}
	else
		cell = nil;
	
	
    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	
   	NSArray *titleNodes = [ [DataManager sharedInstance].sectionsArray objectAtIndex:section];
	
    return [titleNodes count];
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
	NSArray *titleNodes = [[DataManager sharedInstance].sectionsArray objectAtIndex:idxPath.section];
	
	if (idxPath.row <[titleNodes count])
	{
		
		TitleNode *tn  = [titleNodes objectAtIndex: idxPath.row];
		if (!tn)
		{
			NSLog(@"no titleNodes found for item at row %d", idxPath.row);
			return;
		}
		//each dict has different entries, just get the first 
		
		
		for (NSString *path in tn.variants) // only executed for the first variant
			
		if ([[tn.variants objectForKey:path] unsignedIntValue] == 0) 
		
		{
			
			
			NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],path];
			NSURL    *docURL = [NSURL fileURLWithPath: url
										  isDirectory: NO];
			
			NSLog (@"=>webview  %@",url);
			
			OneTuneViewController *wvc = [[[OneTuneViewController alloc]
										   initWithURL: docURL andWithTitle: tn.title andWithShortPath: path]
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
