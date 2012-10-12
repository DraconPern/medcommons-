//
//  MCShooterController.m
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "BreadCrumbs.h"
#import "CameraDevice.h"
#import "CustomViews.h"
#import "DataManager.h"
#import "GPSDevice.h"
#import "MCPanningController.h"
#import "MCShooterController.h"
#import "MCSlideSorter.h"
//#import "MCUploadController.h"
#import "ShooterStorageConnector.h"
#import "VideoDevice.h"

#pragma mark -
#pragma mark Public Class MCShooterController
#pragma mark -

@interface MCShooterController () <UIActionSheetDelegate, UITextFieldDelegate, UITextViewDelegate>

- (void) cameraDone: (id) obj;

- (void) cancelAction: (id) sender;

- (void) confirmResetAfterUpload;

- (void) confirmShootPartChoices;

- (void) confirmShootSubjectChoices;

- (BOOL) dispatchTouchEndEvent: (UIView *) theView
                    toPosition: (CGPoint) position;

- (void) flipit;

- (BOOL) isPhotoShootingAvailable;

- (BOOL) isVideoRecordingAvailable;

- (void) prepareNavBar;

- (void) saveAction: (id) sender;

- (void) setTabInt: (NSInteger) num;

- (void) shootPartPhotoFromAlbum;

- (void) shootPartPhotoFromCamera;

- (void) shootPartPhotoFromRoll;

- (void) shootSubjectPhotoFromAlbum;

- (void) shootSubjectPhotoFromCamera;

- (void) shootSubjectPhotoFromRoll;

- (void) shootVideoFromCamera;

- (void) shootVideoFromRoll;

- (void) showActionSheet;

- (void) slidesorter: (id) obje;

- (void) subjectScreenUpdate: (NSTimer *) timer;

- (void) subjectViewFooter;

- (void) subjectViewMainLayout;

- (void) switchToFlipside;

- (void) switchToWideView: (NSInteger) partNum;

-(void) uploadit;

- (void) wideviewit;

@end

@implementation MCShooterController

// none of this works if we try to shoot directly from self, not sure why this is need

#pragma mark Public Instance Methods

- (id) initWithShootingController: (UIViewController *) vc
{
    ///self = [super initWithRootViewController: _mvc]; // make ourself a nav controller
    
    if (self = [super init])
    {
        DataManager *dm = [DataManager sharedInstance];
        
        customViews_ = dm.ffCustomViews;
        disableTouchesSemaphore_ = 0;   // semaphore to disable touch recognition
        patientStore_ = dm.ffPatientStore;
        shootingController_ = [vc retain];
        validFieldEntered_ = NO;
        
        [dm.ffGPSDevice gpsEnable];     // get the gps warmed up
        
        BREADCRUMBS_PUSH;
    }
    
    return self;
}

#pragma mark Private Instance Methods

- (void) cameraDone: (id) obj
{
    // called when camera is disappearing
    //[outerViewController screenUpdate: nil]; // reapint and go back
    
    NSInteger numpics = [patientStore_ countPartPics];
    
    [self setTabInt: numpics];
    
    //[patientStore dumpPatientStore];
}

- (void) cancelAction: (id) sender
{
    // finish typing text/dismiss the keyboard by removing it as the first responder
    //
    [savingTextView_ resignFirstResponder];
    
    [self prepareNavBar];
}

- (void) confirmResetAfterUpload
{
    alertState_ = 21;
    
    actionSheet_ = [[[UIActionSheet alloc] initWithTitle: @"You Have Successfully Uploaded this Photo Series and Can Open a New One"
                                                delegate: self
                                       cancelButtonTitle: @"Cancel"
                                  destructiveButtonTitle: nil
                                       otherButtonTitles: @"New Subject", @"New Series for Subject", nil]
                    autorelease];
    
    //[self showActionSheet];
}

