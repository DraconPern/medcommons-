//
//  SettingsViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "SettingsViewController.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"
#import "AllTunesViewController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "AllSetListsController.h"

#define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]

#pragma mark -
#pragma mark Public Class SettingsViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
	ABOUT_SECTION = 0,
    DOCUMENTS_SECTION ,  // MUST be kept in display order ...
	
	
    //
    SECTION_COUNT
};

//
// About table section rows:
//
enum
{
    ABOUT_VERSION_ROW = 0,  // MUST be kept in display order ...
    FACTORY_SETTINGS_ROW,
	MUSICSTAND_MANUAL_ROW,
    //
    ABOUT_ROW_COUNT
};


@interface LoggingViewController: UIViewController
{
	UITextView *tv;
}
@end
@implementation LoggingViewController
-(void) pressedClear
{
	
	freopen([STDERR_OUT fileSystemRepresentation], "w+", stderr); // clear log
	tv.text = @"cleared log by user request";
	[self.view reloadInputViews];
}

-(id) initWithView: (UITextView *) tvx;
{
	self = [super init];
	if (self) 
	{
		tv = tvx;
	}
	return self;
}

-(void) loadView
{
	self.view = [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
	self.view.backgroundColor = [DataManager sharedInstance].appColor;
	self.navigationItem.title = @"Debug: Trace Log";
	self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Clear" 
																			  style:UIBarButtonItemStyleBordered
																			 target: self 
																			 action: @selector (pressedClear)];
	
	[self.view 	addSubview: tv];
}

@end


@interface SettingsViewController () < UITableViewDataSource, UITableViewDelegate,UITextViewDelegate>

@property (nonatomic, retain, readonly)  NSCalendar      *calendar;
@property (nonatomic, retain, readonly)  NSDateFormatter *dateFormatter;
@property (nonatomic, retain, readonly)  NSArray         *documents;

- (void) updateNavigationItemAnimated: (BOOL) animated;


- (void) reloadUserInfo;

@end

@implementation SettingsViewController

@dynamic    calendar;
@dynamic    dateFormatter;
@dynamic    documents;
@dynamic    hidesMasterViewInLandscape;

#pragma mark Public Instance Methods
#pragma mark Private Instance Methods
-(void) donePressed;
{
	//[self.parentViewController dismissModalViewControllerAnimated:YES];
		[self.navigationController popViewControllerAnimated:YES];
}

