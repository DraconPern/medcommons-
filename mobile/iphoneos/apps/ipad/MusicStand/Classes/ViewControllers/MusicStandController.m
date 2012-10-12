//
//  MusicStandController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

//#import "AdvancedSettingsViewController.h"
#import "AppDelegate.h"
#import "AsyncImageView.h"
//#import "DictionaryAdditions.h"
#import "DocumentsManager.h"
#import "MCDocumentTableViewCell.h"
//#import "HurlChooserController.h"
//#import "InfoHeaderView.h"
#import "MusicStandController.h"
//#import "LoginViewController.h"
//#import "Session.h"
//#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"
#import "ZipViewController.h"
#import "DataStore.h"
#import "DataManager.h"


#pragma mark -
#pragma mark Public Class MusicStandController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
    DOCUMENTS_SECTION = 0,  // MUST be kept in display order ...
    APPLIANCE_SECTION,
    SETTINGS_SECTION,
    ABOUT_SECTION,
    //
    SECTION_COUNT
};

//
// About table section rows:
//
enum
{
    ABOUT_VERSION_ROW = 0,  // MUST be kept in display order ...
    MEDCOMMONS_REGISTRATION_ROW,
	WEB_HOME_ROW,
    //
    ABOUT_ROW_COUNT
};

//
// Settings table section rows:
//
enum
{
    SETTINGS_REMEMBER_LOGIN_ROW = 0,    // MUST be kept in display order ...
    SETTINGS_USE_NOTES_ROW,             // featureLevel >= 1
    SETTINGS_USE_CAMERA_ROW,            // featureLevel >= 3
    //
    SETTINGS_ROW_COUNT_FL3,             // 'FL' == 'feature level'
    SETTINGS_ROW_COUNT_FL2      = SETTINGS_ROW_COUNT_FL3 - 1,
    SETTINGS_ROW_COUNT_FL1      = SETTINGS_ROW_COUNT_FL2
};

//
// Action sheet button indexes:
//
enum
{
    CHANGE_APPLIANCE_BUTTON_INDEX = 0
};

enum
{
    LOGOUT_BUTTON_INDEX = 0
};

//
// Assorted view tags:
//
enum
{
    CHANGE_APPLIANCE_ACTION_SHEET_TAG = 101,
    LOGOUT_ACTION_SHEET_TAG
};

@interface MusicStandController () < UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readonly)  NSCalendar      *calendar;
@property (nonatomic, retain, readonly)  NSDateFormatter *dateFormatter;
@property (nonatomic, retain, readonly)  NSArray         *documents;




- (void) updateNavigationItemAnimated: (BOOL) animated;

- (void) periodicNewFilePoller;


@end

@implementation MusicStandController

@dynamic    calendar;
@dynamic    dateFormatter;
@dynamic    documents;
@dynamic    hidesMasterViewInLandscape;


#pragma mark Public Instance Methods




#pragma mark Private Instance Methods


- (void) didReceiveSecretHandshake: (UIGestureRecognizer *) gr
{
    /*
	 AdvancedSettingsViewController *asvc = [[[AdvancedSettingsViewController alloc] init]
	 autorelease];
	 
	 [self.navigationController pushViewController: asvc
	 animated: YES];
     */
}


- (NSArray *) documents
{
    if (!self->documents_)
    {
		
       // DocumentsManager *docMgr = self.appDelegate.documentsManager;
       // NSString         *pdfUTI = (NSString *) kUTTypePDF;
       // NSString         *zipUTI = (NSString *) kUTTypeArchive;
		
		// this is a little wierd, scan for documents will expand zips but not add to the list of docs
		//NSArray          *docs = [docMgr documentsConformingToUTIs: [NSArray arrayWithObjects: zipUTI, nil]];
		// that was run just for side effects, now lets build a list of simple top level archive names
		
		self->documents_ =[[NSMutableArray array] retain];		// get the list of alldirectories and add those to the documents list
		NSFileManager *fM = [NSFileManager defaultManager];
		NSArray *fileList = [fM contentsOfDirectoryAtPath:[DataStore pathForSharedDocuments           ] error:NULL];
		//NSMutableArray *directoryList = [[[NSMutableArray alloc] init] autorelease];
		for(NSString *file in fileList) {
			NSString *path = [[DataStore pathForSharedDocuments           ]  stringByAppendingPathComponent:file];
			//NSLog (@"testing %@ for isdir?", path);
			BOOL isDir = NO;
			[fM fileExistsAtPath:path isDirectory:(&isDir)];
			if(isDir) {
				//NSLog (@"success %@ for isdir?", file);
				[self->documents_ addObject: file];
			}
		}
	
	//	NSLog (@"Expanded archives: %@", self->documents_);
		
	}
    
    return self->documents_;
}
- (void) reloadUserInfo
{
    [self updateNavigationItemAnimated: YES];
	
    UITableView    *tabView = (UITableView *) self.view;
	
    [tabView reloadData];
}
	


