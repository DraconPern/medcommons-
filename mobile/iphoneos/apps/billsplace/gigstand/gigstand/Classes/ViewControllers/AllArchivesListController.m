//
//  AllArchivesListController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "DataStore.h"
#import "DataManager.h"
#import "AllArchivesListController.h"
#import "ArchiveInfoController.h"
#import "ModalAlert.h"
#import "ArchivesManager.h"
#import "SettingsManager.h"
#import "GigStandAppDelegate.h"

#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
    DOCUMENTS_SECTION=0 ,  // MUST be kept in display order ...
    //
    SECTION_COUNT
};


@interface AllArchivesListController () < UITableViewDataSource, UITableViewDelegate>

-(NSUInteger ) itemCount;
-(void) reloadListItems : (NSArray *) newItems;
-(void) setBarButtonItems;
-(void) leaveEditMode;
-(void) enterEditMode;
@end

@implementation AllArchivesListController

#pragma mark Public Instance Methods
#pragma mark Private Instance Methods

-(void) donePressed;
{
	[self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
}
-(void) cancelPressed;
{
	[self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
}
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
#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
	// [AsyncImageView clearCache];
}

- (void) loadView
{
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
	
	self.navigationItem.titleView = [DataManager makeTitleView:self->name] ;
	[self setBarButtonItems];
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = tmpView;
	
	self->tableView.backgroundColor = [UIColor clearColor]; //[DataManager applicationColor];
	
	self.navigationItem.titleView = [DataManager makeTitleView:@"Manage Archives"] ;	
	[self setColorForNavBar ];
    
    self.navigationItem.hidesBackButton = NO;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}


#pragma mark Overridden NSObject Methods
-(void) viewDidUnload
{
    // dealloc may never get called
    
    [DataManager worldViewPulse];
    [super viewDidUnload];
}
- (void) dealloc
{//
            NSLog (@"allarchiveslistcontroller dealloc retaincount %d",[self retainCount]);
        
    [DataManager worldViewPulse];
	[logoView release];
    [listItems release];
    [name release];
    [plist release];
   //ta [tableView release];
  
    
    [super dealloc];
}




#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
	// static NSString *CellIdentifier0 = @"InfoCell0";
    static NSString *CellIdentifier1 = @"InfoCell1";
	NSString        *cellIdentifier;
	
	cellIdentifier = CellIdentifier1;
	
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
	
    if (!cell)
    {
        switch (idxPath.section)
        {
				
				
            case DOCUMENTS_SECTION :
				
				
				cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle
                                               reuseIdentifier: cellIdentifier]
                        autorelease];
				
                break;
				
            default :
                break;
        }
    }
	
    //
    // Reset cell properties to default:
    //
    cell.accessoryType = UITableViewCellAccessoryNone;
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;
	
    switch (idxPath.section)
    {
			
        case DOCUMENTS_SECTION :
        {
			
			if ((idxPath.row <  [ArchivesManager archivesCount]))
			{	
				NSString *archiveName = [self->listItems objectAtIndex:idxPath.row];
				
				BOOL b = [ArchivesManager isArchiveEnabled: archiveName];
				if (b == YES)
					cell.accessoryType = UITableViewCellAccessoryCheckmark;  // ov for now	
				if ([archiveName isEqualToString:[ArchivesManager nameForOnTheFlyArchive]])
					cell.imageView.image = 
					[DataManager makeThumbRS:@"onthefly.jpg" size:[DataManager standardThumbSize]]; 
				else 
				cell.imageView.image =[ArchivesManager makeArchiveThumbnail: archiveName];
				cell.textLabel.text =[ArchivesManager shortName:archiveName]; 
	
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForSharedDocuments] 
																							   stringByAppendingPathComponent: archiveName]
																					   error: NULL];
				if ((attrs )&&  [attrs objectForKey:NSFileCreationDate])
				{
					
					double mb = [ArchivesManager fileSize:archiveName ];
					NSUInteger filecount = [ArchivesManager fileCount:archiveName ];
					cell.detailTextLabel.text = [NSString stringWithFormat:@"%.2fMB %d files %@ ",mb, filecount, archiveName];//
				}
				else
					cell = nil;
				
			}
			
			break;
		}
			
        default :
            cell = nil;
            break;
	}
	
	return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{
	//   SettingsManager *settings = self.appDelegate.settingsManager;
	
	switch (sect)
	{
			
		case DOCUMENTS_SECTION :
			return [ArchivesManager archivesCount];
			
			
			
		default :
			return 0;
	}
}


