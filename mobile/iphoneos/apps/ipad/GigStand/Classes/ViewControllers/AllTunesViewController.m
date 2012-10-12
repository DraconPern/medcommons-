//
//  AllTunesViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

//#import "AppDelegate.h"
//#import "SettingsManager.h"
#import "StyleManager.h"
#import "OneTuneViewController.h"
#import "AllTunesViewController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "SettingsViewController.h"
#import "TitleNode.h"
#import "Product.h"
#import "TitleNodeWrapper.h"

#pragma mark -
#pragma mark Public Class AllTunesViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//


enum
{
    SECTION_COUNT = 27  // MUST be kept in display order ...
	
};

@interface AllTunesViewController () <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain) NSMutableArray *titleNodeWrappersByLetter;
@property (nonatomic, retain) UILocalizedIndexedCollation *collation;
- (void)configureSections;
@end

@implementation AllTunesViewController
@synthesize allTitleNodeWrappers,titleNodeWrappersByLetter,collation;


-(void) dumpCollation
{
	NSLog(@"collation is now  %@", self.collation);
	NSLog(@"sectionIndexTitles %@", [self.collation sectionIndexTitles]);
	NSLog(@"sectionTitles %@",[self.collation sectionIndexTitles]);
}

-(void) dumpTitleNodeWrappers
{

	for (NSUInteger index = 0; index < [self.titleNodeWrappersByLetter count]; index++)
	{
		NSLog(@"Section %d",index);
		NSArray *titlenodewrappers = [self.titleNodeWrappersByLetter objectAtIndex:index];
		for (NSUInteger i=0; i<[titlenodewrappers count]; i++)
		{
			TitleNodeWrapper *tnw = [titlenodewrappers objectAtIndex:i];
			NSLog(@"        %@ %@",tnw.titleName,tnw.titleNode);
		}
	}
}



#pragma mark Overridden UIViewController Methods
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}


- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
}
#pragma mark -
#pragma mark Set the data array and configure the section data
-(id) initWithArray:(NSArray *) a andTitle:(NSString *)titl;
{
	self=[super init];
	if (self)
	{
		allTitleNames = [a retain];
		navTitle = [titl retain];
	}
	return self;
}

-(void) configureSections
{
	
	/*
	 Create an array of TitleNode wrappers
	 */
	allTitleNodeWrappers = [[NSMutableArray alloc] initWithCapacity:[allTitleNames count]];
	
	for (Product *oneTitleName in allTitleNames) 
	{		
		
		
		
		// For this example, the time zone itself isn't needed.
		TitleNodeWrapper *titleNodeWrapper = [[TitleNodeWrapper alloc] initWithTitle:oneTitleName.name];
		[allTitleNodeWrappers addObject:titleNodeWrapper];
		[titleNodeWrapper release];
	}
	
	
	self.collation = [UILocalizedIndexedCollation currentCollation];
	
	
//	NSLog(@"collation at top is  %@", self.collation);
//	[self dumpCollation];
	
	
	// Now that all the data's in place, each section array needs to be sorted.
	
	NSUInteger sectionTitlesCount = [[self.collation  sectionTitles] count];
	
	NSMutableArray *newSectionsArray = [[NSMutableArray alloc] initWithCapacity:sectionTitlesCount];
	
	// Set up the sections array: elements are mutable arrays that will contain the object for that section.
	
	for (NSUInteger index = 0; index < sectionTitlesCount; index++) {
		
		NSMutableArray *array = [[NSMutableArray alloc] init];
		
		[newSectionsArray addObject:array];
		
		[array release];
		
	}
	
	
	for (TitleNodeWrapper *tnw in allTitleNodeWrappers)
	{
		NSInteger sectionNumber = [self.collation sectionForObject:tnw collationStringSelector:@selector(titleName)];
		
		// put this in the correct bucket in the sections array of arrays
		
		NSMutableArray *sections = [newSectionsArray objectAtIndex:sectionNumber];
		[sections addObject:tnw];
	}
	
	for (NSUInteger index = 0; index < sectionTitlesCount; index++)
	{
		NSArray *titlenodewrappers = [newSectionsArray objectAtIndex:index];
		//////
		///////////////  IT IS THE getName Selector which prevents this from moving out of the same text file that TitleNode is defined in /////////
		NSArray *na = [collation sortedArrayFromArray:titlenodewrappers collationStringSelector:@selector(titleName)];
		// Replace the existing array with the sorted array.
		[newSectionsArray replaceObjectAtIndex:index withObject:na];
		
	}
	
	
	self.titleNodeWrappersByLetter = newSectionsArray;	
//	[self dumpCollation];
}
//allTitleNodeWrappers
- (void)setAllTitleNodeWrappers :(NSMutableArray *)newDataArray {
	if (newDataArray != allTitleNodeWrappers) {
		[allTitleNodeWrappers release];
		allTitleNodeWrappers = [newDataArray retain];
	}
	if (allTitleNodeWrappers == nil) {
		self.titleNodeWrappersByLetter = nil;
	}
	else {
		[self configureSections];
	}
}
- (void) loadView
{
	
	[self configureSections];
	
	// now build the ui
	
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
                                                         style: UITableViewStylePlain]
                            autorelease];
	
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
    tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]

