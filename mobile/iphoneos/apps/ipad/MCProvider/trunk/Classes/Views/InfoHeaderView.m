//
//  InfoHeaderView.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/25/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

//#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "InfoHeaderView.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "AddressBarWebViewController.h"
#import "Member.h"

#pragma mark -
#pragma mark Private Class InfoHeaderViewCell
#pragma mark -

@interface InfoHeaderViewCell : UITableViewCell

- (id) initWithTag: (NSInteger) tag;

@end

@implementation InfoHeaderViewCell

#pragma mark Public Instance Methods

- (id) initWithTag: (NSInteger) tag
{
    self = [super initWithStyle: UITableViewCellStyleValue1
                reuseIdentifier: nil];

    if (self)
    {
        StyleManager *styles = self.appDelegate.styleManager;

        self.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        self.backgroundColor = [UIColor whiteColor];
        self.border = [MCBorder borderWithColor: [UIColor lightGrayColor]
                                          width: 1.0f
                                   cornerRadius: 0.0f];
        self.tag = tag;

        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        self.contentView.backgroundColor = self.backgroundColor;

        self.textLabel.adjustsFontSizeToFitWidth = YES;
        self.textLabel.backgroundColor = self.backgroundColor;
        self.textLabel.font = styles.labelFontBoldL;
        self.textLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.textLabel.minimumFontSize = styles.labelFontBoldS.pointSize;
        self.textLabel.text = @"XXX";
        self.textLabel.textColor = [UIColor darkTextColor];

        self.detailTextLabel.adjustsFontSizeToFitWidth = YES;
        self.detailTextLabel.backgroundColor = self.backgroundColor;
        self.detailTextLabel.font = styles.labelFontL;
        self.detailTextLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.detailTextLabel.minimumFontSize = styles.labelFontS.pointSize;
        self.detailTextLabel.text = @"XXX";
        self.detailTextLabel.textColor = [UIColor colorWithRed: 0.22f
                                                         green: 0.33f
                                                          blue: 0.53f
                                                         alpha: 1.0f];
    }

    return self;
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...

    CGRect  tmpFrame = CGRectStandardize (self.contentView.bounds);
    CGFloat width = CGRectGetWidth (tmpFrame) - 4.0f;   // inset 2 pts left and right

    tmpFrame.origin.x += 2.0f;
    tmpFrame.size.width = width / 3.0f;

    self.textLabel.frame = tmpFrame;

    tmpFrame.origin.x = CGRectGetMaxX (tmpFrame);
    tmpFrame.size.width = width - CGRectGetWidth (tmpFrame);

    self.detailTextLabel.frame = tmpFrame;
}

@end

#pragma mark -
#pragma mark Public Class InfoHeaderView
#pragma mark -

#pragma mark Internal Constants

#define GAP_THICKNESS 8.0f

//
// Assorted view tags:
//
enum
{
    FAUX_SECTION_VIEW_TAG         = 101,
    SECRET_TOUCH_IMAGE_VIEW_TAG,
    USER_LOGIN_APPLIANCE_CELL_TAG,
    USER_LOGIN_TIME_CELL_TAG,
    USER_LOGIN_USER_ID_CELL_TAG,
    USER_PHOTO_IMAGE_VIEW_TAG,
    USER_REAL_NAME_CELL_TAG
};

@interface InfoHeaderView ()

@property (nonatomic, retain, readwrite) UIActivityIndicatorView *activityIndicator;
@property (nonatomic, retain, readonly)  MCBorder                *sectionBorder;
@property (nonatomic, assign, readonly)  UIEdgeInsets             sectionInsets;
@property (nonatomic, retain, readonly)  InfoHeaderViewCell      *userLoginApplianceCell;
@property (nonatomic, retain, readonly)  InfoHeaderViewCell      *userLoginTimeCell;
@property (nonatomic, retain, readonly)  InfoHeaderViewCell      *userLoginUserIDCell;
//@property (nonatomic, retain, readonly)  AsyncImageView          *userPhotoImageView;
@property (nonatomic, retain, readonly)  InfoHeaderViewCell      *userRealNameCell;

- (NSString *) formatString: (NSString *) str;

- (void) loadSubviews;

@end

@implementation InfoHeaderView

