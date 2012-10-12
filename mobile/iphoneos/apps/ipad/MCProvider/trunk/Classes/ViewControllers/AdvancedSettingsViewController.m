//
//  AdvancedSettingsViewController.m
//  MCProvider
//
//  Created by Bill Donner on 5/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AdvancedSettingsViewController.h"
#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DebugController.h"
#import "MapMeViewController.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "MailEnabledWebController.h"
#import "AddressBarWebViewController.h"
#import "WebViewController.h"
#import "DataManager.h"
#import "InfoHeaderView.h"

#pragma mark -
#pragma mark Public Class AdvancedSettingsViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
    NETWORK_SECTION       = 0,  // MUST be kept in display order ...
	APPLIANCE_SECTION,
    THEME_SECTION,
    MISCELLANEOUS_SECTION,
    //
    SECTION_COUNT
};

//
// Network table section rows:
//
enum
{
    NETWORK_HOST_ROW       = 0, // MUST be kept in display order ...
    NETWORK_INTERNET_ROW,
    NETWORK_LOCAL_WIFI_ROW,
    //
    NETWORK_ROW_COUNT
};

//
// Theme table section rows:
//
enum
{
    THEME_PITCH_BLACK_ROW = 0,  // MUST be kept in display order ...
    THEME_DARK_GRAY_ROW,
    THEME_DARK_TEXTURE_ROW,
    THEME_LIGHT_TEXTURE_ROW,
    THEME_EXPLODING_CLOWN_ROW,
    //
    THEME_ROW_COUNT
};

