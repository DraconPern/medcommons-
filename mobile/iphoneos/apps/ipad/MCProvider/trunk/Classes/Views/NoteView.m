//
//  NoteView.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "Member.h"
#import "NoteView.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class NoteView
#pragma mark -

#pragma mark Internal Constants

#define ANIMATION_DURATION           0.3f

#define NORMAL_EDGE_INSET            20.0f
#define NARROW_EDGE_INSET            8.0f
#define ZERO_EDGE_INSET              0.0f

#define NORMAL_TOOLBAR_HEIGHT        44.0f
#define NARROW_TOOLBAR_HEIGHT        32.0f

#define TOTAL_NOTE_TYPES             ((NoteTypeMax - NoteTypeMin) + 1)
#define INDEX_FROM_NOTE_TYPE(type)   ((type) - NoteTypeMin)
#define NOTE_TYPE_FROM_INDEX(idx)    ((idx) + NoteTypeMin)

@interface NoteView ()

@property (nonatomic, retain, readonly) UIView             *backgroundView;
@property (nonatomic, retain, readonly) UIToolbar          *footerToolbar;
@property (nonatomic, retain, readonly) UIView             *footerView;
@property (nonatomic, retain, readonly) UILabel            *headerLabel;
@property (nonatomic, retain, readonly) UIView             *headerView;
@property (nonatomic, assign, readonly) BOOL                needsFooterToolbar;
@property (nonatomic, retain, readonly) UISegmentedControl *segmentedControl;
@property (nonatomic, assign, readonly) CGFloat             toolbarHeight;

- (void) changeNoteType: (id) sender;

- (void) loadBackgroundView;

- (void) loadContentView;

- (void) loadFooterToolbar;

- (void) loadFooterView;

- (void) loadHeaderView;

- (void) scrollToSelectedRange;

@end

@implementation NoteView

@synthesize backgroundView     = backgroundView_;
@synthesize contentView        = contentView_;
@dynamic    editable;
@synthesize footerToolbar      = footerToolbar_;
@synthesize footerView         = footerView_;
@dynamic    hasUnsavedText;
@synthesize headerLabel        = headerLabel_;
@synthesize headerView         = headerView_;
@dynamic    needsFooterToolbar;
@dynamic    note;
@dynamic    segmentedControl;
@dynamic    toolbarHeight;

#pragma mark Public Instance Methods

- (BOOL) hasUnsavedText
{
    return self.contentView.hasText;
}

- (id) initWithFrame: (CGRect) frame
                note: (Note *) note
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self->date_ = [note.date copy];
        self->isShowingToolbar_ = NO;
        self->noteType_ = note.type;

        [self loadBackgroundView];
        [self loadHeaderView];
        [self loadFooterView];
        [self loadContentView];

        self.contentView.text = note.text;

        self.headerLabel.text = self.note.title;

        self.tracksKeyboard = self.isEditable;
    }

    return self;
}

- (BOOL) isEditable
{
    return self.contentView.isEditable;
}

- (Note *) note
{
    return [Note noteWithIdentifier: nil
                               date: self->date_
                               type: self->noteType_
                               text: self.contentView.text];
}

- (void) setEditable: (BOOL) editable
{
    [self setEditable: editable
             animated: NO];
}

- (void) setEditable: (BOOL) editable
            animated: (BOOL) animated
{
    if (self.isEditable != editable)
    {
        self.contentView.editable = editable;

        //
        // Keyboard will NEVER show if text view is NOT editable, so avoid
        // tracking overhead:
        //
        self.tracksKeyboard = editable;

        [self updateToolbarAnimated: animated];
    }
}