@synthesize activityIndicator      = activityIndicator_;
@synthesize secretView             = secretTouchImageView_;
@dynamic    sectionBorder;
@dynamic    sectionInsets;
@synthesize userLoginApplianceCell = userLoginApplianceCell_;
@synthesize userLoginTimeCell      = userLoginTimeCell_;
@synthesize userLoginUserIDCell    = userLoginUserIDCell_;
@synthesize userPhotoImageView     = userPhotoImageView_;
@synthesize userRealNameCell       = userRealNameCell_;

#pragma mark Public Instance Methods

- (void) startActivityIndicator
{
    //
    // Activity indicator exactly overlays secret touch image view:
    //
    self.activityIndicator = [[[UIActivityIndicatorView alloc]
                               initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhiteLarge]
                              autorelease];

    self.activityIndicator.autoresizingMask = self.secretView.autoresizingMask;
    self.activityIndicator.backgroundColor = self.secretView.backgroundColor;
    self.activityIndicator.border = self.secretView.border;
    self.activityIndicator.frame = self.secretView.frame;
    self.activityIndicator.hidesWhenStopped = YES;

    [self addSubview: self.activityIndicator];

    [self.activityIndicator startAnimating];

    self.secretView.hidden = YES;
}

- (void) stopActivityIndicator
{
    if (self.activityIndicator)
    {
        [self.activityIndicator stopAnimating];
        [self.activityIndicator removeFromSuperview];   // overkill ???

        self.activityIndicator = nil;
    }

    self.secretView.hidden = NO;
}

- (void) update
{
    AppDelegate     *appDel = self.appDelegate;
    SettingsManager *settings = appDel.settingsManager;
    StyleManager    *styles = appDel.styleManager;
    SessionManager  *sm = appDel.sessionManager;
    Session         *session = nil;

    if (sm.isLoggedIn)
    {
        [self stopActivityIndicator];   // better safe than sorry ...

        session = sm.loginSession;

        if (appDel.targetIdiom != UIUserInterfaceIdiomPhone)
        {
            NSURL *photoURL = session.userPhotoURL;

            //--------------------------------------------------------------------
            // Uncomment the following line of code to really test async image
            // loading on a humongoid file:
            //
            //photoURL = [NSURL URLWithString: @"http://inferno999.com/images/Inauguration_2009.jpg"];
            //--------------------------------------------------------------------

            [self.userPhotoImageView loadImageFromURL: photoURL
                                        fallbackImage: styles.fallbackUserPhotoImageXL];
        }

        self.userRealNameCell.textLabel.text = NSLocalizedString (@"Name", @"");
        self.userRealNameCell.textLabel.textColor = [UIColor darkTextColor];
        self.userRealNameCell.detailTextLabel.text = [self formatString: session.userName];
    }
    else if (sm.isLoggingIn)
    {
        if (appDel.targetIdiom != UIUserInterfaceIdiomPhone)
            self.userPhotoImageView.image = styles.fallbackUserPhotoImageXL;

        self.userRealNameCell.textLabel.text = NSLocalizedString (@"Name", @"");
        self.userRealNameCell.textLabel.textColor = [UIColor darkTextColor];
        self.userRealNameCell.detailTextLabel.text =  NSLocalizedString (@"(logging in…)", @"");
    }
    else
    {
        if (appDel.targetIdiom != UIUserInterfaceIdiomPhone)
            self.userPhotoImageView.image = styles.fallbackUserPhotoImageXL;

        self.userRealNameCell.textLabel.text = NSLocalizedString (@"Please log in…", @"");
        self.userRealNameCell.textLabel.textColor = [UIColor redColor];
        self.userRealNameCell.detailTextLabel.text = NSLocalizedString (@"Tap the ‘Log In’ button above", @"");
    }

    self.userLoginUserIDCell.textLabel.text = NSLocalizedString (@"User ID", @"");
    self.userLoginUserIDCell.detailTextLabel.text = [self formatString: session.userID];

    self.userLoginApplianceCell.textLabel.text = NSLocalizedString (@"Appliance", @"");
    self.userLoginApplianceCell.detailTextLabel.text = [self formatString: (sm.isLoggedIn ?
                                                                            session.appliance :
                                                                            settings.appliance)];

    self.userLoginTimeCell.textLabel.text = NSLocalizedString (@"On Since", @"");
    self.userLoginTimeCell.detailTextLabel.text = [self formatString: session.loginDateTime];
}

#pragma mark Private Instance Methods

