//
//  MCView.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCView.h"

#pragma mark -
#pragma mark Public Class MCView
#pragma mark -

@implementation MCView

@synthesize userInfo = userInfo_;

#pragma mark Overridden NSObject Methods

- (void)dealloc
{
    [self->userInfo_ release];

    [super dealloc];
}

@end
