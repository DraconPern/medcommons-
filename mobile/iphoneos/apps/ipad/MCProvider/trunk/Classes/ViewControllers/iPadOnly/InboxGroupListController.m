//
//  InboxGroupListController.m
//  MCProvider
//
//  Created by Bill Donner on 5/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "CCRViewController.h"
#import "DataStore.h"
//#import "DetailViewController+Bill.h"
#import "Group.h"
#import "GroupsActionController.h"
//#import "HurlWebViewController.h"
#import "InboxGroupListController.h"
#import "InfoViewController.h"
#import "SessionManager.h"
#import "Member.h"
#import "MemberListCell.h"
#import "MemberListViewController.h"
#import "MemberStore.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "DataManager.h"

#pragma mark -
#pragma mark Private Class MugFrame
#pragma mark -

@interface MugFrame : NSObject
{
@private

    CGRect frame_;
}

@end

@implementation MugFrame

- (id) initWithFrame: (CGRect) frame
{
    self = [super init];

    if (self)
        self->frame_ = frame;

    return self;
}

- (CGRect) frame
{
    return frame_;
}

@end

#pragma mark -
#pragma mark Public Class InboxGroupListController
#pragma mark -

#pragma mark Internal Constants

enum
{
    VIEWING_MODE_INBOX   = 0,
    VIEWING_MODE_MEMBERS
};

enum
{
    OP_ADD_INBOX_ENTRY      = 0,
    OP_ADD_PERMANENT_MEMBER,
    OP_MERGE
};

#define SECTION_LABEL_HEIGHT 30.0f

//
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

enum
{
    MAX_MEMBERS_ON_PAGE = 100
};

@interface InboxGroupListController () <UIActionSheetDelegate, UIAlertViewDelegate, UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readwrite) MCActionController *groupsActionController;

- (void) addNotificationObservers;

- (void) choices;

- (void) comingSoon;

- (void) dispatch: (NSUInteger) idx;

- (BOOL) dispatchTouchEndEvent: (UIView *) view
                    toPosition: (CGPoint) position;

- (void) doMemberForIndex: (NSUInteger) idx
                  forMode: (NSUInteger) vmode;

- (void) loadStandardSectionDecscriptors;

- (NSArray *) membersForViewingMode: (NSUInteger) vMode;

- (void) showInfo;

- (void) newMember;

- (void) paintInboxPage;

- (void) refreshInboxPage;

- (void) removeNotificationObservers;

- (void) showOriginalNavButtons;


-(void) consent_clickback;

//
// Forward declarations:
//
- (void) groupInfoDidChange: (NSNotification *) notification;

@end

@implementation InboxGroupListController

@synthesize groupsActionController     = groupsActionController_;
@dynamic    hidesMasterViewInLandscape;

#pragma mark Private Instance Methods

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (groupInfoDidChange:)
               name: SessionManagerGroupInfoDidChangeNotification
             object: nil];
}


#pragma mark Private Instance Methods

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

- (void) comingSoon
{
    UIBarButtonItem *bbi = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemSave
                                                                         target: nil
                                                                         action: nil];

    self.navigationItem.rightBarButtonItem = bbi;

    UIBarButtonItem *bbi2 = [[UIBarButtonItem alloc] initWithTitle: @"Cancel"
                                                             style: UIBarButtonItemStylePlain
                                                            target: self
                                                            action: @selector (showOriginalNavButtons)];

    self.navigationItem.leftBarButtonItem = bbi2;

    UIAlertView *av = [[[UIAlertView alloc] initWithTitle: NSLocalizedString (@"Coming Soon", @"")
                                                  message: @"\n\n\n" // IMPORTANT
                                                 delegate: nil
                                        cancelButtonTitle: @"OK" // NSLocalizedString (@"Cancel", @"")
                                        otherButtonTitles:  nil] //NSLocalizedString (@"Enter", @""), nil]
                       autorelease];

    // set place
    [av setTransform: CGAffineTransformMakeTranslation (0.0f, 110.0f)]; // ???
    [av show];
}

