//
//  ShooterController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "AppDelegate.h"
#import "CustomViews.h"
#import "SessionManager.h"
#import "Member.h"
#import "MemberStore.h"
#import "PanningController.h"
#import "Photo.h"
#import "Session.h"
#import "SessionManager.h"
#import "ShooterController.h"
#import "ShooterView.h"
#import "SlideSorter.h"

//
// Simulator        - UIImagePickerControllerSourceTypeSavedPhotosAlbum
//
// iPod touch, iPad - UIImagePickerControllerSourceTypeSavedPhotosAlbum
//
// iPhone 3G        - UIImagePickerControllerSourceTypeSavedPhotosAlbum,
//                    UIImagePickerControllerSourceTypePhotoLibrary,
//                    UIImagePickerControllerSourceTypeCamera - Photos only
//
// iPhone 3GS       - UIImagePickerControllerSourceTypeSavedPhotosAlbum,
//                    UIImagePickerControllerSourceTypePhotoLibrary,
//                    UIImagePickerControllerSourceTypeCamera - Photos & Video
//

#pragma mark -
#pragma mark Public Class ShooterController
#pragma mark -

#pragma mark Internal Constants

//
// Action sheet button indexes:
//
enum
{
    PHOTO_ALBUM_BUTTON_INDEX  = 0,
    STILL_CAMERA_BUTTON_INDEX,
    VIDEO_ROLL_BUTTON_INDEX,
    VIDEO_CAMERA_BUTTON_INDEX
};

#define MAX_VIDEO_DURATION 30.0f

#define LOCAL_FILE_KEY     @"local-file"
#define MEDIA_TYPE_KEY     @"media-type"
#define SHOOT_TIME_KEY     @"shoot-type"
#define SIZE_KEY           @"size"
#define SLOT_KEY           @"slot"

@interface ShooterController () <UIActionSheetDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UITextFieldDelegate>

@property (nonatomic, retain, readwrite) UIActionSheet   *actionSheet;
@property (nonatomic, retain, readwrite) UIBarButtonItem *backButton;
@property (nonatomic, retain, readwrite) UIBarButtonItem *doneButton;
@property (nonatomic, assign, readonly)  BOOL             hasUnsavedPhotos;

//- (void) cameraDone: (id) obj;

- (void) confirmShootChoices;

- (void) dismissImagePickerController;

- (BOOL) handleTapAtLocation: (CGPoint) location;

- (BOOL) isCameraAvailable;

- (BOOL) isVideoRecordingAvailable;

//- (void) photopanning;

- (void) presentImagePickerControllerWithSourceType: (UIImagePickerControllerSourceType) sourceType
                                         mediaTypes: (NSArray *) mediaTypes;

//- (void) rearrangePhotos: (id) sender;

- (void) refresh;

- (void) savePhoto: (NSDictionary *) info;

//- (void) saveVideo: (NSDictionary *) info;

//- (void) setTabInt: (NSInteger) num;

- (void) showActionSheet;

//- (void) switchToPhotoPanning: (NSInteger) partNum;

- (void) updateNavigationItemAnimated: (BOOL) animated;

- (void) uploadPhotos: (id) sender;

- (void) video: (NSString *) videoPath
didFinishSavingWithError: (NSError *) error
   contextInfo: (NSString *) contextInfo;

@end

@implementation ShooterController

@synthesize actionSheet                = actionSheet_;
@synthesize backButton                 = backButton_;
@dynamic    hasUnsavedPhotos;
@dynamic    hidesMasterViewInLandscape;
@synthesize doneButton                 = doneButton_;

#pragma mark Private Instance Methods

//- (void) cameraDone: (id) obj
//{
//    DataManager *dm = self.appDelegate.dataManager;
//    MemberStore *mstore = dm.memberStore;
//
//    // called when camera is disappearing
//    //[outerViewController screenUpdate: nil]; // repaint and go back
//
//    [self setTabInt: [mstore numberOfPartPhotos]];
//
//    //[mstore dump];
//}

