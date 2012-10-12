//
//  MCPanningController.m
//  MCProvider
//
//  Created by Bill Donner on 1/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "CustomViews.h"
#import "DataManager.h"
#import "FullPageController.h"
#import "PanningController.h"
#import "Member.h"
#import "MemberStore.h"
#import "Session.h"
#import "SessionManager.h"

@implementation PanningController

@synthesize scrollView, pageControl, viewControllers;

-(PanningController *) init
{
    self = [super init];

    if (self)
    {
        memberStore = self.appDelegate.sessionManager.loginSession.memberInFocus.store;
        controllers = [[NSMutableArray alloc] init];
        customViews = self.appDelegate.dataManager.customViews;

        fullyInited = NO;
    }

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

    NSUInteger pageCount = 1 + [memberStore numberOfPartPhotos];
    scrollView = [[UIScrollView alloc] initWithFrame:scrollframe];    // a page is the width of the scroll view
    scrollView.pagingEnabled = YES;
    scrollView.contentSize = CGSizeMake(scrollView.frame.size.width * pageCount, scrollView.frame.size.height);
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.showsVerticalScrollIndicator = NO;
    scrollView.scrollsToTop = NO;
    scrollView.delegate = self;


    for (NSUInteger idx = 0; idx <= pageCount; idx++) { // make one extra for effect

        CGRect frame = scrollView.frame;
        frame.origin.x = frame.size.width * idx;
        frame.origin.y = 0;

        FullPageController *controller = [[FullPageController alloc] initWithPageNumber:idx  andWithFrame: frame
                                            ];
        //  PAN_LOG (@"creating MainFullPageController %@ %d",controller,i);
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

    self.navigationItem.title = self.appDelegate.sessionManager.loginSession.memberInFocus.name;

    self.view = outerView;
    fullyInited = YES;
    [outerView release];
    //  TRY_RECOVERY;
}
- (void) dealloc
{
    [viewControllers release];
    [scrollView release];
    [pageControl release];

    [super dealloc];
}
- (void)loadScrollViewWithPage:(NSUInteger)page
{
    if (fullyInited == YES)
    {
        //  PAN_LOG(@"Loading scroll view with page %d",page);
        pageControl.currentPage = page;

        // add the controller's view to the scroll view
        FullPageController *controller = [controllers objectAtIndex:page];
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
    NSUInteger page = (NSUInteger) floor((scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;

}

// At the end of scroll animation, reset the boolean used when scrolls originate from the UIPageControl
- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    pageControlUsed = NO;
}

- (void)changePage:(id)sender {
    NSUInteger page = pageControl.currentPage;
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

    //oldStatusBarStyle = self.application.statusBarStyle;

    //self.application.statusBarStyle = UIStatusBarStyleBlackTranslucent;
}

- (void)viewWillDisappear:(BOOL)animated
{
    //self.application.statusBarStyle = oldStatusBarStyle;
}


@end
