//
//  MCFullPageController.m
//  MCProvider
//
//  Created by Bill Donner on 1/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MediaPlayer/MediaPlayer.h>

#import "AppDelegate.h"
#import "CustomViews.h"
#import "DataManager.h"
#import "FullPageController.h"
#import "Member.h"
#import "MemberStore.h"
#import "Photo.h"
#import "Session.h"
#import "SessionManager.h"

@implementation FullPageController

//make the Subject be the zeroth entry


//-(void) playMovieAtURL: (NSURL*) theURL {
//  NSLog (@"Playing movie at @%",theURL);
//    MPMoviePlayerController* theMovie =
//  [[MPMoviePlayerController alloc] initWithContentURL: theURL];
//
//    theMovie.scalingMode = MPMovieScalingModeAspectFill;
//  //  theMovie.movieControlMode = MPMovieControlModeHidden;
//
//    // Register for the playback finished notification
//    [[NSNotificationCenter defaultCenter]
//   addObserver: self
//   selector: @selector(myMovieFinishedCallback:)
//   name: MPMoviePlayerPlaybackDidFinishNotification
//   object: theMovie];
//
//    // Movie playback is asynchronous, so this method returns immediately.
//    [theMovie play];
//}
//
//// When the movie is done, release the controller.
//-(void) myMovieFinishedCallback: (NSNotification*) aNotification
//{
//    MPMoviePlayerController* theMovie = [aNotification object];
//
//    [[NSNotificationCenter defaultCenter]
//   removeObserver: self
//   name: MPMoviePlayerPlaybackDidFinishNotification
//   object: theMovie];
//
//    // Release the movie instance created in playMovieAtURL:
//    [theMovie release];
//}

// initialize the pageNumber ivar.

- (id)initWithPageNumber:(NSUInteger)page
            andWithFrame:(CGRect )_frame
{
    self = [super init];

    if (self)
    {
        pageNumber = page;
        memberStore = self.appDelegate.sessionManager.loginSession.memberInFocus.store;

        frame = _frame;
    }

    return self;
}

- (void) dealloc
{
    [pageNumberLabel release];

    [super dealloc];
}

- (void) screenUpdate: (id) o
{

}

- (void) loadView
{
    UIView *outerView = [[UIView alloc] initWithFrame: frame];

    outerView.backgroundColor = [UIColor grayColor];

    outerView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                  UIViewAutoresizingFlexibleWidth);

    UIView *oneLabelView = [customViews newCustomMainFullPageLabelView];

    [outerView addSubview: oneLabelView];

    [oneLabelView release];

    UIImageView *oneImageView = [customViews newCustomMainFullPagePhotoView];

    if (pageNumber == 0)
    {
        if(![memberStore hasSubjectPhoto])
            oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_MEMBER_IMAGE];
        else
            oneImageView.image = [UIImage imageWithContentsOfFile: memberStore.subjectPhoto.path];
    }
    else
    {
        if(![memberStore hasPartPhotoAtIndex:pageNumber-1 ])
            oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_MEMBER_IMAGE];
        else
            oneImageView.image = [UIImage imageWithContentsOfFile:[memberStore partPhotoAtIndex:pageNumber-1].path];
    }
    [outerView addSubview:oneImageView];

    [oneImageView release];

    self.view = outerView;

    [outerView release];
    //  TRY_RECOVERY;
}

// Play the movie on a dubl click
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    NSUInteger numTaps = [[touches anyObject] tapCount];
    if(numTaps >= 2) {
        //[memberStore dumpMemberStore];
        NSLog(@"Movie Player Dublclick on page %d currently disabled",pageNumber);
        //if (pageNumber>0)
        //          if ([memberStore haveVideoAtIndex:pageNumber-1])
        //              [self playMovieAtURL:[NSURL fileURLWithPath: [memberStore videoSpecAtIndex:pageNumber-1]]];
    }
}

@end