- (void) confirmShootChoices
{
    if (!self.actionSheet.isVisible)
    {
        NSString *cbTitle = ((self.appDelegate.targetIdiom != UIUserInterfaceIdiomPad) ?
                             NSLocalizedString (@"Cancel", @"") :
                             nil);
        NSString *asTitle = (self->subjectPhoto_ ?
                             (self->replacePhoto_ ?
                              NSLocalizedString (@"Reshoot Subject Photo", @"") :
                              NSLocalizedString (@"Shoot Subject Photo", @"")) :
                             (self->replacePhoto_ ?
                              NSLocalizedString (@"Reshoot Part Photo", @"") :
                              NSLocalizedString (@"Shoot Part Photo", @"")));

        self.actionSheet = [[[UIActionSheet alloc] initWithTitle: asTitle
                                                        delegate: self
                                               cancelButtonTitle: cbTitle
                                          destructiveButtonTitle: nil
                                               otherButtonTitles:  nil]
                            autorelease];
        // reorganize, was wierd!!
        if ([self isCameraAvailable])
        {
            [self.actionSheet addButtonWithTitle: NSLocalizedString (@"Still Camera", @"")];

            [self.actionSheet addButtonWithTitle:  NSLocalizedString (@"Photo Album", @"")]; // see what's up

            if ([self isVideoRecordingAvailable] && !self->subjectPhoto_)
            {

                [self.actionSheet addButtonWithTitle: NSLocalizedString (@"Video Camera", @"")];
                [self.actionSheet addButtonWithTitle: NSLocalizedString (@"Video Roll", @"")];
            }
        }
        else
            [self.actionSheet addButtonWithTitle:  NSLocalizedString (@"Photo Album", @"")]; // see what's up

        [self showActionSheet];
    }
}

- (void) dismissImagePickerController
{
    if (!self->usePopover_)
        [self dismissModalViewControllerAnimated: YES];
    else if (self->popoverController_ && self->popoverController_.isPopoverVisible)
        [self->popoverController_ dismissPopoverAnimated: YES];
}

- (BOOL) handleTapAtLocation: (CGPoint) location
{
    SessionManager *sm = self.appDelegate.sessionManager;
    MemberStore    *mstore = sm.loginSession.memberInFocus.store;
    ShooterView    *sv = (ShooterView *) self.view;

    if ([sv isSubjectThumbAtLocation: location])
    {
        self->partPhotoIndex_ = NSNotFound;
        self->replacePhoto_ = [mstore hasSubjectPhoto];
        self->subjectPhoto_ = YES;
        self->tappedRect_ = [sv rectForSubjectThumb];

        [self confirmShootChoices];

        return YES;
    }

    NSUInteger idx = [sv indexOfPartThumbAtLocation: location];

    if (idx != NSNotFound)
    {
        self->partPhotoIndex_ = idx;
        self->replacePhoto_ = [mstore hasPartPhotoAtIndex: idx];
        self->subjectPhoto_ = NO;
        self->tappedRect_ = [sv rectForPartThumbAtIndex: idx];

        [self confirmShootChoices];

        return YES;
    }

    return NO;
}

- (BOOL) hasUnsavedPhotos
{
    SessionManager *sm = self.appDelegate.sessionManager;
    MemberStore    *mstore = sm.loginSession.memberInFocus.store;

    return ([mstore hasSubjectPhoto] ||
            ([mstore numberOfPartPhotos] > 0));
}

- (BOOL) isCameraAvailable
{
    return [UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera];
}

- (BOOL) isVideoRecordingAvailable
{
    return ([self isCameraAvailable] &&
            [[UIImagePickerController availableMediaTypesForSourceType:
              UIImagePickerControllerSourceTypeCamera]
             containsObject: (NSString *) kUTTypeMovie]);
}

//- (void) photopanning   // need a better method name ...
//{
//    PanningController *pc = [[[PanningController alloc] init]
//                             autorelease];
//
//    [self.navigationController pushViewController: pc
//                                         animated: YES];
//}

- (void) presentImagePickerControllerWithSourceType: (UIImagePickerControllerSourceType) sourceType
                                         mediaTypes: (NSArray *) mediaTypes
{
    UIImagePickerController *ipc = [[[UIImagePickerController alloc] init]
                                    autorelease];

    ipc.allowsEditing  = YES;
    ipc.delegate = self;
    ipc.sourceType = sourceType;

    if (mediaTypes)
    {
        ipc.mediaTypes = mediaTypes;
        ipc.videoMaximumDuration = MAX_VIDEO_DURATION;
        ipc.videoQuality = UIImagePickerControllerQualityTypeMedium;
    }

    if (self->usePopover_)
    {
        if (self->popoverController_)
            self->popoverController_.contentViewController = ipc;
        else
            self->popoverController_ = [[UIPopoverController alloc]
                                        initWithContentViewController: ipc];

        [self->popoverController_ presentPopoverFromRect: self->tappedRect_
                                                  inView: self.view
                                permittedArrowDirections: UIPopoverArrowDirectionAny
                                                animated: YES];
    }
    else
        [self presentModalViewController: ipc
                                animated: YES];
}

