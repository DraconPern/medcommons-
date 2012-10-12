//
//  iphoneHomeController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "SetListsManager.h"
#import "DataManager.h"
#import "DataStore.h"
#import "SongsViewController.h"
#import "ArchiveViewController.h"
#import "SmallArchiveViewController.h"
#import	"LoggingViewController.h"
#import "SetListViewController.h"
#import "SettingsViewController.h"
#import "ModalAlert.h"
#import "ArchivesManager.h"
#import "ArchivesManager.h"
#import "OnTheFlyController.h"
#import "GigSnifferController.h"
#import "GigStandAppDelegate.h"
#import "TunesManager.h"
#import "SettingsManager.h"
#import "iphoneHomeController.h"
#import "CollaborationSplashViewController.h"

#pragma mark -
#pragma mark Public Class iPadHomeController
#pragma mark -

#pragma mark Internal Constants
enum
{
	SETUP_EXTERNAL_SCREEN_TAG =667
};
//
// Table sections:
//
enum
{
	FRONT_SECTION = 0,  // Right Now Will Have Only "All Songs"
    LISTS_SECTION ,     // Will Have All the Lists including Favorites and Recents  
	ARCHIVES_SECTION ,	// Then Comes All The Archives - must be last - is now optional
    MATES_SECTION ,		// 
	
    //
    SECTION_COUNT
};

//
// Tail table section rows:
//
enum
{
    //
    ABOUT_ROW_COUNT
};



@interface iphoneHomeController () < UITableViewDataSource, UITableViewDelegate,UIAlertViewDelegate,UIPopoverControllerDelegate>
- (void) updateNavigationItemAnimated: (BOOL) animated;
- (void) reloadUserInfo;
@end

@implementation iphoneHomeController


#pragma mark Public Instance Methods

@synthesize deviceWindow;
@synthesize consoleTextView;
@synthesize externalWindow;

-(void) popOff;
{
    
}

-(void) perfMode
{
	BOOL ro = 
	[ModalAlert ask :@"Performance Mode will make GigStand read-only. To leave performance mode, go to your device's Settings app and select GigStand."];
	[SettingsManager sharedInstance].normalMode = !ro;	
    [DataManager worldViewPulse];
}
#pragma mark Private Instance Methods




-(void) makenewlists
{
    
    
    // load these values like a little cache    
    ltunecount = [TunesManager tuneCount];
    licount = [TunesManager instancesCount];
    aicount = [ArchivesManager archivesCount];
    
    
	// there might be new setlists
	if (self->listItems) [self->listItems release]; 
	// sort the list and make sure favorites and recents are explicitly on top
	
	self->listItems = [[DataManager list:[SetListsManager makeSetlistsScan]
					   bringToTop:[NSArray arrayWithObjects:@"recents",@"favorites",nil]] retain]; // must go to top
	
	if (self->alistItems) [self->alistItems release];
	
	self->alistItems = [[DataManager list:[ArchivesManager allEnabledArchives] 
						bringToTop:[NSArray arrayWithObjects:[ArchivesManager nameForOnTheFlyArchive],nil]] retain];
	
}
- (UIView *) allocDemoImageView :(CGRect) frame
{//
	UIImageView *logoView_ = [[UIImageView alloc]
							  initWithFrame: CGRectMake (0.0f, 0.0f, 200.0f, 200.0f)];
	
	logoView_.alpha = 0.10f;
	logoView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
								  UIViewAutoresizingFlexibleLeftMargin |
								  UIViewAutoresizingFlexibleRightMargin |
								  UIViewAutoresizingFlexibleTopMargin);
	logoView_.center = CGPointMake (self.view.frame.size.width / 2.0f,
									self.view.frame.size.height /2.0f);
	
	
	logoView_.image =  [UIImage imageNamed:@"MusicStand_512x512.png"];		
	
	logoView_.backgroundColor = [UIColor blueColor];
	return logoView_;
	
}
- (void) pushToMates
{
	
	GigSnifferController *gsc = [[[GigSnifferController alloc] init] autorelease];	
	
	gsc.navigationItem.title = @"Bandmates Nearby";
	[self.navigationController pushViewController:gsc animated:YES];
}
- (void) pushToSongs
{
	if ( [TunesManager tuneCount] > 0)
	{
		SongsViewController *aModalViewController = [[[SongsViewController alloc] init] autorelease];	
		
		aModalViewController.modalPresentationStyle = UIModalPresentationFormSheet;
		;
		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
		//	
		[self presentModalViewController:nav animated: YES];
	}
}

