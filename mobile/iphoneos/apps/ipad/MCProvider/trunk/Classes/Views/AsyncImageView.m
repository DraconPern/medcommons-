//
//  AsyncImageView.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DataManager.h"
#import "ImageCache.h"

#pragma mark -
#pragma mark Public Class AsyncImageView
#pragma mark -

@interface AsyncImageView () <MCRemoteImageViewDelegate>

@end

@implementation AsyncImageView

#pragma mark Public Class Methods

+ (void) clearCache
{
    DataManager *dm = self.appDelegate.dataManager;

    if (dm.imageCache)
        [dm.imageCache removeAllImages];
}

#pragma mark Overridden UIView Methods

- (AsyncImageView *) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        DataManager *dm = self.appDelegate.dataManager;
		self.showsActivityIndicator = YES;
        self.showsProgressBar = NO; //donner 11/26/10
        self.activityIndicatorStyle = UIActivityIndicatorViewStyleWhiteLarge;
        self.clipsToBounds = YES;
        //self.contentMode = UIViewContentModeScaleAspectFill;
        self.contentMode = UIViewContentModeScaleAspectFit;
        self.delegate = self;
        self.imageCache = dm.imageCache;
    }

    return self;
}

#pragma mark MCRemoteImageViewDelegate Methods

- (void) remoteImageView: (MCRemoteImageView *) riv
    didFailLoadWithError: (NSError *) error
{
    [self.appDelegate didStopNetworkActivity];
}

- (void) remoteImageViewDidFinishLoad: (MCRemoteImageView *) riv
{
    [self.appDelegate didStopNetworkActivity];
}

- (void) remoteImageViewDidStartLoad: (MCRemoteImageView *) riv
{
    [self.appDelegate didStartNetworkActivity];
}

@end
