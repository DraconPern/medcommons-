//
//  BasicListCell.m
//  MCProvider
//
//  Created by J G. Pusey on 8/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AsyncImageView.h"
#import "BasicListCell.h"

#pragma mark -
#pragma mark BasicListCell
#pragma mark -

#pragma mark Internal Constants

#define GAP_THICKNESS                    2.0f

#define DEFAULT_IMAGE_HEIGHT             48.0f
#define DEFAULT_IMAGE_WIDTH              DEFAULT_IMAGE_HEIGHT

#define DEFAULT_INFO1_LABEL_FONT_SIZE    14.0f
#define DEFAULT_INFO2_LABEL_FONT_SIZE    14.0f
#define DEFAULT_SUBTITLE_LABEL_FONT_SIZE 14.0f
#define DEFAULT_TITLE_LABEL_FONT_SIZE    18.0f

@implementation BasicListCell

@dynamic asyncImageView;
@dynamic hasAsyncImageView;
@dynamic hasInfo1Label;
@dynamic hasInfo2Label;
@dynamic hasSubtitleLabel;
@dynamic hasTitleLabel;
@dynamic info1Label;
@dynamic info2Label;
@dynamic subtitleLabel;
@dynamic titleLabel;

#pragma mark Public Class Methods

+ (CGFloat) defaultCellHeight
{
    return GAP_THICKNESS + DEFAULT_IMAGE_HEIGHT + GAP_THICKNESS;
}

+ (CGSize) defaultImageSize
{
    return CGSizeMake (DEFAULT_IMAGE_WIDTH,
                       DEFAULT_IMAGE_HEIGHT);
}

#pragma mark Public Instance Methods

- (AsyncImageView *) asyncImageView
{
    if (!self->asyncImageView_)
    {
        CGRect tmpFrame;

        tmpFrame.origin = CGPointZero;
        tmpFrame.size = [[self class] defaultImageSize];

        self->asyncImageView_ = [[AsyncImageView alloc] initWithFrame: tmpFrame];

        self->asyncImageView_.backgroundColor = [UIColor clearColor];

        [self.contentView addSubview: self->asyncImageView_];
    }

    return self->asyncImageView_;
}

- (BOOL) hasAsyncImageView
{
    return (self->asyncImageView_ != nil);
}

- (BOOL) hasInfo1Label
{
    return (self->info1Label_ != nil);
}

- (BOOL) hasInfo2Label
{
    return (self->info2Label_ != nil);
}

- (BOOL) hasSubtitleLabel
{
    return (self->subtitleLabel_ != nil);
}

- (BOOL) hasTitleLabel
{
    return (self->titleLabel_ != nil);
}

- (UILabel *) info1Label
{
    if (!self->info1Label_)
    {
        self->info1Label_ = [[UILabel alloc] initWithFrame: CGRectZero];

        self->info1Label_.backgroundColor = [UIColor clearColor];
        self->info1Label_.font = [UIFont systemFontOfSize: DEFAULT_INFO1_LABEL_FONT_SIZE];
        self->info1Label_.textColor = [UIColor grayColor];

        [self.contentView addSubview: self->info1Label_];
    }

    return self->info1Label_;
}

- (UILabel *) info2Label
{
    if (!self->info2Label_)
    {
        self->info2Label_ = [[UILabel alloc] initWithFrame: CGRectZero];

        self->info2Label_.backgroundColor = [UIColor clearColor];
        self->info2Label_.font = [UIFont systemFontOfSize: DEFAULT_INFO2_LABEL_FONT_SIZE];
        self->info2Label_.textColor = [UIColor grayColor];

        [self.contentView addSubview: self->info2Label_];
    }

    return self->info2Label_;
}

- (id) initWithReuseIdentifier: (NSString *) reuseIdentifier
{
    return [super initWithStyle: UITableViewCellStyleDefault
                reuseIdentifier: reuseIdentifier];
}

- (void) removeAsyncImageView
{
    if (self->asyncImageView_)
    {
        [self->asyncImageView_ removeFromSuperview];
        [self->asyncImageView_ release];

        self->asyncImageView_ = nil;
    }
}

- (void) removeInfo1Label
{
    if (self->info1Label_)
    {
        [self->info1Label_ removeFromSuperview];
        [self->info1Label_ release];

        self->info1Label_ = nil;
    }
}

- (void) removeInfo2Label
{
    if (self->info2Label_)
    {
        [self->info2Label_ removeFromSuperview];
        [self->info2Label_ release];

        self->info2Label_ = nil;
    }
}

- (void) removeSubtitleLabel
{
    if (self->subtitleLabel_)
    {
        [self->subtitleLabel_ removeFromSuperview];
        [self->subtitleLabel_ release];

        self->subtitleLabel_ = nil;
    }
}

- (void) removeTitleLabel
{
    if (self->titleLabel_)
    {
        [self->titleLabel_ removeFromSuperview];
        [self->titleLabel_ release];

        self->titleLabel_ = nil;
    }
}