- (void) confirmShootPartChoices
{
    if (![patientStore_ hasSubjectPhoto])
    {
        [self confirmShootSubjectChoices];  // if no subject photo then force it
        
        return;
    }
    
    actionSheet_ = [[[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Shoot Part Photo", @"")
                                                delegate: self
                                       cancelButtonTitle: NSLocalizedString (@"Cancel", @"")
                                  destructiveButtonTitle: nil
                                       otherButtonTitles: NSLocalizedString (@"Photo Album", @""), nil]
                    autorelease];
    
    if ([self isVideoRecordingAvailable])
    {
        alertState_ = 6;
        
        [actionSheet_ addButtonWithTitle: NSLocalizedString (@"Still Camera", @"")];
#if defined(ENABLE_BIGAPP_PLACES)
#else
        [actionSheet_ addButtonWithTitle: NSLocalizedString (@"Video Roll", @"")];
        [actionSheet_ addButtonWithTitle: NSLocalizedString (@"Video Camera", @"")];
#endif
    }
    else if ([self isPhotoShootingAvailable])
    {
        alertState_ = 5;
        
        [actionSheet_ addButtonWithTitle: NSLocalizedString (@"Still Camera", @"")];
    }
    else
        alertState_ = 4;
    
    [self showActionSheet];
}

- (void) confirmShootSubjectChoices
{
    BOOL      needsSubjectPhoto = ![patientStore_ hasSubjectPhoto];
    NSString *alertTitle = (needsSubjectPhoto ?
                            NSLocalizedString (@"Shoot Subject Photo", @"") :
                            NSLocalizedString (@"Reshoot Subject Photo", @""));
    
    actionSheet_ = [[[UIActionSheet alloc] initWithTitle: alertTitle
                                                delegate: self
                                       cancelButtonTitle: NSLocalizedString (@"Cancel", @"")
                                  destructiveButtonTitle: nil
                                       otherButtonTitles: NSLocalizedString (@"Photo Album", @""), nil]
                    autorelease];
    
    if ([self isPhotoShootingAvailable])
    {
        alertState_ = 31;
        
        [actionSheet_ addButtonWithTitle: NSLocalizedString (@"Still Camera", @"")];
    }
    else
        alertState_ = 3;
    
    [self showActionSheet];
}

//
// Checks to see which view, or views, the point is in and then calls a method
// to perform the closing animation, which is to return the piece to its
// original size, as if it is being put down by the user:
//
- (BOOL) dispatchTouchEndEvent: (UIView *) view
                    toPosition: (CGPoint) position
{
    // Check to see which view, or views, the point is in and then animate to that position.
    if ((CGRectContainsPoint (toggleFrame_, position)))
    {
        [self slidesorter: NULL];
        
        return YES;
    }
    
    // Check to see which view, or views,  the point is in and then animate to that position.
    for (NSInteger idx = 0; idx < [tinyPics_ count] - 1; idx++)
    {
        if (CGRectContainsPoint ([[tinyPics_ objectAtIndex: idx] frame],
                                 position))
        {
            if (idx == 0)
            {
                if ([patientStore_ hasSubjectPhoto])
                    [self switchToWideView: idx];
                else
                    [self confirmShootSubjectChoices];
            }
            else if ([patientStore_ hasPhotoAtIndex: idx - 1])
                [self switchToWideView: idx];
            else
                [self confirmShootPartChoices];
            
            return YES;
        }
    }
    
    return NO;
}

- (void) flipit
{
    //MY_ASSERT (flipsideController != nil);
    
    //  [self.navigationController pushViewController: (MainInfoController *) flipsideController
    //                                         animated: YES];
}

- (BOOL) isPhotoShootingAvailable
{
    return [UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera];
}

- (BOOL) isVideoRecordingAvailable
{
    if (![UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
        return NO;
    
    return [[UIImagePickerController availableMediaTypesForSourceType: UIImagePickerControllerSourceTypeCamera] containsObject: @"public.movie"];
}

- (void) prepareNavBar
{
    ShooterStorageConnector *ps = [[DataManager sharedInstance] ffPatientStore];
    NSInteger                count = [ps countPartPics];
    
    if ([ps hasSubjectPhoto])
        count++;
    
    if (count == 0)
        self.navigationItem.rightBarButtonItem = nil;
    else
        self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle: @"Upload"
                                                                                   style: UIBarButtonItemStylePlain
                                                                                  target: self
                                                                                  action: @selector (uploadit)]
                                                  autorelease];
    
    self.navigationItem.title = @"Camera";
    self.navigationItem.backBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle: @"Back"
                                                                              style: UIBarButtonItemStylePlain
                                                                             target: nil
                                                                             action: nil]
                                             autorelease];
}

- (void) saveAction: (id) sender
{
    //[prefs setObject:savingTextView.text forKey:@"comment"];
    // finish typing text/dismiss the keyboard by removing it as the first responder
    //
    [savingTextView_ resignFirstResponder];
    
    [self prepareNavBar];
}

- (void) setTabInt: (NSInteger) num
{
    [UIApplication sharedApplication].applicationIconBadgeNumber = num; // show however many are lingering
}

