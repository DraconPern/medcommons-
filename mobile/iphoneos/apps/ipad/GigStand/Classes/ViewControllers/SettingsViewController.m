//
//  SettingsViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "SettingsViewController.h"
#import "GigStandHomeController.h"
#import "WebViewController.h"
#import "DataStore.h"
#import "DataManager.h"
#import "AllSetListsController.h"
#import "AllArchivesListController.h"
#import	"LoggingViewController.h";
#import "ProgressString.h"
#import "ZipArchive.h"
#import "InboxInfoController.h";
#import "ModalAlert.h"
#import "SamplesController.h";
#import "SettingsManager.h";
#import "ArchivesManager.h"
#import "ArchivesManager.h"
#import "MatesViewController.h"
#import "GigStandAppDelegate.h"
#import "SetListsManager.h"

#import "TunesManager.h"

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
    SECTION_COUNT
};

//
// About table section rows:
//
enum
{
    ABOUT_PROCESS_INBOX = 0,  // MUST be kept in display order ...

	ABOUT_MANAGE_ARCHIVES,  // MUST be kept in display order ...
	ABOUT_MANAGE_LISTS,
	ABOUT_ADD_SAMPLES,
	//ABOUT_GIGSTAND_HELP,
    ABOUT_ROW_COUNT
};



@interface SettingsViewController () < UITableViewDataSource, UITableViewDelegate,UITextViewDelegate>


- (void) updateNavigationItemAnimated: (BOOL) animated;


- (void) reloadUserInfo;

-(void) processItunesInbox;

-(void) processDirect;

@end

@implementation SettingsViewController

- (void) pushToSamples
{
	
	SamplesController *aModalViewController = [[[SamplesController alloc] init] autorelease];
	aModalViewController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
	
	[self.navigationController presentModalViewController:nav animated:YES];
}
- (void) pushToLists
{
	
	AllSetListsController *aModalViewController = [[[AllSetListsController alloc] init] autorelease];	
	
	[self.navigationController pushViewController:aModalViewController animated:YES];
}
- (void) pushToArchives
{
	
	AllArchivesListController *aModalViewController = [[[AllArchivesListController alloc] init] autorelease];	
	
	[self.navigationController pushViewController:aModalViewController animated:YES];
}
-(void)moveAllSamples
{
	
	NSString *p = [ [SettingsManager sharedInstance] plistForSamples];
	NSString *s = p ? p :@"fail";
	
	NSString     *cvcPath = [[NSBundle mainBundle] pathForResource: s ofType: @"plist" ];	
	
	NSDictionary *cvcDict = [NSDictionary dictionaryWithContentsOfFile: cvcPath];
	
	NSLog (@"moveAllSamples %@ %@ %@",s,cvcPath,cvcDict);
	
	NSArray *archivesamples = [cvcDict objectForKey: @"SampleArchiveNames"];
	
	
	for (NSString *path in archivesamples) 
	{
		NSError *error;
		
		[[NSFileManager defaultManager] copyItemAtPath:[[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:path] 
												toPath:[[DataStore pathForItunesInbox] stringByAppendingPathComponent:path]
												 error:&error];
		
	}
	
}
#pragma mark Public Instance Methods

-(void) selfClean 
{
	// important: delete the incoming file once we processed it
	[[NSFileManager defaultManager] removeItemAtPath:self->incoming  error:NULL];
	[self->incoming release];
	[self->iname release];
	self->displayIncomingInfo = NO;
}
	

-(void) doOneZip:(NSString *)type;
{
	// implicit args are important here
	ZipArchive *za = [[ZipArchive alloc] init];
	
	NSString *archive = [DataStore pathForArchive:self->iname];
	
	if ([za UnzipOpenFile: self->incoming]) {
		BOOL ret = [za UnzipFileTo: archive overWrite: YES];
		if (ret)
		{
			[za UnzipCloseFile];
		}
	
		[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"zip %@",self->iname]];
		
		NSUInteger count = [ArchivesManager convertDirectoryToArchive:self->iname ] ;// hope this works
		NSLog (@"Asimmilated %d entries into %@",count,self->iname);//NSLog (@" processed zip file fullpath %@", fname);
		//later, at a convenient time such as application quit
		[[GigStandAppDelegate sharedInstance] saveContext];
		//NSLog (@"flushed managed object context");
	}
	[za release];
	[self selfClean];
	
}

