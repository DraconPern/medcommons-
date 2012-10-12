//
//  CCRMainView.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRMainView.h"

#pragma mark -
#pragma mark Public Class CCRMainView
#pragma mark -

@interface CCRMainView ()

@property (nonatomic, retain, readwrite) UIActivityIndicatorView *activityIndicator;

- (void) startActivityIndicator;

- (void) stopActivityIndicator;

@end

@implementation CCRMainView

@synthesize activityIndicator = activityIndicator_;

#pragma mark Public Instance Methods

- (void) updateActivityIndicator
{
    if (self.isLoading)
        [self startActivityIndicator];
    else
        [self stopActivityIndicator];
}

#pragma mark Private Instance Methods

- (void) startActivityIndicator
{
    if (!self.activityIndicator)
    {
        self.activityIndicator = [[[UIActivityIndicatorView alloc]
                                   initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhiteLarge]
                                  autorelease];

        self.activityIndicator.hidesWhenStopped = YES;

        self.activityIndicator.center = CGPointMake (CGRectGetMidX (self.bounds),
                                                     CGRectGetMidY (self.bounds));

        [self addSubview: self.activityIndicator];

        [self.activityIndicator startAnimating];
    }
}

- (void) stopActivityIndicator
{
    if (self.activityIndicator)
    {
        [self.activityIndicator stopAnimating];
        [self.activityIndicator removeFromSuperview];   // overkill ???

        self.activityIndicator = nil;
    }
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self.backgroundColor = [UIColor redColor];
        self.dataDetectorTypes = UIDataDetectorTypeLink;
        self.scalesPageToFit = NO;
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->activityIndicator_ release];

    [super dealloc];
}

@end
