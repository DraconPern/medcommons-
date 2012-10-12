//
//  LoginViewController.m
//  MCProvider
//
//  Created by J. G. Pusey on 7/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "LoginViewController.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class LoginViewController
#pragma mark -

#pragma mark Internal Constants

#define MINIMUM_BUTTON_HEIGHT                44.0f
#define MINIMUM_BUTTON_WIDTH                 (MINIMUM_BUTTON_HEIGHT * 2.0f)

#define STANDARD_TABLE_SECTION_HEADER_HEIGHT 34.0f
#define STANDARD_GAP_THICKNESS               8.0f

//
// Table sections:
//
enum
{
    LOGIN_SECTION = 0,
    //
    SECTION_COUNT
};

//
// Login table section rows:
//
enum
{
    LOGIN_USER_ID_ROW  = 0,
    LOGIN_PASSWORD_ROW,
    //
    LOGIN_ROW_COUNT
};

//
// Assorted view tags:
//
enum
{
    PASSWORD_TEXT_FIELD_TAG = 666,
    USER_ID_TEXT_FIELD_TAG
};

@interface LoginViewController () <UITableViewDataSource, UITableViewDelegate, UITextFieldDelegate>

@property (nonatomic, retain, readwrite) UIButton    *cancelButton;
@property (nonatomic, retain, readwrite) UIButton    *demoButton;
@property (nonatomic, copy,   readonly)  NSString    *demoPassword;
@property (nonatomic, copy,   readonly)  NSString    *demoUserID;
@property (nonatomic, assign, readwrite) BOOL         isLandscape;
@property (nonatomic, assign, readwrite) BOOL         isPhone;
@property (nonatomic, retain, readwrite) UIButton    *logInButton;
@property (nonatomic, retain, readonly)  UITextField *passwordTextField;
@property (nonatomic, retain, readonly)  UITextField *userIDTextField;

+ (UIButton *) buttonWithTitle: (NSString *) title
                        target: (id) target
                        action: (SEL) action;

+ (UITextField *) textFieldWithPlaceholder: (NSString *) placeholder
                                  delegate: (id <UITextFieldDelegate>) delegate
                                     frame: (CGRect) frame
                                       tag: (NSInteger) tag;

- (void) cancelLogin: (id) sender;

- (void) finishLogin: (id) sender;

- (UITableViewCell *) passwordCellForTableView: (UITableView *) tabView;

- (void) supplyDemoCredentials: (id) sender;

- (UITextField *) textFieldInCellForRowAtIndexPath: (NSIndexPath *) idxPath;

- (UITableViewCell *) userIDCellForTableView: (UITableView *) tabView;

@end

@implementation LoginViewController

@synthesize cancelButton      = cancelButton_;
@synthesize delegate          = delegate_;
@synthesize demoButton        = demoButton_;
@synthesize demoPassword      = demoPassword_;
@synthesize demoUserID        = demoUserID_;
@synthesize isLandscape       = isLandscape_;
@synthesize isPhone           = isPhone_;
@synthesize logInButton       = logInButton_;
@dynamic    passwordTextField;
@dynamic    userIDTextField;

#pragma mark Private Class Methods

+ (UIButton *) buttonWithTitle: (NSString *) title
                        target: (id) target
                        action: (SEL) action
{
    UIButton *button = [UIButton buttonWithType: UIButtonTypeRoundedRect];

    button.titleLabel.font = self.appDelegate.styleManager.labelFontBoldXL;

    [button setTitle: title
            forState: UIControlStateNormal];

    [button setTitleColor: [UIColor lightGrayColor]
                 forState: UIControlStateDisabled];

    [button addTarget: target
               action: action
     forControlEvents: UIControlEventTouchUpInside];

    CGRect tmpFrame;

    tmpFrame.origin = CGPointZero;
    tmpFrame.size = [button sizeThatFits: CGSizeZero];

    tmpFrame.size.width = MAX (CGRectGetWidth (tmpFrame),
                               MINIMUM_BUTTON_WIDTH);
    tmpFrame.size.height = MAX (CGRectGetHeight (tmpFrame),
                                MINIMUM_BUTTON_HEIGHT);

    button.frame = tmpFrame;

    return button;
}