- (void) dispatch: (NSUInteger) idx
{
    // Go a little bit indirect here because these entries have been loaded optionally

    switch (opIndices_ [idx])
    {
        case OP_ADD_INBOX_ENTRY :
        {
            self.navigationItem.title = @"Adding HealthURL to Inbox";

            topSectionDesc_.text = @" Step 1 - Enter Member First and Last Names:";
            bottomSectionDesc_.text = @" Step 2 - Add Additional Information and Photos Later";

            [self newMember];
            break;
        }

        case OP_ADD_PERMANENT_MEMBER :
        {
            self.navigationItem.title = @"Adding Permanent Member";

            topSectionDesc_.text = @" Step 1 - Choose 1 or more entries:";
            bottomSectionDesc_.text = @" Step 2 - Click anywhere to add new Permanent member";

            [self comingSoon];
            break;
        }

        case OP_MERGE:
        {
            self.navigationItem.title = @"Merging into Permanent Member";

            topSectionDesc_.text = @" Step 1 - Choose 1 or more entries:";
            bottomSectionDesc_.text = @" Step 2 - Click on picture to merge into Permanent member";

            [self comingSoon];
            break;
        }

        default :
            break;
    }
}

//
// Checks to see which view, or views, the point is in and then calls a method
// to perform the closing animation, which is to return the piece to its
// original size, as if it is being put down by the user:
//
- (BOOL) dispatchTouchEndEvent: (UIView *) view
                    toPosition: (CGPoint) position
{
    NSUInteger count = [mugShots_ count];

    //  NSLog (@"Inbox Group List Touches dispatch count %d", count);

    if (count == 0)
        return NO; // don't try this alone

    // Check to see which view, or views,  the point is in and then push to that user
    for (NSUInteger idx = 0; idx < count ; idx++)
    {
        CGRect r = [[mugShots_ objectAtIndex: idx] frame];

        if (CGRectContainsPoint (r, position))
        {
            //  NSLog (@"Chose user %d", idx);
            [self doMemberForIndex: idx
                           forMode: 1]; // this means to use the B Pile

            return YES;
        }
        //else
        //          NSLog (@"Skipped %d  [ %f %f %f %f ]",idx,r.origin.x,r.origin.y,r.size.width,r.size.height);
    }

    return NO;
}