- (NSString *) formatString: (NSString *) str
{
    return (([str length] > 0) ?
            str :
            NSLocalizedString (@"(unknown)", @""));
}

- (void) loadSubviews
{
    SettingsManager *settings = self.appDelegate.settingsManager;
    StyleManager    *styles = self.appDelegate.styleManager;
    BOOL             showsPhoto = (self.appDelegate.targetIdiom != UIUserInterfaceIdiomPhone);
    UIEdgeInsets     padding = self.sectionInsets;
    MCBorder        *border = self.sectionBorder;
    CGRect           tmpFrame;

    //
    // User photo image view:
    //
    CGRect photoFrame;

    photoFrame.origin.x = padding.left;
    photoFrame.origin.y = padding.top;
    photoFrame.size = styles.fallbackUserPhotoImageXL.size;

    if (showsPhoto)
    {
        self->userPhotoImageView_ = [[AsyncImageView alloc] initWithFrame: photoFrame];

        self->userPhotoImageView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                                      UIViewAutoresizingFlexibleRightMargin);
        self->userPhotoImageView_.backgroundColor = [UIColor whiteColor];
        self->userPhotoImageView_.border = border;
        self->userPhotoImageView_.tag = USER_PHOTO_IMAGE_VIEW_TAG;

        [self addSubview: self->userPhotoImageView_];
		
	}


    //
    // Faux section view:
    //
    tmpFrame.origin.x = (showsPhoto ?
                         (CGRectGetMaxX (photoFrame) + (GAP_THICKNESS * 2.0f)) :
                         CGRectGetMinX (photoFrame));
    tmpFrame.origin.y = CGRectGetMinY (photoFrame);
    tmpFrame.size.width = (CGRectGetWidth (self.bounds) -
                           tmpFrame.origin.x -
                           padding.right);
    tmpFrame.size.height = CGRectGetHeight (photoFrame);    // for starters ...

    self->fauxSectionView_ = [[UIView alloc] initWithFrame: tmpFrame];

    //
    // For some reason, it screws up when UIViewAutoresizingFlexibleWidth is
    // specified -- ugh! Must tweak in layoutSubviews ...
    //
    self->fauxSectionView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                               UIViewAutoresizingFlexibleRightMargin);
    // UIViewAutoresizingFlexibleWidth);
    self->fauxSectionView_.backgroundColor = [UIColor whiteColor];
    self->fauxSectionView_.border = border;
    self->fauxSectionView_.clipsToBounds = YES;
    self->fauxSectionView_.tag = FAUX_SECTION_VIEW_TAG;

    [self addSubview: self->fauxSectionView_];

    //
    // User real name cell:
    //
    self->userRealNameCell_ = [[InfoHeaderViewCell alloc]
                               initWithTag: USER_REAL_NAME_CELL_TAG];

    tmpFrame.origin = CGPointZero;
    tmpFrame.size.width = CGRectGetWidth (self->fauxSectionView_.bounds);
    tmpFrame.size.height = [self->userRealNameCell_ sizeThatFits: CGSizeZero].height;

    self->userRealNameCell_.frame = tmpFrame;

    [self->fauxSectionView_ addSubview: self->userRealNameCell_];

    //
    // User login user ID cell:
    //
    self->userLoginUserIDCell_ = [[InfoHeaderViewCell alloc]
                                  initWithTag: USER_LOGIN_USER_ID_CELL_TAG];

    tmpFrame.origin.x = CGRectGetMinX (self->userRealNameCell_.frame);
    tmpFrame.origin.y = CGRectGetMaxY (self->userRealNameCell_.frame) - 1.0f;   // overlap borders
    tmpFrame.size.width = CGRectGetWidth (self->userRealNameCell_.frame);
    tmpFrame.size.height = [self->userLoginUserIDCell_ sizeThatFits: CGSizeZero].height;

    self->userLoginUserIDCell_.frame = tmpFrame;

    [self->fauxSectionView_ addSubview: self->userLoginUserIDCell_];

    //
    // User login appliance cell:
    //
    self->userLoginApplianceCell_ = [[InfoHeaderViewCell alloc]
                                     initWithTag: USER_LOGIN_APPLIANCE_CELL_TAG];

    tmpFrame.origin.x = CGRectGetMinX (self->userLoginUserIDCell_.frame);
    tmpFrame.origin.y = CGRectGetMaxY (self->userLoginUserIDCell_.frame) - 1.0f;  // overlap borders
    tmpFrame.size.width = CGRectGetWidth (self->userLoginUserIDCell_.frame);
    tmpFrame.size.height = [self->userLoginApplianceCell_ sizeThatFits: CGSizeZero].height;

    self->userLoginApplianceCell_.frame = tmpFrame;

    [self->fauxSectionView_ addSubview: self->userLoginApplianceCell_];

    //
    // User login time cell:
    //
    self->userLoginTimeCell_ = [[InfoHeaderViewCell alloc]
                                initWithTag: USER_LOGIN_TIME_CELL_TAG];

    tmpFrame.origin.x = CGRectGetMinX (self->userLoginApplianceCell_.frame);
    tmpFrame.origin.y = CGRectGetMaxY (self->userLoginApplianceCell_.frame) - 1.0f; // overlap borders
    tmpFrame.size.width = CGRectGetWidth (self->userLoginApplianceCell_.frame);
    tmpFrame.size.height = [self->userLoginTimeCell_ sizeThatFits: CGSizeZero].height;

    self->userLoginTimeCell_.frame = tmpFrame;

    [self->fauxSectionView_ addSubview: self->userLoginTimeCell_];

    //
    // Adjust faux section view height:
    //
    tmpFrame = self->fauxSectionView_.frame;

    tmpFrame.size.height = (CGRectGetHeight (self->userRealNameCell_.bounds) - 1.0f) * 4.0f;

    self->fauxSectionView_.frame = tmpFrame;

    //
    // Secret touch image view:
    //
    UIImage *tmpImage = [UIImage imageNamed: settings.infoPageImageName];

    tmpFrame.origin.x = (CGRectGetWidth (self.bounds) - tmpImage.size.width) / 2.0f;
    tmpFrame.origin.y = (CGRectGetMaxY (self->fauxSectionView_.frame) +
                         padding.top);
    tmpFrame.size = tmpImage.size;

    self->secretTouchImageView_ = [[UIImageView alloc] initWithFrame: tmpFrame];

    self->secretTouchImageView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                                    UIViewAutoresizingFlexibleLeftMargin |
                                                    UIViewAutoresizingFlexibleRightMargin);
    self->secretTouchImageView_.backgroundColor = [UIColor whiteColor];
    self->secretTouchImageView_.border = border;
    self->secretTouchImageView_.image = tmpImage;

    [self addSubview: self->secretTouchImageView_];

    //
    // Adjust info header view height:
    //
    tmpFrame = CGRectStandardize (self.bounds);

    tmpFrame.size.height = (CGRectGetMaxY (self->secretTouchImageView_.frame) +
                            padding.bottom);

    self.frame = tmpFrame;
}

