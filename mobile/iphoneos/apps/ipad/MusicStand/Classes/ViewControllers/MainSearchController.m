
#import "DataStore.h"
#import "DataManager.h"
#import "OneTuneViewController.h"
#import "MainSearchController.h"
#import "Product.h"

@implementation MainSearchController

@synthesize listContent, filteredListContent, savedSearchTerm, savedScopeButtonIndex, searchWasActive;

#pragma mark - 
#pragma mark Lifecycle methods


- (void)viewDidLoad
{
	self.title = @"MusicStand: Search";
	
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

- (void)viewDidDisappear:(BOOL)animated
{
    // save the state of the search UI so that it can be restored if the view is re-created
    self.searchWasActive = [self.searchDisplayController isActive];
    self.savedSearchTerm = [self.searchDisplayController.searchBar text];
    self.savedScopeButtonIndex = [self.searchDisplayController.searchBar selectedScopeButtonIndex];
}

- (void)dealloc
{
	//[listContent release];  // not really allocated or deallocated, just bound to presetup storage in DataManager
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
		cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:kCellID] autorelease];
		cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
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
	
	cell.textLabel.text = product.name;
	return cell;
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
	
	
	//	NSArray *titleNodes = [[DataManager sharedInstance].titleSections objectAtIndex:idxPath.section];
	//	
	//	if (idxPath.row <[titleNodes count])
	//	{
	//		
	//		TitleNode *tn  = [titleNodes objectAtIndex: idxPath.row];
	//		if (!tn)
	//		{
	//			NSLog(@"no titleNodes found for item at row %d", idxPath.row);
	//			return;
	//		}
	//		//each dict has different entries, just get the first 
	//		for (NSString *path in tn.variants) // only executed for the first variant
	//			
	//			if ([[tn.variants objectForKey:path] unsignedIntValue] == 0) 
	//				
	//			{
	
	NSString *path = @"shortpathhere";
	NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],path];
	NSURL    *docURL = [NSURL fileURLWithPath: url
								  isDirectory: NO];
	
	
	
	NSLog (@"=>search webview  %@",url);
	
	OneTuneViewController *wvc = [[[OneTuneViewController alloc]
								   initWithURL: docURL andWithTitle:product.name // tn.title 
								   andWithShortPath: path andWithBackLabel:@"search"]
								  autorelease];
	wvc.title = @"Bertha" ;
	
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
	
	[self presentModalViewController:nav animated: YES];
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