-(void) processDBRebuild
{	
	// come here after the Inbox has been polled, essentially rebuild the entire db
	[[DataManager  sharedInstance  ]  buildNewDB];
	[DataManager finishDBSetup];
	
	[[DataManager sharedInstance] setProgressString:[NSString stringWithFormat:@"Assimilation of iTunes Inbox Completed"
													]];
	[self->zipalert dismissWithClickedButtonIndex:0 animated:YES];;
	[self->zipalert release];
	self->zipalert = nil;
	[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
	
	UIAlertView *zipalert2 =  [[[UIAlertView alloc] initWithTitle:@"iTunes Inbox Processing Complete"
														  message:[NSString stringWithFormat:@"%d archives %d setlists",
																   archive_import_count,setlist_import_count]  delegate:nil 
												cancelButtonTitle:@"OK"
												otherButtonTitles:nil] autorelease] ;
	[zipalert2 show];
	self->insideImport = NO;
	
	[[DataManager sharedInstance] showIncomingAsProgressString];
	
	[self reloadUserInfo];
}

-(void) processDBRebuildstart
{
	[[DataManager sharedInstance] setProgressString:[NSString stringWithFormat:@"Imported %d archives, %d setlists - assimilation commencing",
											   archive_import_count,setlist_import_count]];
	
//	self->zipalert =  [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"Imported %d archives, %d setlists",
//										archive_import_count,setlist_import_count] 
//										message:@"assimilation commencing" delegate:nil 
//									   cancelButtonTitle:nil
//									   otherButtonTitles:nil] ;
//	
//	UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
//	
//	// Adjust the indicator so it is up a few pixels from the bottom of the alert
//	indicator.center = CGPointMake(zipalert.bounds.size.width / 2 +140 , zipalert.bounds.size.height /2 +70);
//	[indicator startAnimating];
//	[zipalert addSubview:indicator];
//	[indicator release];
//	[self->zipalert show];
	[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processDBRebuild) userInfo:nil repeats:NO];
}
-(void) processItunesInboxInner {
	//[[DataManager sharedInstance] dismissZipExpansionWaitIndicators]; // turn off signalling
	
	BOOL didSomething = [[DataManager sharedInstance] processIncomingFromItunes]; // puts up alert while working
	if (!didSomething)
	{
		
		[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processDBRebuildstart) userInfo:nil repeats:NO];
		[self reloadUserInfo];
		return;
		
	}
	else
	{
		archive_import_count++;
		[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processItunesInboxInner) userInfo:nil repeats:NO];
		[self reloadUserInfo];
	}
	
}
-(void) processItunesInbox
{
	self->insideImport = YES;
	
	[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
	// at this point the titleDictionary and variant structure has been rebuilt either thru expandFiles or by the reload of the TitlesSection
	archive_import_count=setlist_import_count=0;
	[DataManager cleanUpOldDB];
	[self processItunesInboxInner]; // this completes at ast level sometime later, i dont really like this
}
-(void) alltunespressed
{
	AllTunesViewController *allTunesViewController = [[[AllTunesViewController alloc] initWithArray:[DataManager sharedInstance].allTitles
													   
																						   andTitle:@"All Tunes"] autorelease];
	allTunesViewController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: allTunesViewController
									] autorelease];
	[self presentModalViewController:nav animated: YES];
	
}
- (void) didReceiveSecretHandshake: (UIGestureRecognizer *) gr
{
	LoggingViewController *asvc = [[[LoggingViewController alloc] initWithView:textView] autorelease];
	
	[self.navigationController pushViewController: asvc
										 animated: YES];
}

- (void) reloadUserInfo;
{
	lastzipcount = [DataManager zipcountItunesInbox];
    [self updateNavigationItemAnimated: YES];
	
    UITableView    *tabView = (UITableView *) self.view;
	
    [tabView reloadData];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
	//   SessionManager *sm = self.appDelegate.sessionManager;
    self.navigationItem.title = NSLocalizedString (@"Gig Stand", @"");
    self.navigationItem.hidesBackButton = NO;
}


#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
    [AsyncImageView clearCache];
}

