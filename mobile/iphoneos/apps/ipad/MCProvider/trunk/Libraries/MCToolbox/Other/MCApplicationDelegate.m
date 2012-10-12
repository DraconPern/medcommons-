//
//  MCApplicationDelegate.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of MedCommons, Inc. nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#import "MCApplicationDelegate.h"
#import "NSObject+MCToolbox.h"

@interface MCApplicationDelegate ()

@property (nonatomic, assign, readwrite) NSUInteger networkActivityCount;

@end

@implementation MCApplicationDelegate

@synthesize networkActivityCount = networkActivityCount_;
@synthesize rootViewController   = rootViewController_;
@synthesize window               = window_;

#pragma mark Public Instance Methods

- (void) didStartNetworkActivity
{
    if (self.networkActivityCount < NSUIntegerMax)
        self.networkActivityCount++;

    self.application.networkActivityIndicatorVisible = YES;
}

- (void) didStopNetworkActivity
{
    if (self.networkActivityCount > 0)
        self.networkActivityCount--;

    self.application.networkActivityIndicatorVisible = (self.networkActivityCount > 0);
}

- (void) setInitialRootViewController: (UIViewController *) vc
{
    self->rootViewController_ = [vc retain];

    self->window_ = [[UIWindow alloc] initWithFrame: [UIScreen mainScreen].bounds];

    [self->window_ addSubview: self->rootViewController_.view];
    [self->window_ makeKeyAndVisible];
}

#pragma mark Overridden NSObject Instance Methods

- (void) dealloc
{
    [self->rootViewController_ release];
    [self->window_ release];

    [super dealloc];
}

@end