+ (UITextField *) textFieldWithPlaceholder: (NSString *) placeholder
                                  delegate: (id <UITextFieldDelegate>) delegate
                                     frame: (CGRect) frame
                                       tag: (NSInteger) tag
{
    UITextField *txtFld = [[[UITextField alloc] initWithFrame: frame]
                           autorelease];

    txtFld.autocapitalizationType = UITextAutocapitalizationTypeNone;
    txtFld.autocorrectionType = UITextAutocorrectionTypeNo;
    txtFld.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    txtFld.backgroundColor = [UIColor whiteColor];
    txtFld.clearButtonMode = UITextFieldViewModeWhileEditing;
    txtFld.delegate = delegate;
    txtFld.enablesReturnKeyAutomatically = YES;
    txtFld.font = self.appDelegate.styleManager.labelFontL;
    txtFld.keyboardAppearance = UIKeyboardAppearanceAlert;
    txtFld.keyboardType = UIKeyboardTypeEmailAddress;
    txtFld.placeholder = placeholder;
    txtFld.returnKeyType = UIReturnKeyNext;
    txtFld.secureTextEntry = NO;
    txtFld.tag = tag;

    return txtFld;
}

//#pragma mark Public Instance Methods

#pragma mark Private Instance Methods

- (void) cancelLogin: (id) sender
{
    [self dismissModalViewControllerAnimated: YES];

    if ([self.delegate respondsToSelector: @selector (loginViewControllerDidCancel:)])
        [self.delegate loginViewControllerDidCancel: self];
}

- (void) finishLogin: (id) sender
{
    [self dismissModalViewControllerAnimated: YES];

    self.appDelegate.settingsManager.savedUserID = self.userIDTextField.text;

    if ([self.delegate respondsToSelector: @selector (loginViewController:didFinishWithUserID:password:)])
        [self.delegate loginViewController: self
                       didFinishWithUserID: self.userIDTextField.text
                                  password: self.passwordTextField.text];
}

- (UITableViewCell *) passwordCellForTableView: (UITableView *) tabView
{
    static NSString *CellIdentifier = @"PasswordCell";

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];

    if (!cell)
    {
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
                                       reuseIdentifier: CellIdentifier]
                autorelease];

        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell.textLabel.text = NSLocalizedString (@"Password", @"");

        UITextField *txtFld = [LoginViewController textFieldWithPlaceholder: NSLocalizedString (@"Required", @"")
                                                                   delegate: self
                                                                      frame: CGRectZero
                                                                        tag: PASSWORD_TEXT_FIELD_TAG];

        txtFld.returnKeyType = UIReturnKeyGo;
        txtFld.secureTextEntry = YES;

        CGFloat cvWidth = CGRectGetWidth (cell.contentView.bounds) - 4.0f;
        CGFloat cvHeight = CGRectGetHeight (cell.contentView.bounds);
        CGFloat tmpHeight = [txtFld preferredHeight];
        CGFloat tmpWidth = cvWidth / 3.0f;
        CGRect  tmpFrame = CGRectMake (tmpWidth + 2.0f,
                                       (cvHeight - tmpHeight) / 2.0f,
                                       cvWidth - tmpWidth,
                                       tmpHeight);

        tmpFrame = CGRectIntegral (tmpFrame);

        tmpFrame.size.height = tmpHeight;

        txtFld.frame = tmpFrame;
        txtFld.text = nil;

        [cell.contentView addSubview: txtFld];
    }

    return cell;
}

- (UITextField *) passwordTextField
{
    NSIndexPath *idxPath = [NSIndexPath indexPathForRow: LOGIN_PASSWORD_ROW
                                              inSection: LOGIN_SECTION];

    return [self textFieldInCellForRowAtIndexPath: idxPath];
}

- (void) supplyDemoCredentials: (id) sender
{
    UITextField *pwTxtFld = self.passwordTextField;
    UITextField *uiTxtFld = self.userIDTextField;

    pwTxtFld.text = self.demoPassword;
    uiTxtFld.text = self.demoUserID;

    self.logInButton.enabled = YES;

    [pwTxtFld becomeFirstResponder];
}

- (UITextField *) textFieldInCellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    UITableViewCell *cell = [(UITableView *) self.view cellForRowAtIndexPath: idxPath];
    UITextField     *txtFld = nil;

    switch (idxPath.section)
    {
        case LOGIN_SECTION :
        {
            switch (idxPath.row)
            {
                case LOGIN_PASSWORD_ROW :
                    txtFld = (UITextField *) [cell.contentView viewWithTag: PASSWORD_TEXT_FIELD_TAG];
                    break;

                case LOGIN_USER_ID_ROW :
                    txtFld = (UITextField *) [cell.contentView viewWithTag: USER_ID_TEXT_FIELD_TAG];
                    break;

                default :
                    break;
            }

            break;
        }

        default :
            break;
    }

    return txtFld;
}

