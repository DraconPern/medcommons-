//
//  CCRActionItem.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRActionItem.h"

#pragma mark -
#pragma mark Public Class CCRActionItem
#pragma mark -

@implementation CCRActionItem

@synthesize action      = action_;
@synthesize identifier  = identifier_;
@synthesize jsEvent     = jsEvent_;
@synthesize jsParameter = jsParameter_;
@synthesize target      = target_;
@synthesize title       = title_;

#pragma mark Public Instance Methods

- (id) initWithIdentifier: (NSString *) identifier
{
    self = [super init];

    if (self)
        self->identifier_ = [identifier copy];

    return self;
}

- (void) performAction
{
    [self.target performSelector: self.action
                      withObject: self];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->identifier_ release];
    [self->jsEvent_ release];
    [self->jsParameter_ release];
    [self->title_ release];

    [super dealloc];
}

@end