- (void) doMemberForIndex: (NSUInteger) idx
                  forMode: (NSUInteger) vmode
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
		
		
		
		
		self->stashedidx=idx;
		self->stashedvmode=vmode;
		
		// wait for clickback
		[av show];
		
	}
	else {

    // responds to a tap from either the table part of the picture part

    NSArray        *members = [self membersForViewingMode: vmode];
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;

    if (session.memberInFocus)
        [session.memberInFocus dump];

    Member *member = [members objectAtIndex: idx];

    session.memberInFocus = member;

    //
    // Choose controller based on environment setting:
    //
    SettingsManager *settings = self.appDelegate.settingsManager;

    if (settings.canUseNewViewer)
    {
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
//    else
//    {
//        HurlWebViewController *hwvc = [[[HurlWebViewController alloc]
//                                        initWithURL: [NSURL URLWithFormat:
//                                                      settings.healthURLFormat,
//                                                      session.appliance,
//                                                      member.identifier,
//                                                      session.authToken]]
//                                       autorelease];
//
//        hwvc.title = [NSString stringWithFormat:
//                      @"%@ %@",
//                      member.name,
//                      member.dateTime];
//
//        [self.navigationController pushViewController: hwvc
//                                             animated: YES];
//    }
	}
}
-(void) consent_clickback
{
	//
	// if we actually get here and didnt cancel out 
	[DataManager sharedInstance].userHasConsented = YES;
	[self doMemberForIndex:self->stashedidx forMode:self->stashedvmode];
}
- (void) loadStandardSectionDecscriptors
{
    NSArray        *members = [self membersForViewingMode: VIEWING_MODE_MEMBERS];
    SessionManager *sm = self.appDelegate.sessionManager;
    Group          *group = sm.loginSession.groupInFocus;
    //  NSString       *path = [DataStore pathForGroupWithIdentifier: group.identifier];
    //  NSDate         *modDate = [NSFileManager fileModificationDateOfItemAtPath: path];

    topSectionDesc_.text = [NSString stringWithFormat:
                            @" Inbox for %@", group.name] ;
    // [[modDate description] substringToIndex: 19]];

    bottomSectionDesc_.text = (([members count] > 0) ?
                               [NSString stringWithFormat: @" Members in %@ - %d",group.name,
                                [members count]] :
                               [NSString stringWithFormat:
                                @" There are no permanent members in %@",
                                group.name]);
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

- (void) showInfo
{
    InfoViewController *ivc = [[[InfoViewController alloc] init]
                               autorelease];

    [self.navigationController pushViewController: ivc
                                         animated: YES];
}

- (void) newMember
{
    // Really force the user to enter something
    UIAlertView *av = [[[UIAlertView alloc] initWithTitle: NSLocalizedString (@"Enter New Member Name", @"")
                        //@"A New MedCommons HealthURL Will Be Created in Your Inbox"
                                                  message: @"\n\n\n" // IMPORTANT
                                                 delegate: self
                                        cancelButtonTitle: @"Cancel" // NSLocalizedString (@"Cancel", @"")
                                        otherButtonTitles: @"OK", nil] //NSLocalizedString (@"Enter", @""), nil]
                       autorelease];

    av.tag = ACCT_MADE_ALERT_VIEW_TAG;

    CGRect tmpFrame = CGRectMake (12.0f, 50.0f, 260.0f, 25.0f);

    givenNameTextField_ = [[UITextField alloc]
                           initWithFrame: tmpFrame];

    givenNameTextField_.backgroundColor = [UIColor whiteColor];
    givenNameTextField_.placeholder = NSLocalizedString (@" first", @"");

    [av addSubview: givenNameTextField_];

    tmpFrame.origin.y += 35.0f;

    familyNameTextField_ = [[UITextField alloc]
                            initWithFrame: tmpFrame];

    familyNameTextField_.backgroundColor = [UIColor whiteColor];
    familyNameTextField_.placeholder = NSLocalizedString (@" last", @"");

    [av addSubview: familyNameTextField_];

    tmpFrame.origin.y += 35.0f;

    // set place
    [av setTransform: CGAffineTransformMakeTranslation (0.0f, 110.0f)]; // ???
    [av show];

    // set cursor and show keyboard
    [givenNameTextField_ becomeFirstResponder];
}

- (void) paintInboxPage
{
    CGRect  mainViewBounds = self.parentViewController.view.bounds;
    CGFloat deviceWidth = CGRectGetWidth (mainViewBounds);
    CGFloat deviceHeight = CGRectGetHeight (mainViewBounds);

    mugShots_ = [[NSMutableArray alloc] init];

    NSArray    *members = [self membersForViewingMode: VIEWING_MODE_MEMBERS];
    // pic sizes and pic layout based on number of members
    NSUInteger  memberCount = [members count];
    CGFloat     spaceBetweenPics;
    CGFloat     labelFontSize;
    CGFloat     footerMargin;
    CGFloat     labelOffsetX;
    CGFloat     labelOffsetY;
    CGFloat     labelHeight;
    CGFloat     rowHeight;
    NSUInteger  maxRows;
    CGFloat     picSide;
    NSUInteger  cols;

    if (UIInterfaceOrientationIsPortrait (self.interfaceOrientation))
    {
        // portrait orientations
        if (memberCount <= 20)
        {
            cols = 5;
            maxRows = 4;
            labelHeight = 14.0f;
            labelFontSize = 14.0f;
            rowHeight = 170.0f;
            picSide = 130.0f;
            spaceBetweenPics = 15.0f;

            footerMargin = 50.0f;
            labelOffsetX = 20.0f;
            labelOffsetY = 28.0f;
        }
        else if (memberCount <= 48)
        {
            cols = 8;
            maxRows = 6;
            labelHeight = 12.0f;
            labelFontSize = 12.0f;
            rowHeight = 110.0f;
            picSide = 80.0f;
            spaceBetweenPics = 11.0f;

            footerMargin = 70.0f;
            labelOffsetX = 20.0f;
            labelOffsetY = 34.0f;
        }
        else //if (memberCount <= 99)
        {
            // this case not yet debugged -bill
            cols = 11;
            maxRows = 10;
            labelHeight = 10.0f;
            labelFontSize = 10.0f;
            rowHeight = 85.0f;//was 70
            picSide = 60.0f;
            spaceBetweenPics = 10.0f;

            footerMargin = 84.0f;
            labelOffsetX = 4.0f;
            labelOffsetY = 38.0f;
        }
    } // end portrait
    else
    {
        // landscape orientations
        if (memberCount <= 14)
        {
            cols = 7;
            maxRows = 2;
            labelHeight = 14.0f;
            labelFontSize = 14.0f;
            rowHeight = 170.0f;
            picSide = 130.0f;
            spaceBetweenPics = 13.0f;

            footerMargin = 50.0f;
            labelOffsetX = 11.0f;
            labelOffsetY = 28.0f;
        }
        else if (memberCount <= 40)
        {
            cols = 10;
            maxRows = 4;
            labelHeight = 12.0f;
            labelFontSize = 12.0f;
            rowHeight = 110.0f;
            picSide = 85.0f;
            spaceBetweenPics = 15.0f; // note spacing is different here

            footerMargin = 50.0f;
            labelOffsetX = 12.0f;
            labelOffsetY = 30.0f;
        }
        else //if (memberCount <= 99)
        {
            // this case not yet debugged -bill
            cols = 14;
            maxRows = 5;
            labelHeight = 10.0f;
            labelFontSize = 10.0f;
            rowHeight = 80.0f;//was 72
            picSide = 59.0f;
            spaceBetweenPics = 13.0f;

            footerMargin = 40.0f;
            labelOffsetX = 10.0f;
            labelOffsetY = 15.0f;
        }
    } // end landscape

    NSUInteger rows = (memberCount + cols - 1) / cols;

    if (rows > maxRows)
        rows = maxRows; // keep this from getting out of control

    // adjust the content height so it is a precise multiple of TABLE_ROW_HEIGHT split the excess within the footer

    CGFloat footerHeight = (rows * rowHeight) + footerMargin;
    CGFloat rect = (deviceHeight -
                    footerHeight -
                    CGRectGetMinY (mainViewBounds) -
                    (2.0f * SECTION_LABEL_HEIGHT));

    CGFloat contentRowHeight = [MemberListCell defaultCellHeight];
    CGFloat contentHeight = (contentRowHeight *
                             floorf (rect / contentRowHeight));
    CGFloat halfDelta = (rect - contentHeight) / 2.0f;

    //  footerMargin += halfdelta;
    footerHeight += halfDelta;

    CGFloat picWidthWithSpacing = picSide + spaceBetweenPics;

    // now figure out the labels, they are full width and should look the same

    topSectionDesc_ = [[UILabel alloc] initWithFrame: CGRectMake (CGRectGetMinX (mainViewBounds),
                                                                  CGRectGetMinY (mainViewBounds),
                                                                  CGRectGetWidth (mainViewBounds),
                                                                  SECTION_LABEL_HEIGHT)];

    topSectionDesc_.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    topSectionDesc_.backgroundColor =[UIColor grayColor];
    topSectionDesc_.font = [UIFont systemFontOfSize: 18.0f];
    topSectionDesc_.textAlignment = UITextAlignmentLeft;
    topSectionDesc_.textColor = [UIColor whiteColor];

    contentView_ = [[UITableView alloc] initWithFrame: CGRectMake (CGRectGetMinX (mainViewBounds),
                                                                   CGRectGetMinY (mainViewBounds) + SECTION_LABEL_HEIGHT,
                                                                   deviceWidth,
                                                                   contentHeight)
                                                style: UITableViewStylePlain];

    bottomSectionDesc_ = [[UILabel alloc] initWithFrame: CGRectMake (CGRectGetMinX (mainViewBounds),
                                                                     CGRectGetMinY (mainViewBounds) + SECTION_LABEL_HEIGHT + contentHeight,
                                                                     CGRectGetWidth (mainViewBounds),
                                                                     SECTION_LABEL_HEIGHT)];

    bottomSectionDesc_.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    bottomSectionDesc_.backgroundColor = [UIColor grayColor];
    bottomSectionDesc_.font = [UIFont systemFontOfSize: 18.0f];
    bottomSectionDesc_.textAlignment = UITextAlignmentLeft;
    bottomSectionDesc_.textColor = [UIColor whiteColor];

    footerView_ = [[UIView alloc] initWithFrame: CGRectMake (0.0f,
                                                             0.0f,
                                                             CGRectGetWidth (mainViewBounds),
                                                             rows * rowHeight)];

    contentView_.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    contentView_.backgroundColor = [UIColor clearColor];
    contentView_.dataSource = self;
    contentView_.delegate = self;
    contentView_.rowHeight = contentRowHeight;

    footerView_.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    footerView_.backgroundColor = [UIColor clearColor];
    footerView_.userInteractionEnabled = YES;

    StyleManager *styles = self.appDelegate.styleManager;
    NSUInteger    maxIdx = [members count];
    NSUInteger    idx;

    for (NSUInteger row = 0; row < rows; row++)
    {
        for (NSUInteger col = 0; col < cols; col++)
        {
            idx = (row * cols) + col;

            if (idx < maxIdx)
            {
                Member  *member = [members objectAtIndex: idx];
                NSURL   *photoURL = member.photoURL;

                UILabel *label = [[UILabel alloc] initWithFrame: CGRectMake (0.0f,
                                                                             picSide,
                                                                             picSide,
                                                                             labelHeight)];

                label.backgroundColor = [UIColor whiteColor];
                label.font = [UIFont fontWithName: @"Arial"
                                             size: labelFontSize];
                label.frame = CGRectMake (labelOffsetX + (col * picWidthWithSpacing),
                                          labelOffsetY + (row * rowHeight) + picSide,
                                          picSide,
                                          labelHeight);
                label.text = member.name;
                label.textAlignment = UITextAlignmentCenter;
                label.textColor = [UIColor darkGrayColor];

                AsyncImageView *photoImageView = [[AsyncImageView alloc]
                                                  initWithFrame: CGRectMake (0.0f,
                                                                             0.0f,
                                                                             picSide,
                                                                             picSide)];

                [photoImageView loadImageFromURL: photoURL
                                   fallbackImage: styles.fallbackMemberPhotoImageXL];

                photoImageView.backgroundColor = [UIColor whiteColor];
                photoImageView.border = styles.thumbBorderNormal;
                photoImageView.frame = CGRectMake (labelOffsetX + col * picWidthWithSpacing,
                                                   labelOffsetY + row * rowHeight,
                                                   picSide,
                                                   picSide);
                photoImageView.userInteractionEnabled = YES;

                CGRect mugShotFrame = photoImageView.frame;

                mugShotFrame.origin.y += contentHeight + 60.0f; // this is the vertical touchpoint

                MugFrame *frame = [[MugFrame alloc] initWithFrame: mugShotFrame];

                [mugShots_ addObject: frame];

                [footerView_ addSubview: photoImageView];
                [footerView_ addSubview: label];

                [photoImageView release];
                [label release];
                [frame release];
            }
        }
    }

    [footerView_ setFrame:CGRectMake (CGRectGetMinX (mainViewBounds),
                                      (CGRectGetMinY (mainViewBounds) +
                                       contentHeight +
                                       (2 * SECTION_LABEL_HEIGHT)),
                                      deviceWidth,
                                      footerHeight)];

    [self loadStandardSectionDecscriptors];
}

- (void) refreshInboxPage
{

    for (UIView *view in contentView_.subviews)
        [view removeFromSuperview];

    for (UIView *view in footerView_.subviews)
        [view removeFromSuperview];

    [topSectionDesc_ removeFromSuperview];
    [bottomSectionDesc_ removeFromSuperview];
    [contentView_ removeFromSuperview];
    [footerView_ removeFromSuperview];
    [mugShots_ release];

    [self paintInboxPage];

    [backgroundView_ addSubview: topSectionDesc_];
    [backgroundView_ addSubview: bottomSectionDesc_];
    [backgroundView_ addSubview: contentView_];
    [backgroundView_ addSubview: footerView_];

}

- (void) removeNotificationObservers
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void) showOriginalNavButtons
{

    SessionManager *sm = self.appDelegate.sessionManager;
    self.navigationItem.hidesBackButton = NO;
    self.navigationItem.leftBarButtonItem = nil;

    if ([sm.loginSession.groups count]>1)
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemOrganize
                                                                                               target: self
                                                                                               action: @selector (choices)];

    self.navigationItem.title = sm.loginSession.groupInFocus.name;
    [self loadStandardSectionDecscriptors];
}