- (void) updateNavigationItemAnimated: (BOOL) animated
{
	//   SessionManager *sm = self.appDelegate.sessionManager;
    self.navigationItem.title = NSLocalizedString (@"Music Stand", @"");
    self.navigationItem.hidesBackButton = YES;
}

- (void) periodicNewFilePoller
{
	
    self.application.networkActivityIndicatorVisible = YES;
	BOOL didsomething = [[DocumentsManager sharedInstance] decompressAllZipFilesInDocumentDirectory]; // shud return BOOL if anything was done
	if (didsomething) {
		//[self.view release];
		[self loadView]; // resets 
	}
        self.application.networkActivityIndicatorVisible = NO;
	// check again in 10s
	[NSTimer scheduledTimerWithTimeInterval: 10.0f target:self selector:@selector(periodicNewFilePoller) userInfo:nil repeats:NO];
	
}
#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
    [AsyncImageView clearCache];
}

- (void) loadView
{
	
	
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
                                                         style: UITableViewStyleGrouped]
                            autorelease];
	
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
		
		//        UIView *secretView = ((InfoHeaderView *) tmpView.tableHeaderView).secretView;
		//
		//        secretView.userInteractionEnabled = YES;
		//
		//        [secretView addGestureRecognizer: tgr];
    }
	// now put up the initial view and then scan for files to add
	[NSTimer scheduledTimerWithTimeInterval: 2.0f target:self selector:@selector(periodicNewFilePoller) userInfo:nil repeats:NO];
    self.view = tmpView;
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