- (void) pushToSettings
{
	
	SettingsViewController *aModalViewController = [[[SettingsViewController alloc] init] autorelease];	// was autorelease

        [self.navigationController pushViewController:aModalViewController animated:YES];

}




- (void) updateNavigationItemAnimated: (BOOL) animated
{
	self.navigationItem.titleView = [DataManager makeAppTitleView:@"Home"] ;
    [self  setColorForNavBar];
	
	BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
	if (settingsCanEdit) {
		
		self.navigationItem.rightBarButtonItem =[[UIBarButtonItem alloc]   initWithTitle:@"settings" style: UIBarButtonItemStyleBordered
                                          target: self action: @selector (pushToSettings)];

		
        
	}
	else 
	{
		self.navigationItem.rightBarButtonItem = nil; // truly erase this
		
		self.navigationItem.leftBarButtonItem = nil; // truly erase this
        
	}
}
- (void) reloadUserInfo;
{
    
 
	[self makenewlists];
	
	[self updateNavigationItemAnimated: NO];	
	UITableView    *tabView = self->mainTableView ;
	[tabView reloadData];
   

}

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
	
	NSLog (@"GSH didReceiveMemoryWarning");
	[super didReceiveMemoryWarning];
	
	// [AsyncImageView clearCache];
}

- (BOOL) askAboutSamples
{
	BOOL beenTouched = [[NSUserDefaults standardUserDefaults] boolForKey:@"BeenTouched"];
	[[NSUserDefaults standardUserDefaults] setBool: YES forKey:@"BeenTouched"]; // now fix this and then synch
	[[NSUserDefaults standardUserDefaults] synchronize];
	if (beenTouched == NO)
	{
		BOOL answer = [ModalAlert ask:@"It looks like your first time in GigStand. Please be sure to only load content that is of your own creation or in the public domain. Would you like to load some public domain samples?"];
		if (answer == YES)
		{
			// settings view controller will do all the heavy lifting
			SettingsViewController *aModalViewController = [[[SettingsViewController alloc] initWithAutoStart:YES] autorelease];	// was autorelease
			[self.navigationController pushViewController:aModalViewController animated:YES];
			return YES; // dont want to check inbox
			
		}
		
	}
	return NO;
}
-(void) processInboxViaPushController
{
	// settings view controller will do all the heavy lifting
	SettingsViewController *aModalViewController = [[[SettingsViewController alloc] initWithAutoStart:NO] autorelease];	// was autorelease
	[self.navigationController pushViewController:aModalViewController animated:YES];
}
// do something modal here based on inbox
-(void) checkInbox {
    if (checkTimer == nil)return;
	
	BOOL didLoadSamples  = [self askAboutSamples]; // can this come back
	
	if (!didLoadSamples)
	{
		NSUInteger zipcount=[DataManager incomingInboxDocsCount];
		if ((zipcount!=0))
		{
			BOOL answer = [ModalAlert ask:@"You have unprocessed items in your iTunes inbox. Would you like to process them now? Please be patient as it can take a while"];
			if (answer == YES)
			{
				[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processInboxViaPushController) userInfo:nil repeats:NO];
			}
		}
	}
}
- (void)log:(NSString *)msg
{
	//[consoleTextView setText:[consoleTextView.text stringByAppendingString:[NSString stringWithFormat:@"%@\r\r", msg]]];
	NSLog (@"%@",msg);
}

