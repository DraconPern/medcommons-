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

#define HEADER_LABEL_TAG                101
#define UPLOAD_INDICATOR_TAG            102
#define UPLOAD_LABEL_TAG                103

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

+ (UIView *) newBackgroundView;

+ (UIView *) newContentView;

+ (UIView *) newFooterView;

+ (UIView *) newHeaderView;

- (void) changeNoteType: (id) sender;

@end

@implementation MCSOAPNoteView

@dynamic editable;
@dynamic note;
@dynamic uploading;

#pragma mark Dynamic Property Methods

- (BOOL) isEditable
{
    UITextView *textView = (UITextView *) self.contentView;
    
    return textView.isEditable;
}

- (BOOL) isUploading
{
    return uploadIndicator_.isAnimating;
}

- (MCSOAPNote *) note
{
    UITextView *textView = (UITextView *) self.contentView;
    
    return [MCSOAPNote noteWithPatientID: patientID_
                                    date: date_
                                    type: noteType_
                                    text: textView.text];
}

- (void) setEditable: (BOOL) editable
{
    UITextView *textView = (UITextView *) self.contentView;
    BOOL        oldEditable = textView.isEditable;
    
    if (!oldEditable != !editable)
    {
        textView.editable = editable;

        //
        // Keyboard will NEVER show if text view is NOT editable, so avoid the
        // overhead:
        //
        self.resizesForKeyboard = editable;

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
    if (self = [super initWithFrame: frame
                     backgroundView: [[MCSOAPNoteView newBackgroundView] autorelease]
                        contentView: [[MCSOAPNoteView newContentView] autorelease]
                         headerView: [[MCSOAPNoteView newHeaderView] autorelease]
                         footerView: [[MCSOAPNoteView newFooterView] autorelease]])
    {
        date_ = [note.date copy];
        headerLabel_ = [[self.headerView viewWithTag: HEADER_LABEL_TAG] retain];
        noteType_ = note.type;
        patientID_ = [note.patientID retain];
        uploadIndicator_ = [[self.footerView viewWithTag: UPLOAD_INDICATOR_TAG] retain];
        uploadLabel_ = [[self.footerView viewWithTag: UPLOAD_LABEL_TAG] retain];
        
        UITextView *textView = (UITextView *) self.contentView;
        
        textView.text = note.text;
        headerLabel_.text = self.note.title;

        self.resizesForKeyboard = self.isEditable;
    }
    
    return self;
}

#pragma mark Private Class Methods

+ (UIView *) newBackgroundView
{
    UIView *view = [[UIView alloc] initWithFrame: CGRectZero];
    
    view.backgroundColor = [UIColor colorWithWhite: 0.96f
                                             alpha: 1.0f];
    
    return view;
}

+ (UIView *) newContentView
{
    UITextView *textView = [[UITextView alloc] initWithFrame: CGRectZero];
    
    textView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                 UIViewAutoresizingFlexibleWidth);
    textView.backgroundColor = [UIColor colorWithWhite: 0.92f
                                                 alpha: 1.0f];
    textView.editable = NO; // avoid keyboard notification overhead
    textView.font = [UIFont systemFontOfSize: 14.0f];
    textView.keyboardType = UIKeyboardTypeDefault;
    textView.returnKeyType = UIReturnKeyDefault;
    textView.scrollEnabled = YES;
    textView.textColor = [UIColor darkTextColor];
    
    return textView;
}

+ (UIView *) newFooterView
{
    UIView *view = [[UIView alloc] initWithFrame: CGRectZero];
    
    view.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                             UIViewAutoresizingFlexibleWidth);
    view.backgroundColor = [UIColor clearColor];
    
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
        
        [view addSubview: button];
    }
    
    UIActivityIndicatorView *aiv = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhite];
    
    aiv.hidden = YES;
    aiv.tag = UPLOAD_INDICATOR_TAG;
    
    [view addSubview: aiv];
    
    UILabel *label = [[UILabel alloc] initWithFrame: CGRectZero];
    
    label.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                              UIViewAutoresizingFlexibleWidth);
    label.backgroundColor = [UIColor whiteColor];
    label.font = [UIFont fontWithName: @"Arial"
                                 size: 14.0f];
    label.hidden = YES;
    label.tag = UPLOAD_LABEL_TAG;
    label.text = NSLocalizedString (@"Uploading to MedCommons...", @"");
    label.textAlignment = UITextAlignmentLeft;
    label.textColor = [UIColor grayColor];
    
    [view addSubview: label];
    
    return view;
}