- (void) viewDidLoad
{
    [super viewDidLoad];
	
    [self updateNavigationItemAnimated: NO]; // first time up, put it up
	
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
    self.appDelegate.MusicStandController = nil; // mark us gone
	
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
		
        self.appDelegate.MusicStandController = self;
    }
	
    return self;
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
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
    static NSString *CellIdentifier2 = @"InfoCell2";
	
    SettingsManager *settings = self.appDelegate.settingsManager;
    NSString        *cellIdentifier;
	
    switch (idxPath.section)
    {
        case ABOUT_SECTION :
        case APPLIANCE_SECTION :
        default :
            cellIdentifier = CellIdentifier0;
            break;
			
        case DOCUMENTS_SECTION :
            cellIdentifier = CellIdentifier1;
            break;
			
        case SETTINGS_SECTION :
            cellIdentifier = CellIdentifier2;
            break;
    }
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
	
    if (!cell)
    {
        switch (idxPath.section)
        {
            case ABOUT_SECTION :
            case APPLIANCE_SECTION :
                cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
                                               reuseIdentifier: cellIdentifier]
                        autorelease];
                break;
				
            case DOCUMENTS_SECTION :
                cell = [[[MCDocumentTableViewCell alloc]
                         initWithReuseIdentifier: cellIdentifier]
                        autorelease];
                break;
				
            case SETTINGS_SECTION :
                cell = [[[MCSwitchTableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
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
                    cell.detailTextLabel.text = settings.applicationVersion;
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@ %@",
                                           settings.applicationName,
                                           NSLocalizedString (@"Version", @"")];
                    break;
                }
					
                case MEDCOMMONS_REGISTRATION_ROW :
                {
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@",
                                           NSLocalizedString (@"Factory Settings", @"")];
					
                    cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
                    break;
                }
					
				case WEB_HOME_ROW :
                {
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@",
                                           NSLocalizedString (@"MusicStand Manual", @"")];
					
                    cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
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
            if ((idxPath.row < [self.documents count]))
            {				
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                cell.textLabel.text = [self.documents objectAtIndex: idxPath.row];
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

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{
	//   SettingsManager *settings = self.appDelegate.settingsManager;
	
    switch (sect)
    {
        case ABOUT_SECTION :
            return ABOUT_ROW_COUNT;
			
        case APPLIANCE_SECTION :
            return 0; //[settings.knownAppliances count];
			
        case DOCUMENTS_SECTION :
            return [self.documents count];
			
        case SETTINGS_SECTION :
        {
			
            return 0;
        }
			
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
			NSDate *date = [NSDate date];
			return [NSString stringWithFormat:@"© 2010 planBsoft %@",date];
			
        
		}
			
        case APPLIANCE_SECTION :
        case SETTINGS_SECTION :
        default :
            return nil;
			
        case DOCUMENTS_SECTION :
            return NSLocalizedString (@"Add more archives with iTunes File Sharing on Device>iPad>Apps", @"");
    }
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {
        case ABOUT_SECTION :
		{
			NSFileManager *fm = [NSFileManager defaultManager];
			double squ = 1024.*1024.*1024.;
			NSDictionary *adict = [fm attributesOfFileSystemForPath:NSHomeDirectory() error:NULL];
			NSString *s = [NSString stringWithFormat:@"System size: %4.2fGB free: %4.2fGB app: %4.2fGB",[[adict objectForKey:NSFileSystemSize] doubleValue]/squ,
						    [[adict objectForKey:NSFileSystemFreeSize] doubleValue]/squ,
						   [ [DataManager sharedInstance].fileSpaceTotal longLongValue] ];
							 
            return NSLocalizedString (s, @"");
		}
			/*
			 case APPLIANCE_SECTION :
			 return NSLocalizedString (@"Choose an Appliance…", @"");
			 */
        case DOCUMENTS_SECTION :
        {
            switch ([self.documents count])
            {
                case 0 :
                    return NSLocalizedString (@"No Archives", @"");
					
                case 1 :
                    return NSLocalizedString (@"1 Archive", @"");
					
                default :
                    return [NSString stringWithFormat:
                            NSLocalizedString (@"%ld Archives", @""),
                            [self.documents count]];
            }
        }
			/*
			 case SETTINGS_SECTION :
			 return NSLocalizedString (@"Settings", @"");
			 */
        default :
            return nil;
    }
}

-(void) alertView:(UIAlertView *) alertView clickedButtonAtIndex: (int)index

{ 
	if (index!=0) { // not cancelled
	// come here after confirmation
	[[DocumentsManager sharedInstance] deleteAllFilesInDocumentDirectory];
	[alertView release];
	exit(0); // really leave so that we come back up clean in itunes
	}
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
				case WEB_HOME_ROW:
				{
					NSURL           *splashURL = [NSURL URLWithString:@"http://www.medcommons.net/m/MusicStand/MusicStandV01.pdf"];
					
                    if (splashURL)
                    {
                        WebViewController *wvc = [[[WebViewController alloc]
                                                   initWithURL: splashURL]
                                                  autorelease];
						
                        wvc.navigationItem.hidesBackButton = NO;
		
						
                        // take title from environment variable
                        wvc.title = NSLocalizedString (@"Music Stand Help", @"");
						
                        [self.navigationController pushViewController: wvc
                                                             animated: YES];
						
						break;
                    }
				}
					
                case MEDCOMMONS_REGISTRATION_ROW:
					
                {
					UIAlertView *av = [[[UIAlertView alloc] initWithTitle:@"Are You Sure?"
																 message: @"you will need to reload thru iTumes"
																delegate: self
													   cancelButtonTitle: @"Cancel"
														otherButtonTitles: @"OK",nil] 
									   autorelease];
					[av show];
					
					break;
				}
				default: break;
			}
		}
			
			
        case SETTINGS_SECTION :
        default :
            break;
			
        case APPLIANCE_SECTION :
        {
			
            break;
        }
			
        case DOCUMENTS_SECTION :
        {
            NSString *doc = [self.documents objectAtIndex: idxPath.row];
            if (doc)
            {
		
					ZipViewController *zvc = [[[ZipViewController alloc]
											   initWithBase:doc]
											  autorelease];
					
					zvc.title = [NSString stringWithFormat:@"%@:",doc];
				
				zvc.navigationItem.hidesBackButton = NO;
				
					
					
					[self.navigationController pushViewController: zvc
														 animated: NO];
					
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

@end
