//
//  ShooterView.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "Member.h"
#import "MemberStore.h"
#import "Photo.h"
#import "Session.h"
#import "SessionManager.h"
#import "ShooterView.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class ShooterView
#pragma mark -

#pragma mark Internal Constants

#define STANDARD_GAP_THICKNESS 8.0f
#define STANDARD_PADDING_INSET 20.0f

//
// Assorted view tags:
//
enum
{
    COMMENT_TEXT_FIELD_TAG       = 101,
    SUBJECT_DOB_LABEL_TAG,
    SUBJECT_NAME_LABEL_TAG,
    SUBJECT_THUMB_IMAGE_VIEW_TAG,
    //
    THUMB_TAG_BASE
};

@interface ShooterView ()

@property (nonatomic, retain, readonly) UIView      *contentView;
@property (nonatomic, retain, readonly) UIView      *headerView;
@property (nonatomic, retain, readonly) UILabel     *subjectDOBLabel;
@property (nonatomic, retain, readonly) UILabel     *subjectNameLabel;
@property (nonatomic, retain, readonly) UIImageView *subjectThumbImageView;

- (NSString *) formatSubjectDOB: (NSString *) dob;

- (NSString *) formatSubjectName: (NSString *) name;

- (void) loadContentView;

- (void) loadHeaderView;

- (void) updateContentView;

- (void) updateHeaderView;

@end

@implementation ShooterView

@synthesize commentTextField      = commentTextField_;
@synthesize contentView           = contentView_;
@synthesize headerView            = headerView_;
@synthesize subjectDOBLabel       = subjectDOBLabel_;
@synthesize subjectNameLabel      = subjectNameLabel_;
@synthesize subjectThumbImageView = subjectThumbImageView_;

#pragma mark Public Instance Methods

- (NSUInteger) indexOfPartThumbAtLocation: (CGPoint) location
{
    NSUInteger idx = 0;

    for (UIView *view in self.contentView.subviews)
    {
        if ([view isKindOfClass: [UIImageView class]])
        {
            if (!view.hidden &&
                [view pointInside: [self convertPoint: location
                                               toView: view]
                        withEvent: nil])
                return idx;

            idx++;
        }
    }

    return NSNotFound;
}

- (BOOL) isSubjectThumbAtLocation: (CGPoint) location
{
    return [self.subjectThumbImageView pointInside: [self convertPoint: location
                                                                toView: self.subjectThumbImageView]
                                         withEvent: nil];
}

- (CGRect) rectForPartThumbAtIndex: (NSUInteger) idx
{
    NSUInteger tmpIdx = 0;

    for (UIView *view in self.contentView.subviews)
    {
        if ([view isKindOfClass: [UIImageView class]])
        {
            if (!view.hidden && (idx == tmpIdx))
                return [self convertRect: view.bounds
                                fromView: view];

            if (idx > tmpIdx)
                break;

            tmpIdx++;
        }
    }

    return CGRectZero;
}

- (CGRect) rectForSubjectThumb
{
    return [self convertRect: self.subjectThumbImageView.bounds
                    fromView: self.subjectThumbImageView];
}

- (void) update
{
    [self updateHeaderView];
    [self updateContentView];
}

#pragma mark Private Instance Methods

- (NSString *) formatSubjectDOB: (NSString *) dob
{
    return [NSString stringWithFormat:
            @"%@: %@",
            NSLocalizedString (@"DOB", @""),
            ((dob && ([dob length] > 0)) ?
             dob :
             NSLocalizedString (@"(unknown)", @""))];
}

- (NSString *) formatSubjectName: (NSString *) name
{
    return ((name && ([name length] > 0)) ?
            name :
            NSLocalizedString (@"(unknown subject)", @""));
}