-(void) titleandbacksplash: (Group *) group
{

    NSLog (@"InboxListGroup switching to %@", group.name);

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
                     initWithFrame: CGRectMake (0.0f, 0.0f, 200.0f, 200.0f)];

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

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];

    // Release any cached data, images, etc that aren't in use.
    [AsyncImageView clearCache]; // take it back to zero entries
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Group *g = sm.loginSession.groupInFocus;
    [self titleandbacksplash:g];
    [self refreshInboxPage];
}
- (void) loadView
{
    // this is sized to just whatever the parent view controller wants but isnt quite working
    backgroundView_ = [[UIView alloc] initWithFrame: self.parentViewController.view.bounds];

    self.view = backgroundView_;

    backgroundView_.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    backgroundView_.backgroundColor = [UIColor whiteColor];

    [backgroundView_ sizeToFit];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    [contentView_ flashScrollIndicators];
}

- (void) viewDidLoad
{
    [super viewDidLoad];


    SessionManager *sm = self.appDelegate.sessionManager;

    if ([sm.loginSession.groups count]>1)
        self.navigationItem.rightBarButtonItem  = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemOrganize
                                                                                                target: self
                                                                                                action: @selector (choices)];

    Group          *group = sm.loginSession.groupInFocus;

    self.navigationItem.hidesBackButton = NO;
    [self titleandbacksplash: group];
    [self refreshInboxPage];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    [self addNotificationObservers];

    NSIndexPath *idxPath = [contentView_ indexPathForSelectedRow];

    if (idxPath)
        [contentView_ deselectRowAtIndexPath: idxPath
                                    animated: NO];

    // SessionManager *sm = self.appDelegate.sessionManager;
    //    Group          *group = sm.loginSession.groupInFocus;
    //  [self titleandbacksplash: group];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];

    [self removeNotificationObservers];
}

