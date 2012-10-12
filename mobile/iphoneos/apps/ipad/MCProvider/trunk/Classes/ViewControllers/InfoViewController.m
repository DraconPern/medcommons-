//
//  InfoViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AdvancedSettingsViewController.h"
#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DictionaryAdditions.h"
#import "DocumentsManager.h"
#import "Group.h"
//#import "HurlChooserController.h"
#import "InboxGroupListController.h"
#import "InfoHeaderView.h"
#import "InfoViewController.h"
#import "LoginViewController.h"
#import "MemberListViewController.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"
#import "ZipViewController.h"
#import "AddressBarWebViewController.h"
#import "Member.h"

#pragma mark -
#pragma mark Public Class InfoViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
    ABOUT_SECTION     = 0,              // MUST be kept in display order ...
   // APPLIANCE_SECTION,
    SETTINGS_SECTION,
    DOCUMENTS_SECTION,
    //
    SECTION_COUNT
};

//
// About table section rows:
//
enum
{
    ABOUT_VERSION_ROW = 0,              // MUST be kept in display order ...
    ABOUT_REGISTER_ROW,
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

@interface InfoViewController () <LoginViewControllerDelegate, UIActionSheetDelegate, UIAlertViewDelegate, UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readwrite) UIActionSheet   *actionSheet;
@property (nonatomic, retain, readwrite) UIBarButtonItem *backButton;
@property (nonatomic, retain, readonly)  NSCalendar      *calendar;
@property (nonatomic, retain, readonly)  NSDateFormatter *dateFormatter;
@property (nonatomic, retain, readonly)  NSArray         *documents;
@property (nonatomic, retain, readwrite) UIBarButtonItem *inboxButton;
@property (nonatomic, retain, readwrite) UIBarButtonItem *logInButton;
@property (nonatomic, retain, readwrite) UIBarButtonItem *logOutButton;
@property (nonatomic, retain, readonly)  NSDateFormatter *timeFormatter;

- (void) addNotificationObservers;

- (void) clickedInboxButton;


- (void) didReceiveSecretHandshake: (UIGestureRecognizer *) gr;

- (NSString *) formatRememberLoginSettingName;

- (BOOL) isToday: (NSDate *) date;

- (void) logIn;

- (void) logOut;

- (void) reloadUserInfo;

- (void) removeNotificationObservers;

- (void) showActionSheet;

- (void) toggleRememberLogin: (id) sender;

- (void) toggleUseCamera: (id) sender;

- (void) toggleUseNotes: (id) sender;

- (void) updateNavigationItemAnimated: (BOOL) animated;

//
// Forward declarations:
//
- (void) logInSessionDidFinish: (NSNotification *) notification;

- (void) logInSessionDidStart: (NSNotification *) notification;

- (void) sessionInfoDidChange: (NSNotification *) notification;

@end

@implementation InfoViewController

@synthesize actionSheet                = actionSheet_;
@synthesize backButton                 = backButton_;
@dynamic    calendar;
@dynamic    dateFormatter;
@dynamic    documents;
@dynamic    hidesMasterViewInLandscape;
@synthesize inboxButton                = inboxButton_;
@synthesize logInButton                = logInButton_;
@synthesize logOutButton               = logOutButton_;
@dynamic    timeFormatter;

#pragma mark Public Instance Methods

- (id) initWithNoGroups
{
    self = [super init];

    if (self)
    {
        self->pushIntoSoloURL_ = nil;

        self.appDelegate.infoViewController = self;
    }

    return self;

}

- (id) initWithSoloURL: (NSString *) URL
{
    self = [super init];

    if (self)
    {
        self->pushIntoSoloURL_ = [URL copy];

        self.appDelegate.infoViewController = self;
    }

    return self;
}