//- (void) rearrangePhotos: (id) sender
//{
//    SlideSorter *ss = [[[SlideSorter alloc] init]
//                       autorelease];
//
//    NSAssert (ss != nil,
//              @"Nil slide sorter!");
//
//    [self.navigationController pushViewController: ss
//                                         animated: YES];
//}

- (void) refresh
{
    // called when coming out of camera
    [(ShooterView *) self.view update];

    [self updateNavigationItemAnimated: NO];
}

- (void) savePhoto: (NSDictionary *) info
{
    UIImage        *image = [info objectForKey: UIImagePickerControllerEditedImage];
    NSData         *png = UIImagePNGRepresentation (image);
    SessionManager *sm = self.appDelegate.sessionManager;
    MemberStore    *mstore = sm.loginSession.memberInFocus.store;
    NSString       *path = [mstore nextFreePathForMediaDataWithType: (NSString *) kUTTypeImage];
    //
    // I'm not 100% sure, but I think the server is expecting the subject photo
    // to be labeled as slot 0 and the part photos to start at slot 1 -- JGP:
    //
    NSUInteger    slot = (self->subjectPhoto_ ? 0 : self->partPhotoIndex_ + 1);
    NSDictionary *attrs = [NSDictionary dictionaryWithObjectsAndKeys:
                           path, LOCAL_FILE_KEY,
                           @"photo", MEDIA_TYPE_KEY,
                           [NSDate date], SHOOT_TIME_KEY,
                           [NSString stringWithFormat: @"%d", [png length]], SIZE_KEY,
                           [NSString stringWithFormat: @"%d", slot], SLOT_KEY,
                           nil];
    Photo        *photo = [Photo photoWithPath: path
                                    attributes: attrs];

    if ([png writeToFile: path  // push this down into MemberStore ???
              atomically: YES])
    {
        [mstore beginUpdates];

        if (self->subjectPhoto_)
            mstore.subjectPhoto = photo;
        else if (self->replacePhoto_)
            [mstore replacePartPhotoAtIndex: self->partPhotoIndex_
                              withPartPhoto: photo];
        else
            [mstore addPartPhoto: photo];

        [mstore endUpdates];
    }
    else
        NSLog (@"Error writing PNG file to %@", path);
}

//- (void) saveVideo: (NSDictionary *) info
//{
//    MemberStore *mstore = member.memberStore;
//    NSString    *photoPath = [mstore nextFreePathForMediaData];
//    NSUInteger   slot = [mstore nextFreeIndexForPhoto] + 1;
//
//    //
//    // Create thumbnail:
//    //
//    NSString *uniquePath = [NSHomeDirectory () stringByAppendingPathComponent: photoPath];
//    UIImage  *image = [UIImage imageNamed: @"qt.gif"];
//    //                [info objectForKey: UIImagePickerControllerEditedImage]
//    NSData   *png = UIImagePNGRepresentation (image);
//
//    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithObjectsAndKeys:
//                                 @"thumbnail", @"media-type",
//                                 uniquePath, @"local-file",
//                                 [NSDate date], @"shoot-time",
//                                 [NSString stringWithFormat: @"%d", [png length]], @"size",
//                                 [NSString stringWithFormat: @"%d", slot], @"slot",
//                                 nil];
//
//    [png writeToFile: uniquePath
//          atomically: YES];
//
//    [mstore writeToPropertyList];
//
//    NSLog (@"*** Saved %d bytes of photo to %@, index %d ***",
//           [png length],
//           photoPath,
//           slot);
//
//    //
//    // Save thumbnail:
//    //
//    [mstore setPartPhotoAtIndex: slot
//               withRelativePath: photoPath
//                     attributes: dict];
//
//    //
//    // Save video (if compatible):
//    //
//    NSURL    *videoURL = [info objectForKey: UIImagePickerControllerMediaURL];
//    NSString *videoPath = [videoURL path];
//
//    if (UIVideoAtPathIsCompatibleWithSavedPhotosAlbum (videoPath))
//    {
//        [dict setValue: @"video"
//                forKey: @"media-type"];
//
//        UISaveVideoAtPathToSavedPhotosAlbum (videoPath,
//                                             self,
//                                             @selector (video:didFinishSavingWithError:contextInfo:),
//                                             NULL);
//
//        [mstore setVideoAtIndex: slot
//               withRelativePath: videoPath
//                     attributes: dict];
//
//        NSLog (@"**** Save of video to %@ index %d ***",
//               videoPath,
//               slot);
//    }
//}

