//
//  MCFullPageController.m
//  MedCommons
//
//  Created by bill donner on 1/24/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MCFullPageController.h"

#import "MedCommons.h"
#import "PatientStore.h"
#import "CustomViews.h"
#import <MediaPlayer/MediaPlayer.h>
#import "DataManager.h"

@implementation MCFullPageController

//make the Subject be the zeroth entry


-(void) playMovieAtURL: (NSURL*) theURL {
	NSLog (@"Playing movie at @%",theURL);
    MPMoviePlayerController* theMovie =
	[[MPMoviePlayerController alloc] initWithContentURL: theURL];
	
    theMovie.scalingMode = MPMovieScalingModeAspectFill;
    theMovie.movieControlMode = MPMovieControlModeHidden;
	
    // Register for the playback finished notification
    [[NSNotificationCenter defaultCenter]
	 addObserver: self
	 selector: @selector(myMovieFinishedCallback:)
	 name: MPMoviePlayerPlaybackDidFinishNotification
	 object: theMovie];
	
    // Movie playback is asynchronous, so this method returns immediately.
    [theMovie play];
}

// When the movie is done, release the controller.
-(void) myMovieFinishedCallback: (NSNotification*) aNotification
{
    MPMoviePlayerController* theMovie = [aNotification object];
	
    [[NSNotificationCenter defaultCenter]
	 removeObserver: self
	 name: MPMoviePlayerPlaybackDidFinishNotification
	 object: theMovie];
	
    // Release the movie instance created in playMovieAtURL:
    [theMovie release];
}

// initialize the pageNumber ivar.
- (id)initWithPageNumber:(int)page 
			andWithFrame:(CGRect )_frame  
{
    if (self = [super init])
	{ 
        pageNumber = page;
		patientStore = [[DataManager sharedInstance] ffPatientStore];

		frame = _frame;
		customViews = [[DataManager sharedInstance] ffCustomViews];

    }
	//BREADCRUMBS_PUSH;
			
    return self;
}

- (void)dealloc {
    [pageNumberLabel release];
	//BREADCRUMBS_POP;
    [super dealloc];
}

-(void)screenUpdate: (id)o
{
	
}
-(void)loadView
{
	UIView *outerView = [[UIView alloc] initWithFrame:frame];  
 
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	//UIView *oneLabelView =[customViews customMainFullPageLabelView];
	//[outerView addSubview:oneLabelView];	
	UIImageView *oneImageView = [customViews customMainFullPagePhotoView];
	
	if (pageNumber == 0)
	{
		if(![patientStore haveSubjectPhoto]) 
			oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];	else
				oneImageView.image = [UIImage imageWithContentsOfFile:[patientStore fullSubjectPhotoSpec]];
	}
	else
	{
		if(![patientStore havePhotoAtIndex:pageNumber-1 ]) 
			oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];
		else
			oneImageView.image = [UIImage imageWithContentsOfFile:[patientStore fullPhotoSpecAtIndex:pageNumber-1]];
	}
	[outerView addSubview:oneImageView];
	[oneImageView release];
//
	self.view = outerView;
	self.view.backgroundColor = [UIColor blackColor];	
	[outerView release];
//	TRY_RECOVERY;
}

// Play the movie on a dubl click
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
	NSUInteger numTaps = [[touches anyObject] tapCount];	
	if(numTaps >= 2) {
		//[patientStore dumpPatientStore];
		NSLog(@"Movie Player Dublclick on page %d",pageNumber);
		if (pageNumber>0)
			if ([patientStore haveVideoAtIndex:pageNumber-1])
				[self playMovieAtURL:[NSURL fileURLWithPath: [patientStore videoSpecAtIndex:pageNumber-1]]];
	}
}

@end