#pragma mark Private Instance Methods

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (logInSessionDidFinish:)
               name: SessionManagerLogInSessionDidFinishNotification
             object: nil];

    [nc addObserver: self
           selector: @selector (logInSessionDidStart:)
               name: SessionManagerLogInSessionDidStartNotification
             object: nil];

    [nc addObserver: self
           selector: @selector (sessionInfoDidChange:)
               name: SessionManagerSessionInfoDidChangeNotification
             object: nil];
}

- (NSCalendar *) calendar
{
    if (!self->calendar_)
        self->calendar_ = [[NSCalendar alloc]
                           initWithCalendarIdentifier: NSGregorianCalendar];

    return self->calendar_;
}

- (void) clickedInboxButton
{
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
    {
        InboxGroupListController *iglc = [[[InboxGroupListController alloc] init]
                                          autorelease];

        [self.navigationController pushViewController: iglc
                                             animated:YES];
    }
    else
    {
        MemberListViewController *mlvc = [[[MemberListViewController alloc] init]
                                          autorelease];

        [self.navigationController pushViewController: mlvc
                                             animated: YES];
    }
}

//- (void) confirmApplianceChoice
//{
//    if (!self.actionSheet.isVisible)
//    {
//        NSString       *newAppliance = self.selectedAppliance;
//        AppDelegate    *appDel = self.appDelegate;
//        SessionManager *sm = appDel.sessionManager;
//
//        NSAssert ((newAppliance != nil) &&
//                  ![newAppliance isEqualToString: appDel.settingsManager.appliance],
//                  @"Bad appliance selection!");
//
//        NSString *cbTitle = ((appDel.targetIdiom != UIUserInterfaceIdiomPad) ?
//                             NSLocalizedString (@"Cancel", @"") :
//                             nil);
//        NSString *asTitle = ((appDel.targetIdiom != UIUserInterfaceIdiomPad) ?
//                             [NSString stringWithFormat:
//                              NSLocalizedString (@"Change Appliance to %@?", @""),
//                              newAppliance] :
//                             NSLocalizedString (@"Change to Selected Appliance?", @""));
//
//        if (sm.isLoggedIn)
//            asTitle = [asTitle stringByAppendingString:
//                       NSLocalizedString (@"\nYou will be logged out", @"")];
//
//        self.actionSheet = [[[UIActionSheet alloc] initWithTitle: asTitle
//                                                        delegate: self
//                                               cancelButtonTitle: cbTitle
//                                          destructiveButtonTitle: nil
//                                               otherButtonTitles:
//                             NSLocalizedString (@"Change Appliance", @""),
//                             nil]
//                            autorelease];
//
//        self.actionSheet.tag = CHANGE_APPLIANCE_ACTION_SHEET_TAG;
//
//        [self showActionSheet];
//    }
//}

- (NSDateFormatter *) dateFormatter
{
    if (!self->dateFormatter_)
    {
        self->dateFormatter_ = [NSDateFormatter new];

        [self->dateFormatter_ setDateStyle: NSDateFormatterMediumStyle];
        [self->dateFormatter_ setTimeStyle: NSDateFormatterNoStyle];
    }

    return self->dateFormatter_;
}

- (void) didReceiveSecretHandshake: (UIGestureRecognizer *) gr
{
    AdvancedSettingsViewController *asvc = [[[AdvancedSettingsViewController alloc] init]
                                            autorelease];

    [self.navigationController pushViewController: asvc
                                         animated: YES];
}

- (NSArray *) documents
{
    if (!self->documents_)
    {
        //
        // For now we only want PDF documents:
        //
        /*
         NSSortDescriptor *sort = [[[NSSortDescriptor alloc] initWithKey: @"title"
         ascending: YES
         selector: @selector (caseInsensitiveCompare:)]
         autorelease];
         */

        DocumentsManager *docMgr = self.appDelegate.documentsManager;
        NSString         *pdfUTI = (NSString *) kUTTypePDF;
        NSString         *zipUTI = (NSString *) kUTTypeArchive;
        NSArray          *docs = [docMgr documentsConformingToUTIs: [NSArray arrayWithObjects: pdfUTI, zipUTI, nil]];
        /*
         self->documents_ = [[docs sortedArrayUsingDescriptors: [NSArray arrayWithObject: sort]]
         retain];


         NSLog (@"docs is %@",docs);

         */

        self->documents_ =[docs copy];

    }
    //    NSLog (@"documents_ is %@",self->documents_);
    return self->documents_;
}

