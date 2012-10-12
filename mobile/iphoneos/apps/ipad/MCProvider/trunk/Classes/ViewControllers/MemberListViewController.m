//
//  MemberListViewController.m
//  MCProvider
//
//  Created by Bill Donner on 4/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//
// This mostly runs on the iphone but can be run on the iPad when it is in Mono Mode

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "Group.h"
#import "GroupsActionController.h"
#import "CCRViewController.h"
#import "Member.h"
#import "MemberListCell.h"
#import "MemberListViewController.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "DataManager.h"


#pragma mark -
#pragma mark Public Class MemberListViewController
#pragma mark -

#pragma mark Internal Constants

enum
{
    VIEWING_MODE_INBOX   = 0,
    VIEWING_MODE_MEMBERS
};

// Assorted view tags:
//
enum
{
    ERROR_ALERT_VIEW_TAG     = 666,
    ACCT_MADE_ALERT_VIEW_TAG,
    LOGIN_ALERT_VIEW_TAG,
	USER_CONSENTED_TAG,
    //
    LABEL1_TAG               = 901,
    LABEL2_TAG,
    LABEL3_TAG,
    LABEL4_TAG
};

@interface MemberListViewController () <UITableViewDataSource, UITableViewDelegate,UIAlertViewDelegate>

@property (nonatomic, retain, readwrite) MCActionController *groupsActionController;
@property (nonatomic, retain, readonly)  UISegmentedControl *segmentedControl;

- (void) addNotificationObservers;

- (void) chooseView: (id) sender;

- (NSArray *) membersForViewingMode: (NSUInteger) vMode;

- (void) removeNotificationObservers;

//
// Forward declarations:
//
- (void) groupInfoDidChange: (NSNotification *) notification;

-(void) consent_clickback;


-(void) doMemberForIndex:(NSUInteger) row;
@end

@implementation MemberListViewController

@synthesize groupsActionController = groupsActionController_;
@dynamic   segmentedControl;

#pragma mark Private Instance Methods

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
	
    [nc addObserver: self
           selector: @selector (groupInfoDidChange:)
               name: SessionManagerGroupInfoDidChangeNotification
             object: nil];
}

- (void) choices
{
    self.groupsActionController = [[[GroupsActionController alloc] init]
                                   autorelease];
	
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
        [self.groupsActionController showFromBarButtonItem: self.navigationItem.rightBarButtonItem
                                                  animated: YES];
    else
        [self.groupsActionController showInView: self.view];
}

- (void) chooseView: (UISegmentedControl *) sender
{
    self->viewingMode_ = self.segmentedControl.selectedSegmentIndex;
	
    [(UITableView *) self.view reloadData];
}

- (NSArray *) membersForViewingMode: (NSUInteger) vMode
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Group          *group = sm.loginSession.groupInFocus;
	
    switch (vMode)
    {
        case VIEWING_MODE_INBOX :
            return group.membersFilteredA;
			
        case VIEWING_MODE_MEMBERS :
            return group.membersFilteredB;
			
        default :
            return group.members;
    }
}

- (void) removeNotificationObservers
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}


-(void) titleandbacksplash: (Group *) group
{
	
    NSLog (@"Memberlistviewcontroller switching to %@", group.name);
	
    self.navigationItem.title = group.name; // should match segment from above
	
    if (logoView_)
    { // remove previous
        [logoView_ removeFromSuperview];
        [logoView_ release];
        logoView_ = nil;
    }
	
    // if we have a logo make a backsplash
	
    NSURL *logoURL = group.logoURL;
	
    if (logoURL)
    {
		
		
        StyleManager *styles = self.appDelegate.styleManager;
		
        logoView_ = [[AsyncImageView alloc]
                     initWithFrame: CGRectMake (0.0f, 0.0f, 100.0f, 100.0f)];
		
        logoView_.alpha = 0.10f;
        logoView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                      UIViewAutoresizingFlexibleLeftMargin |
                                      UIViewAutoresizingFlexibleRightMargin |
                                      UIViewAutoresizingFlexibleTopMargin);
        logoView_.center = CGPointMake (self.view.frame.size.width / 2.0f,
                                        self.view.frame.size.height /5.0f);
		
        [self.view addSubview: logoView_];
		
        [logoView_ loadImageFromURL: logoURL
                      fallbackImage: styles.fallbackGroupLogoImageXXL];
    }
}

- (UISegmentedControl *) segmentedControl
{
    if (!self->segmentedControl_)
    {
        SettingsManager *settings = self.appDelegate.settingsManager;
        NSDictionary    *options = settings.memberListDisplayOptions;
		
        self->segmentedControl_ = [[UISegmentedControl alloc] initWithItems: [NSArray arrayWithObjects:
                                                                              [options objectForKey: @"VisibleItemsLabel"],
                                                                              [options objectForKey: @"HiddenItemsLabel"],
                                                                              nil]];
		
        self->segmentedControl_.backgroundColor = [UIColor blackColor];
        self->segmentedControl_.momentary = NO;
        self->segmentedControl_.segmentedControlStyle = UISegmentedControlStyleBar;
        self->segmentedControl_.selectedSegmentIndex = 0;
        self->segmentedControl_.tintColor = [UIColor darkGrayColor];
		
        [self->segmentedControl_ addTarget: self
                                    action: @selector (chooseView:)
                          forControlEvents: UIControlEventValueChanged];
    }
	
    return self->segmentedControl_;
}
-(void) consent_clickback
{
	//
	// if we actually get here and didnt cancel out 
	[DataManager sharedInstance].userHasConsented = YES;
	[self doMemberForIndex:self->stashedidx ];
}