- (UILabel *) subtitleLabel
{
    if (!self->subtitleLabel_)
    {
        self->subtitleLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

        self->subtitleLabel_.backgroundColor = [UIColor clearColor];
        self->subtitleLabel_.font = [UIFont systemFontOfSize: DEFAULT_SUBTITLE_LABEL_FONT_SIZE];
        self->subtitleLabel_.textColor = [UIColor grayColor];

        [self.contentView addSubview: self->subtitleLabel_];
    }

    return self->subtitleLabel_;
}

- (UILabel *) titleLabel
{
    if (!self->titleLabel_)
    {
        self->titleLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

        self->titleLabel_.backgroundColor = [UIColor clearColor];
        self->titleLabel_.font = [UIFont boldSystemFontOfSize: DEFAULT_TITLE_LABEL_FONT_SIZE];
        self->titleLabel_.textColor = [UIColor darkTextColor];

        [self.contentView addSubview: self->titleLabel_];
    }

    return self->titleLabel_;
}

#pragma mark Overridden UITableViewCell Methods

- (id) initWithStyle: (UITableViewCellStyle) style
     reuseIdentifier: (NSString *) reuseIdentifier
{
    return [self initWithReuseIdentifier: reuseIdentifier];
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...

    UIEdgeInsets padding = UIEdgeInsetsMake (GAP_THICKNESS,
                                             GAP_THICKNESS,
                                             GAP_THICKNESS,
                                             GAP_THICKNESS);

    //
    // Size of content view (less padding):
    //
    CGSize cvSize = CGSizeMake ((CGRectGetWidth (self.contentView.bounds) -
                                 padding.left -
                                 padding.right),
                                (CGRectGetHeight (self.contentView.bounds) -
                                 padding.top -
                                 padding.bottom));

    //
    // X-offset to start of title/subtitle labels:
    //
    CGFloat tmpXOffset = 0.0f;
    CGFloat tmpHeight = 0.0f;

    //
    // Compute async image view frame:
    //
    CGRect aivFrame;

    if (self.hasAsyncImageView)
    {
        aivFrame.origin.x = padding.left;
        aivFrame.origin.y = padding.top;
        aivFrame.size = self.asyncImageView.bounds.size;

        tmpXOffset = CGRectGetMaxX (aivFrame);

        cvSize.width -= tmpXOffset;
    }
    else
        aivFrame = CGRectZero;


    CGFloat cvHalfWidth = (cvSize.width / 2.0f) - GAP_THICKNESS;

    //
    // Compute title label frame:
    //
    CGRect tlFrame;

    if (self.hasTitleLabel)
    {
        tlFrame.origin.x = (padding.left + tmpXOffset);
        tlFrame.origin.y = padding.top;
        tlFrame.size.width = (self.hasInfo1Label ? cvHalfWidth : cvSize.width);
        tlFrame.size.height = [self.titleLabel preferredHeight];
    }
    else
        tlFrame = CGRectZero;

    //
    // Compute subtitle label frame:
    //
    CGRect slFrame;

    if (self.hasSubtitleLabel)
    {
        tmpHeight = [self.subtitleLabel preferredHeight];

        slFrame.origin.x = (padding.left + tmpXOffset);
        slFrame.origin.y = (padding.top + cvSize.height - tmpHeight);
        slFrame.size.width = (self.hasInfo2Label ? cvHalfWidth : cvSize.width);
        slFrame.size.height = tmpHeight;
    }
    else
        slFrame = CGRectZero;

    tmpXOffset += (cvHalfWidth + GAP_THICKNESS);

    //
    // Compute info1 label frame:
    //
    CGRect i1lFrame;

    if (self.hasInfo1Label)
    {
        tmpHeight = [self.info1Label preferredHeight];

        i1lFrame.origin.x = (padding.left + tmpXOffset);
        i1lFrame.origin.y = (self.hasTitleLabel ?
                             CGRectGetMaxY (tlFrame) - tmpHeight :
                             padding.top);
        i1lFrame.size.width = cvHalfWidth;
        i1lFrame.size.height = tmpHeight;
    }
    else
        i1lFrame = CGRectZero;

    //
    // Compute info2 label frame:
    //
    CGRect i2lFrame = CGRectZero;

    if (self.hasInfo2Label)
    {
        tmpHeight = [self.info2Label preferredHeight];

        i2lFrame.origin.x = (padding.left + tmpXOffset);
        i2lFrame.origin.y = (padding.top + cvSize.height - tmpHeight);
        i2lFrame.size.width = cvHalfWidth;
        i2lFrame.size.height = tmpHeight;
    }
    else
        i2lFrame = CGRectZero;

    //
    // Set all frames:
    //
    if (self.hasAsyncImageView)
        self.asyncImageView.frame = aivFrame;

    if (self.hasTitleLabel)
        self.titleLabel.frame = tlFrame;

    if (self.hasSubtitleLabel)
        self.subtitleLabel.frame = slFrame;

    if (self.hasInfo1Label)
        self.info1Label.frame = i1lFrame;

    if (self.hasInfo2Label)
        self.info2Label.frame = i2lFrame;
}

#pragma mark Overridden NSObject Methods

- (void)dealloc
{
    [self->asyncImageView_ release];
    [self->info1Label_ release];
    [self->info2Label_ release];
    [self->subtitleLabel_ release];
    [self->titleLabel_ release];

    [super dealloc];
}

@end