- (void) shootPartPhotoFromAlbum
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: NO
                                                 wantsCamera: NO
                                                   wantsRoll: NO];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootPartPhotoFromCamera
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: NO
                                                 wantsCamera: YES
                                                   wantsRoll: NO];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootPartPhotoFromRoll
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: NO
                                                 wantsCamera: NO
                                                   wantsRoll: YES];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootSubjectPhotoFromAlbum
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: YES
                                                 wantsCamera: NO
                                                   wantsRoll: NO];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootSubjectPhotoFromCamera
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: YES
                                                 wantsCamera: YES
                                                   wantsRoll: NO];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootSubjectPhotoFromRoll
{
    CameraDevice *cd = [[CameraDevice alloc] initWithSubject: YES
                                                 wantsCamera: NO
                                                   wantsRoll: YES];
    
    [shootingController_ presentModalViewController: cd
                                           animated: YES];
    
    [cd release];
}

- (void) shootVideoFromCamera
{
    VideoDevice *vd = [[VideoDevice alloc] initWithCamera: YES];
    
    [shootingController_ presentModalViewController: vd
                                           animated: YES];
    
    [vd release];
}

- (void) shootVideoFromRoll
{
    VideoDevice *vd = [[VideoDevice alloc] initWithCamera: NO];
    
    [shootingController_ presentModalViewController: vd
                                           animated: YES];
    
    [vd release];
}

- (void) showActionSheet
{
    [actionSheet_ showFromTabBar: (UITabBar *) self.view];
}

- (void) slidesorter: (id) obje
{
    MCSlideSorter *horizontalController = [[MCSlideSorter alloc] init];
    
    MY_ASSERT (horizontalController != nil);
    
    [self.navigationController pushViewController: horizontalController
                                         animated: YES];
    
    [horizontalController release];
}

- (void) subjectScreenUpdate: (NSTimer *) Timer
{
    [customViews_ customMainPortraitControllerFieldReload];  //reload from prefs incase was updated from dashboard
    
    [self subjectViewMainLayout];
    [self subjectViewFooter];
    [self prepareNavBar];
}

- (void) subjectViewFooter
{
    for (NSInteger idx = 1; idx <= 10; idx++)
    {
        // get the next possible file
        UIImageView *oneImageView = [tinyPics_ objectAtIndex: idx]; //compensate for photo pic
        
        if ([patientStore_ hasPhotoAtIndex: idx - 1])
            oneImageView.image = [UIImage imageWithContentsOfFile:  [patientStore_ fullPhotoSpecAtIndex: idx - 1]];
        else
            oneImageView.image = [UIImage imageNamed: PLEASE_SHOOT_PATIENT_IMAGE];
    }
}

- (void) subjectViewMainLayout
{
    UIImageView *oneImageView = [tinyPics_ objectAtIndex: 0];
    
    //  if ([patientStore_ hasRemoteSubjectPhoto])
    //      oneImageView.image = [patientStore_ remoteSubjectPhoto];
    //else
    if (![patientStore_ hasSubjectPhoto])
        oneImageView.image = [UIImage imageNamed: PLEASE_SHOOT_PATIENT_IMAGE];
    else
        oneImageView.image = [UIImage imageWithContentsOfFile: [patientStore_ fullSubjectPhotoSpec]];
}

- (void) switchToFlipside
{
    //let the actionsheet unwind before flipping
    [self performSelector: @selector (flipit)
               withObject: nil
               afterDelay: 0.0f];
}

- (void) switchToWideView: (NSInteger) partNum
{
    //let the actionsheet unwind before flipping -- need to set initial page
    //  [horizontalController loadScrollViewWithPage: partNum];
    //  [horizontalController changePage: nil];
    
    [self performSelector: @selector (wideviewit)
               withObject: nil
               afterDelay: 0.0f];
}

-(void) uploadit
{
#if 0
    if (disableTouchesSemaphore == 0)
        [mainViewController confirmUpload]; // here when button was hit
    {
        NSString *msg1 = @"";
        NSString *msg2 = @"";
        
        if ([patientStore haveSubjectPhoto])
            msg1 = @"a new Patient Photo";
        
        if ([patientStore countPartPics] > 0)
            msg2 = [NSString stringWithFormat: @"- %d target photos", [patientStore countPartPics]];
        
        NSString *middle = [NSString stringWithFormat: @"Uploading %@ %@", msg1, msg2];
        
        MCUploadController *aneController = [[MCUploadController alloc] initWithTitle: @"Photo Uploading"
                                                                           andWithTop: @"hitting upload will store photos to the patient's medcommons account"
                                                                        andWithMiddle: middle
                                                                       andWithSuccess: @"Silhouette.png"
                                                                       andWithFailure: @"logoGray.png"];
        
        [self.navigationController pushViewController: aneController
                                             animated: YES];
        
        [aneController release];
    }
#endif
}

