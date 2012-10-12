//
//  MCSOAPNoteView.m
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCPatientID.h"
#import "MCSOAPNoteView.h"
#import "UIButton+MCShared.h"

#pragma mark -
#pragma mark "Package-private" Class MCSOAPNoteView
#pragma mark -

#pragma mark Internal Constants

#define HEADER_LABEL_HEIGHT             38.0f

#define HEADER_LABEL_MARGIN_TOP         8.0f
#define HEADER_LABEL_MARGIN_RIGHT       CONTENT_MARGIN_RIGHT
#define HEADER_LABEL_MARGIN_BOTTOM      8.0f
#define HEADER_LABEL_MARGIN_LEFT        CONTENT_MARGIN_LEFT

#define CONTENT_MARGIN_TOP              0.0f
#define CONTENT_MARGIN_RIGHT            20.0f
#define CONTENT_MARGIN_BOTTOM           0.0f
#define CONTENT_MARGIN_LEFT             20.0f

#define FOOTER_BUTTONS_HEIGHT           FOOTER_BUTTON_HEIGHT

#define FOOTER_BUTTONS_MARGIN_TOP       8.0f
#define FOOTER_BUTTONS_MARGIN_RIGHT     CONTENT_MARGIN_RIGHT
#define FOOTER_BUTTONS_MARGIN_BOTTOM    8.0f
#define FOOTER_BUTTONS_MARGIN_LEFT      CONTENT_MARGIN_LEFT

#define FOOTER_BUTTON_WIDTH             47.0f
#define FOOTER_BUTTON_HEIGHT            FOOTER_BUTTON_WIDTH

#define TOTAL_SOAP_NOTE_TYPES           ((MCSOAPNoteTypeMax - MCSOAPNoteTypeMin) + 1)

#define INDEX_FROM_SOAP_NOTE_TYPE(type) ((type) - MCSOAPNoteTypeMin)
#define TAG_FROM_SOAP_NOTE_TYPE(type)   ((type) + 1000)
#define SOAP_NOTE_TYPE_FROM_TAG(tag)    ((tag) - 1000)

@interface MCSOAPNoteView ()

- (void) addBackgroundView;

- (void) addContentView;

- (void) addFooterView;

- (void) addHeaderView;

- (void) addNotificationObservers;

- (void) changeNoteType: (id) sender;

- (void) layoutBackgroundView;

- (void) layoutContentView;

- (void) layoutFooterView;

- (void) layoutHeaderView;

- (void) removeNotificationObservers;

@end

@implementation MCSOAPNoteView

@synthesize contentView = contentView_;
@dynamic    editable;
@dynamic    note;

#pragma mark Dynamic Property Methods

- (BOOL) isEditable
{
    return contentView_.isEditable;
}

- (BOOL) isUploading
{
    return uploadIndicator_.isAnimating;
}

- (MCSOAPNote *) note
{
    return [MCSOAPNote noteWithPatientID: patientID_
                                    date: date_
                                    type: noteType_
                                    text: contentView_.text];
}

- (void) setEditable: (BOOL) editable
{
    BOOL oldEditable = contentView_.isEditable;

    if (!oldEditable != !editable)
    {
        contentView_.editable = editable;

        [self setNeedsDisplay];
    }
}

- (void) setUploading: (BOOL) uploading
{
    BOOL oldUploading = uploadIndicator_.isAnimating;

    if (!oldUploading != !uploading)
    {
        if (uploading)
            [uploadIndicator_ startAnimating];
        else
            [uploadIndicator_ stopAnimating];

        [self setNeedsDisplay];
    }
}

#pragma mark Public Instance Methods

- (id) initWithFrame: (CGRect) frame
                note: (MCSOAPNote *) note
{
    if (self = [super initWithFrame: frame])
    {
        date_ = [note.date copy];
        noteType_ = note.type;
        patientID_ = [note.patientID retain];

        keyboardHeight_ = 0.0f;

        [self addNotificationObservers];
        [self addBackgroundView];
        [self addHeaderView];
        [self addFooterView];
        [self addContentView];

        contentView_.text = note.text;
        headerLabel_.text = self.note.title;

        [self layoutSubviews];
    }

    return self;
}

#pragma mark Private Instance Methods

- (void) addBackgroundView
{
    backgroundView_ = [[UIView alloc] initWithFrame: CGRectZero];

    backgroundView_.backgroundColor = [UIColor colorWithWhite: 0.96f
                                                        alpha: 1.0f];

    [self addSubview: backgroundView_];
}

- (void) addContentView
{
    contentView_ = [[UITextView alloc] initWithFrame: CGRectZero];

    contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                     UIViewAutoresizingFlexibleWidth);
    contentView_.backgroundColor = [UIColor colorWithWhite: 0.92f
                                                     alpha: 1.0f];
    contentView_.editable = YES;
    contentView_.font = [UIFont systemFontOfSize: 14.0f];
    contentView_.keyboardType = UIKeyboardTypeDefault;
    contentView_.returnKeyType = UIReturnKeyDefault;
    contentView_.scrollEnabled = YES;
    contentView_.textColor = [UIColor darkTextColor];

    [backgroundView_ addSubview: contentView_];
}