#pragma mark Overridden UIResponder Methods

- (void) touchesBegan: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    NSUInteger numTaps = [[touches anyObject] tapCount];

    if(numTaps >= 2) //???
    {

    }
}

- (void) touchesEnded: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    for (UITouch *touch in touches)
    {
        // Sends to the dispatch method, which will make sure the appropriate subview is acted upon
        if ([self dispatchTouchEndEvent: [touch view]
                             toPosition: [touch locationInView: self.view]] == YES)
            return;
    }

}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [backgroundView_ release];
    [bottomSectionDesc_ release];
    [contentView_ release];
    [familyNameTextField_ release];
    [footerView_ release];
    [givenNameTextField_ release];
    [groupsActionController_ release];
    [logoView_ release];
    [mugShots_ release];
    [segmentedControl_ release];
    [topSectionDesc_ release];

    //self.appDelegate.liveInboxController = nil; // mark us gone

    [super dealloc];
}

- (id) init
{
    // iphone gets a simpler treatment
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPhone)
        return [[MemberListViewController alloc] init];

    NSArray      *members = [self membersForViewingMode: VIEWING_MODE_MEMBERS];
    // pic sizes and pic layout based on number of members
    NSUInteger    memberCount = [members count];

    // if too many faces, just show the iphone style lists
    if (memberCount > MAX_MEMBERS_ON_PAGE)
        return [[MemberListViewController alloc] init];

    self = [super init];

    //if (self)
    //    self.appDelegate.liveInboxController = self; // set this up

    return self;
}