- (NSString *) formatRememberLoginSettingName
{
    AppDelegate     *appDel = self.appDelegate;
    SettingsManager *settings = appDel.settingsManager;
    SessionManager  *sm = appDel.sessionManager;

    if (sm.isLoggedIn && settings.rememberLogin)
    {
        NSDate *expDate = [NSDate dateWithTimeIntervalSinceReferenceDate:
                           settings.loginExpiration];

        return [NSString stringWithFormat:
                NSLocalizedString (@"Remember Me until %@", @""),
                ([self isToday: expDate] ?
                 [self.timeFormatter stringFromDate: expDate] :
                 [self.dateFormatter stringFromDate: expDate])];
    }

    return NSLocalizedString (@"Remember Me", @"");
}

- (BOOL) isToday: (NSDate *) date
{
    NSCalendarUnit   units = (NSDayCalendarUnit|
                              NSMonthCalendarUnit |
                              NSYearCalendarUnit);
    NSDate           *now = [NSDate date];
    NSDateComponents *nowComps = [self.calendar components: units
                                                  fromDate: now];
    NSDateComponents *inComps = [self.calendar components: units
                                                 fromDate: date];

    return (([inComps year] == [nowComps year]) &&
            ([inComps month] == [nowComps month]) &&
            ([inComps day] == [nowComps day]));
}

- (void) logIn
{
    LoginViewController *lvc = [[[LoginViewController alloc] init]
                                autorelease];

    lvc.delegate = self;

    if (UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPad)
        lvc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self presentModalViewController: lvc
                            animated: YES];
}

- (void) logOut
{
    if (!self.actionSheet.isVisible)
    {
        NSString *cbTitle = ((self.appDelegate.targetIdiom != UIUserInterfaceIdiomPad) ?
                             NSLocalizedString (@"Cancel", @"") :
                             nil);
        NSString *asTitle = NSLocalizedString (@"Log Out?\n"
                                               @"You will need to enter credentials again", @"");

        self.actionSheet = [[[UIActionSheet alloc] initWithTitle: asTitle
                                                        delegate: self
                                               cancelButtonTitle: cbTitle
                                          destructiveButtonTitle: nil
                                               otherButtonTitles:
                             NSLocalizedString (@"Log Out", @""),
                             nil]
                            autorelease];

        self.actionSheet.tag = LOGOUT_ACTION_SHEET_TAG;

        [self showActionSheet];
    }
}

- (void) reloadUserInfo
{
    [self updateNavigationItemAnimated: YES];

    UITableView    *tabView = (UITableView *) self.view;
    InfoHeaderView *hdrView = (InfoHeaderView *) tabView.tableHeaderView;

    [tabView reloadData];
    [hdrView update];
}

- (void) removeNotificationObservers
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}
//
//- (NSString *) selectedAppliance
//{
//    SettingsManager *settings = self.appDelegate.settingsManager;
//    NSIndexPath     *idxPath = [(UITableView *) self.view indexPathForSelectedRow];
//
//    return ((idxPath &&
//             (idxPath.section == APPLIANCE_SECTION) &&
//             (idxPath.row < [settings.knownAppliances count])) ?
//            [settings.knownAppliances objectAtIndex: idxPath.row] :
//            nil);
//}