- (void) addFooterView
{
    footerView_ = [[UIView alloc] initWithFrame: CGRectZero];

    footerView_.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                                    UIViewAutoresizingFlexibleWidth);
    footerView_.backgroundColor = [UIColor clearColor];

    NSString *buttonTitles [TOTAL_SOAP_NOTE_TYPES];

    buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (MCSOAPNoteTypeAdmissions)]   = NSLocalizedString (@"A", @"");
    buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (MCSOAPNoteTypeProgress)]     = NSLocalizedString (@"P", @"");
    buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (MCSOAPNoteTypeSignOut)]      = NSLocalizedString (@"S", @"");
    buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (MCSOAPNoteTypeDischarge)]    = NSLocalizedString (@"D", @"");
    buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (MCSOAPNoteTypeConsultation)] = NSLocalizedString (@"C", @"");

    //
    // Create these on demand ???
    //
    for (int type = MCSOAPNoteTypeMin; type <= MCSOAPNoteTypeMax; type++)
    {
        UIButton *button = [UIButton buttonWithTitle: buttonTitles [INDEX_FROM_SOAP_NOTE_TYPE (type)]
                                              target: self
                                            selector: @selector (changeNoteType:)
                                               frame: CGRectZero
                                                 tag: TAG_FROM_SOAP_NOTE_TYPE (type)];

        button.hidden = YES;

        [footerView_ addSubview: button];
    }

    uploadIndicator_ = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhite];

    uploadIndicator_.hidden = YES;

    [footerView_ addSubview: uploadIndicator_];

    uploadLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    uploadLabel_.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                                     UIViewAutoresizingFlexibleWidth);
    uploadLabel_.backgroundColor = [UIColor whiteColor];
    uploadLabel_.font = [UIFont fontWithName: @"Arial"
                                        size: 14.0f];
    uploadLabel_.hidden = YES;
    uploadLabel_.text = NSLocalizedString (@"Uploading to MedCommons...", @"");
    uploadLabel_.textAlignment = UITextAlignmentLeft;
    uploadLabel_.textColor = [UIColor grayColor];

    [footerView_ addSubview: uploadLabel_];

    [backgroundView_ addSubview: footerView_];
}

- (void) addHeaderView
{
    headerView_ = [[UIView alloc] initWithFrame: CGRectZero];

    headerView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                    UIViewAutoresizingFlexibleWidth);
    headerView_.backgroundColor = [UIColor whiteColor];

    headerLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    headerLabel_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                     UIViewAutoresizingFlexibleWidth);
    headerLabel_.backgroundColor = [UIColor whiteColor];
    headerLabel_.font = [UIFont fontWithName: @"Arial"
                                        size: 14.0f];
    headerLabel_.textAlignment = UITextAlignmentLeft;
    headerLabel_.textColor = [UIColor grayColor];

    [headerView_ addSubview: headerLabel_];

    [backgroundView_ addSubview: headerView_];
}

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (keyboardWillShow:)
               name: UIKeyboardWillShowNotification
             object: nil];
    [nc addObserver: self
           selector: @selector (keyboardWillHide:)
               name: UIKeyboardWillHideNotification
             object: nil];
}

- (void) changeNoteType: (id) sender
{
    UIButton       *button = (UIButton *) sender;
    MCSOAPNoteType  newNoteType = SOAP_NOTE_TYPE_FROM_TAG (button.tag);

    if (noteType_ != newNoteType)
    {
        noteType_ = newNoteType;
        headerLabel_.text = self.note.title;
    }
}

- (void) layoutBackgroundView
{
    CGRect backgroundFrame = self.frame;

    backgroundFrame.size.height -= keyboardHeight_;

    backgroundView_.frame = backgroundFrame;
}

- (void) layoutContentView
{
    contentView_.frame = CGRectMake (CONTENT_MARGIN_LEFT,
                                     (headerView_.frame.size.height +
                                      CONTENT_MARGIN_TOP),
                                     (backgroundView_.frame.size.width -
                                      CONTENT_MARGIN_LEFT -
                                      CONTENT_MARGIN_RIGHT),
                                     (backgroundView_.frame.size.height -
                                      headerView_.frame.size.height -
                                      CONTENT_MARGIN_TOP -
                                      CONTENT_MARGIN_BOTTOM -
                                      footerView_.frame.size.height));
}