//
// Miscellaneous table section rows:
//
enum
{
    MISCELLANEOUS_HEALTH_URL_FORMAT_ROW = 0,    // MUST be kept in display order ...
    MISCELLANEOUS_DEBUG_ROW,
    MISCELLANEOUS_TEST_CONNECTION_ROW,
    MISCELLANEOUS_SPLASH_PAGE_ROW,
    MISCELLANEOUS_MAP_ROW,
    //
    MISCELLANEOUS_ROW_COUNT
};
//
// Assorted view tags:
//
enum
{
    CHANGE_APPLIANCE_ACTION_SHEET_TAG = 101,
    LOGOUT_ACTION_SHEET_TAG
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
@interface AdvancedSettingsViewController () <UITableViewDataSource, UITableViewDelegate,  UIActionSheetDelegate, UITextFieldDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem *backButton;
@property (nonatomic, retain, readwrite) UIActionSheet   *actionSheet;

@property (nonatomic, retain, readonly)  NSString        *selectedAppliance;
- (void) updateNavigationItemAnimated: (BOOL) animated;

- (void) confirmApplianceChoice;


- (void) showActionSheet;
@end

@implementation AdvancedSettingsViewController

@synthesize backButton                 = backButton_;

@synthesize actionSheet                 = actionSheet_;
@dynamic    hidesMasterViewInLandscape;

#pragma mark Private Instance Methods

- (void) reloadUserInfo
{
    [self updateNavigationItemAnimated: YES];
	
    UITableView    *tabView = (UITableView *) self.view;
    InfoHeaderView *hdrView = (InfoHeaderView *) tabView.tableHeaderView;
	
    [tabView reloadData];
    [hdrView update];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    if (!self.backButton)
        self.backButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Advanced", @"")
                                                            style: UIBarButtonItemStylePlain
                                                           target: nil
                                                           action: NULL]
                           autorelease];

    self.navigationItem.title = NSLocalizedString (@"Advanced Settings", @"");

    self.navigationItem.backBarButtonItem = self.backButton;
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

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    [self updateNavigationItemAnimated: animated];

    NSIndexPath *idxPath = [(UITableView *) self.view indexPathForSelectedRow];

    if (idxPath)
        [(UITableView *) self.view deselectRowAtIndexPath: idxPath
                                                 animated: NO];

    [(UITableView *) self.view reloadData];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->backButton_ release];

    [super dealloc];
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
- (void) confirmApplianceChoice
{
    if (!self.actionSheet.isVisible)
    {
        NSString       *newAppliance = self.selectedAppliance;
        AppDelegate    *appDel = self.appDelegate;
        SessionManager *sm = appDel.sessionManager;
		
        NSAssert ((newAppliance != nil) &&
                  ![newAppliance isEqualToString: appDel.settingsManager.appliance],
                  @"Bad appliance selection!");
		
        NSString *cbTitle = ((appDel.targetIdiom != UIUserInterfaceIdiomPad) ?
                             NSLocalizedString (@"Cancel", @"") :
                             nil);
        NSString *asTitle = ((appDel.targetIdiom != UIUserInterfaceIdiomPad) ?
                             [NSString stringWithFormat:
                              NSLocalizedString (@"Change Appliance to %@?", @""),
                              newAppliance] :
                             NSLocalizedString (@"Change to Selected Appliance?", @""));
		
        if (sm.isLoggedIn)
            asTitle = [asTitle stringByAppendingString:
                       NSLocalizedString (@"\nYou will be logged out", @"")];
		
        self.actionSheet = [[[UIActionSheet alloc] initWithTitle: asTitle
                                                        delegate: self
                                               cancelButtonTitle: cbTitle
                                          destructiveButtonTitle: nil
                                               otherButtonTitles:
                             NSLocalizedString (@"Change Appliance", @""),
                             nil]
                            autorelease];
		
        self.actionSheet.tag = CHANGE_APPLIANCE_ACTION_SHEET_TAG;
		
        [self showActionSheet];
    }
}
- (NSString *) selectedAppliance
{
    SettingsManager *settings = self.appDelegate.settingsManager;
    NSIndexPath     *idxPath = [(UITableView *) self.view indexPathForSelectedRow];
	
    return ((idxPath &&
             (idxPath.section == APPLIANCE_SECTION) &&
             (idxPath.row < [settings.knownAppliances count])) ?
            [settings.knownAppliances objectAtIndex: idxPath.row] :
            nil);
}
#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return SECTION_COUNT;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier0 = @"AdvancedSettingsCell0";
    static NSString *CellIdentifier1 = @"AdvancedSettingsCell1";

    AppDelegate     *appDel = self.appDelegate;
    SettingsManager *settings = appDel.settingsManager;

    BOOL             useTextField = (settings.canUseNewViewer &&
                                     (idxPath.section == MISCELLANEOUS_SECTION) &&
                                     (idxPath.row == MISCELLANEOUS_HEALTH_URL_FORMAT_ROW));
    NSString        *cellIdentifier = (useTextField ?
                                       CellIdentifier0 :
                                       CellIdentifier1);

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];

    if (!cell)
    {
        switch (idxPath.section)
        {
            case MISCELLANEOUS_SECTION :
			case APPLIANCE_SECTION:
            case NETWORK_SECTION :
            case THEME_SECTION :
                cell = (useTextField ?
                        [[[MCTextFieldTableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
                                                         reuseIdentifier: cellIdentifier]
                         autorelease] :
                        [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
                                                reuseIdentifier: cellIdentifier]
                         autorelease]);
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
        case MISCELLANEOUS_SECTION :
        {
            switch (idxPath.row)
            {
                case MISCELLANEOUS_DEBUG_ROW :
                    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                    cell.textLabel.text = NSLocalizedString (@"Debug", @"");
                    break;

                case MISCELLANEOUS_HEALTH_URL_FORMAT_ROW :
                    cell.textLabel.text = @"Health URL Format";

                    if (useTextField)
                    {
                        UITextField *txtFld = ((MCTextFieldTableViewCell *) cell).textField;

                        txtFld.autocapitalizationType = UITextAutocapitalizationTypeNone;
                        txtFld.autocorrectionType = UITextAutocorrectionTypeNo;
                        txtFld.backgroundColor = [UIColor whiteColor];
                        txtFld.clearButtonMode = UITextFieldViewModeWhileEditing;
                        txtFld.delegate = self;
                        txtFld.enablesReturnKeyAutomatically = NO;
                        txtFld.keyboardAppearance = UIKeyboardAppearanceDefault;
                        txtFld.keyboardType = UIKeyboardTypeURL;
                        txtFld.placeholder = settings.defaultHealthURLFormat;
                        txtFld.returnKeyType = UIReturnKeyDone;
                        txtFld.secureTextEntry = NO;
                        txtFld.text = settings.healthURLFormat;
                        txtFld.textAlignment = UITextAlignmentRight;
                    }
                    else
                        cell.detailTextLabel.text = settings.healthURLFormat;
                    break;

                case MISCELLANEOUS_MAP_ROW :
//                    if (settings.canUseNewFeatures)
//                    {
//                        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
//                        cell.textLabel.text = NSLocalizedString (@"GameKit", @"");
//                    }
//                    else
                        cell = nil;
                    break;

                case MISCELLANEOUS_SPLASH_PAGE_ROW :
                    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                    cell.textLabel.text = NSLocalizedString (@"Splash Page", @"");
                    break;

                case MISCELLANEOUS_TEST_CONNECTION_ROW :
                    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                    cell.textLabel.text = NSLocalizedString (@"Test Connection", @"");
                    break;

                default :
                    cell = nil;
                    break;
            }

            break;
        }
		case APPLIANCE_SECTION :
        {
            if (idxPath.row < [settings.knownAppliances count])
            {
                NSString *appliance = [settings.knownAppliances objectAtIndex: idxPath.row];
				
                cell.textLabel.text = appliance;
				
                if ([appliance isEqualToString: settings.appliance])
                    cell.accessoryType = UITableViewCellAccessoryCheckmark;
            }
            else
                cell = nil;
			
            break;
        }

        case NETWORK_SECTION :
        {
            NSString              *reachType = nil;
            MCNetworkReachability *reach = nil;

            switch (idxPath.row)
            {
                case NETWORK_HOST_ROW :
                    reachType = NSLocalizedString (@"Host", @"");
                    reach = appDel.hostReachability;
                    break;

                case NETWORK_INTERNET_ROW :
                    reachType = NSLocalizedString (@"Internet", @"");
                    reach = appDel.internetReachability;
                    break;

                case NETWORK_LOCAL_WIFI_ROW :
                    reachType = NSLocalizedString (@"Local Wi-Fi", @"");
                    reach = appDel.localWiFiReachability;
                    break;

                default :
                    break;
            }

            if (reach && reachType)
            {
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                cell.detailTextLabel.text = reach.formattedStatus;
                cell.textLabel.text = reachType;
            }
            else
                cell = nil;

            break;
        }

        case THEME_SECTION :
        {
            if (idxPath.row < [settings.knownThemes count])
            {
                NSString *theme = [settings.knownThemes objectAtIndex: idxPath.row];

                cell.textLabel.text = theme;

                if ([theme isEqualToString: settings.theme])
                    cell.accessoryType = UITableViewCellAccessoryCheckmark;
            }
            else
                cell = nil;

            break;
        }
    }

    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{

    SettingsManager *settings = self.appDelegate.settingsManager;
    switch (sect)
    {
        case MISCELLANEOUS_SECTION :
            return (settings.canUseNewFeatures ?
                    MISCELLANEOUS_ROW_COUNT :
                    MISCELLANEOUS_ROW_COUNT - 1);
			
			
        case APPLIANCE_SECTION :
            return [settings.knownAppliances count];
			

        case NETWORK_SECTION :
            return NETWORK_ROW_COUNT;

        case THEME_SECTION :
            return THEME_ROW_COUNT;

        default :
            return 0;
    }
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {
        case MISCELLANEOUS_SECTION :
            return NSLocalizedString (@"Miscellaneous", @"");
			
			
        case APPLIANCE_SECTION :
            return NSLocalizedString (@"Choose an Appliance…", @"");
        case NETWORK_SECTION :
            return NSLocalizedString (@"Network", @"");

        case THEME_SECTION :
            return NSLocalizedString (@"Choose a Theme…", @"");

        default :
            return nil;
    }
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    SettingsManager *settings = self.appDelegate.settingsManager;

    switch (idxPath.section)
    {
        case MISCELLANEOUS_SECTION :
        {
            switch (idxPath.row)
            {
                case MISCELLANEOUS_DEBUG_ROW :
                {
                    DebugController *dc = [[[DebugController alloc] init]
                                           autorelease];

                    [self.navigationController pushViewController: dc
                                                         animated: YES];

                    break;
                }

                case MISCELLANEOUS_TEST_CONNECTION_ROW :
                {
                    MailEnabledWebController *wvc = [[[MailEnabledWebController alloc]
                                               initWithURL: [NSURL URLWithString:
                                                             @"http://ci.myhealthespace.com/probe/selftest.html"]]
                                              autorelease];

                    wvc.title = NSLocalizedString (@"Connection Test", @"");

                    [self.navigationController pushViewController: wvc
                                                         animated: YES];

                    break;
                }
                case MISCELLANEOUS_MAP_ROW :
                {
//
//                    if (settings.canUseNewFeatures)
//                    {//
//                        MapMeViewController *mmvc = [[[MapMeViewController alloc] init]
//                                                     autorelease];
//
//                        mmvc.title = NSLocalizedString (@"Gamekit", @"");
//
//                        [self.navigationController pushViewController: mmvc
//                                                             animated: YES];
//                    }

                    break;
                }
                case MISCELLANEOUS_SPLASH_PAGE_ROW :
                {
                    NSURL           *splashURL = settings.splashURL;

                    if (splashURL)
                    {
                        WebViewController *wvc = [[[WebViewController alloc]
                                                   initWithURL: splashURL]
                                                  autorelease];

						
                        // take title from environment variable
                        wvc.title = NSLocalizedString (@"MedCommons", @"");
						
						
					
						
                       [self.navigationController pushViewController: wvc
                                                            animated: YES];
                    }

                    break;
                }

                default :
                    break;
            }

            break;
        }
			
        case APPLIANCE_SECTION :
        {
            SettingsManager *settings = self.appDelegate.settingsManager;
			
            if (idxPath.row < [settings.knownAppliances count])
            {
                NSString *appliance = [settings.knownAppliances objectAtIndex: idxPath.row];
				
                if (![appliance isEqualToString: settings.appliance])
				{
                    [self confirmApplianceChoice];
					
				}
            }
			
            break;
			
			
        }
        case NETWORK_SECTION :
            // need NetworkDetailViewController or some such to show
            // all the reachability flags in gory detail ...
            break;

        case THEME_SECTION :
        {
            if (idxPath.row < [settings.knownThemes count])
            {
                NSString *theme = [settings.knownThemes objectAtIndex: idxPath.row];

                if (![theme isEqualToString: settings.theme])
                {
                    settings.theme = theme;

                    [settings synchronizeReadWriteSettings];

                    [tabView reloadData];
                }
            }

            break;
        }

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

#pragma mark UITextFieldDelegate Methods

- (BOOL) textField: (UITextField *) txtFld
shouldChangeCharactersInRange: (NSRange) range
 replacementString: (NSString *) string
{
    //
    // Disallow whitespace and newline characters:
    //
    NSRange badRange = [string rangeOfCharacterFromSet:
                        [NSCharacterSet whitespaceAndNewlineCharacterSet]];

    if ((badRange.location != NSNotFound) || (badRange.length > 0))
        return NO;

    return YES;
}

- (BOOL) textFieldShouldReturn: (UITextField *) txtFld
{
    [txtFld resignFirstResponder];

    SettingsManager *settings = self.appDelegate.settingsManager;

    if ([txtFld.text length] == 0)
        txtFld.text = settings.defaultHealthURLFormat;

    settings.healthURLFormat = txtFld.text;

    [settings synchronizeReadWriteSettings];

    return NO;
}
#pragma mark UIActionSheetDelegate Methods

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
			
   
			
        default:
            [self.actionSheet showInView: self.view];
            break;
    }
}
- (void)  actionSheet: (UIActionSheet *) actSheet
 clickedButtonAtIndex: (NSInteger) buttonIdx
{
    if ((buttonIdx >= actSheet.firstOtherButtonIndex) &&
        (buttonIdx != actSheet.cancelButtonIndex))
    {
        SettingsManager *settings = self.appDelegate.settingsManager;
        SessionManager *sm = self.appDelegate.sessionManager;
		
        switch (actSheet.tag)
        {
            case CHANGE_APPLIANCE_ACTION_SHEET_TAG :
            {
                switch (buttonIdx)
                {
                    case CHANGE_APPLIANCE_BUTTON_INDEX :
                    default :
                        [sm logOutSessionWithOptions: SessionManagerOptionNone];
						
                        settings.appliance = self.selectedAppliance;
						
                        [settings synchronizeReadWriteSettings];
						
                        [self reloadUserInfo];
                        break;
						
				
						
                }
				
                break;
            }
				
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

@end
