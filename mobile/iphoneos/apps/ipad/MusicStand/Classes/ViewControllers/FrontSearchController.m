//
//  FrontSearchController.m
//  MusicStand
//
//  Created by bill donner on 11/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//



#import "DataStore.h"
#import "DataManager.h"
#import "OneTuneViewController.h"
#import "Product.h"
#import "FrontSearchController.h"
#import "AllSetListsController.h"
#import "SettingsViewController.h"
#import "TitleNode.h"


@implementation FrontSearchController

@synthesize listContent, filteredListContent, savedSearchTerm, savedScopeButtonIndex, searchWasActive;

#pragma mark - 
#pragma mark Lifecycle methods
- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
	NSLog(@"Cancel button from search bar");
	[self.tableView scrollRectToVisible:CGRectMake(0, 0, 1, 1) animated:YES];
	
}

-(void) loadView
{
	UISearchBar *searchbar7 = [[[UISearchBar alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 320.0f, 50.0f)] autorelease];
	searchbar7.frame = CGRectMake(0.0f, 0.0f, 320.0f, 50.0f);
	searchbar7.alpha = 1.000f;
	searchbar7.autoresizesSubviews = YES;
	searchbar7.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
	searchbar7.barStyle = UIBarStyleDefault;
	searchbar7.clearsContextBeforeDrawing = YES;
	searchbar7.clipsToBounds = NO;
	searchbar7.contentMode = UIViewContentModeRedraw;
	searchbar7.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	searchbar7.hidden = NO;
	searchbar7.multipleTouchEnabled = YES;
	searchbar7.opaque = YES;
	searchbar7.placeholder =@"Enter the name of the tune you are looking for...";
	searchbar7.scopeButtonTitles = [NSArray arrayWithObjects:
									@"Chords"
									, nil];
	searchbar7.showsBookmarkButton = NO;
	searchbar7.showsCancelButton = NO;
	searchbar7.showsScopeBar = NO;
	searchbar7.showsSearchResultsButton = NO;
	searchbar7.tag = 0;
	searchbar7.userInteractionEnabled = YES;
	
	UITableView *tableview3 = [[UITableView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 320.0f, 460.0f) style:UITableViewStylePlain];
	tableview3.frame = CGRectMake(0.0f, 0.0f, 320.0f, 460.0f);
	tableview3.allowsSelectionDuringEditing = NO;
	tableview3.alpha = 1.000f;
	tableview3.alwaysBounceHorizontal = NO;
	tableview3.alwaysBounceVertical = NO;
	tableview3.autoresizesSubviews = YES;
	tableview3.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
	tableview3.backgroundColor = [UIColor colorWithRed:1.000f green:1.000f blue:1.000f alpha:1.000f];
	tableview3.bounces = YES;
	tableview3.bouncesZoom = YES;
	tableview3.canCancelContentTouches = YES;
	tableview3.clearsContextBeforeDrawing = NO;
	tableview3.clipsToBounds = YES;
	tableview3.contentMode = UIViewContentModeScaleToFill;
	tableview3.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	tableview3.delaysContentTouches = YES;
	tableview3.directionalLockEnabled = NO;
	tableview3.hidden = NO;
	tableview3.indicatorStyle = UIScrollViewIndicatorStyleDefault;
	tableview3.maximumZoomScale = 1.000f;
	tableview3.minimumZoomScale = 1.000f;
	tableview3.multipleTouchEnabled = NO;
	tableview3.opaque = NO;
	tableview3.pagingEnabled = NO;
	tableview3.rowHeight = 44;
	tableview3.scrollEnabled = YES;
	tableview3.sectionFooterHeight = 27;
	tableview3.sectionHeaderHeight = 27;
	tableview3.sectionIndexMinimumDisplayRowCount = 0;
	tableview3.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tableview3.showsHorizontalScrollIndicator = YES;
	tableview3.showsVerticalScrollIndicator = YES;
	tableview3.tag = 0;
	tableview3.userInteractionEnabled = YES;
	
	
	
	[tableview3 addSubview:searchbar7];
	
	self.tableView = tableview3;
	
	searchDC = [[UISearchDisplayController alloc] initWithSearchBar:searchbar7 contentsController:self];
	
   // [self performSelector:@selector(setSearchDisplayController:) withObject:searchDC];
	
	searchbar7.delegate = self;
	tableview3.delegate = self;
	tableview3.dataSource = self;
	
	searchDC.delegate = self;
	searchDC.searchResultsDataSource = self.tableView.dataSource;
	searchDC.searchResultsDelegate = self.tableView.delegate;
}

- (void) pushToLists
{
	
	AllSetListsController *viewController3 = [[[AllSetListsController alloc] init] autorelease];	
	
	viewController3.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal
;
//	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: viewController3] autorelease];
//	
//	[self presentModalViewController:nav animated: YES];
	[self.navigationController pushViewController:viewController3 animated:YES];
}
	
- (void) pushToArchives
{
	
	SettingsViewController *viewController3 = [[[SettingsViewController alloc] init] autorelease];	
	
	viewController3.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal
	;
//	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: viewController3] autorelease];
//	
//	[self presentModalViewController:nav animated: YES];
	
	
	[self.navigationController pushViewController:viewController3 animated:YES];
}	