-(void) doOneStl:(NSString *)type;
{
	
	NSError *error;
	NSRange pos = [iname rangeOfString:@"."]; // split this
	[[NSFileManager defaultManager] copyItemAtPath:incoming
											toPath:[[DataStore pathForTuneLists] 
													stringByAppendingPathComponent:[NSString stringWithFormat:@"%@%@", [iname substringToIndex:pos.location],@".plist"]]
											 error:&error];
	// delete the stl file once we have a good copy
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"processed stl %@",self->iname]];
	NSLog (@" processed stl file incoming %@", incoming);
	
	[self selfClean];
}
-(void) doOneImage:(NSString *)type;
{
    [ArchivesManager copyFromInboxToOnTheFlyArchive:self->incoming ofType:type withName:self->iname];
	NSLog (@" processed doOneImage file incoming %@", incoming);
	
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"processed doOneImage %@",self->iname]];
	
	[self selfClean];
}
-(void) doOneOpaqueWebView:(NSString *)type;
{
	[ArchivesManager copyFromInboxToOnTheFlyArchive:self->incoming ofType:type withName:self->iname];
	NSLog (@" processed doOneOpaqueWebView file incoming %@", incoming);
	
	
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"processed doOneOpaqueWebView %@",self->iname]];
	[self selfClean];
}
- (void) doOneAssembledWebView:(NSString *)type;
{
[ArchivesManager copyFromInboxToOnTheFlyArchive:self->incoming ofType:type withName:self->iname];
	NSLog (@" processed doOneAssembledWebView file incoming %@", incoming);	
	
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"processed doOneAssembledWebView %@",self->iname]];
	
	[self selfClean];
}
	
-(BOOL) processIncomingFromItunes;
{

	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForItunesInbox]  error: NULL];
	if (paths)
	{
		
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSString *fullpath = [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path];
				
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: fullpath
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
				{
					
					NSString *filetype = [fullpath pathExtension];  // returns @"zip"
					NSString *filename = [fullpath lastPathComponent]; 
					
					//NSLog (@"processing %@ of type %@", filename, filetype);
					
					self->incoming = [[NSString alloc] initWithString:fullpath];
					self->iname = [[NSString alloc] initWithString:filename];							
					self->displayIncomingInfo = YES;
					
					if ([@"zip" isEqualToString:filetype])
					
						//case @"zip":
						{
							[self performSelector:@selector(doOneZip:) withObject:filetype afterDelay:.001f];
							
							return YES;
						}
					
					else 
						if ([@"stl" isEqualToString:filetype])
						
							//case @"stl":
							{
								
								[self performSelector:@selector(doOneStl:) withObject:filetype afterDelay:.001f];
								
								return YES;
							}
						else 
							if ([@"jpg" isEqualToString:filetype] 
								|| [@"jpeg" isEqualToString:filetype]
								|| [@"png" isEqualToString:filetype]
								|| [@"gif" isEqualToString:filetype]
								
								|| [@"mp3" isEqualToString:filetype]
								|| [@"m4v" isEqualToString:filetype]
								)
								
								//case @"image":
							{
								
								[self performSelector:@selector(doOneImage:) withObject:filetype afterDelay:.001f];
								
								return YES;
							}
							else 
								if ([@"pdf" isEqualToString:filetype] 
									|| [@"html" isEqualToString:filetype]
									|| [@"doc" isEqualToString:filetype]
									|| [@"rtf" isEqualToString:filetype]
									)
									
									//case @"opaque document webview will handle alone":
								{
									
									[self performSelector:@selector(doOneOpaqueWebView:) withObject:filetype afterDelay:.001f];
									
									return YES;
								}
								else 
									if ([@"txt" isEqualToString:filetype] 
										|| [@"htm" isEqualToString:filetype]
										)
										
										//case @"processed document assemble before webview ":
									{
										
										[self performSelector:@selector(doOneAssembledWebView:) withObject:filetype afterDelay:.001f];
										
										return YES;
									}
						
	
				}// not a regular file (in this case a zip file or something else)
				
			}// not .DS_store and for loop
	} // have some paths
	return NO;
}