#pragma mark Extended UIViewController Methods

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

#pragma mark SessionManager Notification Methods

- (void) groupInfoDidChange: (NSNotification *) notification
{

    SessionManager *sm = self.appDelegate.sessionManager;
    Group *g = sm.loginSession.groupInFocus;
    [self titleandbacksplash:g];

    [self refreshInboxPage];
}

#pragma mark UIAlertViewDelegate Methods

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

        case ACCT_MADE_ALERT_VIEW_TAG :
        {
            if (idx == 1)   // 1 is index of OK
            {
                Member         *member = [Member memberWithIdentifier: @""
                                                            givenName: self->givenNameTextField_.text
                                                           familyName: self->familyNameTextField_.text
                                                             dateTime: @""];
                SessionManager *sm = self.appDelegate.sessionManager;

                [sm insertMember: member
                       intoGroup: sm.loginSession.groupInFocus
                         options: SessionManagerOptionNone];

                [self.navigationController popViewControllerAnimated: NO];  // let this suffice until we can do the refresh
            }

            break;
        }

        default :
            NSAssert1 (NO,
                       @"Unknown alert view tag: %d",
                       av.tag);
            break;
    }
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

    NSArray *members = [self membersForViewingMode: self->viewingMode_];
    Member  *member = [members objectAtIndex: idxPath.row];

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];

    if (!cell)
        cell = [[[MemberListCell alloc]
                 initWithReuseIdentifier: CellIdentifier]
                autorelease];

    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.highlighted = NO;

    ((MemberListCell *) cell).member = member;
    ((MemberListCell *) cell).showsCustom = YES;
    ((MemberListCell *) cell).showsPhoto = NO;

    return cell;
}

- (NSInteger)tableView: (UITableView *) tabView
 numberOfRowsInSection: (NSInteger) section
{
    return [[self membersForViewingMode: self->viewingMode_] count];
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    [self doMemberForIndex: idxPath.row
                   forMode: self->viewingMode_];
}

@end