- (MCBorder *) sectionBorder
{
    return [MCBorder borderWithColor: [UIColor lightGrayColor]
                               width: 1.0f
                        cornerRadius: 12.0f];
}

- (UIEdgeInsets) sectionInsets
{
    switch (UI_USER_INTERFACE_IDIOM ())
    {
        case UIUserInterfaceIdiomPad :
            return UIEdgeInsetsMake (20.0f,
                                     44.0f,
                                     GAP_THICKNESS,
                                     44.0f);

        case UIUserInterfaceIdiomPhone :
        default :
            return UIEdgeInsetsMake (10.0f,
                                     9.0f,
                                     GAP_THICKNESS,
                                     9.0f);
    }
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                 UIViewAutoresizingFlexibleWidth);

        [self loadSubviews];
    }

    return self;
}

- (void) layoutSubviews
{
    //
    // Handjob faux section view:
    //
    CGRect tmpFrame = CGRectStandardize (self->fauxSectionView_.frame);

    tmpFrame.size.width = (CGRectGetWidth (self.bounds) -
                           tmpFrame.origin.x -
                           self.sectionInsets.right);

    self->fauxSectionView_.frame = tmpFrame;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->activityIndicator_ release];
    [self->backgroundView_ release];
    [self->fauxSectionView_ release];
    [self->secretTouchImageView_ release];
    [self->userLoginApplianceCell_ release];
    [self->userLoginTimeCell_ release];
    [self->userLoginUserIDCell_ release];
    [self->userPhotoImageView_ release];
    [self->userRealNameCell_ release];

    [super dealloc];
}

@end