//	// this will appear as the title in the navigation bar
//	CGRect frame = CGRectMake(0, 0, 300, 44);
//	UILabel *label = [[[UILabel alloc] initWithFrame:frame] autorelease];
//	label.backgroundColor = [UIColor clearColor];
//	label.font = [UIFont boldSystemFontOfSize:20.0f];
//	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
//	label.textAlignment = UITextAlignmentCenter;
//	label.textColor = [UIColor whiteColor];
//	self.navigationItem.titleView = label;
//	label.text = self->navTitle;
//	self.navigationController.navigationBar.tintColor = 
//	[DataManager sharedInstance].appColor;
	
	self.navigationItem.title = navTitle;
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemDone
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
    
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
	[navTitle release];
	[allTitleNames release];
	[titleNodeWrappersByLetter release];
	[allTitleNodeWrappers release];
	[collation release]; // hey this is what the apple code does
    [super dealloc];
}



#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles from the collation.
 */

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return [[self.collation sectionTitles] objectAtIndex:section];
}


- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView {
    return [self.collation sectionIndexTitles];
}


- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {
	// juST map this staight so even the missing guys has a section header
//	NSLog (@"sectionForSectionIndexTitle:%@ atIndex:%d",title,index);
//	NSLog (@"sectionForSectionIndexTitle: coolation %@",self.collation);
//	[self dumpCollation];
	NSInteger ret = [self.collation sectionForSectionIndexTitleAtIndex:index];
	
//	NSLog (@"returns %d", ret);
	
	return ret;
}


- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	// The number of sections is the same as the number of titles in the collation.
    return [[collation sectionTitles] count];
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
	
	
	if (section < [ self.titleNodeWrappersByLetter count])
	{
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		NSArray *titleNodeWrappers = [ self.titleNodeWrappersByLetter objectAtIndex:section];
		
		if (row <[titleNodeWrappers count])
		{
			TitleNodeWrapper *tnw = [titleNodeWrappers objectAtIndex: row];
			TitleNode *tn = tnw.titleNode;
			if (!tn)
			{
				NSLog(@"All tunes view controller cant find TitleNode for wrapper %@ item at row %d", tnw.titleName, row);
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
	
   	NSArray *titleNodes = [ self.titleNodeWrappersByLetter objectAtIndex:section];
	
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
	NSArray *titleNodeWrappers = [self.titleNodeWrappersByLetter objectAtIndex:idxPath.section];
	
	if (idxPath.row <[titleNodeWrappers count])
	{
		TitleNodeWrapper *tnw = [titleNodeWrappers objectAtIndex: idxPath.row];
		TitleNode *tn = tnw.titleNode;
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
				
				//NSLog (@"=>selected webview  %@",url);
				
				OneTuneViewController *wvc = [[[OneTuneViewController alloc]
											   initWithURL: docURL andWithTitle: tn.title andWithShortPath: path andWithBackLabel:@"archive"]
											  autorelease];
				wvc.title = tn.title  ;
			
			UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
				
				[self presentModalViewController:nav animated: YES];
				//[self.navigationController pushViewController: wvc animated:YES];
				
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
