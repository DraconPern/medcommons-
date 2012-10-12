//
//  AllTunesViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "SearchableTunesViewController.h"
#import "DataManager.h"
#import "ArchiveInfoController.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"
#import "TunesManager.h"

#pragma mark SEARCH TABLE methods

@implementation SearchResultsTableView
-(id) initWithFrame:(CGRect)oframe style:(UITableViewStyle)ostyle controller:(SearchableTunesViewController *)ocontroller;
{
	self=[super initWithFrame:oframe style:ostyle];
	if (self)
	{
		self->thisController = ocontroller;
	}
	return self;
}

#pragma mark UITableViewDataSource Methods



- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	// The number of sections is just 1 in this view
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"ZipCell1";
	//NSUInteger section = idxPath.section;
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
	
	
	if (row < [ self->thisController.searchResults count])
	{
	
		
			NSString *tune = [self->thisController.searchResults objectAtIndex: row];   //// NOTE THIS IS DIFFERENT IN SEARCH
	
		
		cell.textLabel.text = tune;		
		cell.detailTextLabel.text = [DataManager newBlurb:tune] ;
//		}
//		else cell = nil;
	}
	else
		cell = nil;
	
	
    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	
	
    return [self->thisController.searchResults count];
}
//
//@end
//
//@implementation SearchResultsTableViewDelegate


#pragma mark UITableViewDelegate Methods

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return [DataManager standardRowSize];
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	if (idxPath.row <[self->thisController.searchResults count])
	{
		NSString *tune  = [self->thisController.searchResults objectAtIndex: idxPath.row];
		TuneInfo *tn = [TunesManager tuneInfo:tune];
		
		//each dict has different entries, just get the first 
		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title]) // only executed for the first variant
			
				
			{			
			
				[self->thisController presentModalViewController:[[DataManager allocOneTuneViewController:
																   [[DataManager newLongPath:ii.filePath forArchive:ii.archive] autorelease]	
													title:[tn title] 
											       items:self->thisController.allTitleNames] autorelease] animated: YES];
				
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


#pragma mark MAIN TABLE  Methods

@implementation MainTableView


-(id) initWithFrame:(CGRect)oframe style:(UITableViewStyle)ostyle controller:(SearchableTunesViewController *)ocontroller;
{
	self=[super initWithFrame:oframe style:ostyle];
	if (self)
	{
		self->thisController = ocontroller;
	}
	return self;
}
//
//
//@end
//
//@implementation MainTableViewDataSource 

#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles from the collation.
 */

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	NSRange range;
	range.location = section; // use this as an index 
	range.length = 1;
	return [@"ABCDEFGHIJKLMNOPQRSTUVWXYZ#" substringWithRange:range];
	
	
}

-(NSInteger ) sectionNumberForTitle:(NSString *) title;
{
	
	NSInteger sectionNumber = [@"ABCDEFGHIJKLMNOPQRSTUVWXYZ#" rangeOfString:[title  substringToIndex:1]].location;
	if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;	
	return sectionNumber;
}
- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView {
	
	
	return [NSArray arrayWithObjects: @"A",
						  @"B",@"C",@"D",@"E",@"F",@"G",@"H",@"I",@"J",@"K",@"L",@"M",@"N",@"O",@"P",@"Q",@"R",@"S",@"T",@"U",@"V",@"W",@"Y",@"Z",@"#",nil];
	
}


- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {

	NSInteger ret = index; //[self->thisController.collation sectionForSectionIndexTitleAtIndex:index];
	
	//	NSLog (@"returns %d", ret);
	
	return ret;
}


- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	// The number of sections is the same as the number of titles in the collation.
    return 27;}

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
	
	
	if (section < [ self->thisController.tunesByLetter count])
	{
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		NSArray *tunes = [ self->thisController.tunesByLetter objectAtIndex:section];
		
		if (row <[tunes count])
		{
			NSString *tune = [tunes objectAtIndex: row];
			TuneInfo *tn = [TunesManager tuneInfo:tune];
			
			cell.textLabel.text = tn.title;			
		    cell.detailTextLabel.text = [DataManager newBlurb:tn.title] ;
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
	
   	NSArray *tunes = [ self->thisController.tunesByLetter objectAtIndex:section];
	
    return [tunes count];
}
//
//
//@end
//@implementation MainTableViewDelegate
#pragma mark UITableViewDelegate Methods

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return [DataManager standardRowSize];
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	NSArray *tunes = [self->thisController.tunesByLetter objectAtIndex:idxPath.section];
	
	if (idxPath.row <[tunes count])
	{
		NSString *tune = [tunes objectAtIndex: idxPath.row];
		TuneInfo *tn = [TunesManager tuneInfo:tune];
		//each dict has different entries, just get the first 
		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title]) // only executed for the first variant
			
				
			{
				[self->thisController presentModalViewController:[[DataManager allocOneTuneViewController:
																   [[DataManager newLongPath:ii.filePath forArchive:ii.archive] autorelease] title:[tn title] 
									   items:self->thisController.allTitleNames] autorelease] animated: YES];
				
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


#pragma mark -
#pragma mark MAIN CONTROLLER SearchableTunesViewController
#pragma mark -
@interface SearchableTunesViewController ()
-(NSInteger ) sectionNumberForTitle:(NSString *) title;
@end
@implementation SearchableTunesViewController
@synthesize tunesByLetter,searchResultsOverlayView,searchResults,allTitleNames;


#pragma mark -
#pragma mark Support of all sorts

-(NSInteger ) sectionNumberForTitle:(NSString *) title;
{
	
	NSInteger sectionNumber = [@"ABCDEFGHIJKLMNOPQRSTUVWXYZ#" rangeOfString:[title  substringToIndex:1]].location;
	if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;	
	return sectionNumber;
}
-(void) archiveInfoPressed;
{

	{
		ArchiveInfoController *zvc = [[[ArchiveInfoController alloc] initWithArchive:self->archive  ]	autorelease];
		
		zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
		
		[self presentModalViewController:nav animated: YES];
	}
}
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
	//[self.navigationController popViewControllerAnimated:YES];
}





-(id) initWithArray:(NSArray *) a andTitle:(NSString *)titl andArchive:(NSString *)archiv;
{
	//NSLog (@"STV initWithArray");
	self=[super init];
	if (self)
	{
		allTitleNames = [a retain];
		navTitle = [titl retain];
		archive = [archiv retain];
	}
	return self;
}


-(NSArray *)  allocSearchFor: (NSString *)match;
{
	// this isnt very efficient it is pure brute force, it would be better to keep an intermediary list which was winnowed
	NSMutableArray *results = [[NSMutableArray alloc] init];
	NSUInteger matchlen = [match length];
	if (matchlen==0) return results;
	
	for (NSString *longname in allTitleNames) 
	{	
		if (matchlen <= [longname length]) 
		{
			NSString *trial =[longname substringToIndex:matchlen];
			if ([match caseInsensitiveCompare: trial ] == NSOrderedSame) 
			{
				[results addObject:longname]; // we want an array of strings
				
			}
		}
	}
	
	return results;
}
-(void) configureSections
{
    // Now that all the data's in place, each section array needs to be sorted.
	
	NSUInteger sectionTitlesCount = 27; 
	
	NSMutableArray *newSectionsArray = [[NSMutableArray alloc] initWithCapacity:sectionTitlesCount] ;
	
	// Set up the sections array: elements are mutable arrays that will contain the object for that section.
	
	for (NSUInteger index = 0; index < sectionTitlesCount; index++) {
		
		NSMutableArray *array = [[NSMutableArray alloc] init];
		
		[newSectionsArray addObject:array];
		
		[array release];
		
	}
	
	
	for (NSString *tune  in allTitleNames)
	{
	
		NSInteger sectionNumber = [self sectionNumberForTitle: tune];
		
		// put this in the correct bucket in the sections array of arrays
		
		NSMutableArray *sections = [newSectionsArray objectAtIndex:sectionNumber];
		[sections addObject:tune];
	}
	//sor each of these 
	for (NSUInteger index = 0; index < sectionTitlesCount; index++)
	{
		NSMutableArray *tunes = [newSectionsArray objectAtIndex:index];
		
		[tunes sortUsingSelector:@selector(compare:)];  
		// Replace the existing array with the sorted array.
		[newSectionsArray replaceObjectAtIndex:index withObject:tunes];
		//[na release]; //??
		
	}
	
	
	self.tunesByLetter = newSectionsArray;	
//[newSectionsArray release];
	
}



#pragma mark Overridden UIViewController Methods

