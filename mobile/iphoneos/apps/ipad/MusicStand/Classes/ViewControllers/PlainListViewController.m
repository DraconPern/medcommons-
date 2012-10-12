    //
//  PlainListViewController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "PlainListViewController.h"


#import "AppDelegate.h"
//#import "AsyncImageView.h"
//#import "DocumentsManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "DataManager.h"
#import "SettingsViewController.h"
#import "TitleNode.h"
#import "OneTuneViewController.h"
#import "DataStore.h"

@implementation PlainListViewController


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
											   andWithItems:self->listItems]
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


 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}


@end