- (void) showActionSheet
{
    NSInteger tag = ((self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad) ?
                     self.actionSheet.tag :
                     -1);

    switch (tag)
    {
        case CHANGE_APPLIANCE_ACTION_SHEET_TAG :
        {
            UITableView     *tabView = (UITableView *) self.view;
            NSIndexPath     *idxPath = [tabView indexPathForSelectedRow];
            UITableViewCell *cell = [tabView cellForRowAtIndexPath: idxPath];
            CGRect           rect = [tabView convertRect: cell.textLabel.frame
                                                fromView: cell.contentView];

            [self.actionSheet showFromRect: rect
                                    inView: tabView
                                  animated: YES];

            break;
        }

        case LOGOUT_ACTION_SHEET_TAG :
            [self.actionSheet showFromBarButtonItem: self.logOutButton
                                           animated: YES];
            break;

        default:
            [self.actionSheet showInView: self.view];
            break;
    }
}

- (NSDateFormatter *) timeFormatter
{
    if (!self->timeFormatter_)
    {
        self->timeFormatter_ = [NSDateFormatter new];

        [self->timeFormatter_ setDateStyle: NSDateFormatterNoStyle];
        [self->timeFormatter_ setTimeStyle: NSDateFormatterMediumStyle];
    }

    return self->timeFormatter_;
}

- (void) toggleRememberLogin: (id) sender
{
    AppDelegate     *appDel = self.appDelegate;
    SettingsManager *settings = appDel.settingsManager;

    settings.rememberLogin = ((UISwitch *) sender).on;

    [appDel.sessionManager updateLoginExpiration];

    [settings synchronizeReadWriteSettings];

    [self reloadUserInfo];
}

- (void) toggleUseCamera: (id) sender
{
    SettingsManager *settings = self.appDelegate.settingsManager;

    settings.useCamera = ((UISwitch *) sender).on;

    [settings synchronizeReadWriteSettings];
}

- (void) toggleUseNotes: (id) sender
{
    SettingsManager *settings = self.appDelegate.settingsManager;

    settings.useNotes = ((UISwitch *) sender).on;

    [settings synchronizeReadWriteSettings];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    SessionManager *sm = self.appDelegate.sessionManager;

    if (!self.inboxButton)
        self.inboxButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Inbox", @"")
                                                             style: UIBarButtonItemStylePlain
                                                            target: self
                                                            action: @selector (clickedInboxButton)]
                            autorelease];

    if (!self.logInButton)
        self.logInButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Log In", @"")
                                                             style: UIBarButtonItemStylePlain
                                                            target: self
                                                            action: @selector (logIn)]
                            autorelease];

    if (!self.logOutButton)
        self.logOutButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Log Out", @"")
                                                              style: UIBarButtonItemStylePlain
                                                             target: self
                                                             action: @selector (logOut)]
                             autorelease];

    self.navigationItem.title = NSLocalizedString (@"Control Panel", @"");

    [self.navigationItem setHidesBackButton: !sm.isLoggedIn
                                   animated: animated];

    [self.navigationItem setLeftBarButtonItem: (sm.isLoggedIn ?
                                                self.inboxButton :
                                                nil)
                                     animated: animated];

    [self.navigationItem setRightBarButtonItem: (!sm.isLoggedIn ?
                                                 self.logInButton :
                                                 self.logOutButton)
                                      animated: animated];
}

#pragma mark Overridden UIViewController Methods

