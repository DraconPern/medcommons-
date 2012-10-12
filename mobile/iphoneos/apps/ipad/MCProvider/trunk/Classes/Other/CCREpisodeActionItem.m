//
//  CCREpisodeActionItem.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/21/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCREpisodeActionItem.h"

#pragma mark -
#pragma mark Public Class CCREpisodeActionItem
#pragma mark -

@implementation CCREpisodeActionItem

@synthesize docList = docList_;

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->docList_ release];

    [super dealloc];
}

@end
