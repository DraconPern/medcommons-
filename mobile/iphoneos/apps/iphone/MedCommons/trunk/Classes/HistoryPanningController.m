//
//  HistoryPanningController.m
//  MedCommons
//
//  Created by bill donner on 10/20/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//
#import "PatientStore.h"
#import "HistoryFullPageController.h"
#import "MedCommons.h"
#import "HistoryPanningController.h"
#import "HistoryCase.h"
#import "DataManager.h"

#define kStatusBarHeight 20.0f
@implementation HistoryPanningController
@synthesize scrollView, pageControl, viewControllers;
#pragma mark abstract methods

#pragma mark concrete methods
-(HistoryPanningController *)initWithIndex: (int)j andWithCase: (HistoryCase *)_hcase
{
	self = [super init];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	fullyInited = NO;	
	currentPage = j;
	hcase = _hcase;	
	controllers= [[NSMutableArray alloc] init];
	//BREADCRUMBS_PUSH;

	return self;
}
-(void)screenUpdate: (id)o
{
	//if (fullyInited == YES)
	//	{
	//		
	//		
	//		[pageControl setNumberOfPages:pageCount+1]; // this may have changed due to moving stuff to trash
	//		
	//		scrollView.contentSize = CGSizeMake(scrollView.frame.size.width * pageCount, scrollView.frame.size.height);
	//		// for this update we need to iterate thru each of the page controllers and call the respective udpate routines
	//		//int theCount = pageCount;
	//		//if (theCount < kHowManySlots) theCount ++; //update the one all the way on the right to show its gone
	//		//for (unsigned i = 0; i < theCount; i++) {
	//		//	NSLog (@"calling screen update on page view controller %d",i);
	//			//[[self.viewControllers objectAtIndex:i] screenUpdate:o];
	//		//}
	//	}
}
- (void)loadScrollViewWithPage:(int)page 
{
	if (fullyInited == YES)
	{
		NSLog (@"Loading scroll view with page %d",page);
		pageControl.currentPage = page;
		
		// add the controller's view to the scroll view
		HistoryFullPageController *controller = [controllers objectAtIndex:page];
		CGRect frame = scrollView.frame;
        frame.origin.x = frame.size.width * page;
        frame.origin.y = 0;
        controller.view.frame = frame;
    	//[controller release];
	}
}
-(void)loadView
{
	
	
	
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y- kStatusBarHeight;
	appFrame.size.height = appFrame.size.height + kStatusBarHeight;
	UIView *outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor whiteColor];  
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  	
	CGRect pageControlFrame =    CGRectMake(100,-4.0f+435-44, 140.0f, 12.0f);
	CGRect scrollframe = CGRectMake (0,-4.0f+41-44,320.0f,422.0f);
	fullyInited = YES;
	int count =0;
	
    for (unsigned i = 0; i < kHowManySlots; i++) {
		NSDictionary *dict = [[hcase attrdicts] objectAtIndex:i];// slot zero always for the patient
		NSString *ss =nil;
		if(dict) ss = [dict objectForKey:@"remoteurl"];
		if (ss)
			count++;
	}
	int pageCount =  count;
	scrollView = [[UIScrollView alloc] initWithFrame:scrollframe];    // a page is the width of the scroll view
    scrollView.pagingEnabled = YES;
    scrollView.contentSize = CGSizeMake(scrollView.frame.size.width * (pageCount), scrollView.frame.size.height);
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.showsVerticalScrollIndicator = NO;
    scrollView.scrollsToTop = NO;
    scrollView.delegate = self;	
	
	NSString *ss =nil;
    for (unsigned i = 0; i <=pageCount; i++) {
		NSDictionary *dict = [[hcase attrdicts] objectAtIndex:i];// slot zero always for the patient
		if(dict) ss = [dict objectForKey:@"remoteurl"];
		if (ss)
		{
			CGRect frame = scrollView.frame;
			frame.origin.x = frame.size.width * i;
			frame.origin.y = 0;
			HistoryFullPageController *hfpc = 
			[[HistoryFullPageController alloc] initWithPageNumber:i andWithImageURL:ss andWithFrame: frame 
													  andWithName:[hcase name]
											   andWithHistoryCase:hcase ];	
			PAN_LOG (@"creating HistoryFullPageController %@ %d",hfpc,i);
			[controllers addObject:hfpc];					
			[scrollView addSubview:hfpc.view];
			[hfpc release];
		}
    }
	
    self.viewControllers = controllers;
	
	
    //[controllers release];
	[outerView addSubview:scrollView];
	pageControl = [[UIPageControl alloc] initWithFrame: pageControlFrame];
	[pageControl setNumberOfPages: pageCount];
	[pageControl setCurrentPage: currentPage];
	[pageControl addTarget:self action:@selector(changePage:) forControlEvents: UIControlEventValueChanged];
	[outerView addSubview:pageControl];
	
	self.navigationItem.title = [hcase name]; 
	self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Back" 
																			 style:UIBarButtonItemStylePlain
																			target:nil action:nil];
	
	self.view = outerView;	
	[self loadScrollViewWithPage: currentPage];
	[outerView release];
	//NSLog (@"Panning Loadview ends");
}
- (void)dealloc
{
	[viewControllers release];
    [scrollView release];
    [pageControl release];
	//BREADCRUMBS_POP;
	[super dealloc];
}


- (void)scrollViewDidScroll:(UIScrollView *)sender {
    // We don't want a "feedback loop" between the UIPageControl and the scroll delegate in
    // which a scroll event generated from the user hitting the page control triggers updates from
    // the delegate method. We use a boolean to disable the delegate logic when the page control is used.
    if (pageControlUsed) {
        // do nothing - the scroll was initiated from the page control, not the user dragging
        return;
    }
    // Switch the indicator when more than 50% of the previous/next page is visible
    CGFloat pageWidth = scrollView.frame.size.width;
    int page = floor((scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;
	
}

// At the end of scroll animation, reset the boolean used when scrolls originate from the UIPageControl
- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    pageControlUsed = NO;
}

- (IBAction)changePage:(id)sender {
    int page = pageControl.currentPage;
    // load the visible page and the page on either side of it (to avoid flashes when the user starts scrolling)
	//NSLog (@"Panning to page %d",page);
    // update the scroll view to the appropriate page
    CGRect frame = scrollView.frame;
    frame.origin.x = frame.size.width * page;
    frame.origin.y = 0;
    [scrollView scrollRectToVisible:frame animated:YES];
    // Set the boolean used when scrolls originate from the UIPageControl. See scrollViewDidScroll: above.
    pageControlUsed = YES;
}

- (void)viewWillAppear:(BOOL)animated
{	
	[self dismissModalViewControllerAnimated:YES]; //kill any modal view like series or landscape
	// the trashcan is not put on the image list because it is fixed
	[super viewWillAppear:animated];
	
  //  oldStatusBarStyle = [[UIApplication sharedApplication] statusBarStyle];
  //  [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleBlackTranslucent animated:NO];
	
}

- (void)viewWillDisappear:(BOOL)animated
{
	  
}


@end