- (void) updateToolbarAnimated: (BOOL) animated
{
    CGRect  contentFrame = CGRectStandardize (self.contentView.frame);
    CGRect  footerFrame = CGRectStandardize (self.footerView.frame);
    CGRect  toolbarFrame = CGRectZero;
    CGFloat heightAdjust;

    //
    // Determine frame height adjustments; if footer toolbar is needed, load
    // it, if necessary:
    //
    if (self.needsFooterToolbar)
    {
        if (!self.footerToolbar)
            [self loadFooterToolbar];

        heightAdjust = CGRectGetHeight (footerFrame) - self.toolbarHeight;
    }
    else
        heightAdjust = CGRectGetHeight (footerFrame) - NORMAL_EDGE_INSET;

    //
    // Adjust content view and footer view frames (but don't apply just yet):
    //
    if (heightAdjust != 0.0f)
    {
        contentFrame.size.height += heightAdjust;
        footerFrame.origin.y += heightAdjust;
        footerFrame.size.height -= heightAdjust;
    }

    //
    // Adjust footer toolbar frame to be on- or off-screen as appropriate
    // (but don't apply just yet):
    //
    toolbarFrame.size.width = CGRectGetWidth (footerFrame);
    toolbarFrame.size.height = self.toolbarHeight;

    if (!self.needsFooterToolbar)
        toolbarFrame.origin.y = CGRectGetHeight (toolbarFrame);

    //
    // Start of (possibly) animated actions:
    //
    if (animated)
    {
        [UIView beginAnimations: nil
                        context: NULL];
        [UIView setAnimationDuration: ANIMATION_DURATION];
    }

    //
    // Adjust content view, footer view, and footer toolbar frames with
    // appropriate animation (if so desired); order of calls depends on
    // whether toolbar is appearing or disappearing:
    //
    if (self.needsFooterToolbar)    // toolbar appearing ...
    {
        self.contentView.frame = contentFrame;
        self.footerView.frame = footerFrame;
        self.footerToolbar.frame = toolbarFrame;
    }
    else                            // toolbar disappearing ...
    {
        self.footerToolbar.frame = toolbarFrame;
        self.footerView.frame = footerFrame;
        self.contentView.frame = contentFrame;
    }

    //
    // End of (possibly) animated actions:
    //
    if (animated)
        [UIView commitAnimations];

    self->isShowingToolbar_ = self.needsFooterToolbar;    // should be showing now ...

    [self scrollToSelectedRange];
}

#pragma mark Private Instance Methods

- (void) changeNoteType: (id) sender
{
    UISegmentedControl *segCtl = (UISegmentedControl *) sender;

    if ((segCtl == self.segmentedControl) && self.needsFooterToolbar)
    {
        NoteType newNoteType = NOTE_TYPE_FROM_INDEX (segCtl.selectedSegmentIndex);

        if (self->noteType_ != newNoteType)
        {
            self->noteType_ = newNoteType;
            self.headerLabel.text = self.note.title;
        }
    }
}

- (void) loadBackgroundView
{
    StyleManager *styles = self.appDelegate.styleManager;

    self->backgroundView_ = [[UIView alloc]
                             initWithFrame: CGRectStandardize (self.bounds)];

    self.backgroundView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                            UIViewAutoresizingFlexibleWidth);
    self.backgroundView.backgroundColor = styles.backgroundColorLighter;

    [self addSubview: self.backgroundView];
}

- (void) loadContentView
{
    StyleManager *styles = self.appDelegate.styleManager;
    CGRect        tmpFrame;

    //
    // Content view frame uses coordinates relative to its containing view
    // (i.e., background view):
    //
    tmpFrame = CGRectStandardize (self.backgroundView.bounds);

    CGFloat headerViewHeight = CGRectGetHeight (self.headerView.bounds);

    tmpFrame.origin.y += headerViewHeight;
    tmpFrame.size.height -= (headerViewHeight +
                             CGRectGetHeight (self.footerView.bounds));

    //
    // Create content text view with adjusted content view frame and set as
    // content view:
    //
    tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
                                      UIEdgeInsetsMake (ZERO_EDGE_INSET,
                                                        NORMAL_EDGE_INSET,
                                                        ZERO_EDGE_INSET,
                                                        NORMAL_EDGE_INSET));

    self->contentView_ = [[UITextView alloc] initWithFrame: tmpFrame];

    self.contentView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                         UIViewAutoresizingFlexibleWidth);
    self.contentView.backgroundColor = styles.backgroundColorLight;
    self.contentView.editable = NO;     // avoid keyboard notification overhead
    self.contentView.font = styles.textFontM;
    self.contentView.keyboardType = UIKeyboardTypeDefault;
    self.contentView.returnKeyType = UIReturnKeyDefault;
    self.contentView.scrollEnabled = YES;
    self.contentView.textColor = [UIColor darkTextColor];

    [self.backgroundView addSubview: self.contentView];
}

