//
//  MCPanningController.h
//  MCProvider
//
//  Created by Bill Donner on 1/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MemberStore,CustomViews;

@interface PanningController :  UIViewController <UIScrollViewDelegate>
{
@private

    UIScrollView *scrollView;
    UIPageControl *pageControl;
    NSMutableArray *viewControllers;
    // To be used when scrolls originate from the UIPageControl
    BOOL pageControlUsed;
    MemberStore *memberStore;
    CustomViews *customViews;

    BOOL fullyInited;
    NSMutableArray *controllers;
}

@property (nonatomic, retain, readwrite) UIScrollView *scrollView;
@property (nonatomic, retain, readwrite) UIPageControl *pageControl;
@property (nonatomic, retain, readwrite) NSMutableArray *viewControllers;

- (void)changePage:(id)sender;
-(void)screenUpdate: (id)o;
-(void)loadScrollViewWithPage:(NSUInteger)page ;

-(PanningController *) init;
@end
