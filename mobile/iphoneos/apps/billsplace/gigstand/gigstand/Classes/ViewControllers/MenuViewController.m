//
//  MenuViewController.m
//  gigstand
//
//  Created by bill donner on 4/1/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//



#import <MobileCoreServices/MobileCoreServices.h>
#import "iPadHomeSplashController.h"
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
#import "MenuViewController.h"
#import "CollaborationSplashViewController.h"
#import "LocalWebViewController.h"

#pragma mark -
#pragma mark Public Class MenuViewController
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



@interface MenuViewController () < UITableViewDataSource, UITableViewDelegate,UIAlertViewDelegate>
- (void) updateNavigationItemAnimated: (BOOL) animated;
- (void) reloadUserInfo;
@end

@implementation MenuViewController


#pragma mark Public Instance Methods

-(void) presentController:(UIViewController *) aModalViewController
{
    
    [(ipadHomeSplashController *)self->homeController popOff]; // remove popover up there
    [(ipadHomeSplashController *)self->homeController pushToController: aModalViewController];
    
    //    aModalViewController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    //   // UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
    //    //	
    //    //    
    //        if (self->dismissalMode == 0)[(ipadHomeSplashController *)self->homeController popOff]; // remove popover up there
    //    
    //   // //    
    //    
    //    
    //    [self->homeController presentModalViewController:aModalViewController animated: YES];
    //  //  [self->homeController.navigationController pushViewController:aModalViewController animated:YES];
}

-(void) presentControllerFormsheet:(UIViewController *) aModalViewController
{
    aModalViewController.modalPresentationStyle = UIModalPresentationFormSheet;
    ;
    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
    
    nav.modalPresentationStyle = UIModalPresentationFormSheet;
    //	
    //    
    //    if (self->dismissalMode == 0)
    [(ipadHomeSplashController *)self->homeController popOff]; // remove popover up there
    //    
    [self->homeController presentModalViewController:nav animated: YES];
}



-(void) rightButtonPressed: (id) obj
{   
    NSError *error;
    NSStringEncoding encoding;
    
    NSString *readmesrc = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"readme.html"];
   // NSData *readmebytes = [[NSFileManager defaultManager] contentsAtPath:readmesrc ];
    NSString *html = [NSString stringWithContentsOfFile:readmesrc usedEncoding:&encoding error:&error];
    
    if (html)
    {
        
        LocalWebViewController *wvc = 
        [[[LocalWebViewController alloc]
          initWithHTML: html title:@"GigStand Help" ]
         autorelease];
        
        
        UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
        
        
        //[self presentModalViewController:nav animated: YES];
        [(ipadHomeSplashController *)self->homeController popOff]; // remove popover up there
        //    
        [self->homeController presentModalViewController:nav animated: YES];
        
    }
    
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
    
    self->listItems = [[DataManager list:[SetListsManager makeSetlistsScan]  // recents need to go first so the "choices" for set list control are linear
                       bringToTop:[NSArray arrayWithObjects:@"recents",@"favorites",nil]] retain];
    
    if (self->alistItems) [self->alistItems release];
    
    self->alistItems = [[DataManager list:[ArchivesManager allEnabledArchives] 
                        bringToTop:[NSArray arrayWithObjects:[ArchivesManager nameForOnTheFlyArchive],nil]] retain];
    
}








- (void) updateNavigationItemAnimated: (BOOL) animated
{
    self.navigationItem.titleView = [DataManager makeAppTitleView:@"Content"] ;
    [self  setColorForNavBar];
    self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"help" style:UIBarButtonItemStylePlain target:self action:@selector(rightButtonPressed:)] autorelease];
    
}
- (void) reloadUserInfo;
{
    
    //NSLog (@"GSH reloadUserInfo");
    
    [self makenewlists];
    
    [self updateNavigationItemAnimated: NO];	
    UITableView    *tabView = (UITableView *) self.view;
    [tabView reloadData];
}

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    
    NSLog (@"GSH didReceiveMemoryWarning");
    [super didReceiveMemoryWarning];
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
    
    CGRect frame = [[UIScreen mainScreen] bounds];
    frame.size.height -=[DataManager statusBarHeight]; // shorten this up we are scrolling too far
    UITableView *tmpView = //[
    [[UITableView alloc] initWithFrame: frame style: UITableViewStyleGrouped];// autorelease];
    tmpView.backgroundColor =  [UIColor clearColor]; 
    tmpView.opaque = YES;
    tmpView.backgroundView = nil;
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor blackColor]; //was lightgray
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    
    
    [self updateNavigationItemAnimated:YES];
    self->mainTableView = tmpView;
    self.view = tmpView	;
    
    
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidLoad
{	
    [super viewDidLoad];
    //NSLog (@"GSH viewDidLoad");
    
    [self updateNavigationItemAnimated: NO]; // first time up, put it up
    
    
    
    
}



//////



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
    
    
    [self->mainTableView release]; // ***************
    
    [super dealloc];
}

- (id) initWithHomeController:(UIViewController *) pvc mode:(NSUInteger) mode;
{
    //NSLog (@"GSH init");
    self = [super init];
    if (self) 
    {
        self->listItems = nil;
        
        self->alistItems = nil;
        self->homeController = pvc;
        self->dismissalMode = mode;
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
                                             licount,aicount
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




#pragma mark UITableViewDelegate Methods


- (CGFloat) tableView: (UITableView *) tabView heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) 
        return [DataManager standardRowSize]*.8f;
    
    return [DataManager standardRowSize];
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    switch (idxPath.section)
    {	
        case FRONT_SECTION :			
        {
            if ( [TunesManager tuneCount] > 0)
            {
                SongsViewController *aModalViewController = [[[SongsViewController alloc] init] autorelease];	
                
                [self presentController:aModalViewController];
                return;
            }
        }
            
        case MATES_SECTION:
        {
            BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
            BOOL bonjourWithPeers  = [[SettingsManager sharedInstance] bonjourWithPeers];
            if (bonjourWithPeers&&collabfeatures)
            {
                
                
                {
                    
                    GigSnifferController *aModalViewController = [[[GigSnifferController alloc] init] autorelease];	
                    
                    aModalViewController.navigationItem.title = @"Bandmates Nearby";                
                    
                    [self presentController:aModalViewController];
                }
                return ;
            }
            
            
            
            CollaborationSplashViewController *aModalViewController = [[[CollaborationSplashViewController alloc] init ] autorelease];	
            
            [self presentControllerFormsheet:aModalViewController];
            
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
                SetListViewController *aModalViewController = [[[SetListViewController alloc]
                                                                initWithPlist:filepath name:filepath
                                                                edit:settingsCanEdit]
                                                               autorelease];
                
                
                ;
                [self presentController:aModalViewController];
                
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
                
                if ([archive isEqualToString:[ArchivesManager nameForOnTheFlyArchive]])
                    //if (archIdx==0)
                {
                    OnTheFlyController *aModalViewController = [[[OnTheFlyController alloc] init]	autorelease];
                    [self presentController:aModalViewController];
                }
                else {
                    
                    if ([ArchivesManager fileCount: archive] >50)
                    {
                        ArchiveViewController *aModalViewController = [[[ArchiveViewController alloc] initWithArchive:archive]	autorelease];
                        [self presentController:aModalViewController];
                    }
                    else 
                    {
                        
                        SmallArchiveViewController *aModalViewController = [[[SmallArchiveViewController alloc] initWithArchive: archive]	autorelease];
                        [self presentController:aModalViewController];
                        
                    }
                    
                }
                
                
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