- (void) loadFooterToolbar
{
    if (!self.footerToolbar)
    {
        UIBarButtonItem *flexSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
                                                                                    target: nil
                                                                                    action: NULL]
                                      autorelease];
        UIBarButtonItem *segCtlBBI = [[[UIBarButtonItem alloc]
                                       initWithCustomView: self.segmentedControl]
                                      autorelease];

        self->footerToolbar_ = [[UIToolbar alloc] init];

        self.footerToolbar.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                                               UIViewAutoresizingFlexibleWidth);
        self.footerToolbar.barStyle = UIBarStyleBlack;
        self.footerToolbar.items = [NSArray arrayWithObjects:
                                    flexSpace,
                                    segCtlBBI,
                                    flexSpace,
                                    nil];

        CGRect tmpFrame = CGRectStandardize (self.backgroundView.bounds);

        tmpFrame.size.height = self.toolbarHeight;
        tmpFrame.origin.y = CGRectGetMaxY (tmpFrame);   // offset it just beyond visible bounds

        self.footerToolbar.frame = tmpFrame;

        [self.footerView addSubview: self.footerToolbar];
    }
}

- (void) loadFooterView
{
    CGRect tmpFrame;

    //
    // Footer view frame uses coordinates relative to its containing view
    // (i.e., background view):
    //
    tmpFrame = CGRectStandardize (self.backgroundView.bounds);

    //
    // Compute footer height according to whether or not toolbar is needed:
    //
    CGFloat footerHeight = (self.needsFooterToolbar ?
                            self.toolbarHeight :
                            NORMAL_EDGE_INSET);

    tmpFrame.origin.y = (CGRectGetMaxY (tmpFrame) - footerHeight);
    tmpFrame.size.height = footerHeight;

    //
    // Create footer view:
    //
    self->footerView_ = [[UIView alloc] initWithFrame: tmpFrame];

    self.footerView.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                                        UIViewAutoresizingFlexibleWidth);
    self.footerView.backgroundColor = [UIColor clearColor];

    [self.backgroundView addSubview: self.footerView];

    //
    // Finally, load footer toolbar, if needed:
    //
    if (self.needsFooterToolbar)
        [self loadFooterToolbar];
}

- (void) loadHeaderView
{
    UIEdgeInsets  headerLabelInsets = UIEdgeInsetsMake (NARROW_EDGE_INSET,
                                                        NORMAL_EDGE_INSET,
                                                        NARROW_EDGE_INSET,
                                                        NORMAL_EDGE_INSET);
    StyleManager *styles = self.appDelegate.styleManager;
    CGRect        tmpFrame;

    //
    // Header view frame uses coordinates relative to its containing view
    // (i.e., background view):
    //
    tmpFrame = CGRectStandardize (self.backgroundView.bounds);

    //
    // In order to figure out height of header view, must first determine
    // height of header label by creating it with bogus text:
    //
    self->headerLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.headerLabel.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                         UIViewAutoresizingFlexibleWidth);
    self.headerLabel.backgroundColor = [UIColor whiteColor];
    self.headerLabel.font = styles.labelFontM;
    self.headerLabel.textAlignment = UITextAlignmentLeft;
    self.headerLabel.textColor = [UIColor grayColor];

    //
    // Add top and bottom header label insets to its height to get header
    // view height:
    //
    tmpFrame.size.height = ([self.headerLabel preferredHeight] +
                            headerLabelInsets.top +
                            headerLabelInsets.bottom);

    //
    // Create header view:
    //
    self->headerView_ = [[UIView alloc] initWithFrame: tmpFrame];

    self.headerView.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                        UIViewAutoresizingFlexibleWidth);
    self.headerView.backgroundColor = [UIColor whiteColor];

    //
    // Adjust header label frame and add to header view:
    //
    self.headerLabel.frame = UIEdgeInsetsInsetRect (tmpFrame,
                                                    headerLabelInsets);

    [self.headerView addSubview: self.headerLabel];
    [self.backgroundView addSubview: self.headerView];
}

- (BOOL) needsFooterToolbar
{
    return (self.contentView.isEditable && !self.contentView.hasText);
}

- (void) scrollToSelectedRange
{
    if (self.contentView.hasText)
        [self.contentView scrollRangeToVisible: self.contentView.selectedRange];
    else
        [self.contentView scrollRectToVisible: CGRectZero
                                     animated: YES];
}

