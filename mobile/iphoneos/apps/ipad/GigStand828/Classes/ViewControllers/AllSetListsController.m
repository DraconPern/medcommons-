//
//  AllSetListsController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AllSetListsController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "SetListsManager.h"
#import "ModalAlert.h"

#import <MobileCoreServices/MobileCoreServices.h>

enum
{
    SECTION_COUNT = 1  // MUST be kept in display order ...
	
    //
	
};

@interface AllSetListsController () <UITableViewDataSource, UITableViewDelegate>

-(void) enterEditMode;

-(void) leaveEditMode;
@end

@implementation  AllSetListsController

-(NSUInteger ) itemCount;
{
	return [self->listItems count];
}


#pragma mark Overridden UIViewController Methods

-(void) reloadListItems : (NSArray *) newItems
{
	[self->listItems release];
	self->listItems = [newItems retain];
	
}


- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void) enterEditMode;
{
	[self->tableView deselectRowAtIndexPath:[self->tableView indexPathForSelectedRow] animated:YES];
	[self->tableView setEditing:YES animated: YES];
	[self setBarButtonItems];
}
-(void) leaveEditMode;
{
	[self->tableView setEditing:NO animated:YES];
	[self setBarButtonItems];
}



-(id) initWithList: (NSArray *)arr name: (NSString *) namxe   canEdit:(BOOL)cane canReorder:(BOOL)canr tag:(NSUInteger)tagg;
{
	// this initializer does not read the plist but instead takes the list of actual items, it is used by the AllSetListsController
	
	self = [super init];
	if (self)
	{
		self->listItems = [arr retain]; //locallistItems;
		self->name = [namxe  retain];
		self->canEdit = cane;
		self->canReorder = canr;
		self->tag = tagg;
		self->plist = nil;
		
		
		NSLog(@"GeneralList.%@ special items %d canEdit %d canReorder %d name %@ ", self,[self->listItems count],self->canEdit,self->canReorder,self->name);
	}
	//[locallistItems release];
	return self;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView 
		   editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	
	if (self->canEdit)		{
        return UITableViewCellEditingStyleDelete;
		
    } else {
		
        return UITableViewCellEditingStyleNone;
		
    }
	
}


#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	
	[logoView_ release];
	[self->plist release];
	[self->name release];
	[super dealloc];
}


#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles
 */



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




//
//
//
//
//
//
//
//



#pragma mark Public Instance Methods
#pragma mark Private Instance Methods

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

-(void) quickTouchup
{
	
	[self->tableView reloadData];

}

-(void) addPressed;
{	
	
	// Solicit text respons
	NSString *answer = [ModalAlert ask:@"New list name?" withTextPrompt:@"Name"];
	
	if ([answer length]<1) return;
	
	BOOL unique = YES;
	
	// check for duplicates
	for (NSString *s in self->listItems)
	{
		if ([s isEqualToString: answer])
		{
			unique = NO;	
			break;
		}
	}
	if (unique == YES)
	{
		
		[SetListsManager rewriteTuneList:[NSArray array]  toPropertyList:answer];  // empty plist is fine
		
		[ModalAlert say:@"We created a list named %@.", answer];
		
		[NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(quickTouchup) userInfo:nil repeats:NO];
		
	}
	else {
		[ModalAlert say:@"Sorry, there is already a list with that name"];
	}
	
	
}
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}
-(void) actionPressed
{
	
	NSString *cancel = (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"Cancel":nil;
	
	self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Options", @"")
											  delegate:self
									 cancelButtonTitle:cancel
								destructiveButtonTitle:nil
									 otherButtonTitles:nil];
	
	[self->toass addButtonWithTitle:NSLocalizedString (@"Add Setlist", @"")];
	
	if (self->canEdit)	
		
		[self->toass addButtonWithTitle:NSLocalizedString (@"Edit", @"")];
	
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	[self->toass showFromBarButtonItem: self.navigationItem.rightBarButtonItem
							  animated: YES];
}
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//cancel is zero in this scheme
	
	if (actionSheet.tag == 1) // this is invoked from the upper left corner
	{
		// ipad is one lower because of the lack of cancel button on the actionsheet
		if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) buttonIndex++;
		if (buttonIndex==1)
			
		{
			[self addPressed];            
		}
		
		if (buttonIndex==2)
			
		{
			[self setBarButtonItems];
			[self enterEditMode];            
		}
		
	}
}

-(void) setupLeftSideNavItems
{
	
	//	UIBarButtonItem *doneButton = [[[UIBarButtonItem alloc] 
	//									initWithTitle: @"Home" style: UIBarButtonItemStyleBordered
	//									target:self 
	//									action:@selector(donePressed)] autorelease];
	//	
	//	
	//	// place the toolbar into the navigation bar
	//	self.navigationItem.leftBarButtonItem =doneButton;
	
	self.navigationItem.hidesBackButton = NO; 
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc]
											   initWithBarButtonSystemItem:UIBarButtonSystemItemAction
											   target:self
											   action:@selector(actionPressed)] autorelease];
}
-(void) setBarButtonItems
{
	
	//BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
	
	if (self->canEdit)
	{
		//when editing, display the done button
		//when not, only display edit if listItems exist
		if (self->tableView.isEditing){
			self.navigationItem.rightBarButtonItem =
			[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(leaveEditMode)];
		}
		else {
			
			self.navigationItem.rightBarButtonItem = 
			[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(actionPressed)] 
			;
		}
	}
	
}