#pragma mark Private Instance Methods

-(NSString *) allocInfoProgressString;
{
	
	NSUInteger zipcount=[DataManager incomingInboxDocsCount];
	

	NSString *s =  [NSString stringWithFormat:@"inbox: %d files to process",zipcount];
	return [s retain];

}

-(void) donePressed;
{
	//[self.parentViewController dismissModalViewControllerAnimated:YES];
	[self.navigationController popViewControllerAnimated:YES];
	//NSLog (@"SVC donePressed");
}

-(void) processDBRebuild
{	
	if (aTimer == nil) return;
	aTimer=nil;
	// come here after the Inbox has been polled, essentially rebuild the entire db
	
	NSLog (@"processDBRebuild setupDB commencing...");[ArchivesManager setupDB]; NSLog (@"processDBRebuild setupDB finished...");
	//;
	
	
	
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"Assimilation of iTunes Inbox Completed"
													]];

	[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
	[self updateNavigationItemAnimated: NO]; // put buttons back
	
	self.navigationController.navigationBarHidden = NO;
	
	
	if (self->activityIndicator) 
	{
	[self->activityIndicator removeFromSuperview];
	[self->activityIndicator release];
	self->activityIndicator = nil;
	}
	

	
	[ModalAlert say:[NSString stringWithFormat:@"iTunes Inbox Complete %d archives %d setlists",
					 archive_import_count,setlist_import_count]];
	self->insideImport = NO;
	
//	[[DataManager sharedInstance].progressString text:self->incoming];
	
//	[self reloadUserInfo];
	[self donePressed]; // force pop back to outer level
}

-(void) processDBRebuildstart
{
	
	if (aTimer == nil) return;
	aTimer = nil;
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"Commencing Assimilation - %d archives, %d setlists",
											   archive_import_count,setlist_import_count]];

	aTimer = [NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processDBRebuild) userInfo:nil repeats:NO];
}
-(void) processItunesInboxInner {
	if (aTimer == nil) return;
	aTimer=nil;
	
	BOOL didSomething = [self processIncomingFromItunes]; // puts up alert while working
	if (!didSomething)
	{
		
		aTimer = [NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processDBRebuildstart) userInfo:nil repeats:NO];
		[self reloadUserInfo];
		return;
		
	}
	else
	{
		archive_import_count++;
		aTimer = [NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processItunesInboxInner) userInfo:nil repeats:NO];
		[self reloadUserInfo];
	}
	
}