- (void) wideviewit
{
    MCPanningController *horizontalController = [[MCPanningController alloc] init];
    
    MY_ASSERT (horizontalController != nil);
    
    [self.navigationController pushViewController: horizontalController
                                         animated: YES];
    
    [horizontalController release];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    CGRect appFrame = [[UIScreen mainScreen] applicationFrame];
    
    appFrame.origin.y = appFrame.origin.y + 44.0f;
    appFrame.size.height = appFrame.size.height - 44.0f;
    
    outerView_ = [[UIView alloc] initWithFrame: appFrame];
    
    outerView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                   UIViewAutoresizingFlexibleWidth);
    outerView_.backgroundColor = [UIColor whiteColor];
    
    self.view = outerView_;
    
    //*******************   LAYOUT OF Subject Demographics Standard View
    float picsRow1Start = -4.0f + 327.0f - 44.0f;
    float picsRow2Start = -4.0f + 392.0f - 44.0f;
    
    touchFrame_ = CGRectMake (0.0f,
                              picsRow1Start - 5.0f,
                              320.0f,
                              picsRow2Start + 80.0f);    // defined at outer level
    
    pictureframe_ = CGRectMake (4.0f + 4.0f,
                                -4.0f + 56.0f,
                                80.0f,
                                80.0f);
    
    toggleFrame_ = CGRectMake (17.0f, 198.0f, 60.0f, 60.0f);
    iconFrame_ = CGRectMake (18.0f, 145.0f, 57.0f, 57.0f);
    
    //  NSLog (@"This specific app %@ is calling remote server with appkey %@",
    //[[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleName"], appkeyCFBundleIconFile }
    
    UIImageView *iconView = [[UIImageView alloc] initWithFrame: iconFrame_];
    
    //iconView.image = [UIImage imageNamed:[[[NSBundle mainBundle] infoDictionary]
    //                                        objectForKey:@"CFBundleIconFile"]];
    
    iconView.image = [UIImage imageNamed: [[DataManager sharedInstance] ffAppLogoImage]];
    
    [outerView_ addSubview: iconView];
    
    [iconView release];
    
    UIButton *cbutton = [UIButton buttonWithType: UIButtonTypeInfoDark];
    
    [cbutton setFrame: toggleFrame_];
    //  [cbutton addTarget: self
    //                action: @selector (slidesorter:)
    //      forControlEvents: UIControlEventTouchUpInside];

    [outerView_ addSubview: cbutton];
    //****************   End of Layout
    
    //
    // At the very bottom there will be an array of up to 10 photos:
    //
    tinyPics_  = [[NSMutableArray alloc] init];
    
    //
    // Make the Subject be the zeroth entry:
    //
    UIImageView *oneImageView = [[UIImageView alloc] initWithFrame: pictureframe_];
    
    //  if ([patientStore haveRemoteSubjectPhoto])
    //      oneImageView.image  = [patientStore remoteSubjectPhoto];
    //  else
    //  {
    //  if(![patientStore haveSubjectPhoto])
    //          oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];   else
    //              oneImageView.image = [UIImage imageWithContentsOfFile:[patientStore fullSubjectPhotoSpec]]; //??
    //  }
    
    [tinyPics_ addObject: oneImageView]; // remember this, we need it to show the uploads
    [outerView_ addSubview: oneImageView];
    
    [oneImageView release];
    
    for (NSInteger idx = 0; idx < 5; idx++)
    {
        NSInteger xpos = 4.0f + (idx * 63.0f);
        CGRect    tf = CGRectMake (xpos, picsRow1Start, 60.0f, 60.0f);
        
        oneImageView = [[UIImageView alloc] initWithFrame: tf];
        
        [tinyPics_ addObject: oneImageView];     // remember this, we need it to show the uploads
        [outerView_ addSubview: oneImageView ];
        
        [oneImageView release];
    }
    
    for (NSInteger idx = 5; idx < 10; idx++)
    {
        NSInteger xpos = 4.0f + ((idx - 5) * 63.0f);
        CGRect    tf = CGRectMake (xpos, picsRow2Start, 60.0f, 60.0f);
        
        oneImageView = [[UIImageView alloc] initWithFrame: tf];
        
        [tinyPics_ addObject: oneImageView];     // remember this, we need it to show the uploads
        [outerView_ addSubview: oneImageView ];
        
        [oneImageView release];
    }
    
    UIView *cView = [customViews_ customMainPortaitControllerLoadView: self];
    
    [outerView_ addSubview: cView ];
    
    [cView release];
    [outerView_ release];
    
    // prepare the navbar
    [self prepareNavBar];
    
    //[self subjectScreenUpdate: nil];
    
    // Set up the window
    TRY_RECOVERY;
}