- (void) loadContentView
{
    //NSLog (@"*** ShooterView.loadContentView ***");

    UIEdgeInsets  padding = UIEdgeInsetsMake (STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET);
    StyleManager *styles = self.appDelegate.styleManager;
    CGFloat       minGap = STANDARD_GAP_THICKNESS;
    CGRect        tmpFrame;

    //
    // Content view frame uses coordinates relative to its containing view:
    //
    tmpFrame.size.width = CGRectGetWidth (self.bounds);
    tmpFrame.size.height = (CGRectGetHeight (self.bounds) -
                            CGRectGetHeight (self.headerView.frame));
    tmpFrame.origin.x = 0.0f;
    tmpFrame.origin.y = (CGRectGetHeight (self.bounds) -
                         tmpFrame.size.height);

    self->contentView_ = [[UIView alloc] initWithFrame: tmpFrame];

    self.contentView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                         UIViewAutoresizingFlexibleWidth);
    self.contentView.backgroundColor = styles.backgroundColorLighter;

    //
    // All remaining views are subviews of content view; their view frames use
    // coordinates relative to content view:
    //
    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = padding.top;
    tmpFrame.size = styles.defaultPartThumbImage.size;

    CGFloat thumbSkip = tmpFrame.size.width + minGap;

    // lazily create image views for thumbs ...

    for (NSUInteger idx = 0; idx < 32; idx++)
    {
        UIImageView *thumb = [[[UIImageView alloc] initWithFrame: tmpFrame]
                              autorelease];

        thumb.tag = THUMB_TAG_BASE + idx;

        [self.contentView addSubview: thumb];

        tmpFrame.origin.x += thumbSkip;
    }

    [self addSubview: self.contentView];
}

- (void) loadHeaderView
{
    UIEdgeInsets  padding = UIEdgeInsetsMake (STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET);
    StyleManager *styles = self.appDelegate.styleManager;
    CGRect        tmpFrame;

    //
    // Header view frame uses coordinates relative to its containing view:
    //
    tmpFrame.origin = CGPointZero;
    tmpFrame.size.width = CGRectGetWidth (self.bounds);
    tmpFrame.size.height = (padding.top +
                            styles.defaultSubjectThumbImage.size.height +
                            padding.bottom);

    self->headerView_ = [[UIView alloc] initWithFrame: tmpFrame];

    self.headerView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                        UIViewAutoresizingFlexibleWidth);
    self.headerView.backgroundColor = styles.backgroundColorLight;

    //
    // Subject thumb image view (positioned at upper left corner of header
    // view):
    //
    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = padding.top;
    tmpFrame.size = styles.defaultSubjectThumbImage.size;

    self->subjectThumbImageView_ = [[UIImageView alloc] initWithFrame: tmpFrame];

    self.subjectThumbImageView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                                   UIViewAutoresizingFlexibleRightMargin);
    self.subjectThumbImageView.tag = SUBJECT_THUMB_IMAGE_VIEW_TAG;

    [self.headerView addSubview: self.subjectThumbImageView];

    //
    // Subject name label:
    //
    self->subjectNameLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.subjectNameLabel.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                              UIViewAutoresizingFlexibleWidth);
    self.subjectNameLabel.backgroundColor = [UIColor clearColor];
    self.subjectNameLabel.font = styles.labelFontBoldL;
    self.subjectNameLabel.tag = SUBJECT_NAME_LABEL_TAG;
    self.subjectNameLabel.text = [self formatSubjectName: nil];
    self.subjectNameLabel.textAlignment = UITextAlignmentLeft;
    self.subjectNameLabel.textColor = [UIColor darkTextColor];

    tmpFrame.origin.x = (CGRectGetMinX (self.subjectThumbImageView.frame) +
                         CGRectGetWidth (self.subjectThumbImageView.frame) +
                         padding.left);
    tmpFrame.origin.y = padding.top;
    tmpFrame.size.width = (CGRectGetWidth (self.headerView.frame) -
                           tmpFrame.origin.x -
                           padding.right);
    tmpFrame.size.height = [self.subjectNameLabel preferredHeight];

    self.subjectNameLabel.frame = tmpFrame;

    [self.headerView addSubview: self.subjectNameLabel];

    //
    // Subject DOB label:
    //
    self->subjectDOBLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.subjectDOBLabel.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                             UIViewAutoresizingFlexibleWidth);
    self.subjectDOBLabel.backgroundColor = [UIColor clearColor];
    self.subjectDOBLabel.font = styles.labelFontM;
    self.subjectDOBLabel.tag = SUBJECT_DOB_LABEL_TAG;
    self.subjectDOBLabel.text = [self formatSubjectDOB: nil];
    self.subjectDOBLabel.textAlignment = UITextAlignmentLeft;
    self.subjectDOBLabel.textColor = [UIColor grayColor];

    tmpFrame.origin.x += padding.left;
    tmpFrame.origin.y += tmpFrame.size.height;
    tmpFrame.size.width = (CGRectGetWidth (self.headerView.frame) -
                           tmpFrame.origin.x -
                           padding.right);
    tmpFrame.size.height = [self.subjectDOBLabel preferredHeight];

    self.subjectDOBLabel.frame = tmpFrame;

    [self.headerView addSubview: self.subjectDOBLabel];

    //
    // Comment text field:
    //
    self->commentTextField_ = [[UITextField alloc] initWithFrame: CGRectZero];

    self.commentTextField.autocorrectionType = UITextAutocorrectionTypeNo;
    self.commentTextField.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                              UIViewAutoresizingFlexibleWidth);
    self.commentTextField.backgroundColor = [UIColor groupTableViewBackgroundColor];
    self.commentTextField.borderStyle = UITextBorderStyleRoundedRect;
    self.commentTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.commentTextField.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    self.commentTextField.font = styles.labelFontL;
    self.commentTextField.placeholder = NSLocalizedString (@"Enter a comment...", @"");
    self.commentTextField.returnKeyType = UIReturnKeyDone;
    self.commentTextField.tag = COMMENT_TEXT_FIELD_TAG;
    self.commentTextField.textColor = [UIColor darkTextColor];

    tmpFrame.origin.x -= padding.left;
    tmpFrame.size.width = (CGRectGetWidth (self.headerView.frame) -
                           tmpFrame.origin.x -
                           padding.right);
    tmpFrame.size.height = [self.commentTextField preferredHeight];
    tmpFrame.origin.y = (CGRectGetMinY (self.subjectThumbImageView.frame) +
                         CGRectGetHeight (self.subjectThumbImageView.frame) -
                         tmpFrame.size.height);

    self.commentTextField.frame = tmpFrame;

    [self.headerView addSubview: self.commentTextField];

    [self addSubview: self.headerView];
}