-(void) processDirect
{
	
	self->insideImport = YES;
	
	[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
	
	
	//
    // Add Activity indicator view:
    //
    self->activityIndicator= 
	[[UIView alloc] init]; //[[UIImageView alloc] initWithImage:   [UIImage imageNamed:@"Default.png"]];
  
	CGRect frame = self.parentViewController.view.bounds; // inset this a little bit
	
	frame = [DataManager busyOverlayFrame:frame];
	
	self->activityIndicator.frame  =frame;
	self->activityIndicator.backgroundColor  =[DataManager applicationColor];
	
	UIActivityIndicatorView *indicator = [[[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge] autorelease];
	
	// Adjust the indicator so it is up a few pixels from the bottom of the alert
	indicator.center = CGPointMake(frame.size.width / 2.f, frame.size.height /2.f);
	[indicator startAnimating];
	[self->activityIndicator addSubview:indicator];
	
	// display in non-offensive
	// manner
	
    [self.view addSubview: self->activityIndicator];
	
	//turn off the nav for now
	self.navigationController.navigationBarHidden = YES;
	
	// at this point the titleDictionary and variant structure has been rebuilt either thru expandFiles or by the reload of the TitlesSection
	archive_import_count=setlist_import_count=0;

	[self processItunesInboxInner]; // this completes at ast level sometime later, i dont really like this
}
-(void) processItunesInbox
{
	
	
	BOOL answer = [ModalAlert ask:@"Processing the iTunes Inbox might take a long time. \r\nPlease be patient. \r\nAre you sure you want to proceed?"];
	if (answer == YES)
	{
		[self processDirect];
	}
}

- (void) didReceiveSecretHandshake: (UIGestureRecognizer *) gr
{
	LoggingViewController *asvc = [[[LoggingViewController alloc] 
									 init ] autorelease];
	
	[self.navigationController pushViewController: asvc animated: YES];
}

- (void) reloadUserInfo;
{
	[self updateNavigationItemAnimated: YES];
	
    UITableView    *tabView = (UITableView *) self.view;
	
    [tabView reloadData];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
	
	self.navigationItem.titleView = [[DataManager allocTitleView:@"Actions"] autorelease];
	
	//self.navigationItem.leftBarButtonItem =
//	[[UIBarButtonItem alloc] initWithTitle:@"back" style: UIBarButtonItemStylePlain target:self action:@selector(donePressed)];
//	
	self.navigationItem.hidesBackButton = NO; 
	if (([DataManager incomingInboxDocsCount]>0))
	{
	self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"process iTunes Inbox" 
																			  style:UIBarButtonItemStyleBordered
																			 target: self 
																			 action: @selector (processItunesInbox)];
	}
}
-(void) invalidateTimer
{
	if (aTimer) 
	{
		[aTimer invalidate];
		[aTimer release];
		aTimer = nil;
	}
}

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
	NSLog (@"SVC didReceiveMemoryWarning");
	[self->mainTableView release]; 
	self->mainTableView = nil;
	[self invalidateTimer];
    [super didReceiveMemoryWarning];
	
}

- (void) loadView
{
	
	aTimer = nil;
	
	//NSLog (@"SVC loadview");
	//[ArchivesManager dump];
	CGRect frame = self.parentViewController.view.bounds; //[[UIScreen mainScreen] applicationFrame];

	
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: frame style: UITableViewStyleGrouped] autorelease];
	tmpView.backgroundColor =  [UIColor lightGrayColor]; 
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
	UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self action: @selector (didReceiveSecretHandshake:)] autorelease];
		
	tgr.numberOfTapsRequired = 4;
		
	[tmpView addGestureRecognizer: tgr];

	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	
	[self updateNavigationItemAnimated:YES];

	
	self->mainTableView = tmpView;
	
	
	self.view = tmpView	;
	if (self->autostart==YES)
	{
		[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self 
				  
						selector:@selector(processDirect) userInfo:nil repeats:NO];
	}
		
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	
//	NSLog (@"SVC didRotateFromInterfaceOrientation %d",(UIInterfaceOrientation) fromOrient);
 //   self.view = [self allocBuildSearchUI] ; // rebuild whole UI
}
- (void) viewDidAppear: (BOOL) animated
{
	
	//NSLog (@"SVC viewDidAppear");
    [super viewDidAppear: animated];
	
   // [(UITableView *) self.view flashScrollIndicators];
}
- (void) monitorAST: (NSTimer *) timer;
{
	if (aTimer == nil) return; // if already cloeared, then ignore this
	
	aTimer = nil; // not in timer
	// while we are at it, lets monitor the inbox
	
	[self reloadUserInfo]; // do this unconditionally whilst monitoring inbox
	
	aTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
}
- (void) viewDidLoad
{
	
    [self updateNavigationItemAnimated: NO]; // first time up, put it up
	
    [super viewDidLoad];
	
	// errors were redirected during initial startup in the appdelegate
	aTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(monitorAST:) userInfo:nil repeats:NO];
	
}
- (void) viewDidUnload

{
	//NSLog (@"SVC viewDidUnLoad");
	[self->mainTableView release]; 
	self->mainTableView = nil;
	[self invalidateTimer];
    [super viewDidUnload];
	
	
}
- (void) viewWillAppear: (BOOL) animated
{	
	//NSLog (@"SVC viewWillAppear");
    [super viewWillAppear: animated];

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
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	// this doesnt get called all the time? wtf?
	//NSLog (@"SVC dealloc");
	// crashes here if uncommented
//	[self->mainTableView release]; 
//	self->mainTableView = nil;
	[self invalidateTimer];
    [super dealloc];
}

