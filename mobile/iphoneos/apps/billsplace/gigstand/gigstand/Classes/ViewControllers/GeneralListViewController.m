//
//  GeneralListViewController.m
//  MusicStand
//
//  Created by bill donner on 10/18/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import "GeneralListViewController.h"
#import "DataManager.h"
#import "SettingsManager.h"
#import "GigStandAppDelegate.h"

#import <MobileCoreServices/MobileCoreServices.h>


#pragma mark -
#pragma mark Public Class GeneralListViewController
#pragma mark -
#pragma mark Internal Constants


//
// Table sections:
//


enum
{
    SECTION_COUNT = 1  // MUST be kept in display order ...
	
    //
	
};

@interface GeneralListViewController () <UITableViewDataSource, UITableViewDelegate>

-(void) enterEditMode;

-(void) leaveEditMode;
@end

@implementation  GeneralListViewController

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

-(void) setBarButtonItems
{
	
	BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
	if (self->canEdit)
	{
		//when editing, display the done button
		//when not, only display edit if listItems exist
		if (self->tableView.isEditing){
			self.navigationItem.rightBarButtonItem =
			[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(leaveEditMode)];
		}
		else {
			
			self.navigationItem.rightBarButtonItem = ([self->listItems count] && settingsCanEdit) ? 
			[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemEdit target:self action:@selector(enterEditMode)] 
			: nil;
		}
	}
	
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

-(void) tableView: (UITableView *)aTableView commitEditingStyle: (UITableViewCellEditingStyle)editingStyle
forRowAtIndexPath:(NSIndexPath *)indexPath 
{
	
	NSLog (@"GeneralListViewController expects tableView:commitEditingStyle:forRowAtIndexPath to be overridden");
}
- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView 
		   editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	
	if (self->canEdit)		{
        return UITableViewCellEditingStyleDelete;
		
    } else {
		
        return UITableViewCellEditingStyleNone;
		
    }
	
}


- (void) loadView
{
	NSLog (@"GeneralListViewController loadView");
	CGRect theframe = self.parentViewController.view.bounds;
	float fudge = [DataManager navBarHeight];
	theframe.origin.y+=fudge;
	theframe.size.height-=fudge;
	
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: theframe
							style: UITableViewStylePlain]
							autorelease];
	
	tmpView.dataSource = self;
	tmpView.delegate = self;
    tmpView.backgroundColor =[UIColor clearColor];
    
	tmpView.separatorColor = [UIColor grayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	
	self.navigationItem.titleView = [DataManager makeTitleView:self->name];
	[self setBarButtonItems];
    [self setColorForNavBar];
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = tmpView;
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
	return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	
		NSLog (@"GLV didRotateFromInterfaceOrientation %d",(UIInterfaceOrientation) fromOrient);
	//   self.view = [self allocBuildSearchUI] ; // rebuild whole UI
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
	
	
	[tabView reloadData];
	
	[self setBarButtonItems];
	
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
	[self->plist release];
	[self->name release];
    [self->listItems release];
   // [self->tableView release];
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


#pragma mark UITableViewDelegate Methods
- (UITableViewCell *) tableView: (UITableView *) tabView
		  cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
	NSLog (@"GeneralListViewController expects tableView:cellForRowAtIndexPath: to be overridden");
	return nil;
	
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
	cell.backgroundColor = [UIColor whiteColor];
}

@end