- (NSString *) formatString: (NSString *) str
{
    return (([str length] > 0) ?
            str :
            NSLocalizedString (@"(unknown)", @""));
}
- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];

    [AsyncImageView clearCache];
}
-(void) photoChooser:(id)sender
{


    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;

    if (sm.isLoggedIn)
    {
    AddressBarWebViewController *wvc = [[[AddressBarWebViewController alloc]
                                         initWithMcid:session.identifier]
                                        autorelease];

    // make a decent looking title
    wvc.title = [NSString stringWithFormat:@"%@ Choosing User Portrait for %@",session.identifier, [self formatString: session.userName]];


    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];


    //[nav setModalPresentationStyle:UIModalPresentationFormSheet];

    [self presentModalViewController:nav animated: YES];
    }
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

    tmpView.tableHeaderView = [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
                               autorelease];

    SettingsManager *settings = self.appDelegate.settingsManager;


  //  SessionManager *sm = self.appDelegate.sessionManager;


    if (settings.canUseSettings)
    {
        UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self
                                                                               action: @selector (didReceiveSecretHandshake:)]
                                       autorelease];

        tgr.numberOfTapsRequired = 4;

        UIView *secretView = ((InfoHeaderView *) tmpView.tableHeaderView).secretView;

        secretView.userInteractionEnabled = YES;

        [secretView addGestureRecognizer: tgr];


        UITapGestureRecognizer *tgr2 = [[[UITapGestureRecognizer alloc] initWithTarget: self
                                                                               action: @selector (photoChooser:)]
                                       autorelease];

        tgr2.numberOfTapsRequired = 4;

        UIView *photoView = ((InfoHeaderView *) tmpView.tableHeaderView).userPhotoImageView;

        photoView.userInteractionEnabled = YES;

        [photoView addGestureRecognizer: tgr2];

    }

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

    SessionManager *sm = self.appDelegate.sessionManager;

    if (!sm.isLoggedIn && !sm.isLoggingIn)
        [self logIn];
}