+ (UIView *) newHeaderView
{
    UIView *view = [[UIView alloc] initWithFrame: CGRectZero];
    
    view.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                             UIViewAutoresizingFlexibleWidth);
    view.backgroundColor = [UIColor whiteColor];
    
    UILabel *label = [[UILabel alloc] initWithFrame: CGRectZero];
    
    label.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                              UIViewAutoresizingFlexibleWidth);
    label.backgroundColor = [UIColor whiteColor];
    label.font = [UIFont fontWithName: @"Arial"
                                 size: 14.0f];
    label.tag = HEADER_LABEL_TAG;
    label.textAlignment = UITextAlignmentLeft;
    label.textColor = [UIColor grayColor];
    
    [view addSubview: label];
    
    return view;
}

#pragma mark Private Instance Methods

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

#pragma mark Overridden MCStandardView Methods

- (void) layoutContentView
{
    self.contentView.frame = CGRectMake (CONTENT_MARGIN_LEFT,
                                         (self.headerView.frame.size.height +
                                          CONTENT_MARGIN_TOP),
                                         (self.backgroundView.frame.size.width -
                                          CONTENT_MARGIN_LEFT -
                                          CONTENT_MARGIN_RIGHT),
                                         (self.backgroundView.frame.size.height -
                                          self.headerView.frame.size.height -
                                          CONTENT_MARGIN_TOP -
                                          CONTENT_MARGIN_BOTTOM -
                                          self.footerView.frame.size.height));
}

- (void) layoutFooterView
{
    self.footerView.frame = CGRectMake (0.0f,
                                        (self.backgroundView.frame.size.height -
                                         FOOTER_BUTTONS_MARGIN_TOP -
                                         FOOTER_BUTTONS_HEIGHT -
                                         FOOTER_BUTTONS_MARGIN_BOTTOM),
                                        self.backgroundView.frame.size.width,
                                        (FOOTER_BUTTONS_MARGIN_TOP +
                                         FOOTER_BUTTONS_HEIGHT +
                                         FOOTER_BUTTONS_MARGIN_BOTTOM));
    
    for (UIView *view in self.footerView.subviews)
        view.hidden = YES;
    
    if (self.isEditable && !uploadIndicator_.isAnimating)
    {
        CGFloat deltaX = ((self.footerView.frame.size.width -
                           FOOTER_BUTTONS_MARGIN_LEFT -
                           FOOTER_BUTTON_WIDTH -
                           FOOTER_BUTTONS_MARGIN_RIGHT) /
                          (TOTAL_SOAP_NOTE_TYPES - 1));
        CGFloat origX = FOOTER_BUTTONS_MARGIN_LEFT;
        
        for (UIView *view in self.footerView.subviews)
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
    self.headerView.frame = CGRectMake (0.0f,
                                        0.0f,
                                        self.backgroundView.frame.size.width,
                                        (HEADER_LABEL_MARGIN_TOP +
                                         HEADER_LABEL_HEIGHT +
                                         HEADER_LABEL_MARGIN_BOTTOM));
    
    headerLabel_.frame = CGRectMake (HEADER_LABEL_MARGIN_LEFT,
                                     HEADER_LABEL_MARGIN_TOP,
                                     (self.headerView.frame.size.width -
                                      HEADER_LABEL_MARGIN_LEFT -
                                      HEADER_LABEL_MARGIN_RIGHT),
                                     (self.headerView.frame.size.height -
                                      HEADER_LABEL_MARGIN_TOP -
                                      HEADER_LABEL_MARGIN_BOTTOM));
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [date_ release];
    [headerLabel_ release];
    [patientID_ release];
    [uploadIndicator_ release];
    [uploadLabel_ release];
    
    [super dealloc];
}

@end
