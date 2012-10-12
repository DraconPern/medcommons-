//
//  HistoryFullPageController.m
//  MedCommons
//
//  Created by bill donner on 10/20/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//


#import "MedCommons.h"
#import "PatientStore.h"
#import "HistoryCase.h"
#import "HistoryFullPageController.h"
#import <MediaPlayer/MediaPlayer.h>
#import "MapController.h"
#import "AsyncImageView.h"
#import "DataManager.h"

@implementation HistoryFullPageController

#pragma mark concrete methods
-(void) showMap
{
	NSDictionary *adicts = [[hcase attrdicts] objectAtIndex:0]; //take first object - not quite right
	
	MapController *mapController = [(MapController *)[MapController alloc] 
									initWithConfig:adicts];
	MY_ASSERT (mapController !=nil);
	[self.navigationController pushViewController:(UIViewController *)mapController 	animated:YES];
	[mapController release];
}



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
// initialize the pageNumber ivar.
- (id)initWithPageNumber:(int)page andWithImageURL:(NSString *) _ss andWithFrame:(CGRect) _frame andWithName:(NSString *)_name 	  andWithHistoryCase:(HistoryCase *)_hcase
{
    if (self = [super init])
	{       
		pageNumber = page;
		patientStore =  [[DataManager sharedInstance] ffPatientStore];
		imageCache = [[DataManager sharedInstance] ffImageCache];	
		imageURL = [_ss copy];
		hcase = _hcase;
		frame = _frame;
		name = _name;
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
	//[self customScreenUpdate]; // let specific controller behaviors manifest
	
	
}	


-(void)loadView
{
	PAN_LOG(@"setting up page %d imageURL %@",pageNumber,imageURL);
	UIView *outerView;
	outerView = [[UIView  alloc] initWithFrame:frame];
	outerView.backgroundColor = [UIColor lightGrayColor];  
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	
	
	//CGRect pageNumberLabelFrame = CGRectMake(10,53,300, 16);
//	pageNumberLabel = [[UILabel alloc] initWithFrame:pageNumberLabelFrame] ;
//	pageNumberLabel.textColor = [UIColor whiteColor];		
//	pageNumberLabel.textAlignment = UITextAlignmentCenter;
//	pageNumberLabel.font = [UIFont fontWithName:@"Arial" size:16];
//	pageNumberLabel.backgroundColor = [UIColor lightGrayColor];
//	pageNumberLabel.text = [NSString stringWithFormat:@"%@ - Part %d", name,pageNumber ];
//	
//	[outerView addSubview:pageNumberLabel];
//	[pageNumberLabel release];
	
	CGRect photoframe = CGRectMake(10,82,300,300);
	AsyncImageView *oneImageView = [[AsyncImageView alloc] initWithFrame:photoframe andImageCache:imageCache
									] ; 
	
	
	oneImageView.tag = 995;
	
	[oneImageView loadImageFromURL:[NSURL URLWithString:   imageURL]];
	[outerView addSubview:oneImageView];
	[oneImageView release];
	
	self.view = outerView;
	[outerView release];
}


@end

