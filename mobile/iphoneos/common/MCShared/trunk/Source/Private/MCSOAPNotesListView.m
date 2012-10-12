//
//  MCSOAPNotesListView.m
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

//#import "MCSOAPNote.h"
#import "MCSOAPNotesListView.h"

#pragma mark -
#pragma mark "Package-private" Class MCSOAPNotesListView
#pragma mark -

#pragma mark Internal Constants

#define CONTENT_MARGIN_TOP    0.0f
#define CONTENT_MARGIN_RIGHT  0.0f
#define CONTENT_MARGIN_BOTTOM 0.0f
#define CONTENT_MARGIN_LEFT   0.0f

@interface MCSOAPNotesListView ()

- (void) addBackgroundView;

- (void) addContentView;

- (void) addFooterView;

- (void) addHeaderView;

- (void) layoutBackgroundView;

- (void) layoutContentView;

- (void) layoutFooterView;

- (void) layoutHeaderView;

@end

@implementation MCSOAPNotesListView

@synthesize contentView = contentView_;

#pragma mark Private Instance Methods

- (void) addBackgroundView
{
    backgroundView_ = [[UIView alloc] initWithFrame: CGRectZero];

    backgroundView_.backgroundColor = [UIColor colorWithWhite: 0.96f
                                                        alpha: 1.0f];

    [self addSubview: backgroundView_];
}

- (void) addContentView
{
    contentView_ = [[UITableView alloc] initWithFrame: CGRectZero
                                                style: UITableViewStylePlain];

    contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                     UIViewAutoresizingFlexibleWidth);
    contentView_.backgroundColor = [UIColor colorWithWhite: 0.92f
                                                     alpha: 1.0f];

    [backgroundView_ addSubview: contentView_];
}

- (void) addFooterView
{
    footerView_ = [[UIView alloc] initWithFrame: CGRectZero];

    footerView_.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
                                    UIViewAutoresizingFlexibleWidth);
    footerView_.backgroundColor = [UIColor clearColor];

    [backgroundView_ addSubview: footerView_];
}

- (void) addHeaderView
{
    headerView_ = [[UIView alloc] initWithFrame: CGRectZero];

    headerView_.autoresizingMask = (UIViewAutoresizingFlexibleBottomMargin |
                                    UIViewAutoresizingFlexibleWidth);
    headerView_.backgroundColor = [UIColor whiteColor];

    [backgroundView_ addSubview: headerView_];
}

- (void) layoutBackgroundView
{
    backgroundView_.frame = self.frame;
}

- (void) layoutContentView
{
    contentView_.frame = CGRectMake (CONTENT_MARGIN_LEFT,
                                     (headerView_.frame.size.height +
                                      CONTENT_MARGIN_TOP),
                                     (backgroundView_.frame.size.width -
                                      CONTENT_MARGIN_LEFT -
                                      CONTENT_MARGIN_RIGHT),
                                     (backgroundView_.frame.size.height -
                                      headerView_.frame.size.height -
                                      CONTENT_MARGIN_TOP -
                                      CONTENT_MARGIN_BOTTOM -
                                      footerView_.frame.size.height));
}

- (void) layoutFooterView
{
    footerView_.frame = CGRectMake (0.0f,
                                    backgroundView_.frame.size.height,
                                    backgroundView_.frame.size.width,
                                    0.0f);
}

- (void) layoutHeaderView
{
    headerView_.frame = CGRectMake (0.0f,
                                    0.0f,
                                    backgroundView_.frame.size.width,
                                    0.0f);
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    if (self = [super initWithFrame: frame])
    {
        [self addBackgroundView];
        [self addHeaderView];
        [self addFooterView];
        [self addContentView];

        [self layoutSubviews];
    }

    return self;
}

- (void) layoutSubviews
{
    [self layoutBackgroundView];
    [self layoutHeaderView];
    [self layoutFooterView];
    [self layoutContentView];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [backgroundView_ release];
    [contentView_ release];
    [footerView_ release];
    [headerView_ release];

    [super dealloc];
}

@end