- (void) updateContentView
{
    StyleManager   *styles = self.appDelegate.styleManager;
    SessionManager *sm = self.appDelegate.sessionManager;
    MemberStore    *mstore = sm.loginSession.memberInFocus.store;
    NSUInteger      idx = 0;

    for (UIView *view in self.contentView.subviews)
    {
        if ([view isKindOfClass: [UIImageView class]])
        {
            UIImageView *thumb = (UIImageView *) view;

            if ([mstore hasPartPhotoAtIndex: idx])
            {
                thumb.border = styles.thumbBorderNormal;
                thumb.image = [UIImage imageWithContentsOfFile: [mstore partPhotoAtIndex: idx].path];
            }
            else
            {
                thumb.border = styles.thumbBorderPlaceholder;
                thumb.image = styles.defaultPartThumbImage;
            }

            thumb.hidden = (idx > [mstore numberOfPartPhotos]);

            idx++;
        }
    }
}

- (void) updateHeaderView
{
    StyleManager   *styles = self.appDelegate.styleManager;
    SessionManager *sm = self.appDelegate.sessionManager;
    Member         *member = sm.loginSession.memberInFocus;
    MemberStore    *mstore = member.store;

    if ([mstore hasSubjectPhoto])
    {
        self.subjectThumbImageView.border = styles.thumbBorderNormal;
        self.subjectThumbImageView.image = [UIImage imageWithContentsOfFile: mstore.subjectPhoto.path];
    }
    else
    {
        self.subjectThumbImageView.border = styles.thumbBorderPlaceholder;
        self.subjectThumbImageView.image = styles.defaultSubjectThumbImage;
    }

    self.subjectNameLabel.text = [self formatSubjectName: member.name];
    self.subjectDOBLabel.text = [self formatSubjectDOB: member.dateOfBirth];
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        [self loadHeaderView];
        [self loadContentView];
    }

    return self;
}

