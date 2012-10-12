//
//  MCSlideSorter.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "CustomViews.h"
#import "DataManager.h"
#import "Member.h"
#import "MemberStore.h"
#import "Photo.h"
#import "SlideSorter.h"
#import "Session.h"
#import "SessionManager.h"
#import "Video.h"

@implementation SlideSorter

// Releases necessary resources.
-(void)dealloc
{
    // Release each of the subviews

    [allviews release];

    [super dealloc];
}

-(void) dumpallviews:(NSString *)tag
{
    if (!allviews)
        NSLog (@"Allviews is nil from %@",tag); else
            NSLog(@"Dumping allviews from %@ count %d",tag,[allviews count]);
}

-(SlideSorter *)   init
{
    self = [super init];

    if (self)
    {
        memberStore = self.appDelegate.sessionManager.loginSession.memberInFocus.store;
        customViews = self.appDelegate.dataManager.customViews;

        loaded = NO;
    }

    return self;
}


-(void) slidesortermainlayout
{
    UIImageView* oneImageView = [allviews objectAtIndex:0];
    //  LANDSCAPE_LOG(@":landscapeViewMainLayout  ",nil);
    if(![memberStore hasSubjectPhoto])
        oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_MEMBER_IMAGE];
    else

        oneImageView.image = [UIImage imageWithContentsOfFile: memberStore.subjectPhoto.path];

    for (NSUInteger idx = 0; idx < 10; idx++)
    {
        oneImageView = [allviews objectAtIndex:idx+1]; //compensate for photo pic
        if([memberStore hasPartPhotoAtIndex:idx])
            oneImageView .image = [UIImage imageWithContentsOfFile: [memberStore partPhotoAtIndex:idx].path];
        else

            oneImageView .image = [UIImage imageNamed:PLEASE_SHOOT_MEMBER_IMAGE];
    }

}

- (void) screenUpdate: (NSTimer *) Timer
{
    LANDSCAPE_LOG(@": landscape screenUpdate  ",nil);
    //if (loaded==YES)  // absolutely unknown
    //[customViews updateCustomMainLandscapeViewLabels];
    [self slidesortermainlayout];

}

- (void)loadView
{



    CGRect appFrame = [[UIScreen mainScreen] applicationFrame];
    appFrame.origin.y = appFrame.origin.y + 44.0f;
    appFrame.size.height = appFrame.size.height - 44.0f ;
    outerView = [[UIView alloc] initWithFrame:appFrame];


    //  CGRect logoframe =  CGRectMake(320-123.1f,45, 118.1f, 24.0f);



    //  UIImageView *logoView = [[UIImageView alloc] initWithFrame:logoframe];
    //  logoView.image = [UIImage imageNamed:MAIN_LOGO_IMAGE];
    //  [outerView addSubview: logoView];
    //  [logoView release];

    CGRect instructionsframe =  CGRectMake(10.f,45, 290.f, 24.0f);
    UILabel *instView = [[UILabel alloc] initWithFrame:instructionsframe];
    instView.text = @"drag to rearrange, or drag to trash";
    instView.textColor = [UIColor whiteColor];
    instView.textAlignment = UITextAlignmentLeft;
    instView.font = [UIFont fontWithName:@"Arial" size:12];
    instView.backgroundColor = [UIColor lightGrayColor];

    [outerView addSubview: instView];
    [instView release];


    allviews =[[[NSMutableArray alloc] init] retain]; // a real array please
    NSUInteger xpos,ypos;
    //*******************   LAYOUT OF Subject Demographics Standard View
    CGFloat pics_row_1_start = -4.0f+327.0f;
    CGFloat pics_row_2_start = -4.0f +392.0f;
    CGRect pictureframe = CGRectMake(18.0f, 26.0f+44.0f, 80.0f, 80.0f);// this should be the first touchable field

    CGRect trashframe = CGRectMake(214.0f, 26.0f+44.0f, 80.0f, 80.0f); // this must be the last touchable field
                                                                       // now make imageviews that correspond to each of these file and put the initial pictures in there

    UIImageView *picView = [[UIImageView alloc] initWithFrame:pictureframe];
    if(![memberStore hasSubjectPhoto])
        picView.image = [UIImage imageNamed:PLEASE_SHOOT_MEMBER_IMAGE];    else
            picView.image = [UIImage imageWithContentsOfFile: memberStore.subjectPhoto.path];
    [allviews   addObject: picView];
    [outerView addSubview: picView ];
    [picView release];//:=)

    for (NSUInteger j = 0; j < 10; j++)
    {

        if (j < 5)
        {
            xpos = 4+j*63;
            ypos = pics_row_1_start;
        }
        else
        {
            xpos = 4+(j-5)*63;
            ypos = pics_row_2_start;
        }

        CGRect tfa = CGRectMake(xpos, ypos, 60, 60);
        UIImageView *picView2 = [[UIImageView alloc] initWithFrame:tfa];
        [allviews   addObject: picView2];
        [outerView addSubview: picView2];
        [picView2 release];//:=)
    }

    UIImageView *trashView = [[UIImageView alloc] initWithFrame:trashframe];  // must be last for special "Touches" handling

    trashView.image = [UIImage imageNamed:TRASH_CAN_IMAGE];
    [allviews   addObject: trashView];
    [outerView addSubview: trashView];
    //[self dumpImageList];

    outerView.backgroundColor = [UIColor lightGrayColor];

    [outerView addSubview: [customViews newCustomMainLandscapeView]];
    [self screenUpdate:nil];
    self.view = outerView;
    [outerView release];
    loaded = YES;
    LANDSCAPE_LOG(@":landscape loadView ",nil);
    self.navigationItem.title = self.appDelegate.sessionManager.loginSession.memberInFocus.name;
}