- (void)viewDidLoad
{
	self.navigationItem.title  = @"GigStand: Songs";
	self.navigationItem.leftBarButtonItem =
	[[UIBarButtonItem alloc] initWithTitle:@"Lists" style: UIBarButtonItemStylePlain target:self action:@selector(pushToLists)];
	
	self.navigationItem.rightBarButtonItem =
	[[UIBarButtonItem alloc] initWithTitle:@"Archives" style: UIBarButtonItemStylePlain target:self action:@selector(pushToArchives)];
	
	// create a filtered list that will contain products for the search results table.
	self.filteredListContent = [NSMutableArray arrayWithCapacity:[self.listContent count]];
	
	// restore search settings if they were saved in didReceiveMemoryWarning.
    if (self.savedSearchTerm)
	{
        [self.searchDisplayController setActive:self.searchWasActive];
        [self.searchDisplayController.searchBar setSelectedScopeButtonIndex:self.savedScopeButtonIndex];
        [self.searchDisplayController.searchBar setText:savedSearchTerm];
        
        self.savedSearchTerm = nil;
    }
	
	[self.tableView reloadData];
	self.tableView.scrollEnabled = YES;
	
	
}

- (void)viewDidUnload
{
	self.filteredListContent = nil;
}

-(void) viewWillAppear:(BOOL)animated
{   // its really bad form to do this, shud use notifcation center of some sort
	[super viewWillAppear:animated];
	self.listContent = [DataManager sharedInstance].allTitles;
	// removed reloaddata
	
	[self.tableView reloadData];
	[self.tableView scrollRectToVisible:CGRectMake(0, 0, 1, 1) animated:animated];

}	
- (void)viewDidDisappear:(BOOL)animated
{
    // save the state of the search UI so that it can be restored if the view is re-created
    self.searchWasActive = [self.searchDisplayController isActive];
    self.savedSearchTerm = [self.searchDisplayController.searchBar text];
    self.savedScopeButtonIndex = [self.searchDisplayController.searchBar selectedScopeButtonIndex];
}

- (void)dealloc
{
	[searchDC release];
	[filteredListContent release];
	
	[super dealloc];
}


#pragma mark -
#pragma mark UITableView data source and delegate methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	/*
	 If the requesting table view is the search display controller's table view, return the count of
     the filtered list, otherwise return the count of the main list.
	 */
	if (tableView == self.searchDisplayController.searchResultsTableView)
	{
        return [self.filteredListContent count];
    }
	else
	{
        return [self.listContent count];
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	static NSString *kCellID = @"cellID";
	
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:kCellID];
	if (cell == nil)
	{
		cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:kCellID] autorelease];
		cell.accessoryType = UITableViewCellAccessoryNone;
	}
	
	/*
	 If the requesting table view is the search display controller's table view, configure the cell using the filtered content, otherwise use the main list.
	 */
	Product *product = nil;
	if (tableView == self.searchDisplayController.searchResultsTableView)
	{
      product = [self.filteredListContent objectAtIndex:indexPath.row];
   }
else
{
        product = [self.listContent objectAtIndex:indexPath.row];
 }
	
	// look it up again
	
	TitleNode *tn2 = [[DataManager sharedInstance].titlesDictionary objectForKey:product.name ];
	if (!tn2)
	{
		NSLog(@"cant find TitleNode2for %@", product.name);
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
	return cell;
}
- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	if (idxPath.row == 0) return 44.0f; else
    return 60.0f;
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	/*
	 If the requesting table view is the search display controller's table view, configure the next view controller using the filtered content, otherwise use the main list.
	 */
	Product *product = nil;
	if (tabView == self.searchDisplayController.searchResultsTableView)
	{
        product = [self.filteredListContent objectAtIndex:idxPath.row];
    }
	else
	{
        product = [self.listContent objectAtIndex:idxPath.row];
    }

	//			{
	
	NSString *path = @"shortpathhere";
	NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],path];
	NSURL    *docURL = [NSURL fileURLWithPath: url
								  isDirectory: NO];
	
	
	
	//NSLog (@"=>search webview  %@",url);
	
	OneTuneViewController *wvc = [[[OneTuneViewController alloc]
								   initWithURL: docURL andWithTitle:product.name // tn.title 
								   andWithShortPath: path andWithBackLabel:@"search"]
								  autorelease];

	
	//UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
	
	//[self presentModalViewController:nav animated: YES];
	[self.navigationController pushViewController:wvc animated:YES];
	//		
	//				break;
	//			}
	//	}
}


#pragma mark -
#pragma mark Content Filtering

- (void)filterContentForSearchText:(NSString*)searchText scope:(NSString*)scope
{
	/*
	 Update the filtered array based on the search text and scope.
	 */
	
	[self.filteredListContent removeAllObjects]; // First clear the filtered array.
	
	/*
	 Search the main list for products whose type matches the scope (if selected) and whose name matches searchText; add items that match to the filtered array.
	 */
	for (Product *product in listContent)
	{
		if ([scope isEqualToString:@"All"] || [product.type isEqualToString:scope])
		{
			NSComparisonResult result = [product.name compare:searchText options:(NSCaseInsensitiveSearch|NSDiacriticInsensitiveSearch) range:NSMakeRange(0, [searchText length])];
            if (result == NSOrderedSame)
			{
				[self.filteredListContent addObject:product];
            }
		}
	}
}


#pragma mark -
#pragma mark UISearchDisplayController Delegate Methods

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    [self filterContentForSearchText:searchString scope:
	 [[self.searchDisplayController.searchBar scopeButtonTitles] objectAtIndex:[self.searchDisplayController.searchBar selectedScopeButtonIndex]]];
    
    // Return YES to cause the search result table view to be reloaded.
    return YES;
}


- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchScope:(NSInteger)searchOption
{
    [self filterContentForSearchText:[self.searchDisplayController.searchBar text] scope:
	 [[self.searchDisplayController.searchBar scopeButtonTitles] objectAtIndex:searchOption]];
    
    // Return YES to cause the search result table view to be reloaded.
    return YES;
}
- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
	return YES;
}

@end

