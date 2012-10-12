//
//  MCPanningController.m
//  MedCommons
//
//  Created by bill donner on 1/24/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//



#import "MCPanningController.h"
#import "PatientStore.h"
#import "MCFullPageController.h"
#import "MedCommons.h"
#import "CustomViews.h"
#import "DataManager.h"
#import "DashboardPatient.h"

@implementation MCPanningController
@synthesize scrollView, pageControl, viewControllers;

-(MCPanningController *) init
{
	self = [super init];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	controllers = [[NSMutableArray alloc] init];
	customViews = [[DataManager sharedInstance] ffCustomViews];

	fullyInited = NO;	
//	BREADCRUMBS_PUSH;
	return self;
}
-(void)screenUpdate: (id)o
{

}

-(void)loadView
{
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	UIView *outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor lightGrayColor];  
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  	
	CGRect pageControlFrame =    CGRectMake(100,441, 140.0f, 12.0f);
	CGRect scrollframe = CGRectMake (0,42,320.0f,418.0f);
	
	int pageCount = 1 + [patientStore countPartPics];
	scrollView = [[UIScrollView alloc] initWithFrame:scrollframe];    // a page is the width of the scroll view
    scrollView.pagingEnabled = YES;
    scrollView.contentSize = CGSizeMake(scrollView.frame.size.width * pageCount, scrollView.frame.size.height);
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.showsVerticalScrollIndicator = NO;
    scrollView.scrollsToTop = NO;
    scrollView.delegate = self;
    // view controllers are no longer created lazily
    // in the meantime, load the array with placeholders which will be replaced on demand
	
    for (unsigned i = 0; i <= pageCount; i++) { // make one extra for effect
		
		CGRect frame = scrollView.frame;
        frame.origin.x = frame.size.width * i;
        frame.origin.y = 0;
		
		MCFullPageController *controller = [[MCFullPageController alloc] initWithPageNumber:i  andWithFrame: frame
																			];
		PAN_LOG (@"creating MainFullPageController %@ %d",controller,i);
        [controllers addObject:controller];		
		
        [scrollView addSubview:controller.view];
		[controller release];
    }
    self.viewControllers = controllers;
    //[controllers release];
	[outerView addSubview:scrollView];
	pageControl = [[UIPageControl alloc] initWithFrame: pageControlFrame];
	[pageControl setNumberOfPages: pageCount];
	[pageControl setCurrentPage: 0];
	[pageControl addTarget:self action:@selector(changePage:) forControlEvents: UIControlEventValueChanged];
	[outerView addSubview:pageControl];
	
	self.navigationItem.title =	[[DataManager sharedInstance].ffPatientWrapper nameForTitle];
	
	self.view = outerView;
	fullyInited = YES;
	[outerView release];
//	TRY_RECOVERY;
}
- (void)dealloc
{
	[viewControllers release];
    [scrollView release];
    [pageControl release];
//	BREADCRUMBS_POP;
	[super dealloc];
}
- (void)loadScrollViewWithPage:(int)page 
{
	if (fullyInited == YES)
	{
		PAN_LOG(@"Loading scroll view with page %d",page);
		pageControl.currentPage = page;
		
		// add the controller's view to the scroll view
		MCFullPageController *controller = [controllers objectAtIndex:page];
		CGRect frame = scrollView.frame;
        frame.origin.x = frame.size.width * page;
        frame.origin.y = 0;
        controller.view.frame = frame;
    	//[controller release];
	}
	
	
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
	
	// oldStatusBarStyle = [[UIApplication sharedApplication] statusBarStyle];
	//  [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleBlackTranslucent animated:NO];
	
}

- (void)viewWillDisappear:(BOOL)animated
{
	
	//[[UIApplication sharedApplication] setStatusBarStyle:oldStatusBarStyle animated:NO];    
}


@end