- (UIView *) allocBuildSearchUI
{
	// returns a completely rebuilt page
	//
	
	CGRect pframe = self.parentViewController.view.bounds;
	//[[UIScreen mainScreen] applicationFrame];//self.parentViewController.view.bounds;
	
	UIView *outer = [[UIView alloc] initWithFrame:pframe]; 
	outer.backgroundColor = [UIColor whiteColor];
	
	//float fudge = [DataManager topFudgeFactor];
//	pframe.origin.y+=fudge;
//	pframe.size.height-=fudge;
	
	
	UISearchBar *searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(pframe.origin.x, pframe.origin.y, pframe.size.width, [DataManager searchBarHeight])];  // do not autorelease
	searchBar.alpha = 1.000f;
	searchBar.autoresizesSubviews = NO;
	//searchBar.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
	searchBar.barStyle = UIBarStyleDefault;
	searchBar.clearsContextBeforeDrawing = YES;
	searchBar.clipsToBounds = NO;
	//searchBar.contentMode = UIViewContentModeRedraw;
	searchBar.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	searchBar.hidden = NO;
	searchBar.multipleTouchEnabled = YES;
	searchBar.opaque = YES;
	searchBar.placeholder =@"Enter the name of the tune you are looking for...";
	searchBar.scopeButtonTitles = [NSArray arrayWithObjects:
								   @"Chords"
								   , nil];
	searchBar.showsBookmarkButton = NO;
	searchBar.showsCancelButton = NO;
	searchBar.showsScopeBar = NO;
	searchBar.showsSearchResultsButton = NO;
	searchBar.tag = 0;
	searchBar.userInteractionEnabled = YES;
	
	searchBar.delegate = self;
	
	// the main table
	
	pframe.size.height -= (0.0f+40.0f); // very confused, played with this until z and then # showed up
	pframe.origin.y +=[DataManager searchBarHeight]-[DataManager statusBarHeight];
	
	
    MainTableView *mainTableView = [[[MainTableView alloc] initWithFrame: pframe
																   style: UITableViewStylePlain controller:self] autorelease];
	
	mainTableView.backgroundColor = [UIColor whiteColor];
	mainTableView.opaque = YES;
	mainTableView.backgroundView = nil;
    mainTableView.dataSource = mainTableView;
    mainTableView.delegate = mainTableView;
    mainTableView.separatorColor = [UIColor lightGrayColor];
    mainTableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    mainTableView.tableHeaderView = nil; 
	
	// the overlay, with search results has a table of its own built right in
	
	CGRect oframe =  CGRectMake(0,[DataManager searchBarHeight],self.parentViewController.view.bounds.size.width,self.parentViewController.view.bounds.size.height-[DataManager statusBarHeight]
								-[DataManager navBarHeight]-[DataManager searchBarHeight]);
	
	self->searchResultsOverlayView = [[UIView alloc]initWithFrame:oframe];
	self->searchResultsOverlayView.backgroundColor=[UIColor blackColor];
    self->searchResultsOverlayView.alpha = 0;
	
	oframe = CGRectMake(0,0,self.parentViewController.view.bounds.size.width,self.parentViewController.view.bounds.size.height-[DataManager statusBarHeight]
						-[DataManager navBarHeight]-[DataManager searchBarHeight]);
	SearchResultsTableView *oView = [[[SearchResultsTableView alloc] initWithFrame: oframe
																			 style: UITableViewStylePlain controller:self] autorelease];
	
	
    oView.dataSource = oView;
    oView.delegate = oView;
    oView.separatorColor = [UIColor lightGrayColor];
    oView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    oView.tableHeaderView = nil; 
	
	[self->searchResultsOverlayView addSubview:oView];
	
	// do not add the overlay in here yet, only when we have search results
	
	self.navigationItem.titleView = [[DataManager allocTitleView:navTitle] autorelease];
	
    [outer addSubview:mainTableView];
	[outer addSubview:searchBar];
	self->tableview = mainTableView;
	self->searchbarview = searchBar;
	self->searchresultstableview = oView;
	return outer;
}


- (void) loadView
{
	//NSLog (@"STV loadView");
	[self configureSections];
    self.view = [self allocBuildSearchUI];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
	
//	NSLog (@"STV viewDidAppear");
    [super viewDidAppear: animated];
	self->searchbarview.hidden = NO;
    [self->tableview flashScrollIndicators];
}

- (void) viewDidLoad
{
	
//	NSLog (@"STV viewDidLoad");
    [super viewDidLoad];
	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemDone
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemOrganize
											  target:self 
											  action:@selector(archiveInfoPressed)] autorelease];
	
	
}

- (void) viewWillAppear: (BOOL) animated
{
	//NSLog (@"STV viewWillAppear");
    [super viewWillAppear: animated];
	
	//see if this will keep it in place
		//self->searchbarview.hidden = NO;
		//[self.view addSubview:self->searchbarview];
    UITableView *tabView = (UITableView *) self->tableview;
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
	[tabView reloadData];
}

- (void) viewWillDisappear: (BOOL) animated
{
	
	//NSLog (@"STV viewWillDisappear");
    [super viewWillDisappear: animated];
	
}


- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	
	//NSLog (@"STV didRotateFromInterfaceOrientation %d",(UIInterfaceOrientation) fromOrient);
    self.view = [self allocBuildSearchUI] ; // rebuild whole UI
}
#pragma mark Overridden NSObject Methods
-(void) didReceiveMemoryWarning
{
	
	NSLog (@"STV didReceiveMemoryWarning");
	// release these views, presumably we are coming back in with another loadview and these will get recreated
	[self->tableview release];
	self->tableview = nil;
	[self->searchbarview release];
	self->searchbarview = nil;
	[self->searchresultstableview release];
	self->searchresultstableview=nil;
	[super didReceiveMemoryWarning];
}
-(void) viewDidUnload
{
	//NSLog (@"STV viewDidUnload");
	// views already released 
	[super viewDidUnload];

	self->tableview = nil;
	self->searchbarview = nil;
	self->searchresultstableview=nil;
	
}
	
- (void) dealloc
{
	
	//NSLog (@"STV dealloc");
	
	[self->tableview release];
	[self->searchbarview release];
	[self->searchresultstableview release];
	
	[navTitle release];
	[allTitleNames release];
	[tunesByLetter release];
	
    [super dealloc];
}


#pragma mark -
#pragma mark UISearchBarDelegate Methods

- (void)searchBar:(UISearchBar *)searchBar
    textDidChange:(NSString *)searchText {
	// We don't want to do anything until the user clicks 
	// the 'Search' button.
	// If you wanted to display results as the user types 
	// you would do that here.
	//   NSArray *results = [SomeService doSearch:searchBar.text];
	
	if (self->searchResults) [self->searchResults release];
	
	// [self searchBar:searchBar activate:NO];
	
//	NSLog (@"Now searching for %@", searchBar.text);
	
	self->searchResults = [self allocSearchFor: searchBar.text];  // allocSearchFor 
	
//	NSLog (@"Got %d results..",[self->searchResults count]);
	
	
	//
	//    [self.tableData removeAllObjects];
	//    [self.tableData addObjectsFromArray:results];
	
	
	[self->searchresultstableview reloadData];
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    // searchBarTextDidBeginEditing is called whenever 
    // focus is given to the UISearchBar
    // call our activate method so that we can do some 
    // additional things when the UISearchBar shows.
    [self searchBar:searchBar activate:YES];
}

- (void)searchBarTextDidEndEditing:(UISearchBar *)searchBar {
    // searchBarTextDidEndEditing is fired whenever the 
    // UISearchBar loses focus
    // We don't need to do anything here.
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
    // Clear the search text
    // Deactivate the UISearchBar
    searchBar.text=@"";
    [self searchBar:searchBar activate:NO];
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    // Do the search and show the results in tableview
    // Deactivate the UISearchBar
	
    // You'll probably want to do this on another thread
    // SomeService is just a dummy class representing some 
    // api that you are using to do the search
	
	//	//   NSArray *results = [SomeService doSearch:searchBar.text];
	//	if (self->searchResults) [self->searchResults release];
	//	
	//    [self searchBar:searchBar activate:NO];
	//	
	//	NSLog (@"Now searching for %@", searchBar.text);
	//	
	//	self->searchResults = [self allocSearchFor: searchBar.text];
	//	
	//	NSLog (@"Got %d results..",[self->searchResults count]);
	//	
	//	
	//	//
	//	//    [self.tableData removeAllObjects];
	//	//    [self.tableData addObjectsFromArray:results];
	//	
	//	
	//    [self->tableview reloadData];
}

// We call this when we want to activate/deactivate the UISearchBar
// Depending on active (YES/NO) we disable/enable selection and 
// scrolling on the UITableView
// Show/Hide the UISearchBar Cancel button
// Fade the screen In/Out with the searchResultsOverlayView and 
// simple Animations

- (void)searchBar:(UISearchBar *)searchBar activate:(BOOL) active{	
	self->tableview.allowsSelection = !active;
    self->tableview.scrollEnabled = !active;
    if (!active) {
        [searchResultsOverlayView removeFromSuperview];
        [searchBar resignFirstResponder];
    } else {
        self.searchResultsOverlayView.alpha = 0;
        [self.view addSubview:self->searchResultsOverlayView];
		
        [UIView beginAnimations:@"FadeIn" context:nil];
        [UIView setAnimationDuration:0.5f];
        self.searchResultsOverlayView.alpha = 0.8f;
        [UIView commitAnimations];
		
        // probably not needed if you have a details view since you 
        // will go there on selection
        NSIndexPath *selected = [self->tableview
								 indexPathForSelectedRow];
        if (selected) {
            [self->tableview deselectRowAtIndexPath:selected 
										   animated:NO];
        }
    }
    [searchBar setShowsCancelButton:active animated:YES];
}


@end
