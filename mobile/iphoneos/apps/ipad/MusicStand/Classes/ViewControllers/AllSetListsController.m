//
//  AllSetListsController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AllSetListsController.h"
#import "SetListSingleViewController.h"
#import "DataStore.h"
#import "CreateNewSetListController.h"
#import "DataManager.h"
#import "StyleManager.h"
#import "AppDelegate.h"
#import "DataStore.h"


@implementation AllSetListsController


-(void) backsplash
{
	
	
    if (logoView_)
    { // remove previous
        [logoView_ removeFromSuperview];
        [logoView_ release];
        logoView_ = nil;
    }
	
    // if we have a logo make a backsplash
	
	
	
	logoView_ = [[UIImageView alloc]
				 initWithFrame: CGRectMake (0.0f, 0.0f, 200.0f, 200.0f)];
	
	logoView_.alpha = 0.10f;
	logoView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
								  UIViewAutoresizingFlexibleLeftMargin |
								  UIViewAutoresizingFlexibleRightMargin |
								  UIViewAutoresizingFlexibleTopMargin);
	logoView_.center = CGPointMake (self.view.frame.size.width / 2.0f,
									self.view.frame.size.height /2.0f);
	
	
	logoView_.image =  [UIImage imageNamed:@"MusicStand_512x512.png"];		
	[self.view addSubview: logoView_];
	
}

-(void) addPressed;
{	
	CreateNewSetListController *wvc = [[[CreateNewSetListController alloc]
										init] autorelease];
	///////////////////////////////////////////autorelease];
	
	
	wvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	wvc.modalPresentationStyle = UIModalPresentationFormSheet;
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
	nav.modalPresentationStyle = UIModalPresentationFormSheet;
	// its having a hard time when the controller up above is created dynamically nested modal animations
	[self presentModalViewController:nav animated: YES];
}
-(void) donePressed;
{
	//[self.parentViewController dismissModalViewControllerAnimated:YES];
	[self.navigationController popViewControllerAnimated:YES];
}

-(void) setupLeftSideNavItems:(BOOL)wantsAddButton
{
	
	// create a toolbar where we can place some buttons
	UIToolbar* toolbar;
	if (wantsAddButton)
	{
		toolbar = [[[UIToolbar alloc]
					initWithFrame:CGRectMake(0, 0, 100, 45)] autorelease];
	}
	else
	{
		toolbar = [[[UIToolbar alloc]
					initWithFrame:CGRectMake(0, 0, 50, 45)] autorelease];
	}
	//[toolbar setBarStyle: UIBarStyleBlackOpaque];
	
	// create an array for the buttons
	NSMutableArray* buttons = [[[NSMutableArray alloc] initWithCapacity:3] autorelease];
	// create a standard done action
	UIBarButtonItem *doneButton = [[[UIBarButtonItem alloc] 
									initWithTitle:@"Songs" style:UIBarButtonItemStyleBordered 
									target:self 
									action:@selector(donePressed)] autorelease];
	doneButton.style = UIBarButtonItemStyleBordered;
	[buttons addObject:doneButton];
	
	if (wantsAddButton)
	{
		// create a spacer between the buttons
		UIBarButtonItem *spacer = [[[UIBarButtonItem alloc]
									initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace
									target:nil
									action:nil] autorelease];
		[buttons addObject:spacer];
		
		// create a spacer between the buttons
		UIBarButtonItem *spacer2 = [[[UIBarButtonItem alloc]
									 initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace
									 target:nil
									 action:nil] autorelease];
		[buttons addObject:spacer2];
		
		// create a standard action button
		UIBarButtonItem *playButton = [[[UIBarButtonItem alloc]
										initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
										target:self
										action:@selector(addPressed)] autorelease];
		playButton.style = UIBarButtonItemStyleBordered;
		[buttons addObject:playButton];
		
		
		
	}
	// put the buttons in the toolbar and release them
	[toolbar setItems:buttons animated:NO];
	
	
	// place the toolbar into the navigation bar
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc]
											  initWithCustomView:toolbar] autorelease];
}