- (id) init
{
    self = [super init];
	
    if (self)
    {
		self->mainTableView = nil;
		self->lastzipcount = 0; // make sure it never does match !! got wierd when assigned to -1
		self->insideImport = NO;
			self->autostart = NO;
    }
	//NSLog (@"SVC init");
    return self;
}

- (id) initWithAutoStart:(BOOL)moveSamples;
{
	self = [super init];
	
    if (self)
    {
		self->mainTableView = nil;
		self->lastzipcount = 0; // make sure it never does match !! got wierd when assigned to -1
		self->insideImport = NO;
		
		self->autostart = YES;
		
		if (moveSamples)
		[self moveAllSamples];
		
    }
	//NSLog (@"SVC initWithAutoStart");
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
//    static NSString *CellIdentifier1 = @"InfoCell1";
	NSString        *cellIdentifier;
	
    switch (idxPath.section)
    {
        case ABOUT_SECTION :
        default :
            cellIdentifier = CellIdentifier0;
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
				case ABOUT_ADD_SAMPLES:
				{
					cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
					cell.textLabel.text =@"Manage Built-In Samples";
					cell.detailTextLabel.text = @"add to your iTunes Inbox";
					break;
				}
                case ABOUT_PROCESS_INBOX :
                {
					
                    NSUInteger zipcount=[DataManager incomingInboxDocsCount];
					cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
					if (self->insideImport ==NO)
					{
                    if ((zipcount!=0))cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
						[[DataManager sharedInstance].progressString text:[[self allocInfoProgressString] autorelease]]; // had autorelease
					}
					
					cell.textLabel.text =@"Manage iTunes Inbox";
					NSString *s = [[DataManager sharedInstance].progressString pretty]; //
					cell.detailTextLabel.text = s;
                    break;
                }
					
       
//				case ABOUT_GIGSTAND_HELP :
//                {
//                    cell.textLabel.text = [NSString stringWithFormat:
//                                           @"%@",
//                                           NSLocalizedString (@"gigstand.net", @"")];
//					
//					cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
//                    break;
//                }
					
				case ABOUT_MANAGE_ARCHIVES :
				{
					
					cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;  // off for now
					cell.textLabel.text =@"Manage Archives";
					cell.detailTextLabel.text  = [NSString stringWithFormat:@"archives: %d files: %d tunes: %d ",
												   [ArchivesManager archivesCount],
												  [TunesManager instancesCount],
												  [TunesManager tuneCount]];
					break;
				}
				case ABOUT_MANAGE_LISTS :
				{
					NSUInteger setlistcount = [[[SetListsManager newSetlistsScan] autorelease] count];
					cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
					cell.textLabel.text =@"Manage SetLists";
					if (setlistcount == 0) 
					cell.detailTextLabel.text=@"no setlists";
					else if (setlistcount ==1)
					cell.detailTextLabel.text=@"1 setlist";
					else cell.detailTextLabel.text = [NSString stringWithFormat:@"%d setlists",setlistcount];
					break;
				}
			
					
                default :
                    cell = nil;
                    break;
            }
			
            break;
        }
			