#pragma mark Overridden UIViewController Methods

//
//- (void) viewDidAppear: (BOOL) animated
//{
//	[super viewDidAppear: animated];
//	
//	[(UITableView *) self.view flashScrollIndicators];
//}



- (void) viewWillAppear: (BOOL) animated
{
	[super viewWillAppear: animated];
	
	[self backsplash];	 	
	NSArray *a = [SetListsManager newSetlistsScan];
	[self reloadListItems: a];
	[a release];
	[self quickTouchup ];
//	UITableView *tabView = (UITableView *) self.view;
	
	
	[self->tableView reloadData];
	
	[self setBarButtonItems];
	
	NSIndexPath *idxPath = [self->tableView indexPathForSelectedRow];
	
	if (idxPath)
		[self->tableView deselectRowAtIndexPath: idxPath
							   animated: NO];
}


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView 
{NSLog (@"GeneralListViewController loadView");
	CGRect theframe = self.parentViewController.view.bounds;
	float fudge = [DataManager navBarHeight];
	theframe.origin.y+=fudge;
	theframe.size.height-=fudge;
	
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: theframe
														 style: UITableViewStylePlain]
							autorelease];
	
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [UIColor lightGrayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	
	self.navigationItem.titleView = [[DataManager allocTitleView:self->name] autorelease];
	[self setBarButtonItems];
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = tmpView;
	[self setBarButtonItems];
	[self setupLeftSideNavItems];
	
	self->tableView.backgroundColor = [UIColor lightGrayColor]; //[DataManager applicationColor];
	
	
	
	
	self.navigationItem.titleView = [[DataManager allocTitleView:@"Manage Setlists"] autorelease];	
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	//
	//	self.navigationItem.leftBarButtonItem =
	//	[[UIBarButtonItem alloc] initWithTitle:@"back" style: UIBarButtonItemStylePlain target:self action:@selector(donePressed)];
	
	self.navigationItem.hidesBackButton = NO;
	
	
}






- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}







#pragma mark Overridden NSObject Methods

- (id) init 
{
	
	
	NSArray *a = [SetListsManager newSetlistsScan];
	
	self = [self initWithList: a
						  name:@"Manage Your Lists"
					   canEdit: YES canReorder:YES tag:-1000];
	
	
	
	
	if (self)
	{
	}
	[a release];
	return self;
}



#pragma mark UITableViewDataSource Methods

-(void) tableView: (UITableView *)aTableView commitEditingStyle: (UITableViewCellEditingStyle)editingStyle
forRowAtIndexPath:(NSIndexPath *)indexPath 
{
	
	NSString *listname = [self->listItems objectAtIndex:indexPath.row];
	
	if (editingStyle == UITableViewCellEditingStyleDelete) {
		if ((![listname isEqualToString: @"recents"])
		&& (![listname isEqualToString: @"favorites"])
		)
	{
		// DELETE A SETLIST
		
		
		// actually delete this item from the listItems array
		[self->listItems removeObjectAtIndex:indexPath.row];	// make this persistent by actually deleting the particular plist
		NSString *path = [DataStore pathForTuneListWithName :listname];
		//Remove any file at the destination path
		NSError *moveError = nil;
		if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
			[[NSFileManager defaultManager] removeItemAtPath:path error:&moveError];
			if (moveError) {
		
				NSLog(@"file Error on setlist delete is %@",moveError);
			}
		}
		[self->tableView reloadData];
		[self setBarButtonItems];
	}
		else
		{
			UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"You can't delete that list" 
															message:@"It is built into GigStand"
														   delegate:self cancelButtonTitle:@"OK" otherButtonTitles: nil];
			[alert show];
			[alert release]; 
		}
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
	[self setBarButtonItems];
	
	
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
		
		cell.detailTextLabel.text =[NSString stringWithFormat:@"%d tunes", [SetListsManager itemCountForList:listName]];
		
	}
	else
		cell = nil;
	
	
	return cell;
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// since we are editing, lets just return without doing anything
	
	// section should always be 1
	//
	//	
	//	NSUInteger count = [self->listItems  count];
	//	
	//	if (idxPath.row < count)
	//	{
	//		
	//		
	//		NSString *filepath = [ self->listItems  objectAtIndex:idxPath.row];
	//		if (!filepath)
	//		{
	//			NSLog(@"no filepath found for item at row %d", idxPath.row);
	//			return;
	//		}
	//		
	//		TuneListViewController *wvc = [[[TuneListViewController alloc]
	//											 initWithPlist:filepath name:[NSString stringWithFormat: @"~%@",filepath]
	//											  edit:YES]
	//											autorelease];
	//		
	//		
	//		wvc.modalTransitionStyle = UIModalTransitionStyleCrossDissolve
	//		;
	////		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
	//		
	//	//	[self presentModalViewController:nav animated: YES];
	//		
	//		[self.navigationController pushViewController:wvc animated:YES];
	//		
	//	}
	
}

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return [DataManager standardRowSize];
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
}

@end
