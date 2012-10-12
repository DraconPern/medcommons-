//
//  AllArchivesListController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "DataStore.h"
#import "DataManager.h"
#import "AllArchivesListController.h"
#import "ArchiveInfoController.h"
#import "ModalAlert.h"
#import "ArchivesManager.h"

#import "ArchivesManager.h"
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

@end

@implementation AllArchivesListController

#pragma mark Public Instance Methods
#pragma mark Private Instance Methods

-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}
-(void) resetPressed;

{
	
	BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
	
	if (settingsCanEdit)
	{

		BOOL answer = [ModalAlert ask:@"Are You Sure? You will need to reload everything."];
		if (answer == YES)
		{
			[ModalAlert say:@"This is very slow, you may want get a coffee"];
			
			[ArchivesManager factoryReset];
			[ModalAlert say:@"All archives are gone. You can reload via iTunes or add more sample tunes."];
			
		}
	}
	else {
		[ModalAlert say:@"Locked.... please go to device settings to Unlock"];
	}
	
	
	return;
}
-(void) setupLeftSideNavItems:(BOOL)wantsAddButton
{
//	// place the toolbar into the navigation bar
//	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
//											  initWithTitle:@"back" style:UIBarButtonItemStyleBordered 
//											  target:self 
//											  action:@selector(donePressed)] autorelease];
	
	
	self.navigationItem.hidesBackButton = NO; 
	
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"Reset" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(resetPressed)] autorelease];
}


#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
	// [AsyncImageView clearCache];
}

- (void) loadView
{
	
	[super loadView];
	//[self backsplash];	 
	BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
	
	[self setupLeftSideNavItems: settingsCanEdit];
	
	
	self->tableView.backgroundColor = [UIColor lightGrayColor]; //[DataManager applicationColor];
	
	
	
	
	self.navigationItem.titleView = [[DataManager allocTitleView:@"Manage Archives"] autorelease];	
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	//
//	self.navigationItem.leftBarButtonItem =
//	[[UIBarButtonItem alloc] initWithTitle:@"back" style: UIBarButtonItemStylePlain target:self action:@selector(donePressed)];
	
	self.navigationItem.hidesBackButton = NO;
	
	
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}


#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	
    [super dealloc];
}


- (id) init 
{
	
	
	NSArray *a =[ArchivesManager allArchives] ;

	self = [super initWithList: a
						  name:@"Manage Archives"
					   canEdit: NO//YES 
					canReorder: NO//YES 
						   tag:-1000];
	
	
	
	
	if (self)
	{
	}
	//[a release];
	return self;
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
				
				
				cell.imageView.image =[ArchivesManager newArchiveThumbnail: archiveName];
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
				//NSError *fileError = [NSError errorWithDomain:NetworkRequestErrorDomain code:ASIFileManagementError 
				//													 userInfo:[NSDictionary 
				//															   dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unable to remove file at path '%@'",path],NSLocalizedDescriptionKey,moveError,NSUnderlyingErrorKey,nil]];
				//				
				NSLog(@"file Error on setlist delete is %@",moveError);
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
		// come here after confirmation
		//		[[DataManager sharedInstance] factoryReset];
		//		[alertView release];
		//		
		//		[self reloadUserInfo];
		//		
		//		// need to clean out data structures
		
		//exit(0); // really leave so that we come back up clean in itunes
	}
}
@end