- (void) loadView
{
	//[TunesHelper dump];
	
	//NSLog (@"GSH loadView");
	[self makenewlists];
    
    
    
	CGRect theframe = self.parentViewController.view.bounds;//
	UIView *oview = [[[UIView alloc] initWithFrame: theframe ] autorelease];
	oview.backgroundColor = [DataManager applicationColor];//[UIColor clearColor];
	float fudge =  [DataManager navBarHeight];
	theframe.origin.y+=fudge;
	theframe.size.height-=fudge;
	
	// outer view installed just to get background colors right
    
  
    
    
	UITableView *tmpView = //[
	[[UITableView alloc] initWithFrame: theframe style: UITableViewStyleGrouped];// autorelease];
	tmpView.backgroundColor =  [UIColor clearColor]; 
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [DataManager applicationColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    
	
	[self updateNavigationItemAnimated:YES];
	self->mainTableView = tmpView;
        
    
    [oview addSubview:tmpView];
	
 
    self.view = oview;
	
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
	return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
    
	CGRect frame = [[UIScreen mainScreen] bounds];
    NSLog (@"in didRotateFromInterfaceOrientation height %f width %f",frame.size.height,frame.size.width);

    [self loadView];
}
- (void) viewDidAppear: (BOOL) animated
{
	
    
	[super viewDidAppear: animated];
    
    if (checkTimer == nil)
	checkTimer = [NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(checkInbox) 
								   userInfo:nil repeats:NO];
}
-(void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
	//[self invalidateTimer];
    checkTimer = nil;
}
- (void) monitorAST: (NSTimer *) timer;
{
    
    //	if (aTimer == nil) return; // if already cloeared, then ignore this
    //	
    //	aTimer = nil; // not in timer
    //	// while we are at it, lets monitor the inbox
    
    NSLog (@"GSH GigStandWorldViewHasChanged was fired");
	
	[self reloadUserInfo]; // do this unconditionally whilst monitoring inbox
	
	//aTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
}
- (void) monitorASTR: (NSTimer *) timer;
{
    
    NSLog(@"GSH User defaults were changed ");
    
    [SettingsManager sharedInstance].normalMode = YES;
    
    [self monitorAST:(NSTimer *) timer];
    
}
- (void) viewDidLoad
{	
	[super viewDidLoad];
	//NSLog (@"GSH viewDidLoad");
	
	[self updateNavigationItemAnimated: NO]; // first time up, put it up
	
	
	
	externalWindow.hidden = YES;
	
	
	// Make iPad window visible.
	//[deviceWindow makeKeyAndVisible];
	
	// Check for external screen.
	if ([[UIScreen screens] count] > 1) {
		[self log:@"Found an external screen."];
		
		// Internal display is 0, external is 1.
		externalScreen = [[[UIScreen screens] objectAtIndex:1] retain];
		[self log:[NSString stringWithFormat:@"External screen: %@", externalScreen]];
		
		screenModes = [externalScreen.availableModes retain];
		[self log:[NSString stringWithFormat:@"Available modes: %@", screenModes]];
		
		// Allow user to choose from available screen-modes (pixel-sizes).
		UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"External Display Size" 
														 message:@"Choose a size for the external display." 
														delegate:self 
											   cancelButtonTitle:nil 
											   otherButtonTitles:nil] autorelease];
		for (UIScreenMode *mode in screenModes) {
			CGSize modeScreenSize = mode.size;
			[alert addButtonWithTitle:[NSString stringWithFormat:@"%.0f x %.0f pixels", modeScreenSize.width, modeScreenSize.height]];
		}
		alert.tag = SETUP_EXTERNAL_SCREEN_TAG;
		[alert show];
		
	} else {
		//[self log:@"External screen not found."];
	}
    
    [[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(monitorAST:)
												 name:GigStandWorldViewHasChanged
											   object:self];
    
    [ [NSNotificationCenter defaultCenter] addObserver:self 
                                              selector:@selector(monitorASTR:) 
                                                  name:NSUserDefaultsDidChangeNotification 
                                                object:self];
    
    //	aTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
	
}

-(void) invalidateTimer
{
	if (aTimer) 
	{
		[aTimer invalidate];
		//[aTimer release];
		aTimer = nil;
	}
}


//////

- (void) viewDidUnload

{
	[self invalidateTimer];
    [super viewDidUnload];
}