-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    //return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
    return YES;
}

#define GROW_ANIMATION_DURATION_SECONDS 0.15    // Determines how fast a piece size grows when it is moved.
#define SHRINK_ANIMATION_DURATION_SECONDS 0.15  // Determines how fast a piece size shrinks when a piece stops moving.
#pragma mark -
#pragma mark === Touch handling  ===
#pragma mark
BOOL indubltouch;
// Handles the start of a touch
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    NSUInteger numTaps = [[touches anyObject] tapCount];
    //NSLog (@"touchesBegan %d count %d",numTaps,[allviews count],[imagelist count]);
    indubltouch = NO;
    if(numTaps >= 2) {
        //touchInfoText.text = [NSString stringWithFormat:@"%d taps",numTaps];
        //NSLog (@"touchesBegan count %d",numTaps);
        indubltouch =YES;
    }
    // Enumerate through all the touch objects.
    NSUInteger touchCount = 0;
    for (UITouch *touch in touches) {
        // Send to the dispatch method, which will make sure the appropriate subview is acted upon
        [self dispatchFirstTouchAtPoint:[touch locationInView:self.view] forEvent:nil];
        touchCount++;
    }

}

// Checks to see which view, or views, the point is in and then calls a method to perform the opening animation,
// which  makes the piece slightly larger, as if it is being picked up by the user.
-(void)dispatchFirstTouchAtPoint:(CGPoint)touchPoint forEvent:(UIEvent *)event
{

    for (NSUInteger j = 0; j < [allviews count] - 1; j++)
    {
        BOOL test; // if no photo then deaden this point
        if (indubltouch == YES)
            test=NO;
        else
            if (j==0) test = ([@"" isEqualToString: memberStore.subjectPhoto.path]);
            else      test = ([@"" isEqualToString: [memberStore partPhotoAtIndex:j-1].path]);
        if (!test )

            if (CGRectContainsPoint([[allviews objectAtIndex:j] frame], touchPoint)) {
                if (indubltouch==YES) {


                    return;
                }
                initialtouchcenter = j;initialtouchView = [allviews objectAtIndex:j];
                [self animateFirstTouchAtPoint:touchPoint forView:[allviews objectAtIndex:j]];

                return;
            }
    }

    if (CGRectContainsPoint([[allviews objectAtIndex:[allviews count]-1] frame], touchPoint)) {
        initialtouchcenter = [allviews count]-1;
        initialtouchView = [allviews objectAtIndex:[allviews count]-1]; // we are going to null this out
        NSLog (@"dispatchFirstTouchAtPoint trash");
    }
    else
    {
        NSLog (@"dispatchFirstTouchAtPoint nowhere");
        initialtouchView = nil;
        initialtouchcenter = -1;
    }
}


