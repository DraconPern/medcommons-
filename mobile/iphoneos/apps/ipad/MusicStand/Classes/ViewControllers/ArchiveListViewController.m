//
//  ArchiveListViewController.m
//  MusicStand
//
//  Created by bill donner on 10/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ArchiveListViewController.h"
#import "AppDelegate.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "DataManager.h"
#import "SettingsViewController.h"
#import "TitleNode.h"
#import "OneTuneViewController.h"
#import "DataStore.h"

// put up a done button so this can be shown modally from the settings page
@interface ArchiveListViewController () <UITableViewDataSource, UITableViewDelegate>

@end

@implementation ArchiveListViewController
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(id) initWithArchive:(NSString * )archivex  listItems:(NSArray *) archiveItems;
{	

	self = [super init];
	if (self)
	{
		self->listItems = [archiveItems retain];
		self->name = [archivex retain];
	}
	return self;

}
-(void) loadView
{
	[super loadView];	
	
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
														 style: UITableViewStylePlain]
							autorelease];
	
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [UIColor lightGrayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	// this will appear as the title in the navigation bar
	CGRect frame = CGRectMake(0, 0, 450, 44);
	UILabel *label = [[[UILabel alloc] initWithFrame:frame] autorelease];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0f];
	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor = [UIColor whiteColor];
	self.navigationItem.titleView = label;
	label.text = [NSString stringWithFormat:@"MusicStand: archive: %@",self->name];

	self->tableView = tmpView; // make everyone else happy too!
	
	
	self.navigationItem.leftBarButtonItem =
	[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone 
												   target:self action:@selector(donePressed)] autorelease];
	
		self.view = tmpView;
	

}
-(void) dealloc;
{
	//[self->listItems release];
	//[self->name release];
	[super dealloc];
}
- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	return 1;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	
	//section should always be 1
	
	return [self->listItems count];
	
}
- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// section should always be 1
	NSUInteger count = [self->listItems  count];
	
	if (idxPath.row < count)
	{
	
		RefNode *tn = [ self->listItems  objectAtIndex:idxPath.row];
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
											   initWithURL: docURL andWithTitle: tn.title andWithShortPath: path andWithBackLabel:self->name
											    ]
											  autorelease];
				wvc.title = tn.title  ;
				
				UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
				
				[self presentModalViewController:nav animated: YES];
				
				break;
			}
	}
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
	
	
	if (section < [self->listItems count]) // this is always 1
	{
		//MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		
		
		RefNode *tn = [ self->listItems objectAtIndex:row];
		
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
- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return 60.0f;
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