- (void) viewWillAppear: (BOOL) animated
{
    
	[super viewWillAppear: animated];
	[self reloadUserInfo];
    
}


- (void)becomeActive:(NSNotification *)notification {
    NSLog(@"GSH multi-tasking re-activation");
	
	[self makenewlists];
	
	[self reloadUserInfo];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	//NSLog (@"GSH dealloc");
    
    
	[[NSNotificationCenter defaultCenter] removeObserver:self];
	
	if (self->listItems) [self->listItems release]; 
	
	if (self->alistItems) [self->alistItems release]; 
    
	
	[consoleTextView release];
    [deviceWindow release];
	[externalWindow release];
    [externalScreen release];
    [mainTableView release];
    
	[super dealloc];
}

- (id) init
{
	//NSLog (@"GSH init");
	self = [super init];
	if (self) 
	{
		self->listItems = nil;
		
		self->alistItems = nil;
		
	}
	return self;
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    
  
    BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
	BOOL ziparchives  = [[SettingsManager sharedInstance] zipArchives];
    BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
    NSUInteger secount = SECTION_COUNT;
    
    if (!settingsCanEdit) secount--;
    
    
	if (ziparchives || collabfeatures)
		return secount; else return secount - 1;
        

}

- (UITableViewCell *) tableView: (UITableView *) tabView
		  cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
	static NSString *CellIdentifier0 = @"InfoCell0";
	static NSString *CellIdentifier1 = @"InfoCell1";
	NSString        *cellIdentifier;
	
	switch (idxPath.section)
	{
			
			
		case LISTS_SECTION:
		{
			cellIdentifier = CellIdentifier0;
			break;
		}
			
		case FRONT_SECTION:
		case ARCHIVES_SECTION :
		default:
		{
			cellIdentifier = CellIdentifier1; // this section will have images
			break;
			
		}
	}
	
	UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
	
	if (!cell)
	{
		switch (idxPath.section)
		{
				
			case LISTS_SECTION:
				cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle //UITableViewCellStyleValue1
											   reuseIdentifier: cellIdentifier]
						autorelease];
				break;
			case FRONT_SECTION:	
			case MATES_SECTION:
			case ARCHIVES_SECTION :
				
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
	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	cell.detailTextLabel.text = nil;
	cell.selectionStyle = UITableViewCellSelectionStyleNone;
	cell.textLabel.text = nil;
	
	switch (idxPath.section)
	{
			
		case FRONT_SECTION:
		{ 
			
			cell.imageView.image =     [DataManager makeThumbRS:
										@"alltunes.png"
																  size:[DataManager standardThumbSize]];
			
			if ( [TunesManager tuneCount] > 0)
			{
				cell.textLabel.text =[NSString stringWithFormat: @"All %d Tunes",
									  ltunecount];
				cell.detailTextLabel.text = [NSString stringWithFormat:@"%d source files in %d archives",
											 licount,
											 aicount
											 ];
			}
			else 
			{
				cell.accessoryType = UITableViewCellAccessoryNone;
				cell.textLabel.text = @"Please Add Some Tunes";
				cell.detailTextLabel.text = @"add archives of tunes via Settings";
			}
			break;
		}
			
		case MATES_SECTION:
		{
			
			cell.imageView.image =     [DataManager makeThumbRS:
										@"bonjour.png"
																  size:[DataManager standardThumbSize]];
			
            BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
			BOOL bonjourWithPeers  = [[SettingsManager sharedInstance] bonjourWithPeers];
			
            if (bonjourWithPeers&&collabfeatures)
            {
                cell.textLabel.text = @"BandMates on GigStand";
                cell.detailTextLabel.text=[NSString stringWithFormat:
                                           @"http://%@:%d from non-IOS devices",
                                           [DataManager sharedInstance].myLocalIP,
                                           [DataManager sharedInstance].myLocalPort];}
			else 
			{
                if (collabfeatures)
                    cell.textLabel.text = @"Collaboration Enabled"; else
                        cell.textLabel.text = @"Add Collaboration Features";
				cell.detailTextLabel.text = @"share thousands of tunes with bandmates from your device";
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
			}
            
            
            
			break;
		}
			
		case LISTS_SECTION:
		{
			
			if (idxPath.row < [self->listItems count]) 
			{
				
				
				
				NSString *listName = [ self->listItems objectAtIndex:idxPath.row];
				
				cell.textLabel.text = listName;	
				
				// let's dig out the number of current entries so we can display this as detailed text
				
				cell.detailTextLabel.text =[NSString stringWithFormat:@"%d tunes", [SetListsManager  itemCountForList:listName]];		
				if ([listName isEqualToString:@"recents"])
					cell.imageView.image = 
					[DataManager makeThumbRS:@"recents.jpg" size:[DataManager standardThumbSize]]; else
						if ([listName isEqualToString:@"favorites"])
							cell.imageView.image = 
							[DataManager makeThumbRS:@"favorites.jpg" size:[DataManager standardThumbSize]]; else
								cell.imageView.image = 
								[DataManager makeThumbRS:@"setlistpic.jpg" size:[DataManager standardThumbSize]];;
				
			}
			else
				cell = nil;
			break;
		}
			
		case ARCHIVES_SECTION :
		{
			
			if ((idxPath.row <  [self->alistItems count]))
			{	
				//[ArchivesManager dump];
				
				NSString *archive = [self->alistItems objectAtIndex:idxPath.row];	
				
				
				cell.textLabel.text = [ArchivesManager shortName:archive]; 
				
				
				if ([archive isEqualToString:[ArchivesManager nameForOnTheFlyArchive]])
					cell.imageView.image = 
					[DataManager makeThumbRS:@"onthefly.jpg" size:[DataManager standardThumbSize]]; 
				else 
					cell.imageView.image =[ArchivesManager makeArchiveThumbnail: archive];
				
				
				NSDictionary *attrs = [[NSFileManager defaultManager] 
                                       attributesOfItemAtPath: [[DataStore pathForSharedDocuments] 
                                                                stringByAppendingPathComponent: archive]
                                       error: NULL];
				if ((attrs )&&  [attrs objectForKey:NSFileCreationDate])
				{
                    
					double mb = [ArchivesManager fileSize:archive ]/1024.f;
					NSUInteger filecount = [ArchivesManager fileCount:archive ];
					cell.detailTextLabel.text = [NSString stringWithFormat:@"%.2fGB %d files",mb, filecount];//
				}
				
				
			}
			else
				cell = nil;
			
			break;
		}
			
		default :
			cell = nil;
			break;
	}
	
	return cell;
}

