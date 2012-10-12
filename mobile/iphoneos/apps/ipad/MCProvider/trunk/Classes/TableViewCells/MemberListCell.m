//
//  MemberListCell.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "Member.h"
#import "MemberListCell.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class MemberListCell
#pragma mark -

@interface MemberListCell ()

- (void) setupCommentLabel;

- (void) setupCustomLabel;

- (void) setupDateTimeLabel;

- (void) setupNameLabel;

- (void) setupPhotoImageView;

@end

@implementation MemberListCell

@dynamic    commentLabel;
@dynamic    customLabel;
@dynamic    dateTimeLabel;
@synthesize member         = member_;
@dynamic    nameLabel;
@dynamic    photoImageView;
@synthesize showsCustom    = showsCustom_;
@synthesize showsPhoto     = showsPhoto_;

#pragma mark Public Instance Methods

- (UILabel *) commentLabel
{
    return self.info2Label;
}

- (UILabel *) customLabel
{
    return self.info1Label;
}

- (UILabel *) dateTimeLabel
{
    return self.subtitleLabel;
}

- (UILabel *) nameLabel
{
    return self.titleLabel;
}

- (AsyncImageView *) photoImageView
{
    return self.asyncImageView;
}

- (void) setMember: (Member *) member
{
    if (self->member_ != member)
    {
        [self->member_ release];

        self->member_ = [member retain];

        [self setupPhotoImageView];
        [self setupNameLabel];
        [self setupDateTimeLabel];
        [self setupCustomLabel];
        [self setupCommentLabel];
    }
}

- (void) setShowsCustom: (BOOL) showsCustom
{
    if (self->showsCustom_ != showsCustom)
    {
        self->showsCustom_ = showsCustom;

        [self setupCustomLabel];
    }
}

- (void) setShowsPhoto: (BOOL) showsPhoto
{
    if (self->showsPhoto_ != showsPhoto)
    {
        self->showsPhoto_ = showsPhoto;

        [self setupPhotoImageView];
        [self setupNameLabel];
    }
}

#pragma mark Private Instance Methods

- (void) setupCommentLabel
{
    NSString *comment = self.member.comment;

    if (comment)
    {
        StyleManager *styles = self.appDelegate.styleManager;

        self.commentLabel.font = styles.labelFontM;
        self.commentLabel.text = comment;
        self.commentLabel.textColor = [UIColor darkGrayColor];
    }
    else
        [self removeInfo2Label];
}

- (void) setupCustomLabel
{
    NSString *custom0 = self.member.custom0;
    NSString *custom1 = self.member.custom1;

    if (self.showsCustom && (custom0 || custom1))
    {
        NSString *custom;

        if (custom0 && custom1)
            custom = [NSString stringWithFormat:
                      @"%@ / %@",
                      custom1,
                      custom0];
        else if (custom1)
            custom = custom1;
        else
            custom = custom0;

        StyleManager *styles = self.appDelegate.styleManager;

        self.customLabel.font = styles.labelFontM;
        self.customLabel.text = custom;
        self.customLabel.textColor = [UIColor darkTextColor];
    }
    else
        [self removeInfo1Label];
}

- (void) setupDateTimeLabel
{
    NSString *dateTime = self.member.dateTime;

    if (dateTime)
    {
        StyleManager *styles = self.appDelegate.styleManager;

        self.dateTimeLabel.font = styles.labelFontM;
        self.dateTimeLabel.text = dateTime;
        self.dateTimeLabel.textColor = [UIColor darkGrayColor];
    }
    else
        [self removeSubtitleLabel];
}

- (void) setupNameLabel
{
    NSString *name = self.member.name;

    if (name)
    {
        StyleManager *styles = self.appDelegate.styleManager;

        self.nameLabel.font = styles.labelFontXXL;
        self.nameLabel.text = name;
        self.nameLabel.textColor = ((self.showsPhoto ||
                                     !self.member.isVisible) ?
                                    [UIColor lightGrayColor] :
                                    [UIColor darkTextColor]);
    }
    else
        [self removeTitleLabel];
}

- (void) setupPhotoImageView
{
    if (self.showsPhoto)
    {
        StyleManager *styles = self.appDelegate.styleManager;

        self.photoImageView.border = styles.thumbBorderNormal;

        [self.photoImageView loadImageFromURL: self.member.photoURL
                                fallbackImage: styles.fallbackMemberPhotoImageS];
    }
    else
        [self removeAsyncImageView];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->member_ release];

    [super dealloc];
}

@end
