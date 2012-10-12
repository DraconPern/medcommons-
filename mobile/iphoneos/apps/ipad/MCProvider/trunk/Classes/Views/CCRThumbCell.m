//
//  CCRThumbCell.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "CCRThumbCell.h"
#import "StyleManager.h"

#pragma mark Internal Constants

#define HAIR_GAP_THICKNESS 2.0f
#define THIN_GAP_THICKNESS 8.0f
#define WIDE_GAP_THICKNESS 20.0f

#define IMAGE_HEIGHT       IMAGE_WIDTH
#define IMAGE_WIDTH        136.0f

#define IMAGE_BOTTOM_INSET HAIR_GAP_THICKNESS
#define IMAGE_LEFT_INSET   THIN_GAP_THICKNESS
#define IMAGE_RIGHT_INSET  THIN_GAP_THICKNESS
#define IMAGE_TOP_INSET    HAIR_GAP_THICKNESS

#define PREFERRED_HEIGHT   PREFERRED_WIDTH
#define PREFERRED_WIDTH    176.0f

#pragma mark -
#pragma mark Public Class CCRThumbCell
#pragma mark -

@interface CCRThumbCell ()

@property (nonatomic, retain, readonly) AsyncImageView *asyncImageView;
@property (nonatomic, retain, readonly) UILabel        *subtitleLabel;
@property (nonatomic, retain, readonly) UILabel        *titleLabel;

- (void) loadSubviews;

@end

@implementation CCRThumbCell

@synthesize action         = action_;
@synthesize asyncImageView = asyncImageView_;
@synthesize identifier     = identifier_;
@synthesize jsEvent        = jsEvent_;
@synthesize jsParameter    = jsParameter_;
@dynamic    subtitle;
@synthesize subtitleLabel  = subtitleLabel_;
@synthesize target         = target_;
@dynamic    title;
@synthesize titleLabel     = titleLabel_;

#pragma mark Public Class Methods

+ (CGSize) preferredSize
{
    return CGSizeMake (PREFERRED_WIDTH,
                       PREFERRED_HEIGHT);
}

#pragma mark Public Instance Methods

- (void) activate
{
    StyleManager *styles = self.appDelegate.styleManager;

    self.asyncImageView.border = styles.thumbBorderHighlightedBold;
}

- (void) deactivate
{
    StyleManager *styles = self.appDelegate.styleManager;

    self.asyncImageView.border = styles.thumbBorderNormal;
}

- (id) initWithIdentifier: (NSString *) identifier
{
    CGRect tmpFrame = CGRectZero;

    tmpFrame.size = [[self class] preferredSize];

    self = [super initWithFrame: tmpFrame
                reuseIdentifier: nil];

    if (self)
    {
        self->identifier_ = [identifier copy];

        self.selectionStyle = AQGridViewCellSelectionStyleNone;

        //self.contentView.backgroundColor = [UIColor orangeColor];

        [self loadSubviews];
    }

    return self;
}

- (void) loadImageFromURL: (NSURL *) URL
{
   // StyleManager *styles = self.appDelegate.styleManager;
	// no longer shows Homer 

    [self.asyncImageView loadImageFromURL: URL];
                          //  fallbackImage: styles.defaultCCRThumbImage
                     // shouldOverwriteImage: NO];
}

- (void) setSubtitle: (NSString *) subtitle
{
    self.subtitleLabel.text = subtitle;

    [self setNeedsLayout];
}

- (void) setTitle: (NSString *) title
{
    self.titleLabel.text = title;

    [self setNeedsLayout];
}

- (NSString *) subtitle
{
    return self.subtitleLabel.text;
}

- (NSString *) title
{
    return self.titleLabel.text;
}

#pragma mark Private Instance Methods

- (void) loadSubviews
{
    StyleManager *styles = self.appDelegate.styleManager;

    self->asyncImageView_ = [[AsyncImageView alloc] initWithFrame: CGRectZero];

    self.asyncImageView.backgroundColor = [UIColor clearColor];
    self.asyncImageView.border = styles.thumbBorderNormal;

    UIImage *fallbackImage = styles.defaultCCRThumbImage; // now irrelevant - bill 11/26/10
    CGRect   tmpFrame;

    tmpFrame.origin = CGPointZero;
    tmpFrame.size = fallbackImage.size;

    self.asyncImageView.frame = tmpFrame;       // mucho important!
    self.asyncImageView.image = fallbackImage;

    [self.contentView addSubview: self.asyncImageView];

    self->titleLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.titleLabel.adjustsFontSizeToFitWidth = YES;
    self.titleLabel.backgroundColor = [UIColor clearColor];
    self.titleLabel.font = styles.labelFontBoldM;
    self.titleLabel.minimumFontSize = styles.labelFontBoldS.pointSize;
    self.titleLabel.textAlignment = UITextAlignmentCenter;
    self.titleLabel.textColor = [UIColor whiteColor];

    [self.contentView addSubview: self.titleLabel];

    self->subtitleLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.subtitleLabel.adjustsFontSizeToFitWidth = YES;
    self.subtitleLabel.backgroundColor = [UIColor clearColor];
    self.subtitleLabel.font = styles.labelFontM;
    self.subtitleLabel.minimumFontSize = styles.labelFontS.pointSize;
    self.subtitleLabel.textAlignment = UITextAlignmentCenter;
    self.subtitleLabel.textColor = [UIColor whiteColor];

    [self.contentView addSubview: self.subtitleLabel];
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews];

    UIEdgeInsets padding = UIEdgeInsetsMake (IMAGE_TOP_INSET,
                                             IMAGE_LEFT_INSET,
                                             IMAGE_BOTTOM_INSET,
                                             IMAGE_RIGHT_INSET);

    //
    // Size of content view (less padding):
    //
    CGSize cvSize = CGSizeMake ((CGRectGetWidth (self.contentView.bounds) -
                                 padding.left -
                                 padding.right),
                                (CGRectGetHeight (self.contentView.bounds) -
                                 padding.top -
                                 padding.bottom));
    CGRect tmpFrame;

    //
    // Async image view frame:
    //
    tmpFrame.origin.x = (padding.left +
                         ((cvSize.width -
                           CGRectGetWidth (self.asyncImageView.bounds)) / 2.0f));
    tmpFrame.origin.y = padding.top;
    tmpFrame.size = self.asyncImageView.bounds.size;

    self.asyncImageView.frame = tmpFrame;

    //
    // Title label frame:
    //
    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = (CGRectGetMaxY (self.asyncImageView.frame) +
                         padding.bottom);
    tmpFrame.size.width = cvSize.width;
    tmpFrame.size.height = [self.titleLabel preferredHeight];

    self.titleLabel.frame = tmpFrame;

    //
    // Subtitle label frame:
    //
    CGFloat tmpHeight = [self.subtitleLabel preferredHeight];

    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = (padding.top + cvSize.height - tmpHeight);
    tmpFrame.size.width = cvSize.width;
    tmpFrame.size.height = tmpHeight;

    self.subtitleLabel.frame = tmpFrame;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->asyncImageView_ release];
    [self->identifier_ release];
    [self->jsEvent_ release];
    [self->jsParameter_ release];
    [self->subtitleLabel_ release];
    [self->titleLabel_ release];

    [super dealloc];
}

@end