- (NSInteger) tableView: (UITableView *) tabView numberOfRowsInSection: (NSInteger) sect
{
	//   SettingsManager *settings = self.appDelegate.settingsManager;
	
	switch (sect)
	{
			
		case ARCHIVES_SECTION :
			return  [self->alistItems count];
			
		case FRONT_SECTION:
			return 1;
		case MATES_SECTION:
			return 1;
			
		case LISTS_SECTION: return [self->listItems count];
			
			
			
		default :
			return 0;
	}
}

#pragma mark footer stuff
- (NSString *) tableView: (UITableView *) tabView titleForFooterInSection: (NSInteger) sect
{

    return nil;

}


- (CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
	switch (section)
	{
            //		case LISTS_SECTION :	
            //		case FRONT_SECTION :			
            //			
            //		case MATES_SECTION:	
            //		case ARCHIVES_SECTION :			
            //			
            //			return [DataManager navBarHeight];
		default :
			return 0.f;
			
	}
}
#pragma mark header stuff
- (NSString *) tableView: (UITableView *) tabView titleForHeaderInSection: (NSInteger) sect
{
	
	
	//SettingsManager *settings = self.appDelegate.settingsManager;
	
	switch (sect)
	{
            
			
			
		default :
			return nil;
	}
}
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{

    return nil;
}
- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
	if (section == FRONT_SECTION) return [DataManager infoRowSize]+10.0f;
	return 0.f;
}


#pragma mark UITableViewDelegate Methods