- (void) viewWillAppear: (BOOL) animated
{
    [self subjectScreenUpdate: nil];    // repaint screen
    
    [super viewWillAppear: animated];
    
}

#pragma mark Overridden UIResponder Methods

- (void) touchesBegan: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    if (disableTouchesSemaphore_ == 0)
    {
        NSUInteger numTaps = [[touches anyObject] tapCount];
        
        if(numTaps >= 2) //???
        {
            
        }
    }
}

- (void) touchesEnded: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    if (disableTouchesSemaphore_ == 0)
    {
        // Enumerates through all touch object
        for (UITouch *touch in touches)
        {
            // Sends to the dispatch method, which will make sure the appropriate subview is acted upon
            if ([self dispatchTouchEndEvent: [touch view]
                                 toPosition: [touch locationInView: self.view]] == YES)
                return;
        }
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [[DataManager sharedInstance].ffGPSDevice gpsDisable];  // get the turned off
    
    [tinyPics_ release];
    [shootingController_ release];
    
    BREADCRUMBS_POP;
    
    [super dealloc];
}

#pragma mark UIActionSheetDelegate Methods

- (void) actionSheet: (UIActionSheet *) alertView
clickedButtonAtIndex: (NSInteger) buttonIndex
{
    BOOL needsSubjectPhoto = ![patientStore_ hasSubjectPhoto];
    
    switch (alertState_)
    {
        case 3 :
            if (buttonIndex == 0)
                [self shootSubjectPhotoFromAlbum];
            break;
            
        case 31 :
            if (buttonIndex == 0)
                [self shootSubjectPhotoFromAlbum];
            else if (buttonIndex == 1)
                [self shootSubjectPhotoFromCamera];
            break;
            
        case 4 :
            if (buttonIndex == 0)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromAlbum];
                else
                    [self shootPartPhotoFromAlbum];
            }
            break;
            
        case 5 :
            if (buttonIndex == 0)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromAlbum];
                else
                    [self shootPartPhotoFromAlbum];
            }
            else if (buttonIndex == 1)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromCamera];
                else
                    [self shootPartPhotoFromCamera];
            }
            break;
            
        case 6 :
            if (buttonIndex == 0)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromAlbum];
                else
                    [self shootPartPhotoFromAlbum];
            }
            else if (buttonIndex == 1)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromCamera];
                else
                    [self shootPartPhotoFromCamera];
            }
            else if (buttonIndex == 2)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromCamera];
                else
                    [self shootVideoFromRoll];
            }
            else if (buttonIndex == 3)
            {
                if (needsSubjectPhoto)
                    [self shootSubjectPhotoFromCamera];
                else
                    [self shootVideoFromCamera];
            }
            break;
            
        default :
            break;
    }
}

#pragma mark UITextFieldDelegate Methods

- (void) textFieldDidBeginEditing: (UITextField *) textField
{
    disableTouchesSemaphore_++;  // bump semaphore, will prevent us from recognizing touches while editing from keyboard
    
    validFieldEntered_ = NO;
}

- (void) textFieldDidEndEditing: (UITextField *) textField
{
    validFieldEntered_ = YES;
    
    disableTouchesSemaphore_--;
    
    if (disableTouchesSemaphore_ < 0)
        NSLog (@"text edit semaphore failure");
    
    [customViews_ customMainPortaitControllerStoreTextData: textField];
    
    [textField resignFirstResponder];   // this gets rid of the keyboard
}

- (BOOL) textFieldShouldBeginEditing: (UITextField *) textField
{
    return YES;
}

- (BOOL) textFieldShouldClear:(UITextField *)textField
{
    return YES;
}

- (BOOL) textFieldShouldEndEditing:(UITextField *)textField
{
    return YES;
}

- (BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];   // this gets rid of the keyboard
    
    return YES;
}

#pragma mark UITextViewDelegate Methods

- (void) textViewDidBeginEditing: (UITextView *) textView
{
    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemCancel
                                                                                  target: self
                                                                                  action: @selector (cancelAction:)];
    
    UIBarButtonItem *saveButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemDone
                                                                                target: self
                                                                                action: @selector (saveAction:)];
    
    self.navigationItem.leftBarButtonItem = cancelButton;
    self.navigationItem.rightBarButtonItem = saveButton;
    
    savingTextView_ = textView;  // keep track of this text view
    
    [cancelButton release];
    [saveButton release];
}

@end