// Handles the continuation of a touch.
-(void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{

    NSUInteger touchCount = 0;

    // Enumerates through all touch objects
    for (UITouch *touch in touches) {
        // Send to the dispatch method, which will make sure the appropriate subview is acted upon
        [self dispatchTouchEvent:[touch view] toPosition:[touch locationInView:self.view]];
        touchCount++;
    }

    // When multiple touches, report the number of touches.
}

// Checks to see which view, or views, the point is in and then sets the center of each moved view to the new postion.
// If views are directly on top of each other, they move together.
-(void)dispatchTouchEvent:(UIView *)theView toPosition:(CGPoint)position
{

}

// Handles the end of a touch event.
-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{

    // Enumerates through all touch object
    for (UITouch *touch in touches) {
        // Sends to the dispatch method, which will make sure the appropriate subview is acted upon
        [self dispatchTouchEndEvent:[touch view] toPosition:[touch locationInView:self.view]];
    }
}

// Checks to see which view, or views,  the point is in and then calls a method to perform the closing animation,
// which is to return the piece to its original size, as if it is being put down by the user.
-(void)dispatchTouchEndEvent:(UIView *)theView toPosition:(CGPoint)position
{
    if (initialtouchcenter == (NSUInteger) -1) return; //if started drag outside image
    if (initialtouchcenter == ([allviews count]-1) )// if this was the trashcan trying to move, dont allow it
        return;




    // Check to see which view, or views,  the point is in and then animate to that position.
    for (NSUInteger j=0; j<[allviews count]-1; j++)
    {
        BOOL test; // if no photo then deaden this point
        if (j==0) test = ([@"" isEqualToString: memberStore.subjectPhoto.path]);
        else      test = ([@"" isEqualToString: [memberStore partPhotoAtIndex:j-1].path]);
        if (!test )
        {

            if (CGRectContainsPoint([[allviews objectAtIndex:j] frame], position))
            {


                if (j==0)
                {
                    //ended on subjectphoto
                    //dont allow swap with video
                    if ([@"" isEqualToString:[memberStore videoAtIndex:initialtouchcenter-1].path])
                        [memberStore exchangeSubjectPhotoWithPartPhotoAtIndex:initialtouchcenter-1];
                }
                else
                {
                    if (initialtouchcenter==0)
                    {
                        //started on subject photo
                        // dont allow swap with video

                        if ([@"" isEqualToString:[memberStore videoAtIndex:j-1].path])
                            [memberStore exchangeSubjectPhotoWithPartPhotoAtIndex:j-1];

                    }
                    else
                    {
                        // two general pieces
                        [memberStore exchangePartPhotoAtIndex:j-1 withPartPhotoAtIndex:initialtouchcenter-1];
                    }
                }
                [self screenUpdate:nil]; //fresh after any of these changes

                return;

            }
        }
    }
    // didnt hit any of the others

    //[initialimagepath release]; // if here it never was plugged into the array

    if (CGRectContainsPoint([[allviews objectAtIndex:[allviews count]-1] frame], position))
    {


        if (initialtouchcenter == 0)
        {
            // deleting subject photo
            [memberStore removeSubjectPhoto];

        }
        else
        {
            // deleting bodypart photo
            [memberStore removePartPhotoAtIndex:initialtouchcenter-1];

        }

        [self screenUpdate:nil]; //fresh after any of these changes
                                 //[self animateView:[allviews objectAtIndex:[allviews count]-1] toPosition: position];
    }
    else
    {
        // put it back
        [self animateView:theView   toPosition:position];
        [self animateView:initialtouchView  toPosition:position];
    }
}

-(void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
    // Enumerates through all touch object
    for (UITouch *touch in touches) {
        // Sends to the dispatch method, which will make sure the appropriate subview is acted upon
        [self dispatchTouchEndEvent:[touch view] toPosition:[touch locationInView:self.view]];
    }
}

#pragma mark -
#pragma mark === Animating subviews ===
#pragma mark

// Scales up a view slightly which makes the piece slightly larger, as if it is being picked up by the user.
-(void)animateFirstTouchAtPoint:(CGPoint)touchPoint forView:(UIImageView *)theView
{
    // Pulse the view by scaling up, then move the view to under the finger.
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:GROW_ANIMATION_DURATION_SECONDS];
    theView.transform = CGAffineTransformMakeScale(1.2f, 1.2f);
    [UIView commitAnimations];
    [UIView setAnimationDuration:SHRINK_ANIMATION_DURATION_SECONDS];
    theView.transform = CGAffineTransformIdentity;
    [UIView commitAnimations];
}

// Scales down the view and moves it to the new position.
-(void)animateView:(UIView *)theView toPosition:(CGPoint)thePosition
{
    //[UIView beginAnimations:nil context:NULL];
    //[UIView setAnimationDuration:SHRINK_ANIMATION_DURATION_SECONDS];
    // Set the center to the final postion
    //theView.center = thePosition;
    // Set the transform back to the identity, thus undoing the previous scaling effect.
    //theView.transform = CGAffineTransformIdentity;
    //[UIView commitAnimations];
}
@end
