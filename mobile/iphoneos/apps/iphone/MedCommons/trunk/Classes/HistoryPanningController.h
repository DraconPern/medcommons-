//
//  HistoryPanningController.h
//  MedCommons
//
//  Created by bill donner on 10/20/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//


@class PatientStore,HistoryCase;
@interface HistoryPanningController :  UIViewController <UIScrollViewDelegate>  {
	IBOutlet UIScrollView *scrollView;
    IBOutlet UIPageControl *pageControl;
    NSMutableArray *viewControllers;
    // To be used when scrolls originate from the UIPageControl
    BOOL pageControlUsed;
	PatientStore *patientStore;

	BOOL fullyInited;

	NSInteger currentPage;
	
	HistoryCase *hcase;
	NSMutableArray *controllers;
	
}
@property (nonatomic, retain) UIScrollView *scrollView;
@property (nonatomic, retain) UIPageControl *pageControl;
@property (nonatomic, retain) NSMutableArray *viewControllers;
- (IBAction)changePage:(id)sender;
-(void)screenUpdate: (id)o;
-(void)loadScrollViewWithPage:(int)page ;
-(HistoryPanningController *) initWithIndex: (int)j andWithCase: (HistoryCase *)_hcase;
@end