- (void) alertView: (UIAlertView *) av
clickedButtonAtIndex: (NSInteger) idx
{
    switch (av.tag)
    {
		case USER_CONSENTED_TAG :
		{
			if (idx == 1) 
				[self consent_clickback ];
            break;
		}
			
        case ERROR_ALERT_VIEW_TAG :
            exit (1);
            break;
			
			
			
        default :
            NSAssert1 (NO,
                       @"Unknown alert view tag: %d",
                       av.tag);
            break;
    }
}
#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
    [AsyncImageView clearCache]; // take it back to zero entries
}

- (void) loadView
{
    CGRect       tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    UITableView *tabView = [[[UITableView alloc] initWithFrame: tmpFrame
                                                         style: UITableViewStylePlain]
                            autorelease];
	
    tabView.backgroundColor = self.appDelegate.styleManager.backgroundColorLight;
    tabView.dataSource = self;
    tabView.delegate = self;
    tabView.rowHeight = [MemberListCell defaultCellHeight];
	
    self.view = tabView;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
{
    return YES;
}

- (void) viewDidLoad
{
    [super viewDidLoad];
	
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;
    Group *g = session.groupInFocus;
    [self titleandbacksplash:g];
	
	
	
    if ([sm.loginSession.groups count]>1)
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemOrganize
                                                                                               target: self
                                                                                               action: @selector (choices)];
	
	
    UIBarButtonItem *flexSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
                                                                                target: nil
                                                                                action: NULL]
                                  autorelease];
    UIBarButtonItem *segCtlBBI = [[[UIBarButtonItem alloc] initWithCustomView: self.segmentedControl]
                                  autorelease];
	
    self.toolbarItems = [NSArray arrayWithObjects:
                         flexSpace,
                         segCtlBBI,
                         flexSpace,
                         nil];
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    //
    // For some strange reason, the following two calls MUST go in
    // viewDidAppear; they do NOT work in viewWillAppear -- go figure:
    //
    self.navigationController.toolbar.barStyle = UIBarStyleBlack;
	
    [self.navigationController setToolbarHidden: NO
                                       animated: animated];
	
    [(UITableView *) self.view flashScrollIndicators];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    [self addNotificationObservers];
	
    NSIndexPath *idxPath = [(UITableView *) self.view indexPathForSelectedRow];
	
    if (idxPath)
        [(UITableView *) self.view deselectRowAtIndexPath: idxPath
                                                 animated: NO];
    //
    //  SessionManager *sm = self.appDelegate.sessionManager;
    //    Group          *group = sm.loginSession.groupInFocus;
    //  [self titleandbacksplash: group];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];
	
    [self removeNotificationObservers];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->groupsActionController_ release];
    [self->logoView_ release];
    [self->segmentedControl_ release];
	
    [super dealloc];
}

#pragma mark SessionManager Notification Methods

- (void) groupInfoDidChange: (NSNotification *) notification
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Group *g = sm.loginSession.groupInFocus;
    [self titleandbacksplash:g];
	
    [(UITableView *) self.view reloadData];
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier = @"MemberListCell";
	
    NSArray  *members = [self membersForViewingMode: self->viewingMode_];
    Member   *member = [members objectAtIndex: idxPath.row];
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];
	
    if (!cell)
        cell = [[[MemberListCell alloc]
                 initWithReuseIdentifier: CellIdentifier]
                autorelease];
	
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.highlighted = NO;
	
    ((MemberListCell *) cell).member = member;
    ((MemberListCell *) cell).showsPhoto = (self->viewingMode_ == VIEWING_MODE_MEMBERS);
	
    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
    return [[self membersForViewingMode: self->viewingMode_] count];
}

#pragma mark UITableViewDelegate Methods


-(void) doMemberForIndex:(NSUInteger) row;
{
	if (![DataManager sharedInstance].userHasConsented)
	{
		// put up an alert
		
		// Really force the user to enter something
		UIAlertView *av = [[[UIAlertView alloc] initWithTitle: NSLocalizedString (@"Clinical Use Warning", @"")
													  message: @"This viewer is not FDA approved and should not be used for clinical purposes" // IMPORTANT
													 delegate: self
											cancelButtonTitle: @"Cancel" // NSLocalizedString (@"Cancel", @"")
											otherButtonTitles: @"OK", nil] //NSLocalizedString (@"Enter", @""), nil]
						   autorelease];
		
		av.tag = USER_CONSENTED_TAG;

		
		self->stashedidx=row;
		
		
		// wait for clickback
		[av show];
		
	}
	else 
	{
		SessionManager *sm = self.appDelegate.sessionManager;
		
		// run a little wifi test to please our users
		
		[sm checkNetworkStatus]; // delay a bit to let things settle
		
		NSArray *members = [self membersForViewingMode: self->viewingMode_];
		Session *session = sm.loginSession;
		
		if (session.memberInFocus)
			[session.memberInFocus dump];
		
		Member *member = [members objectAtIndex: row];
		
		session.memberInFocus = member;
		
		
		[self.navigationController setToolbarHidden: YES
										   animated: YES];
		
		//
		// Choose controller based on environment setting:
		//
		SettingsManager *settings = self.appDelegate.settingsManager;
		
		
        CCRViewController *cvc = [[[CCRViewController alloc]
                                   initWithURL: [NSURL URLWithFormat:
                                                 settings.healthURLFormat,
                                                 session.appliance,
                                                 member.identifier,
                                                 session.authToken]]
                                  autorelease];
		
        [self.navigationController pushViewController: cvc
                                             animated: YES];
		
		
		
	}
}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	NSUInteger row = idxPath.row;
	[self doMemberForIndex:row];
}

@end
