//
//  AllSetListsController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import "AllSetListsController.h"

#import "DataStore.h"
#import "DataManager.h"
#import "SetListsManager.h"
#import "ModalAlert.h"
#import "GigStandAppDelegate.h"

#import <MobileCoreServices/MobileCoreServices.h>

enum
{
    SECTION_COUNT = 1  // MUST be kept in display order ...

};

@interface AllSetListsController () <UITableViewDataSource, UITableViewDelegate>

-(void) enterEditMode;

-(void) leaveEditMode;
-(NSUInteger ) itemCount;
-(void) reloadListItems : (NSArray *) newItems;
-(void) setBarButtonItems;

- (void) repaintUI: (NSTimer *) timer;
-(void) invalidateTimer;

@end


@implementation  AllSetListsController

-(NSUInteger ) itemCount;
{
	return [self->listItems count];
}


#pragma mark Overridden UIViewController Methods

-(void) cancelPressed;
{
	[self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
}

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
	[self invalidateTimer]; 
	[self->tableView deselectRowAtIndexPath:[self->tableView indexPathForSelectedRow] animated:YES];
	[self->tableView setEditing:YES animated: YES];
	[self setBarButtonItems];
}
-(void) leaveEditMode;
{
	[self->tableView setEditing:NO animated:YES];
	[self setBarButtonItems];
	
	aTimer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(repaintUI:) userInfo:nil repeats:NO];
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView 
		   editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	
	if (self->canEdit)		{
		
        return UITableViewCellEditingStyleDelete;
		
    } else {
		
        return UITableViewCellEditingStyleNone;
		
    }
	
}


#pragma mark Overridden NSObject Methods];
- (void) dealloc
{
	
    [DataManager worldViewPulse];
	[self->logoView release];
	[self invalidateTimer];
	[self->aTimer release];

	[self->name release];
    [self->listItems release];
    [self->logoView release];
    [self->toass release];
 //   [self->tableView release];
    
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

#pragma mark Public Instance Methods
#pragma mark Private Instance Methods

//-(void) backsplash
//{
//	
//	
//    if (logoView)
//    { // remove previous
//        [logoView removeFromSuperview];
//        [logoView release];
//        logoView = nil;
//    }
//	
//    // if we have a logo make a backsplash
//	
//	
//	
//	logoView = [[UIImageView alloc]
//				 initWithFrame: CGRectMake (0.0f, 0.0f, 200.0f, 200.0f)];
//	
//	logoView.alpha = 0.10f;
//	logoView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
//								  UIViewAutoresizingFlexibleLeftMargin |
//								  UIViewAutoresizingFlexibleRightMargin |
//								  UIViewAutoresizingFlexibleTopMargin);
//	logoView.center = CGPointMake (self.view.frame.size.width / 2.0f,
//									self.view.frame.size.height /2.0f);
//	
//	
//	logoView.image =  [UIImage imageNamed:@"MusicStand_512x512.png"];		
//	[self.view addSubview: logoView];
//	
//}

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
		
		[SetListsManager insertList:answer]; // new list
		
		[ModalAlert say:@"We created a list named %@.", answer];
		
		[NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(quickTouchup) userInfo:nil repeats:NO];
		
	}
	else {
		[ModalAlert say:@"Sorry, there is already a list with that name"];
	}
	
	
}
-(void) donePressed;
{
	[self popOrNot];
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
	

	self.navigationItem.hidesBackButton = NO; 
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc]
											   initWithBarButtonSystemItem:UIBarButtonSystemItemAction
											   target:self
											   action:@selector(actionPressed)] autorelease];
}
-(void) setBarButtonItems
{
	//BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
	
	if (self->canEdit)
	{
		//when editing, display the done button
		//when not, only display edit if listItems exist
		if (self->tableView.isEditing){
			self.navigationItem.rightBarButtonItem =
			[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(leaveEditMode)] autorelease];
		}
		else {
			
			self.navigationItem.rightBarButtonItem = 
			[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(actionPressed)] autorelease];
			;
		}
	}
	
}

#pragma mark Overridden UIViewController Methods


-(void) invalidateTimer
{
	if (aTimer) 
	{
		[aTimer invalidate];
	//	[aTimer release];
		aTimer = nil;
	}
}


-(void) viewDidUnload
{
    // dealloc may never get called
    
	[self invalidateTimer];
    [DataManager worldViewPulse];
    [super viewDidUnload];
}

- (void) repaintUI: (NSTimer *) timer;
{
	if (aTimer == nil) return; // if already cloeared, then ignore this
	
	aTimer = nil; // not in timer
	
	[self->listItems release];
	self->listItems = [[SetListsManager makeSetlistsScan] retain]; //locallistItems;
	
	// while we are at it, lets monitor the inbox
	UITableView *tabView = self->tableView; //(UITableView *) self.view;
	[tabView reloadData];
	
	//aTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(repaintUI:) userInfo:nil repeats:NO];  //042511 - end the chatter
}


- (void) viewDidLoad
{
	[super viewDidLoad];
    
    if ([DataManager modalPopOversEnabled])
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"cancel" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(cancelPressed)] autorelease];
	
	aTimer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(repaintUI:) userInfo:nil repeats:NO];
}
- (void) viewWillAppear: (BOOL) animated
{
	[super viewWillAppear: animated];
	//
	//[self backsplash];	 	
	NSArray *a = [[SetListsManager makeSetlistsScan] retain];
	[self reloadListItems: a];
	[a release];
	[self quickTouchup ];
	[self->tableView reloadData];
	
	[self setBarButtonItems];
	
	NSIndexPath *idxPath = [self->tableView indexPathForSelectedRow];
	
	if (idxPath)
		[self->tableView deselectRowAtIndexPath: idxPath
							   animated: NO];
}


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView 
{
	NSLog (@"ASL loadView");
	
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
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = tmpView;
	[self setBarButtonItems];
	[self setupLeftSideNavItems];
	
	self->tableView.backgroundColor = [UIColor clearColor]; //[DataManager applicationColor];
	self.navigationItem.titleView = [DataManager makeTitleView:@"Manage Setlists"] ;		
		[self setColorForNavBar ];
	
	self.navigationItem.hidesBackButton = NO;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

#pragma mark Overridden NSObject Methods

- (id) init 
{
		self = [super init];
		if (self)
		{
			self->listItems = [[SetListsManager makeSetlistsScan] retain]; //locallistItems;
			self->name = @"Manage Lists";
			self->canEdit = YES;
			self->canReorder = YES;
			self->tag = -1000;
			NSLog(@"ASL %@ special items %d canEdit %d canReorder %d name %@ ", self,[self->listItems count],self->canEdit,self->canReorder,self->name);
		}
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
		[self->listItems removeObjectAtIndex:indexPath.row];
		// make this persistent by actually deleting the particular plist
		[SetListsManager deleteList:listname];		// repaint the UI
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
		
		cell.imageView.image =[DataManager makeThumbRS:[SetListsManager picSpecForList:listName] size:[DataManager standardThumbSize]];
		
	}
	else
		cell = nil;
	
	
	return cell;
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
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