- (void) loadView
{
	
	CGRect frame = [[UIScreen mainScreen] applicationFrame];
	textView =[[UITextView alloc] initWithFrame:frame ] ;
	
	textView.editable = NO;
	
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: frame
                                                         style: UITableViewStyleGrouped]
                            autorelease];
	tmpView.backgroundColor =  [UIColor lightGrayColor]; //[DataManager sharedInstance].appColor; //[UIColor colorWithRed:(CGFloat).516f green:(CGFloat).141f blue:(CGFloat).145 alpha:(CGFloat)1.0f];
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
    SettingsManager *settings = self.appDelegate.settingsManager;
	
    if (settings.canUseSettings)
    {
        UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self
                                                                               action: @selector (didReceiveSecretHandshake:)]
                                       autorelease];
		
        tgr.numberOfTapsRequired = 4;
		
		[tmpView addGestureRecognizer: tgr];
    }
	
	
	// this will appear as the title in the navigation bar
    frame = CGRectMake(0, 0, 300, 44);
	UILabel *label = [[[UILabel alloc] initWithFrame:frame] autorelease];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0f];
	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor = [UIColor whiteColor];
	self.navigationItem.titleView = label;
	label.text = NSLocalizedString(@"GigStand: Archives", @"");
	
	
	// reload recent items the first time, otherwise leave it alone
	
	//[DataManager sharedInstance ].recentItems =  [[DataManager sharedInstance ] allocReadRecents];
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	
	
	self.navigationItem.leftBarButtonItem =
	[[UIBarButtonItem alloc] initWithTitle:@"Songs" style: UIBarButtonItemStylePlain target:self action:@selector(donePressed)];
	
	
	self.view = tmpView	;
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    [(UITableView *) self.view flashScrollIndicators];
}
- (void) monitorAST: (NSTimer *) timer;
{
	
	// while we are at it, lets monitor the inbox
	
		NSUInteger zipcount=[DataManager zipcountItunesInbox];
		if (zipcount!=lastzipcount)
	{
		
		lastzipcount = zipcount;
		
	}
	
	//NSLog(@"Reloading Settings Page");
	
	[self reloadUserInfo]; // do this unconditionally whilst monitoring inbox
	
	
	
	NSError *error;
	NSStringEncoding encoding;
	NSString *contents = [NSString stringWithContentsOfFile:STDERR_OUT usedEncoding:&encoding error: &error];
	//contents = [contents stringByReplacingOccurrencesOfString:@"\n" withString:@"\n\n"];
	if (![contents isEqualToString:textView.text])
	{
	[textView setText:contents];
	
	textView.contentOffset = CGPointMake(0.0f, MAX(textView.contentSize.height - textView.frame.size.height, 0.0f));

	}
	
	[NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
}
- (void) viewDidLoad
{
    [super viewDidLoad];
	
    [self updateNavigationItemAnimated: NO]; // first time up, put it up
	
	// errors were redirected during initial startup in the appdelegate
	[NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
	
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];
	
	//  [self addNotificationObservers];
	
    [self reloadUserInfo];
	
    UITableView *tabView = (UITableView *) self.view;
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];
	
	//  [self removeNotificationObservers];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.appDelegate.SettingsViewController = nil; // mark us gone
	
	// [self removeNotificationObservers];
	
    [self->calendar_ release];
    [self->dateFormatter_ release];
    [self->documents_ release];
	
    [super dealloc];
}

