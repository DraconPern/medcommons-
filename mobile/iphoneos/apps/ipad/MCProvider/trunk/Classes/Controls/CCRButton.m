//
//  CCRButton.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRButton.h"

#pragma mark -
#pragma mark Public Class CCRButton
#pragma mark -

//@interface CCRButton ()
//
//@end

@implementation CCRButton

@synthesize identifier  = identifier_;
@synthesize jsEvent     = jsEvent_;
@synthesize jsParameter = jsParameter_;

#pragma mark Public Instance Methods

- (id) initWithIdentifier: (NSString *) identifier
{
    self = [super init];

    if (self)
        self->identifier_ = [identifier copy];

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->identifier_ release];
    [self->jsEvent_ release];
    [self->jsParameter_ release];

    [super dealloc];
}

@end
