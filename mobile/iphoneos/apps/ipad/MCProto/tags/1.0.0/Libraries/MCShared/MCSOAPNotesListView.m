//
//  MCSOAPNotesListView.m
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

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

+ (UIView *) newBackgroundView;

+ (UIView *) newContentView;

@end

@implementation MCSOAPNotesListView

#pragma mark Private Class Methods

+ (UIView *) newBackgroundView
{
    UIView *view = [[UIView alloc] initWithFrame: CGRectZero];
    
    view.backgroundColor = [UIColor colorWithWhite: 0.96f
                                             alpha: 1.0f];
    
    return view;
}

+ (UIView *) newContentView
{
    UITableView *tabView = [[UITableView alloc] initWithFrame: CGRectZero
                                                        style: UITableViewStylePlain];
    
    tabView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                UIViewAutoresizingFlexibleWidth);
    tabView.backgroundColor = [UIColor colorWithWhite: 0.92f
                                                alpha: 1.0f];
    
    return tabView;
}

#pragma mark Overridden MCStandardView Methods

- (void) layoutContentView
{
    self.contentView.frame = CGRectMake (CONTENT_MARGIN_LEFT,
                                         CONTENT_MARGIN_TOP,
                                         (self.backgroundView.frame.size.width -
                                          CONTENT_MARGIN_LEFT -
                                          CONTENT_MARGIN_RIGHT),
                                         (self.backgroundView.frame.size.height -
                                          CONTENT_MARGIN_TOP -
                                          CONTENT_MARGIN_BOTTOM));
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    if (self = [super initWithFrame: frame
                     backgroundView: [[MCSOAPNotesListView newBackgroundView] autorelease]
                        contentView: [[MCSOAPNotesListView newContentView] autorelease]
                         headerView: nil
                         footerView: nil])
        self.resizesForKeyboard = NO;
    
    return self;
}

@end