-(void) tableView: (UITableView *)aTableView commitEditingStyle: (UITableViewCellEditingStyle)editingStyle
forRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if (editingStyle == UITableViewCellEditingStyleDelete)
	{
		
		NSString *archive = [self->listItems objectAtIndex:indexPath.row];
		
		if (![archive isEqualToString: [ArchivesManager nameForOnTheFlyArchive]])
		{
			// DELETE A Archives
			
			
			// actually delete this item from the listItems array
			//[self->listItems removeObjectAtIndex:indexPath.row];	// make this persistent by actually deleting the particular plist
			
			[ArchivesManager deleteArchive:archive];
			
			[self setBarButtonItems];
			
			[self->listItems release];
			
			self->listItems = [[ArchivesManager allArchives] retain]; //locallistItems;
			
			[self->tableView reloadData];
			
		}	
		else
		{
			UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"You can't delete that archive" 
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
#pragma mark UITableViewDelegate Methods


- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return [DataManager standardRowSize];
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	switch (idxPath.section)
	{
			
		case DOCUMENTS_SECTION :
		{
			
			[tabView deselectRowAtIndexPath:[tabView indexPathForSelectedRow] animated:NO];
			
			
			NSString *archive = [self->listItems objectAtIndex:idxPath.row];
			
			ArchiveInfoController *zvc = [[[ArchiveInfoController alloc] initWithArchive: archive]	autorelease];
			
			zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
			UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
			
			[self presentModalViewController:nav animated: YES];
			//[archiveItems release];
			
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
#pragma mark UIAlertViewDelegate Methods
-(void) alertView:(UIAlertView *) alertView clickedButtonAtIndex: (int)index

{ 
	if (index!=0) { // not cancelled
	}
}

-(NSUInteger ) itemCount;
{
	return [self->listItems count];
}


#pragma mark Overridden UIViewController Methods

-(void) reloadListItems : (NSArray *) newItems
{
	[self->listItems release];
	self->listItems = [newItems retain];
	[self->listItems sortUsingSelector:@selector(compare:)];
	
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



-(id) init;
{
	// this initializer does not read the plist but instead takes the list of actual items, it is used by the AllSetListsController
	
	self = [super init];
	if (self)
	{
		self->listItems = [[ArchivesManager allArchives] retain]; //locallistItems;
		
		[self->listItems sortUsingSelector:@selector(compare:)];
		self->name = @"Manage Archives";
		self->canEdit = YES;
		self->canReorder = YES;
		self->tag = -999;
		self->plist = nil;
		
		
		NSLog(@"GeneralList.%@ special items %d canEdit %d canReorder %d name %@ ", self,[self->listItems count],self->canEdit,self->canReorder,self->name);
	}
	
	//[a release];
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

- (void) viewDidAppear: (BOOL) animated
{
	[super viewDidAppear: animated];
	
	[(UITableView *) self.view flashScrollIndicators];
}

- (void) viewDidLoad
{
	[super viewDidLoad];
    
    if ([DataManager modalPopOversEnabled])
    self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"cancel" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(cancelPressed)] autorelease];
}

- (void) viewWillAppear: (BOOL) animated
{
	[super viewWillAppear: animated];
	//
	//	[self backsplash];	
	UITableView *tabView = (UITableView *) self.view;
		[self->tableView reloadData];
	
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
@end