-(void) tableView: (UITableView *)aTableView commitEditingStyle: (UITableViewCellEditingStyle)editingStyle
forRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if (editingStyle == UITableViewCellEditingStyleDelete)
	{
		// DELETE A SETLIST
		
		NSString *listname = [self->listItems objectAtIndex:indexPath.row];
		
		// actually delete this item from the listItems array
		[self->listItems removeObjectAtIndex:indexPath.row];	// make this persistent by actually deleting the particular plist
		NSString *path = [DataStore pathForTuneListWithName :listname];
		//Remove any file at the destination path
		NSError *moveError = nil;
		if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
			[[NSFileManager defaultManager] removeItemAtPath:path error:&moveError];
			if (moveError) {
				NSError *fileError = [NSError errorWithDomain:NetworkRequestErrorDomain code:ASIFileManagementError 
													 userInfo:[NSDictionary 
															   dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unable to remove file at path '%@'",path],NSLocalizedDescriptionKey,moveError,NSUnderlyingErrorKey,nil]];
				
				NSLog(@"file Error on setlist delete is %@",fileError);
			}
		}
		[self->tableView reloadData];
		[super setBarButtonItems];
	}
}



// override to present a different and simpler display for each
-(void) tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *) oldPath toIndexPath:(NSIndexPath *) newPath
{
	//change the data as we move stuff around, -- thanks Erica Sadun
	
	NSString *path = [[self->listItems objectAtIndex:oldPath.row] retain];
	[self->listItems removeObjectAtIndex:oldPath.row];
	[self->listItems insertObject: path atIndex:newPath.row];
	[path release];
	[super setBarButtonItems];
	
	
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
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		
		NSString *listName = [ self->listItems objectAtIndex:row];
		
		cell.textLabel.text = listName;	
		
		// let's dig out the number of current entries so we can display this as detailed text
		
		cell.detailTextLabel.text =[NSString stringWithFormat:@"%d tunes", [[DataManager sharedInstance] itemCountForList:listName]];
		
	}
	else
		cell = nil;
	
	
	return cell;
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// section should always be 1
	
	
	NSUInteger count = [self->listItems  count];
	
	if (idxPath.row < count)
	{
		
		
		NSString *filepath = [ self->listItems  objectAtIndex:idxPath.row];
		if (!filepath)
		{
			NSLog(@"no filepath found for item at row %d", idxPath.row);
			return;
		}
		
		SetListSingleViewController *wvc = [[[SetListSingleViewController alloc]
											 initWithPlist:filepath name:[NSString stringWithFormat: @"GigStand: list: %@",filepath]]
											autorelease];
		
		
		wvc.modalTransitionStyle = UIModalTransitionStyleCrossDissolve
		;
//		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
		
	//	[self presentModalViewController:nav animated: YES];
		
		[self.navigationController pushViewController:wvc animated:YES];
		
	}
	
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
	cell.backgroundColor = [UIColor blackColor];
	cell.textLabel.textColor = [UIColor whiteColor];
	//cell.backgroundColor = [UIColor colorWithRed:1.000f green:1.000f blue:1.000f alpha:.700f];
}
-(NSArray *) newSetlistsScan
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			if (!(
				  [shortie isEqualToString:@"alltunes"] ||[shortie isEqualToString:@"archives"]))
				[alllists addObject:shortie];
			//NSLog (@"added setlist %@",shortie);
		}
	}
	return alllists;
}

-(void) viewWillAppear:(BOOL)animated
{
	NSArray *a = [self newSetlistsScan];
	
	[super reloadListItems: a];
	
	[super viewWillAppear:animated];
	
	[a release];
	
	
}

- (id) init 
{
	
	
	NSArray *a = [self newSetlistsScan];
	
	self = [super initWithList: a
						  name:@"GigStand: Lists"
					      canEdit: YES canReorder:YES tag:-1000];
	if (self)
	{
	}
	[a release];
	return self;
}


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	[super loadView];
	[self backsplash];	 
	BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
	
		[self setupLeftSideNavItems: settingsCanEdit];
		
	
	self->tableView.backgroundColor = [UIColor lightGrayColor]; //[DataManager sharedInstance].appColor;
	
}

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
	[logoView_ release];
    [super dealloc];
}


@end
