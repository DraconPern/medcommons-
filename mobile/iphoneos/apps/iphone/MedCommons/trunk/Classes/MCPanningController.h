//
//  MCPanningController.h
//  MedCommons
//
//  Created by bill donner on 1/24/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//



	@class PatientStore,CustomViews;
	@interface MCPanningController :  UIViewController <UIScrollViewDelegate>  {
		IBOutlet UIScrollView *scrollView;
		IBOutlet UIPageControl *pageControl;
		NSMutableArray *viewControllers;
		// To be used when scrolls originate from the UIPageControl
		BOOL pageControlUsed;
		PatientStore *patientStore;

		
		BOOL fullyInited;
		NSMutableArray *controllers;
		CustomViews *customViews;
		
	}
	@property (nonatomic, retain) UIScrollView *scrollView;
	@property (nonatomic, retain) UIPageControl *pageControl;
	@property (nonatomic, retain) NSMutableArray *viewControllers;
	- (IBAction)changePage:(id)sender;
	-(void)screenUpdate: (id)o;
	-(void)loadScrollViewWithPage:(int)page ;
	
	-(MCPanningController *) init;
	@end