//- (void) setTabInt: (NSInteger) num
//{
//    self.application.applicationIconBadgeNumber = num;
//    // show however many are lingering
//}

- (void) showActionSheet
{
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
        [self.actionSheet showFromRect: self->tappedRect_
                                inView: self.view
                              animated: YES];
    else
        [self.actionSheet showInView: self.view];
}

//- (void) switchToPhotoPanning: (NSInteger) partNum
//{
//    //let the actionsheet unwind before flipping -- need to set initial page
//    //  [horizontalController loadScrollViewWithPage: partNum];
//    //  [horizontalController changePage: nil];
//
//    [self performSelector: @selector (photopanning)
//               withObject: nil
//               afterDelay: 0.0f];
//}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Member         *member = sm.loginSession.memberInFocus;

    if (!self.backButton)
        self.backButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Photos", @"")
                                                            style: UIBarButtonItemStylePlain
                                                           target: nil
                                                           action: NULL]
                           autorelease];

    if (!self.doneButton)
        self.doneButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemDone
                                                                         target: self
                                                                         action: @selector (uploadPhotos:)]
                           autorelease];

    self.navigationItem.title = [NSString stringWithFormat:
                                 NSLocalizedString (@"Photos for %@", @""),
                                 member.name];

    self.navigationItem.backBarButtonItem = self.backButton;

    [self.navigationItem setRightBarButtonItem: (self.hasUnsavedPhotos ?
                                                 self.doneButton :
                                                 nil)
                                      animated: animated];
}

- (void) uploadPhotos: (id) sender
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Member         *member = sm.loginSession.memberInFocus;
    MemberStore    *mstore = member.store;

    if (mstore.hasSubjectPhoto)
        [sm insertPhoto: mstore.subjectPhoto
             intoMember: member
                options: SessionManagerOptionNone];

    NSUInteger ppCount = mstore.numberOfPartPhotos;

    for (NSUInteger idx = 0; idx < ppCount; idx++)
        [sm insertPhoto: [mstore partPhotoAtIndex: idx]
             intoMember: member
                options: SessionManagerOptionNone];

    NSUInteger vCount = mstore.numberOfVideos;

    for (NSUInteger idx = 0; idx < vCount; idx++)
        [sm insertVideo: [mstore videoAtIndex: idx]
             intoMember: member
                options: SessionManagerOptionNone];

    [self.navigationController popViewControllerAnimated: YES];
}

- (void) video: (NSString *) videoPath
didFinishSavingWithError: (NSError *) error
   contextInfo: (NSString *) contextInfo
{
    if (error)
        CFShow ([error localizedDescription]);
    else
    {
        //NSLog (@"didFinishSavingWithError--videoPath in camera roll: %@", videoPath);
        //NSLog (@"didFinishSavingWithError--videoPath in temp directory: %@", contextInfo);

        if (contextInfo)
        {
            // The thumbnail jpg should located in this directory.
            //      NSString *thumbnailDirectory = [[contextInfo stringByDeletingLastPathComponent] stringByDeletingLastPathComponent];

            // Debug info. list all files in the directory of the video file.
            // e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp/capture
            //      NSLog([contextInfo stringByDeletingLastPathComponent]);
            //      NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:[contextInfo stringByDeletingLastPathComponent] error:nil] description]);
            // Debug info. list all files in the parent directory of the video file, i.e. the "~/tmp" directory.
            // e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp
            //      NSLog(thumbnailDirectory);
            //      NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:thumbnailDirectory error:nil] description]);
            ///////////////////

            // Find the thumbnail for the video just recorded.
            NSDate   *latestDate = [NSDate distantPast];
            //NSString *latestFile = @"BOGUS";
            NSString *file;
            NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
                                              enumeratorAtPath: [[contextInfo stringByDeletingLastPathComponent] stringByDeletingLastPathComponent]];

            // Enumerate all files in the ~/tmp directory
            while ((file = [dirEnum nextObject]))
            {
                // Only check files with jpg extension.
                if ([[file pathExtension] isEqualToString: @"jpg"])
                {
                    NSDictionary *attrs = [dirEnum fileAttributes];

                    //NSLog (@"***latestDate:%@", latestDate);
                    //NSLog (@"***file name:%@", file);
                    //NSLog (@"***NSFileSize:%@", [attrs valueForKey: @"NSFileSize"]);
                    //NSLog (@"***NSFileModificationDate:%@", [attrs valueForKey: @"NSFileModificationDate"]);

                    // Check if current jpg file is the latest one.
                    if ([(NSDate *) [attrs valueForKey:@"NSFileModificationDate"] compare: latestDate] == NSOrderedDescending)
                    {
                        latestDate = [attrs valueForKey: @"NSFileModificationDate"];
                        //latestFile = file;

                        //NSLog(@"***latestFile changed:%@", latestFile);
                    }
                }
            }
            // The thumbnail path.

            //latestFile = [NSTemporaryDirectory() stringByAppendingPathComponent: latestFile];

            //NSLog(@"****** The thumbnail file should be this one:%@", latestFile);
        }
        // Your code ...
        // Your code ...
        // Your code ...
    }
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    ShooterView *tmpView = [[[ShooterView alloc]
                             initWithFrame: self.parentViewController.view.bounds]
                            autorelease];

    tmpView.commentTextField.delegate = self;

    self.view = tmpView;

    //    [customViews_ loadCustomMainPortraitTextFields];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation
{
    return YES;
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    [(ShooterView *) self.view update];

    [self updateNavigationItemAnimated: animated];
}

- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                 duration: (NSTimeInterval) duration
{
    [self.view setNeedsLayout];
}

#pragma mark Overridden UIResponder Methods

- (void) touchesBegan: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    if (self->disableTouchesSemaphore_ == 0)
    {
        UITouch    *touch = [touches anyObject];
        NSUInteger  numTaps = touch.tapCount;

        if (numTaps > 1) //???
        {

        }
    }
}

- (void) touchesEnded: (NSSet *) touches
            withEvent: (UIEvent *) event
{
    if (self->disableTouchesSemaphore_ == 0)
    {
        for (UITouch *touch in touches)
        {
            if ([self handleTapAtLocation: [touch locationInView: self.view]])
                break;
        }
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];

    [self->actionSheet_ release];
    [self->backButton_ release];
    [self->popoverController_ release];
    [self->doneButton_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
    {
        self->disableTouchesSemaphore_ = 0;   // semaphore to disable touch recognition
        self->usePopover_ = (UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPad);
    }

    return self;
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}

#pragma mark UIActionSheetDelegate Methods

- (void) actionSheet: (UIActionSheet *) actSheet
clickedButtonAtIndex: (NSInteger) buttonIdx
{
    NSLog (@"shooter clicked %d first %d cancel %d",buttonIdx,actSheet.firstOtherButtonIndex,actSheet.cancelButtonIndex);
    if ((buttonIdx >= actSheet.firstOtherButtonIndex) &&
        (buttonIdx != actSheet.cancelButtonIndex))
    {
        NSArray                           *mediaTypes = nil;
        UIImagePickerControllerSourceType  sourceType;

        switch (buttonIdx)
        {
            case PHOTO_ALBUM_BUTTON_INDEX :
            default :
                sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
                break;

            case STILL_CAMERA_BUTTON_INDEX :
                sourceType = UIImagePickerControllerSourceTypeCamera;
                break;

            case VIDEO_ROLL_BUTTON_INDEX :
                mediaTypes = [NSArray arrayWithObject: (NSString *) kUTTypeMovie];
                sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
                break;

            case VIDEO_CAMERA_BUTTON_INDEX :
                mediaTypes = [NSArray arrayWithObject: (NSString *) kUTTypeMovie];
                sourceType = UIImagePickerControllerSourceTypeCamera;
                break;
        }

        [self presentImagePickerControllerWithSourceType: sourceType
                                              mediaTypes: mediaTypes];
    }
}

#pragma mark UIImagePickerControllerDelegate Methods

- (void) imagePickerControllerDidCancel: (UIImagePickerController *) picker
{
    [self dismissImagePickerController];
}

- (void) imagePickerController: (UIImagePickerController *) picker
 didFinishPickingMediaWithInfo: (NSDictionary *) info
{
    [self savePhoto: info];
    //[self saveVideo: info];

    [self refresh];

    [self dismissImagePickerController];
}

#pragma mark UITextFieldDelegate Methods

- (void) textFieldDidBeginEditing: (UITextField *) textField
{
    self->disableTouchesSemaphore_++;  // bump semaphore, will prevent us from recognizing touches while editing from keyboard
}

- (void) textFieldDidEndEditing: (UITextField *) textField
{
    self->disableTouchesSemaphore_--;

    if (self->disableTouchesSemaphore_ < 0)
        NSLog (@"text edit semaphore failure");

    //[customViews_ saveCustomMainPortaitFields: textField];

    [textField resignFirstResponder];   // this gets rid of the keyboard
}

- (BOOL) textFieldShouldReturn: (UITextField *) textField
{
    [textField resignFirstResponder];   // this gets rid of the keyboard

    return YES;
}

@end