- (UITableViewCell *) userIDCellForTableView: (UITableView *) tabView
{
    static NSString *CellIdentifier = @"UserIDCell";

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];

    if (!cell)
    {
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
                                       reuseIdentifier: CellIdentifier]
                autorelease];

        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell.textLabel.text = NSLocalizedString (@"User ID", @"");

        UITextField *txtFld = [LoginViewController textFieldWithPlaceholder: NSLocalizedString (@"Email or Open ID", @"")
                                                                   delegate: self
                                                                      frame: CGRectZero
                                                                        tag: USER_ID_TEXT_FIELD_TAG];

        CGFloat cvWidth = CGRectGetWidth (cell.contentView.bounds) - 4.0f;
        CGFloat cvHeight = CGRectGetHeight (cell.contentView.bounds);
        CGFloat tmpHeight = [txtFld preferredHeight];
        CGFloat tmpWidth = cvWidth / 3.0f;
        CGRect  tmpFrame = CGRectMake (tmpWidth + 2.0f,
                                       (cvHeight - tmpHeight) / 2.0f,
                                       cvWidth - tmpWidth,
                                       tmpHeight);

        tmpFrame = CGRectIntegral (tmpFrame);

        tmpFrame.size.height = tmpHeight;

        txtFld.frame = tmpFrame;
        txtFld.text = self.appDelegate.settingsManager.savedUserID;

        [cell.contentView addSubview: txtFld];
    }

    return cell;
}

- (UITextField *) userIDTextField
{
    NSIndexPath *idxPath = [NSIndexPath indexPathForRow: LOGIN_USER_ID_ROW
                                              inSection: LOGIN_SECTION];

    return [self textFieldInCellForRowAtIndexPath: idxPath];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    self.cancelButton = [LoginViewController buttonWithTitle: NSLocalizedString (@"Cancel", @"")
                                                      target: self
                                                      action: @selector (cancelLogin:)];

    self.demoButton = [LoginViewController buttonWithTitle: NSLocalizedString (@"Demo", @"")
                                                    target: self
                                                    action: @selector (supplyDemoCredentials:)];

    self.logInButton = [LoginViewController buttonWithTitle: NSLocalizedString (@"Log In", @"")
                                                     target: self
                                                     action: @selector (finishLogin:)];

    CGRect       tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    UITableView *tabView = [[[UITableView alloc] initWithFrame: tmpFrame
                                                         style: UITableViewStyleGrouped]
                            autorelease];

    tabView.dataSource = self;
    tabView.delegate = self;
    tabView.separatorColor = [UIColor lightGrayColor];
    tabView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;

    UIView *footView = [[[UIView alloc] initWithFrame: tmpFrame]
                        autorelease];

    footView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                 UIViewAutoresizingFlexibleWidth);

    CGSize       btnSize = CGSizeMake (MAX (CGRectGetWidth (self.cancelButton.bounds),
                                            CGRectGetWidth (self.logInButton.bounds)),
                                       MAX (CGRectGetHeight (self.cancelButton.bounds),
                                            CGRectGetHeight (self.logInButton.bounds)));
    UIEdgeInsets padding = (self.isPhone ?
                            UIEdgeInsetsMake (0.0f,
                                              STANDARD_GAP_THICKNESS,
                                              0.0f,
                                              STANDARD_GAP_THICKNESS) :
                            UIEdgeInsetsMake (10.0f,
                                              STANDARD_GAP_THICKNESS,
                                              10.0f,
                                              STANDARD_GAP_THICKNESS));

    //
    // View to hold buttons as a group so they can be easily centered:
    //
    tmpFrame.origin = CGPointZero;
    tmpFrame.size.width = (padding.left +
                           btnSize.width +
                           30.0f +
                           btnSize.width +
                           STANDARD_GAP_THICKNESS +
                           btnSize.width +
                           padding.right);
    tmpFrame.size.height = (padding.top +
                            btnSize.height +
                            padding.bottom);

    UIView *grpView = [[[UIView alloc] initWithFrame: tmpFrame]
                       autorelease];

    grpView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                UIViewAutoresizingFlexibleLeftMargin |
                                UIViewAutoresizingFlexibleRightMargin |
                                UIViewAutoresizingFlexibleTopMargin);

    grpView.center = CGPointMake (CGRectGetWidth (footView.bounds) / 2.0f,
                                  CGRectGetHeight (grpView.bounds) / 2.0f);

    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = padding.top;
    tmpFrame.size = btnSize;

    self.demoButton.enabled = (self.demoPassword && self.demoUserID);
    self.demoButton.frame = tmpFrame;

    tmpFrame.origin.x = (CGRectGetMaxX (tmpFrame) + 30.0f);

    self.cancelButton.enabled = YES;
    self.cancelButton.frame = tmpFrame;

    tmpFrame.origin.x = (CGRectGetMaxX (tmpFrame) + STANDARD_GAP_THICKNESS);

    self.logInButton.enabled = NO;
    self.logInButton.frame = tmpFrame;

    [grpView addSubview: self.demoButton];
    [grpView addSubview: self.cancelButton];
    [grpView addSubview: self.logInButton];

    [footView addSubview: grpView];