- (void) layoutSubviews
{
    //NSLog (@"*** ShooterView.layoutSubviews ***");

    //
    // Programmatically resizing self.view apparently does not cause
    // autoresizing to occur in subviews, thus we must explicitly handle
    // frame resizing here.
    //
    UIEdgeInsets  padding = UIEdgeInsetsMake (STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET,
                                              STANDARD_PADDING_INSET);
    StyleManager *styles = self.appDelegate.styleManager;
    CGSize        thumbSize = styles.defaultPartThumbImage.size;
    CGFloat       minGap = STANDARD_GAP_THICKNESS;
    CGRect        tmpFrame;

    //
    // Header view -- maintain its current origin and height ???; set its width
    // to that of its parent view:
    //
    tmpFrame.origin = CGPointZero;
    tmpFrame.size.width = CGRectGetWidth (self.bounds);
    tmpFrame.size.height = (padding.top +
                            styles.defaultSubjectThumbImage.size.height +
                            padding.bottom);

    self.headerView.frame = tmpFrame;

    //
    // Content view -- maintain its current origin ???; sets its size to that of
    // its parent (less header view height):
    //
    tmpFrame.size.width = CGRectGetWidth (self.bounds);
    tmpFrame.size.height = (CGRectGetHeight (self.bounds) -
                            CGRectGetHeight (self.headerView.frame));
    tmpFrame.origin.x = 0.0f;
    tmpFrame.origin.y = (CGRectGetHeight (self.bounds) -
                         tmpFrame.size.height);

    self.contentView.frame = tmpFrame;

    //
    // Figure out how many thumbs can fit in each row and how many rows
    // of thumbs fit in content view:
    //
    CGFloat    thumbsWidth = (CGRectGetWidth (self.contentView.frame) -
                              padding.left -
                              padding.right);
    NSUInteger colCount = (NSUInteger) ((thumbsWidth - thumbSize.width) /
                                        (thumbSize.width + minGap)) ;
    CGFloat    thumbGap = ((thumbsWidth -
                            (colCount * thumbSize.width)) /
                           (colCount - 1));
    CGFloat    thumbsHeight = (CGRectGetHeight (self.contentView.frame) -
                               padding.top -
                               padding.bottom);
    NSUInteger rowCount = (NSUInteger) ((thumbsHeight + thumbGap) /
                                        (thumbSize.height + thumbGap));
    CGFloat    thumbSkipY = thumbSize.height + thumbGap;
    CGFloat    thumbSkipX = thumbSize.width + thumbGap;
    NSUInteger vThumbCount = (rowCount * colCount);     // visible thumb count
    NSUInteger col = 0;
    NSUInteger idx = 0;

    SessionManager *sm = self.appDelegate.sessionManager;
    MemberStore    *mstore = sm.loginSession.memberInFocus.store;
    NSUInteger      partCount = [mstore numberOfPartPhotos] + 1;

    if (vThumbCount > partCount)
        vThumbCount = partCount;

    tmpFrame.origin.x = padding.left;
    tmpFrame.origin.y = padding.top;
    tmpFrame.size = thumbSize;

    for (UIView *view in self.contentView.subviews)
    {
        if ([view isKindOfClass: [UIImageView class]])
        {
            col++;
            idx++;

            UIImageView *thumb = (UIImageView *) view;

            thumb.frame = tmpFrame;
            thumb.hidden = (idx > vThumbCount);

            if (col == colCount)
            {
                tmpFrame.origin.x = padding.left;
                tmpFrame.origin.y += thumbSkipY;

                col = 0;
            }
            else
                tmpFrame.origin.x += thumbSkipX;
        }
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->commentTextField_ release];
    [self->contentView_ release];
    [self->headerView_ release];
    [self->subjectDOBLabel_ release];
    [self->subjectNameLabel_ release];
    [self->subjectThumbImageView_ release];

    [super dealloc];
}

@end