break;
//		}
			
        default :
            cell = nil;
            break;
	}
	
	return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{
	switch (sect)
	{
		case ABOUT_SECTION :
		{
			BOOL bonjourWithPeers = [[SettingsManager sharedInstance] bonjourWithPeers];
			if (bonjourWithPeers)
				return ABOUT_ROW_COUNT; else return ABOUT_ROW_COUNT -1; // leave off last "Friends Choice" if not set
			//
			
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
			if([GigStandAppDelegate sharedInstance].myLocalIP)
			{
				if([[GigStandAppDelegate sharedInstance].myLocalIP length]>5)
				{
					return [NSString stringWithFormat:@"    connect to %@:%d from any browser", 
							[GigStandAppDelegate sharedInstance].myLocalIP,
							[GigStandAppDelegate sharedInstance].myLocalPort]
					
					;
				}
			}
			
			return NSLocalizedString (@"    Add archives and setlists with iTunes File Sharing", @"");
			
		}
		default :
			return nil;
			//
//		case DOCUMENTS_SECTION :			
//			return @"    for support please visit www.gigstand.net";
	}
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{	
	switch (sect)
	{
		case ABOUT_SECTION :
		{			
			
			NSString *s = [NSString stringWithFormat:@"    %@",
						   [NSString stringWithFormat:
							@"%@ %@ %@",
							[DataManager sharedInstance].applicationName,
							NSLocalizedString (@"Version", @""),
							[DataManager sharedInstance].applicationVersion]];
						   			
			return NSLocalizedString (s, @"");
		}
			
		default :
			return nil;
	}
}

- (UIView *)allocNewTableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
	// create the parent view that will hold header Label
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 
																  self.parentViewController.view.bounds.size.width, [DataManager navBarHeight])];
	
	// create the button object
	UILabel * headerLabel = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
	headerLabel.backgroundColor = [UIColor clearColor];//[DataManager applicationColor];
	headerLabel.opaque = NO;
	headerLabel.textColor = [UIColor whiteColor];
	headerLabel.highlightedTextColor = [UIColor yellowColor];
	headerLabel.font = [UIFont boldSystemFontOfSize:14];
	headerLabel.frame = CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, [DataManager navBarHeight]);
	
	// If you want to align the header text as centered
	
	headerLabel.text = [self tableView: tableView titleForFooterInSection: section];
	[customView addSubview:headerLabel];
	
	return customView;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
	return [[self allocNewTableView: tableView viewForFooterInSection:section] autorelease];
}
- (CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
	return [DataManager navBarHeight];
}
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
	// create the parent view that will hold header Label
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, [DataManager navBarHeight])];
	
	// create the button object
	UILabel * headerLabel = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
	headerLabel.backgroundColor = [UIColor clearColor];//[DataManager applicationColor];
	headerLabel.opaque = NO;
	headerLabel.textColor = [UIColor whiteColor];
	headerLabel.highlightedTextColor = [UIColor yellowColor];
	headerLabel.font = [UIFont boldSystemFontOfSize:16];
	headerLabel.frame = CGRectMake(0.0f, 0.0f, self.parentViewController.view.bounds.size.width, [DataManager navBarHeight]);
	
	headerLabel.text = [self tableView: tableView titleForHeaderInSection: section];
	[customView addSubview:headerLabel];
	
	return [customView autorelease];
}
- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
	return [DataManager navBarHeight];
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
		case ABOUT_SECTION :
		{
			switch (idxPath.row)
			{
				case ABOUT_ADD_SAMPLES:
				{

					{
						[self pushToSamples];
					}
					return;
				}
				case ABOUT_PROCESS_INBOX:
				{
					InboxInfoController *zvc = [[[InboxInfoController alloc] init ]	autorelease];
					
					[self.navigationController pushViewController:zvc animated:YES];
					return;
				}
//					
//				case ABOUT_GIGSTAND_HELP:
//				{
//					NSURL *splashURL = [NSURL URLWithString:@"http://gigstand.net"];
//					
//					if (splashURL)
//					{
//						WebViewController *wvc = [[[WebViewController alloc]
//												   initWithURL: splashURL]
//												  autorelease];
//						
//						//wvc.navigationItem.hidesBackButton = NO;
//						
//						// take title from environment variable
//						wvc.title = NSLocalizedString (@"gigstand.net", @"");
//						
//						UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
//						
//						[self presentModalViewController:nav animated: YES];
//						
//					}
//					
//					return;
//				}
					
				case ABOUT_MANAGE_ARCHIVES:
				{
					[self pushToArchives];
					return;
				}
					
				case ABOUT_MANAGE_LISTS:
				{
					[self pushToLists];
					return;
				}
			
				default: return;
			}
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
		[ArchivesManager factoryReset];
		[alertView release];
		
		[self reloadUserInfo];
		
	}
}
@end