- (UISegmentedControl *) segmentedControl
{
    if (!self->segmentedControl_)
    {
        NSString *segmentTitles [TOTAL_NOTE_TYPES];

        segmentTitles [INDEX_FROM_NOTE_TYPE (NoteTypeAdmissions)]   = NSLocalizedString (@" A ", @"");
        segmentTitles [INDEX_FROM_NOTE_TYPE (NoteTypeProgress)]     = NSLocalizedString (@" P ", @"");
        segmentTitles [INDEX_FROM_NOTE_TYPE (NoteTypeSignOut)]      = NSLocalizedString (@" S ", @"");
        segmentTitles [INDEX_FROM_NOTE_TYPE (NoteTypeDischarge)]    = NSLocalizedString (@" D ", @"");
        segmentTitles [INDEX_FROM_NOTE_TYPE (NoteTypeConsultation)] = NSLocalizedString (@" C ", @"");

        NSMutableArray *segmentItems = [[[NSMutableArray alloc]
                                         initWithCapacity: TOTAL_NOTE_TYPES]
                                        autorelease];

        for (NSUInteger type = NoteTypeMin; type <= NoteTypeMax; type++)
            [segmentItems addObject: segmentTitles [INDEX_FROM_NOTE_TYPE (type)]];

        self->segmentedControl_ = [[UISegmentedControl alloc] initWithItems: segmentItems];

        self->segmentedControl_.backgroundColor = [UIColor blackColor];
        self->segmentedControl_.momentary = NO;
        self->segmentedControl_.segmentedControlStyle = UISegmentedControlStyleBar;
        self->segmentedControl_.selectedSegmentIndex = INDEX_FROM_NOTE_TYPE (self->noteType_);
        self->segmentedControl_.tintColor = [UIColor darkGrayColor];

        [self->segmentedControl_ addTarget: self
                                    action: @selector (changeNoteType:)
                          forControlEvents: UIControlEventValueChanged];
    }

    return self->segmentedControl_;
}

- (CGFloat) toolbarHeight
{
    UIDevice *device = [UIDevice currentDevice];

    //
    // If device is in landscape orientation AND device is iPhone, use narrow
    // toolbar height:
    //
    if (UIDeviceOrientationIsLandscape (device.orientation) &&
        (![device respondsToSelector: @selector (userInterfaceIdiom)] ||
         (device.userInterfaceIdiom == UIUserInterfaceIdiomPhone)))
        return NARROW_TOOLBAR_HEIGHT;

    return NORMAL_TOOLBAR_HEIGHT;
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    //
    // Programmatically resizing self.view apparently does not cause
    // autoresizing to occur in subviews, thus we must explicitly handle
    // frame resizing here.
    //
    CGRect tmpFrame;

    //
    // Background view -- set frame to bounds of its parent (i.e., self) view
    // (less keyboard height, if tracking):
    //
    tmpFrame = CGRectStandardize (self.bounds);

    if (self.tracksKeyboard)
        tmpFrame.size.height -= self.keyboardHeight;

    self.backgroundView.frame = tmpFrame;

    //
    // Header view -- adjust width to that of its parent (i.e., background)
    // view:
    //
    tmpFrame = CGRectStandardize (self.headerView.frame);
    tmpFrame.size.width = CGRectGetWidth (self.backgroundView.bounds);

    self.headerView.frame = tmpFrame;

    //
    // Footer view -- adjust width to that of its parent (i.e., background)
    // view:
    //
    tmpFrame = CGRectStandardize (self.footerView.frame);
    tmpFrame.size.width = CGRectGetWidth (self.backgroundView.bounds);

    self.footerView.frame = tmpFrame;

    //
    // Content view -- simpler to just recalculate the whole mess:
    //
    tmpFrame = CGRectStandardize (self.backgroundView.bounds);

    CGFloat headerViewHeight = CGRectGetHeight (self.headerView.bounds);

    tmpFrame.origin.y += headerViewHeight;
    tmpFrame.size.height -= (headerViewHeight +
                             CGRectGetHeight (self.footerView.bounds));

    tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
                                      UIEdgeInsetsMake (ZERO_EDGE_INSET,
                                                        NORMAL_EDGE_INSET,
                                                        ZERO_EDGE_INSET,
                                                        NORMAL_EDGE_INSET));

    self.contentView.frame = tmpFrame;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->backgroundView_ release];
    [self->contentView_ release];
    [self->date_ release];
    [self->footerToolbar_ release];
    [self->footerView_ release];
    [self->headerLabel_ release];
    [self->headerView_ release];
    [self->segmentedControl_ release];

    [super dealloc];
}

@end