- (id) init
{
    self = [super init];
	
    if (self)
    {
		
        self.appDelegate.SettingsViewController = self;
		self->lastzipcount = 0; // make sure it never does match !! got wierd when assigned to -1
		self->insideImport = NO;
    }
	
    return self;
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return SECTION_COUNT;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier0 = @"InfoCell0";
    static NSString *CellIdentifier1 = @"InfoCell1";
	NSString        *cellIdentifier;
	
    switch (idxPath.section)
    {
        case ABOUT_SECTION :
        default :
            cellIdentifier = CellIdentifier0;
            break;
			
        case DOCUMENTS_SECTION :
            cellIdentifier = CellIdentifier1;
            break;
			
    }
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
	
    if (!cell)
    {
        switch (idxPath.section)
        {
            case ABOUT_SECTION :
                cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle //UITableViewCellStyleValue1
                                               reuseIdentifier: cellIdentifier]
                        autorelease];
                break;
				
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
        case ABOUT_SECTION :
        {
            switch (idxPath.row)
            {
                case ABOUT_VERSION_ROW :
                {
					
                    NSUInteger zipcount=[DataManager zipcountItunesInbox];
					NSUInteger setlistcount=0;
					cell.textLabel.text =@"Add More from iTunes";
					NSString *s = [[DataManager sharedInstance] allocInfoProgressString];
					cell.detailTextLabel.text = s;
					if (self->insideImport ==NO)
					{
                    if ((zipcount!=0) || (setlistcount !=0))cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
					}
                    break;
                }
					
                case FACTORY_SETTINGS_ROW :
                {
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@",
                                           NSLocalizedString (@"Remove All Archives", @"")];
					
                    if (self->insideImport == NO) cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
                    break;
                }
					
				case MUSICSTAND_MANUAL_ROW :
                {
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@",
                                           NSLocalizedString (@"GigStand Manual", @"")];
					
                     if (self->insideImport == NO)  cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
                    break;
                }
					
                default :
                    cell = nil;
                    break;
            }
			
            break;
        }
        case DOCUMENTS_SECTION :
        {
			if (idxPath.row == 0)
			{
				// special case for All Tunes Controller
				
                if (self->insideImport == NO)  cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                cell.textLabel.text = @"Every Last Tune In GigStand";
				cell.imageView.image = [UIImage imageNamed:@"MusicStand_72x72.png"];
			}
			else {
				if ((idxPath.row <= [[DataManager sharedInstance].archives count]))
				{				
				 if (self->insideImport == NO) 	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
					cell.textLabel.text = [[DataManager sharedInstance].archives objectAtIndex: idxPath.row -1];
					cell.imageView.image  = [UIImage 
											 imageWithContentsOfFile:[[DataStore pathForSharedDocuments ] 
																	  stringByAppendingPathComponent:
																	  [[DataManager sharedInstance].archivelogos 
																	   objectAtIndex: idxPath.row-1]]];
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
		case ABOUT_SECTION :
			return ABOUT_ROW_COUNT;
			
		case DOCUMENTS_SECTION :
			return [[DataManager sharedInstance].archives count]+1;
			
			
			
		default :
			return 0;
	}
}

- (NSString *) tableView: (UITableView *) tabView
 titleForFooterInSection: (NSInteger) sect
{
	switch (sect)
	{
		case ABOUT_SECTION :
		{  
			return NSLocalizedString (@"    Add more archives and setlists with iTunes File Sharing on Device>iPad>Apps", @"");
			
		}
		default :
			return nil;
			
		case DOCUMENTS_SECTION :			
			return @"    for support please visit www.gigstand.net";
	}
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
	
	
    SettingsManager *settings = self.appDelegate.settingsManager;
	
	switch (sect)
	{
		case ABOUT_SECTION :
		{			
			
			
			NSString *s = [NSString stringWithFormat:@"    %@ total files: %d comprising %d unique tunes",
						   [NSString stringWithFormat:
							@"%@ %@ %@",
							settings.applicationName,
							NSLocalizedString (@"Version", @""),
							settings.applicationVersion],
						   [[DataManager sharedInstance].totalFiles unsignedIntValue],
						   [[DataManager sharedInstance].uniqueTunes unsignedIntValue]];
			
			return NSLocalizedString (s, @"");
		}
			
		case DOCUMENTS_SECTION :
		{
			switch ([[DataManager sharedInstance].archives count])
			{
				case 0 :
					return NSLocalizedString (@"    No Archives on this iPad", @"");
					
				case 1 :
					return NSLocalizedString (@"    1 Archive on this iPad", @"");
					
				default :
					return [NSString stringWithFormat:
							NSLocalizedString (@"    %ld Archives on this iPad", @""),
							[[DataManager sharedInstance].archives count]];
			}
		}
			
		default :
			return nil;
	}
}

- (UIView *)allocNewTableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
	// create the parent view that will hold header Label
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 
																  self.parentViewController.view.bounds.size.width, 44.0f)];
	
	// create the button object
	UILabel * headerLabel = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
	headerLabel.backgroundColor = [UIColor clearColor];//[DataManager sharedInstance].appColor;
	headerLabel.opaque = NO;
	headerLabel.textColor = [UIColor whiteColor];
	headerLabel.highlightedTextColor = [UIColor yellowColor];
	headerLabel.font = [UIFont boldSystemFontOfSize:14];
	headerLabel.frame = CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, 44.0f);
	
	// If you want to align the header text as centered
	// headerLabel.frame = CGRectMake(150.0, 0.0, 300.0, 44.0);
	
	headerLabel.text = [self tableView: tableView titleForFooterInSection: section];
	[customView addSubview:headerLabel];
	
	return customView;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
	return [self allocNewTableView: tableView viewForFooterInSection:section];
}
- (CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
	return 44.0f;
}
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
	// create the parent view that will hold header Label
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, 44.0f)];
	
	// create the button object
	UILabel * headerLabel = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
	headerLabel.backgroundColor = [UIColor clearColor];//[DataManager sharedInstance].appColor;
	headerLabel.opaque = NO;
	headerLabel.textColor = [UIColor whiteColor];
	headerLabel.highlightedTextColor = [UIColor yellowColor];
	headerLabel.font = [UIFont boldSystemFontOfSize:16];
	headerLabel.frame = CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, 44.0f);
	
	// If you want to align the header text as centered
	// headerLabel.frame = CGRectMake(150.0, 0.0, 300.0, 44.0);
	
	headerLabel.text = [self tableView: tableView titleForHeaderInSection: section];
	[customView addSubview:headerLabel];
	
	return customView;
}
- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
	return 44.0f;
}