- (void) viewDidLoad
{
    [super viewDidLoad];

    [self updateNavigationItemAnimated: NO]; // first time up, put it up

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
    self.navigationController.toolbarHidden = YES;

//    if (self->pushIntoSoloURL_)
//    {
//        // if here then we were started with a particular URL
//        HurlChooserController *hcc  = [[[HurlChooserController alloc]
//                                        initWithSoloURL: self->pushIntoSoloURL_]
//                                       autorelease];
//
//        [self.navigationController pushViewController: hcc
//                                             animated: NO];
//    }
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];



    [self.navigationController setToolbarHidden: YES
                                       animated: animated];

    [self addNotificationObservers];

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

    [self removeNotificationObservers];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.appDelegate.infoViewController = nil; // mark us gone

    [self removeNotificationObservers];

    [self->actionSheet_ release];
    [self->backButton_ release];
    [self->calendar_ release];
    [self->dateFormatter_ release];
    [self->documents_ release];
    [self->inboxButton_ release];
    [self->logInButton_ release];
    [self->logOutButton_ release];
    [self->pushIntoSoloURL_ release];
    [self->timeFormatter_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
    {
        self->pushIntoSoloURL_ = nil;

        self.appDelegate.infoViewController = self;
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

#pragma mark LoginViewControllerDelegate Methods

- (void) loginViewController: (LoginViewController *) lvc
         didFinishWithUserID: (NSString *) userID
                    password: (NSString *) password
{
    SettingsManager *settings = self.appDelegate.settingsManager;
    SessionManager  *sm = self.appDelegate.sessionManager;

    //
    // Prevent leaving this page until login completes ...
    //
    [self.navigationItem setHidesBackButton: YES
                                   animated: YES];

    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];

    [self.navigationItem setRightBarButtonItem: nil
                                      animated: YES];

    [sm logInSessionToAppliance: settings.appliance
                         userID: userID
                       password: password
                        options: SessionManagerOptionInteractive];
//  self->amLoggedIn = YES:
}

- (void) loginViewControllerDidCancel: (LoginViewController *) lvc
{
    //self->amLoggedIn = NO;
    //
    // Better safe than sorry ...
    //
    UITableView    *tabView = (UITableView *) self.view;
    InfoHeaderView *hdrView = (InfoHeaderView *) tabView.tableHeaderView;

    [hdrView stopActivityIndicator];
    [hdrView update];
}

#pragma mark SessionManager Notification Methods

- (void) logInSessionDidFinish: (NSNotification *) notification
{
    UITableView    *tabView = (UITableView *) self.view;
    InfoHeaderView *hdrView = (InfoHeaderView *) tabView.tableHeaderView;
    SessionManager *sm = self.appDelegate.sessionManager;

    [hdrView stopActivityIndicator];

    NSError *error = [notification.userInfo objectForKey: SessionManagerNotificationErrorKey];

    if (error && [[error domain] isEqualToString: SessionManagerErrorDomain])
    {
        switch ([error code])
        {
            case SessionManagerErrorInternalFailure :
                // punt for now ...
                break;

            case SessionManagerErrorServerFailure :
            {
                UIAlertView *av = [[[UIAlertView alloc] initWithTitle: NSLocalizedString (@"Incorrect User ID or Password", @"")
                                                              message: NSLocalizedString (@"Please check your credentials", @"")
                                                             delegate: self
                                                    cancelButtonTitle: NSLocalizedString (@"OK", @"")
                                                    otherButtonTitles: nil]
                                   autorelease];

                [av show];
                break;
            }

            case SessionManagerErrorUnknown :
            default :
                [sm checkNetworkStatus];   // push down into SessionManager ???
                break;
        }
    }

    [self reloadUserInfo];

    //
    // On successful login, pick up where last left off:
    //
    if (sm.isLoggedIn)
    {
        SettingsManager *settings = self.appDelegate.settingsManager;
        NSString        *groupID = settings.lastGroupID;
        Session         *session = sm.loginSession;
        Group           *group = nil;

        if (groupID)
        {
            group = [session groupWithIdentifier: groupID];

            if (!group)
            {
                NSLog (@"Saved group identifier %@ is no longer valid",
                       groupID);

                settings.lastGroupID = nil;

                [settings synchronizeReadWriteSettings];
            }
        }

        if (!group)
            group = [session.groups objectAtIndex: 0];

        session.groupInFocus = group;

        [self clickedInboxButton];  // simulate push of button
    }
}

- (void) logInSessionDidStart: (NSNotification *) notification
{
    UITableView    *tabView = (UITableView *) self.view;
    InfoHeaderView *hdrView = (InfoHeaderView *) tabView.tableHeaderView;

    [hdrView startActivityIndicator];
}

- (void) sessionInfoDidChange: (NSNotification *) notification
{
    [self reloadUserInfo];
}

#pragma mark UIActionSheetDelegate Methods

- (void)  actionSheet: (UIActionSheet *) actSheet
 clickedButtonAtIndex: (NSInteger) buttonIdx
{
    if ((buttonIdx >= actSheet.firstOtherButtonIndex) &&
        (buttonIdx != actSheet.cancelButtonIndex))
    {
//SettingsManager *settings = self.appDelegate.settingsManager;
        SessionManager *sm = self.appDelegate.sessionManager;

        switch (actSheet.tag)
        {
 //           case CHANGE_APPLIANCE_ACTION_SHEET_TAG :
//            {
//                switch (buttonIdx)
//                {
//                    case CHANGE_APPLIANCE_BUTTON_INDEX :
//                    default :
//                        [sm logOutSessionWithOptions: SessionManagerOptionNone];
//
//                        settings.appliance = self.selectedAppliance;
//
//                        [settings synchronizeReadWriteSettings];
//
//                        [self reloadUserInfo];
//                        break;
//                }
//
//                break;
//            }

            case LOGOUT_ACTION_SHEET_TAG :
            {
                switch (buttonIdx)
                {
                    case LOGOUT_BUTTON_INDEX :
                    default :
                        [sm logOutSessionWithOptions: SessionManagerOptionNone];

                        [self reloadUserInfo];
                        break;
                }

                break;
            }

            default :
                break;
        }
    }
}

#pragma mark UIAlertViewDelegate Methods

- (void) alertView: (UIAlertView *) alertView
clickedButtonAtIndex: (NSInteger) buttonIdx
{
    SessionManager *sm = self.appDelegate.sessionManager;

    if (!sm.isLoggedIn)
        [self logIn];
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    SessionManager *sm = self.appDelegate.sessionManager;
    if (sm.isLoggedIn)
    return SECTION_COUNT;
    else return SECTION_COUNT - 2;
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
      //   case APPLIANCE_SECTION :
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
            //case APPLIANCE_SECTION :
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
                case ABOUT_REGISTER_ROW :
                {

                    SessionManager *sm = self.appDelegate.sessionManager;
                    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                    if (!sm.isLoggedIn)
                    cell.textLabel.text = NSLocalizedString (@"Register With MedCommons", @"");
                    else

                        cell.textLabel.text = NSLocalizedString (@"More Information About MedCommons", @"");
                    break;
                }

                case ABOUT_VERSION_ROW :
                {
                    cell.detailTextLabel.text = settings.applicationVersion;
                    cell.textLabel.text = [NSString stringWithFormat:
                                           @"%@ %@",
                                           settings.applicationName,
                                           NSLocalizedString (@"Version", @"")];
                    break;
                }

                default :
                    cell = nil;
                    break;
            }

            break;
        }

   //     case APPLIANCE_SECTION :