- (CGFloat) tableView: (UITableView *) tabView heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    
	return [DataManager standardRowSize];
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	switch (idxPath.section)
	{	
		case FRONT_SECTION :			
			return [self pushToSongs];
			
        case MATES_SECTION:
		{
            BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
			BOOL bonjourWithPeers  = [[SettingsManager sharedInstance] bonjourWithPeers];
			if (bonjourWithPeers&&collabfeatures)
			{
                
                
                {
                    
                    GigSnifferController *aModalViewController = [[[GigSnifferController alloc] init] autorelease];	
                    
                    aModalViewController.navigationItem.title = @"Bandmates Nearby";                
                    
                    [self presentModalViewController:aModalViewController animated: YES];
                }
                return ;
			}
			
            
            
            CollaborationSplashViewController *aModalViewController = [[[CollaborationSplashViewController alloc] init ] autorelease];	
            
            
            UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
            [self presentModalViewController:nav animated: YES];
			return;
			
		}
			
		case LISTS_SECTION:
		{
			NSUInteger count = [self->listItems  count];
			
			if (idxPath.row < count)
			{
				
				
				NSString *filepath = [ self->listItems  objectAtIndex:idxPath.row];
				if (!filepath)
				{
					NSLog(@"no filepath found for item at row %d", idxPath.row);
					return;
				}
				
				BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
				SetListViewController *wvc = [[[SetListViewController alloc]
												initWithPlist:filepath name:filepath
												edit:settingsCanEdit]
											   autorelease];
				
				
				wvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal
				;
				UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
                
                
				
				[self presentModalViewController:nav animated: YES];
				
			}
			break;
		}
			
		case ARCHIVES_SECTION :
		{
			
			NSString *archive = [self->alistItems objectAtIndex:idxPath.row];	
			BOOL b  = [ArchivesManager isArchiveEnabled:archive];
			
			if (b==YES)
			{
				// choose the different controller based upon how many files are in the archive
				
				UINavigationController *nav ;
				if ([archive isEqualToString:[ArchivesManager nameForOnTheFlyArchive]])
					//if (archIdx==0)
				{
					OnTheFlyController *azzvc = [[[OnTheFlyController alloc] init]	autorelease];
					azzvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
					nav = [[[UINavigationController alloc] initWithRootViewController: azzvc] autorelease];
				}
				else {
					
					if ([ArchivesManager fileCount: archive] >50)
					{
						ArchiveViewController *zvc = [[[ArchiveViewController alloc] initWithArchive:archive]	autorelease];
						zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
						nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
					}
					else 
					{
						
						SmallArchiveViewController *zzvc = [[[SmallArchiveViewController alloc] initWithArchive: archive]	autorelease];
						zzvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
						nav = [[UINavigationController alloc] initWithRootViewController: zzvc];// autorelease];
						
					}
					
				}
				[self presentModalViewController:nav animated: YES];
				
			}
			
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
	
	// continues here after screen resolution is picked
	
	{ // not cancelled
		switch (alertView.tag)
		{
			case SETUP_EXTERNAL_SCREEN_TAG:
			{
				UIScreenMode *desiredMode = [screenModes objectAtIndex:index];
				//	[self log:[NSString stringWithFormat:@"Setting mode: %@", desiredMode]];
				externalScreen.currentMode = desiredMode;
				
				CGRect rect = CGRectZero;
				rect.size = desiredMode.size;
				
				externalWindow = [[UIWindow alloc] initWithFrame:rect];
				//externalWindow.frame = rect;
				externalWindow.clipsToBounds = YES;
				externalWindow.screen = externalScreen;
				[[DataManager sharedInstance] setTVScreenBounds: rect window:externalWindow];
				
				NSLog(@"Displaying externalWindow on externalScreen with frame w %.0f h %.0f x %.0f y %.0f",
					  rect.size.width,rect.size.height,rect.origin.x,rect.origin.y);
				externalWindow.hidden = NO;
				UIView *vv = [self allocDemoImageView:rect];
				[externalWindow addSubview:vv];		// use same bounds	
				//[externalWindow addSubview:[self labelForLeft]];
				[externalWindow makeKeyAndVisible];
				[screenModes release];
				[externalScreen release];
				[vv release];
				break;
			}
		}
	}
}



@end