//  // if we are supplying a savedUserID then start on that field
//  if (self.appDelegate.settingsManager.savedUserID)
//  {
//    [self.passwordTextField becomeFirstResponder];
//  }
    //
    // Set footer view height to group view height:
    //
    tmpFrame = CGRectStandardize (footView.bounds);

    tmpFrame.size.height = CGRectGetHeight (grpView.bounds);

    footView.frame = tmpFrame;

    tabView.tableFooterView = footView;

    self.view = tabView;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    UITableView *tabView = (UITableView *) self.view;

    [tabView flashScrollIndicators];

    [self tableView: tabView
didSelectRowAtIndexPath: [NSIndexPath indexPathForRow: LOGIN_USER_ID_ROW
                                            inSection: LOGIN_SECTION]];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    UITableView *tabView = (UITableView *) self.view;
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];

    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
}

- (void) willAnimateRotationToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                          duration: (NSTimeInterval) duration
{
    self.isLandscape = UIInterfaceOrientationIsLandscape (toOrient);

    //
    // Reloading table data somehow loses first responder on iPhone, so we must
    // save the current first responder and restore it after the reload:
    //
    UIResponder *firstResponder = self.userIDTextField;

    if (!firstResponder.isFirstResponder)
        firstResponder = self.passwordTextField;

    if (!firstResponder.isFirstResponder)
        firstResponder = nil;

    [(UITableView *) self.view reloadData];

    [firstResponder becomeFirstResponder];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->cancelButton_ release];
    [self->demoButton_ release];
    [self->demoPassword_ release];
    [self->demoUserID_ release];
    [self->logInButton_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
    {
        SettingsManager *settings = self.appDelegate.settingsManager;

        self->demoPassword_ = [settings.defaultPassword copy];
        self->demoUserID_ = [settings.defaultUserID copy];

        self->isLandscape_ = UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation);
        self->isPhone_ = (UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPhone);
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
    UITableViewCell *cell = nil;

    switch (idxPath.section)
    {
        case LOGIN_SECTION :
        {
            switch (idxPath.row)
            {
                case LOGIN_PASSWORD_ROW :
                    cell = [self passwordCellForTableView: tabView];
                    break;

                case LOGIN_USER_ID_ROW :
                    cell = [self userIDCellForTableView: tabView];
                    break;

                default :
                    cell = nil;
                    break;
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
    switch (sect)
    {
        case LOGIN_SECTION :
            return LOGIN_ROW_COUNT;

        default :
            return 0;
    }
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {
        case LOGIN_SECTION :
            //
            // If on iPhone, display short header title, if in landscape
            // orientation on iPhone, suppress header title altogether:
            //
            return (!self.isPhone ?
                    [NSString stringWithFormat:
                     NSLocalizedString (@"Log In to “%@”…", @""),
                     self.appDelegate.settingsManager.appliance] :
                    (!self.isLandscape ?
                     NSLocalizedString (@"Log In…", @"") :
                     nil));

        default :
            return nil;
    }
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    UITextField *txtFld = [self textFieldInCellForRowAtIndexPath: idxPath];

    [txtFld becomeFirstResponder];
}

- (CGFloat) tableView: (UITableView *) tabView
heightForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {
        case LOGIN_SECTION :
            //
            // If in landscape orientation on iPhone, suppress header:
            //
            if (self.isPhone && self.isLandscape)
                return 0.0f;
            break;

        default :
            break;
    }

    return STANDARD_TABLE_SECTION_HEADER_HEIGHT;
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

    //
    // Log In button is only enabled if both User ID and Password text fields
    // contain text:
    //
    UITextField *pwTxtFld = self.passwordTextField;
    UITextField *uiTxtFld = self.userIDTextField;
    UITextField *otherTxtFld = ((txtFld == pwTxtFld) ?
                                uiTxtFld :
                                ((txtFld == uiTxtFld) ?
                                 pwTxtFld :
                                 nil));

    //
    // Must perform dry run of text replacement to determine outcome:
    //
    NSString *tmpText = [txtFld.text stringByReplacingCharactersInRange: range
                                                             withString: string];

    self.logInButton.enabled = (([tmpText length] > 0) &&
                                ([otherTxtFld.text length] > 0));

    return YES;
}

- (BOOL) textFieldShouldClear: (UITextField *) txtFld
{
    self.logInButton.enabled = NO;  // at least one will be empty

    return YES;
}

- (BOOL) textFieldShouldReturn: (UITextField *) txtFld
{
    switch (txtFld.tag)
    {
        case PASSWORD_TEXT_FIELD_TAG :
            [self finishLogin: txtFld];
            break;

        case USER_ID_TEXT_FIELD_TAG :
        {
            [self.passwordTextField becomeFirstResponder];
            break;
        }

        default:
            break;
    }

    return NO;
}

@end