- (void) layoutFooterView
{
    footerView_.frame = CGRectMake (0.0f,
                                    (backgroundView_.frame.size.height -
                                     FOOTER_BUTTONS_MARGIN_TOP -
                                     FOOTER_BUTTONS_HEIGHT -
                                     FOOTER_BUTTONS_MARGIN_BOTTOM),
                                    backgroundView_.frame.size.width,
                                    (FOOTER_BUTTONS_MARGIN_TOP +
                                     FOOTER_BUTTONS_HEIGHT +
                                     FOOTER_BUTTONS_MARGIN_BOTTOM));

    for (UIView *view in footerView_.subviews)
        view.hidden = YES;

    if (contentView_.isEditable && !uploadIndicator_.isAnimating)
    {
        CGFloat deltaX = ((footerView_.frame.size.width -
                           FOOTER_BUTTONS_MARGIN_LEFT -
                           FOOTER_BUTTON_WIDTH -
                           FOOTER_BUTTONS_MARGIN_RIGHT) /
                          (TOTAL_SOAP_NOTE_TYPES - 1));
        CGFloat origX = FOOTER_BUTTONS_MARGIN_LEFT;

        for (UIView *view in footerView_.subviews)
        {
            if ([view isKindOfClass: [UIButton class]])
            {
                UIButton *button = (UIButton *) view;

                button.frame = CGRectMake (origX,
                                           FOOTER_BUTTONS_MARGIN_TOP,
                                           FOOTER_BUTTON_WIDTH,
                                           FOOTER_BUTTON_HEIGHT);
                button.hidden = NO;

                origX += deltaX;
            }
        }
    }
    else if (uploadIndicator_.isAnimating)
    {
        //uploadIndicator_.frame = ???;
        uploadIndicator_.hidden = NO;

        [uploadIndicator_ sizeToFit];

        //uploadLabel_.frame = ???;
        uploadLabel_.hidden = NO;

        [uploadLabel_ sizeToFit];
    }
}

- (void) layoutHeaderView
{
    headerView_.frame = CGRectMake (0.0f,
                                    0.0f,
                                    backgroundView_.frame.size.width,
                                    (HEADER_LABEL_MARGIN_TOP +
                                     HEADER_LABEL_HEIGHT +
                                     HEADER_LABEL_MARGIN_BOTTOM));

    headerLabel_.frame = CGRectMake (HEADER_LABEL_MARGIN_LEFT,
                                     HEADER_LABEL_MARGIN_TOP,
                                     (headerView_.frame.size.width -
                                      HEADER_LABEL_MARGIN_LEFT -
                                      HEADER_LABEL_MARGIN_RIGHT),
                                     (headerView_.frame.size.height -
                                      HEADER_LABEL_MARGIN_TOP -
                                      HEADER_LABEL_MARGIN_BOTTOM));
}

- (void) removeNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc removeObserver: self
                  name: UIKeyboardWillShowNotification
                object: nil];
    [nc removeObserver: self
                  name: UIKeyboardWillHideNotification
                object: nil];
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [self layoutBackgroundView];
    [self layoutHeaderView];
    [self layoutFooterView];
    [self layoutContentView];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self removeNotificationObservers];

    [backgroundView_ release];
    [contentView_ release];
    [date_ release];
    [footerView_ release];
    [headerLabel_ release];
    [headerView_ release];
    [patientID_ release];
    [uploadIndicator_ release];
    [uploadLabel_ release];

    [super dealloc];
}

#pragma mark UIKeyboard Notifications

- (void) keyboardWillHide: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];

    //
    // Restore original background view frame; animate resize so it will be in
    // sync with disappearance of keyboard:
    //
    NSValue        *animationDurationValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval  animationDuration;

    [animationDurationValue getValue: &animationDuration];

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];

    keyboardHeight_ = 0.0f;

    [self layoutSubviews];

    [UIView commitAnimations];
}

- (void) keyboardWillShow: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];
    CGFloat       tmpKeyboardHeight;

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
    NSValue *frameValue = [userInfo objectForKey: UIKeyboardFrameEndUserInfoKey];
    CGSize   keyboardSize = [frameValue CGRectValue].size;
    CGSize   windowSize = self.window.frame.size;

    //
    // If keyboard width does not match window width, then assume we're in (or
    // rotating to) landscape mode (view controller's interfaceOrientation is
    // not reliable when about to rotate), else assume we're in (or rotating
    // to) portrait mode.
    //
    // Kinda klugey, but whaddya gonna do?
    //
    tmpKeyboardHeight = ((keyboardSize.width != windowSize.width) ?
                         keyboardSize.width :
                         keyboardSize.height);
#else
    NSValue *boundsValue = [userInfo objectForKey: UIKeyboardBoundsUserInfoKey];

    tmpKeyboardHeight = [boundsValue CGRectValue].size.height;
#endif

    //
    // Reduce size of background view so it will not be obscured by keyboard;
    // animate resize so it will be in sync with appearance of keyboard:
    //
    NSValue        *animationDurationValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval  animationDuration;

    [animationDurationValue getValue: &animationDuration];

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];

    keyboardHeight_ = tmpKeyboardHeight;

    [self layoutSubviews];

    [UIView commitAnimations];
}

@end