#pragma mark UITableViewDelegate Methods


- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return 60.0f;
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	switch (idxPath.section)
	{
		case ABOUT_SECTION :
		{
			switch (idxPath.row)
			{
					
				case ABOUT_VERSION_ROW:
				{
					
                    NSUInteger zipcount=[DataManager zipcountItunesInbox];
					NSUInteger setlistcount=0;
                    if ((zipcount!=0) || (setlistcount !=0))					[self processItunesInbox];
					return;
				}
					
				case MUSICSTAND_MANUAL_ROW:
				{
					NSURL *splashURL = [NSURL URLWithString:@"http://gigstand.net/gigstandmanual.pdf"];
					
					if (splashURL)
					{
						WebViewController *wvc = [[[WebViewController alloc]
												   initWithURL: splashURL]
												  autorelease];
						
						wvc.navigationItem.hidesBackButton = NO;
						
						// take title from environment variable
						wvc.title = NSLocalizedString (@"Gig Stand Help", @"");
						
						UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
						
						[self presentModalViewController:nav animated: YES];
						
					}
					return;
				}
					
				case FACTORY_SETTINGS_ROW:
					
				{
					
					BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
					
					if (settingsCanEdit)
					{
						av = [[UIAlertView alloc] initWithTitle:@"Are You Sure?"
														message: @"you will need to reload thru iTumes"
													   delegate: self
											  cancelButtonTitle: @"Cancel"
											  otherButtonTitles: @"OK",nil] ;
						
						[av show];
					}
					else {
						av = [[UIAlertView alloc] initWithTitle:@"GigStand is Locked"
														message: @"go to iPad settings to unlock"
													   delegate: self
											  cancelButtonTitle: nil
											  otherButtonTitles: @"OK",nil] ;
						
						[av show];
					}
					
					
					return;
				}
				default: return;
			}
		}
			
		case DOCUMENTS_SECTION :
		{
			
			NSUInteger archIdx =idxPath.row;
			
			if (archIdx == 0)
			{
				// special case for All Tunes Controller
				[self alltunespressed];
			}
			else 
			{
				
				NSString *archive = [[DataManager sharedInstance].archives objectAtIndex: archIdx-1];
				
				// dynamically build the array of items  of RefNodesin this archiv
				NSArray *archiveItems =   [DataManager    allocItemsFromArchive: archive];
				
				
				
				
				AllTunesViewController *zvc = [[[AllTunesViewController alloc] initWithArray:archiveItems 
																				   andTitle:[NSString stringWithFormat:@"archive: %@",archive]]
											   autorelease];
				
				zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
				UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
				
				[self presentModalViewController:nav animated: YES];
				[archiveItems release];
				
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
	if (index!=0) { // not cancelled
		// come here after confirmation
		[[DataManager sharedInstance] factoryReset];
		[alertView release];
		
		[self reloadUserInfo];
		
		// need to clean out data structures
		
		//exit(0); // really leave so that we come back up clean in itunes
	}
}
@end