//        {
//            if (idxPath.row < [settings.knownAppliances count])
//            {
//                NSString *appliance = [settings.knownAppliances objectAtIndex: idxPath.row];
//
//                cell.textLabel.text = appliance;
//
//                if ([appliance isEqualToString: settings.appliance])
//                    cell.accessoryType = UITableViewCellAccessoryCheckmark;
//            }
//            else
//                cell = nil;
//
//            break;
//        }

        case DOCUMENTS_SECTION :
        {
            if ((idxPath.row < [self.documents count]))
            {
                MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;

                docCell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                docCell.document = [self.documents objectAtIndex: idxPath.row];
            }
            else
                cell = nil;

            break;
        }

        case SETTINGS_SECTION :
        {
            SEL       settingAction = NULL;
            NSString *settingName = nil;
            BOOL      settingOn = NO;

            switch (idxPath.row)
            {
                case SETTINGS_REMEMBER_LOGIN_ROW :
                    settingAction = @selector (toggleRememberLogin:);
                    settingName = [self formatRememberLoginSettingName];
                    settingOn = settings.rememberLogin;
                    break;

                case SETTINGS_USE_CAMERA_ROW :
                    if (settings.featureLevel >= 3)
                    {
                        settingAction = @selector (toggleUseCamera:);
                        settingName = NSLocalizedString (@"Use Camera", @"");
                        settingOn = settings.useCamera;
                    }
                    break;

                case SETTINGS_USE_NOTES_ROW :
                    if (settings.featureLevel >= 1)
                    {
                        settingAction = @selector (toggleUseNotes:);
                        settingName = NSLocalizedString (@"Use Notes", @"");
                        settingOn = settings.useNotes;
                    }
                    break;

                default :
                    break;
            }

            if (settingAction && settingName)
            {
                cell.textLabel.text = settingName;

                UISwitch *switchCtl = ((MCSwitchTableViewCell *) cell).switchControl;

                switchCtl.on = settingOn;

                [switchCtl addTarget: self
                              action: settingAction
                    forControlEvents: UIControlEventValueChanged];
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
    SettingsManager *settings = self.appDelegate.settingsManager;

    switch (sect)
    {
        case ABOUT_SECTION :
            return ABOUT_ROW_COUNT;
//
//        case APPLIANCE_SECTION :
//            return [settings.knownAppliances count];

        case DOCUMENTS_SECTION :
            return [self.documents count];

        case SETTINGS_SECTION :
        {
            switch (settings.featureLevel)
            {
                case 1 :
                    return SETTINGS_ROW_COUNT_FL1;

                case 2 :
                    return SETTINGS_ROW_COUNT_FL2;

                default :
                    if (settings.featureLevel >= 3)
                        return SETTINGS_ROW_COUNT_FL3;
                    break;
            }

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
            return  NSLocalizedString (@"© 2011 MedCommons, Inc.", @"");

      //  case APPLIANCE_SECTION :
            
        case DOCUMENTS_SECTION :
        case SETTINGS_SECTION :
        default :
            return nil;

    }
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {
        case ABOUT_SECTION :
            return NSLocalizedString (@"About", @"");//
//
//        case APPLIANCE_SECTION :
//            return NSLocalizedString (@"Choose an Appliance…", @"");

//        case DOCUMENTS_SECTION :
//        {
//            switch ([self.documents count])
//            {
//                case 0 :
//                    return NSLocalizedString (@"No Documents", @"");
//
//                case 1 :
//                    return NSLocalizedString (@"1 Document", @"");
//
//                default :
//                    return [NSString stringWithFormat:
//                            NSLocalizedString (@"%ld Documents", @""),
//                            [self.documents count]];
//            }
//        }

        case SETTINGS_SECTION :
            return NSLocalizedString (@"Settings", @"");

        default :
            return nil;
    }
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    switch (idxPath.section)
    {
        case ABOUT_SECTION :
        {
            WebViewController *wvc;
            // if tapped in here it means go to registration page


            SessionManager *sm = self.appDelegate.sessionManager;
            if (!sm.isLoggedIn)
            {

           wvc = [[[WebViewController alloc]
                                       initWithURL: [NSURL URLWithString: @"http://www.medcommons.net/m"]]
                                      autorelease];

            //wvc.navigationItem.hidesBackButton = YES;
            wvc.title = NSLocalizedString (@"Register With MedCommons", @"");

            }

            else {
                wvc = [[[WebViewController alloc]
                        initWithURL: [NSURL URLWithString: @"http://www.medcommons.net/m/info.html"]]
                       autorelease];

                //wvc.navigationItem.hidesBackButton = YES;
                wvc.title = NSLocalizedString (@"More Information About MedCommons", @"");
            }
            [self.navigationController pushViewController: wvc
                                                 animated: YES];
            break;

        }
//
//        case APPLIANCE_SECTION :
//        {
//            SettingsManager *settings = self.appDelegate.settingsManager;
//
//            if (idxPath.row < [settings.knownAppliances count])
//            {
//                NSString *appliance = [settings.knownAppliances objectAtIndex: idxPath.row];
//
//                if (![appliance isEqualToString: settings.appliance])
//                    [self confirmApplianceChoice];
//            }
//
//            break;
//        }

        case DOCUMENTS_SECTION :
        {
            MCDocument *doc = [self.documents objectAtIndex: idxPath.row];

            if (doc)
            {
                NSString *zip = @".zip";
                NSRange   range = [[doc.URL absoluteString] rangeOfString: zip
                                                                  options: NSCaseInsensitiveSearch];

                if (range.location != NSNotFound)
                {
                    NSString *doctags = @"/Documents/";

                    NSRange range2 = [[doc.URL absoluteString] rangeOfString: doctags
                                                                     options: NSCaseInsensitiveSearch];

                    if (range2.location != NSNotFound)
                    {
                        int len = range.location - range2.location - 11;
                        NSRange xrange= NSMakeRange(range2.location+11,len);
                        NSString *s = [[doc.URL absoluteString] substringWithRange:xrange];

                        ZipViewController *zvc = [[[ZipViewController alloc]
                                                   initWithBase: s ]
                                                  autorelease];

                        [self.navigationController pushViewController: zvc
                                                             animated: YES];
                    }
                }
                else
                {
                    NSLog (@"Opening doc %@", doc.URL);

                    WebViewController *wvc = [[[WebViewController alloc]
                                               initWithURL: doc.URL]
                                              autorelease];

                    wvc.title = doc.title;

                    [self.navigationController pushViewController: wvc
                                                         animated: YES];
                }
            }

            break;
        }

        case SETTINGS_SECTION :
        default :
            break;
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
