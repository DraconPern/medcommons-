//
//  MCRemoteImageView.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/9/10.
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

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCFormDataRequest.h"
#import "MCRemoteImageView.h"

#pragma mark -
#pragma mark Public Class MCRemoteImageView
#pragma mark -

@interface MCRemoteImageView ()

@property (nonatomic, retain, readwrite) UIActivityIndicatorView *activityIndicator;
@property (nonatomic, retain, readwrite) MCFormDataRequest       *downloadRequest;
@property (nonatomic, retain, readwrite) NSURL                   *imageURL;
@property (nonatomic, retain, readwrite) UIProgressView          *progressBar;

- (void) didFailLoadWithError: (NSError *) error;

- (void) didFinishLoad;

- (void) didStartLoad;

- (void) startProgressIndicators;

- (void) stopProgressIndicators;

@end

@implementation MCRemoteImageView

@synthesize activityIndicator      = activityIndicator_;
@synthesize activityIndicatorStyle = activityIndicatorStyle_;
@synthesize delegate               = delegate_;
@synthesize downloadRequest        = downloadRequest_;
@synthesize imageCache             = imageCache_;
@synthesize imageURL               = imageURL_;
@dynamic    loading;
@synthesize progressBar            = progressBar_;
@synthesize progressBarStyle       = progressBarStyle_;
@synthesize showsActivityIndicator = showsActivityIndicator_;
@synthesize showsProgressBar       = showsProgressBar_;

#pragma mark Public Instance Methods

- (BOOL) isLoading
{
    return (self.downloadRequest && ![self.downloadRequest isFinished]);
}

- (void) loadImageFromURL: (NSURL *) URL
{
    [self loadImageFromURL: URL
             fallbackImage: nil
      shouldOverwriteImage: YES];
}

- (void) loadImageFromURL: (NSURL *) URL
            fallbackImage: (UIImage *) image
{
    [self loadImageFromURL: URL
             fallbackImage: image
      shouldOverwriteImage: YES];
}

- (void) loadImageFromURL: (NSURL *) URL
            fallbackImage: (UIImage *) image
     shouldOverwriteImage: (BOOL) overwrite
{
    static NSSet *SupportedUTIs;

    if (!SupportedUTIs)
        SupportedUTIs = [[NSSet alloc] initWithObjects:
                         (NSString *) kUTTypeBMP,
                         (NSString *) kUTTypeGIF,
                         (NSString *) kUTTypeICO,
                         (NSString *) kUTTypeJPEG,
                         (NSString *) kUTTypePNG,
                         (NSString *) kUTTypeTIFF,
                         nil];

    //
    // Do nothing if load is already in progress:
    //
    if (!self.isLoading)
    {
        //
        // Determine image to "preload":
        //
        UIImage *tmpImage = self.image;

        //
        // If current image is nil or overwrite flag is specified, set image
        // to fallback image (may be nil):
        //
        if (overwrite || !tmpImage)
            tmpImage = image;

        //
        // Note use of self.image rather than super.image; this resets all
        // other load-related properties as well.
        //
        self.image = tmpImage;

        if (URL)
        {
            self.imageURL = URL;

            //
            // Check for image in cache (if any) first:
            //
            tmpImage = (self.imageCache ?
                        [self.imageCache imageForURL: self.imageURL] :
                        nil);

            //
            // If not found in cache, kick off download operation:
            //
            if (!tmpImage)
            {
                self.downloadRequest = [MCFormDataRequest requestWithURL: self.imageURL];

                self.downloadRequest.delegate = self;
                self.downloadRequest.supportedUTIs = SupportedUTIs;

                [self.downloadRequest startAsynchronous];
            }
            else
                super.image = tmpImage; // note use of super.image here ...
        }
    }
}

- (void) stopLoading
{
    [self.downloadRequest cancel];
}

#pragma mark Private Instance Methods

- (void) didFailLoadWithError: (NSError *) error
{
    if ([self.delegate respondsToSelector: @selector (remoteImageView:didFailLoadWithError:)])
        [self.delegate remoteImageView: self
                  didFailLoadWithError: error];
}

- (void) didFinishLoad
{
    if ([self.delegate respondsToSelector: @selector (remoteImageViewDidFinishLoad:)])
        [self.delegate remoteImageViewDidFinishLoad: self];
}

- (void) didStartLoad
{
    if ([self.delegate respondsToSelector: @selector (remoteImageViewDidStartLoad:)])
        [self.delegate remoteImageViewDidStartLoad: self];
}

- (void) startProgressIndicators
{
    //
    // If shown, progress bar is always at very bottom of image view:
    //
    if (self.showsProgressBar)
    {
        self.progressBar = [[[UIProgressView alloc]
                             initWithProgressViewStyle: self.progressBarStyle]
                            autorelease];

        CGFloat pbHeight = CGRectGetHeight (self.progressBar.bounds);

        self.progressBar.frame = CGRectMake (CGRectGetMinX (self.bounds),
                                             CGRectGetMaxY (self.bounds) - pbHeight,
                                             CGRectGetWidth (self.bounds),
                                             pbHeight);

        [self addSubview: self.progressBar];

        self.downloadRequest.downloadProgressDelegate = self.progressBar;
    }

    //
    // If shown, activity indicator is centered in image view:
    //
    if (self.showsActivityIndicator)
    {
        self.activityIndicator = [[[UIActivityIndicatorView alloc]
                                   initWithActivityIndicatorStyle: self.activityIndicatorStyle]
                                  autorelease];

        self.activityIndicator.hidesWhenStopped = YES;

        self.activityIndicator.center = CGPointMake (CGRectGetMidX (self.bounds),
                                                     CGRectGetMidY (self.bounds));

        [self addSubview: self.activityIndicator];

        [self.activityIndicator startAnimating];
    }
}

- (void) stopProgressIndicators
{
    if (self.activityIndicator)
    {
        [self.activityIndicator stopAnimating];
        [self.activityIndicator removeFromSuperview];   // overkill ???

        self.activityIndicator = nil;
    }

    if (self.progressBar)
    {
        self.downloadRequest.downloadProgressDelegate = nil;

        self.progressBar.hidden = YES;                  // overkill ???

        [self.progressBar removeFromSuperview];         // overkill ???

        self.progressBar = nil;
    }
}

#pragma mark Overridden UIImageView Methods

//
// Directly setting the image property kills any load in progress:
//
- (void) setImage: (UIImage *) image
{
    MCFormDataRequest *request = self.downloadRequest;

    //
    // If no download request is currently active, explicitly reset all other
    // download-related properties:
    //
    if (!request)
    {
        self.activityIndicator = nil;
        self.imageURL = nil;
        self.progressBar = nil;
    }
    else    // otherwise, just cancel request and callbacks will reset:
        [request cancel];

    super.image = image;
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self->activityIndicatorStyle_ = UIActivityIndicatorViewStyleWhite;
        self->progressBarStyle_ = UIProgressViewStyleDefault;
        self->showsActivityIndicator_ = YES;
        self->showsProgressBar_ = YES;
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.image = nil;   // resets all other load-related properties as well

    [self->activityIndicator_ release];
    [self->downloadRequest_ release];
    [self->imageCache_ release];
    [self->imageURL_ release];
    [self->progressBar_ release];

    [super dealloc];
}

#pragma mark MCFormDataRequestDelegate Methods

- (void) requestFailed: (MCFormDataRequest *) request
{
    if (self.downloadRequest != request)
        return;

    [self stopProgressIndicators];

    //
    // Do NOT reset self.imageURL and self.downloadRequest properties until
    // AFTER callback (if any) is executed:
    //
    [self didFailLoadWithError: request.error];

    self.imageURL = nil;
    self.downloadRequest = nil;
}

- (void) requestFinished: (MCFormDataRequest *) request
{
    if (self.downloadRequest != request)
        return;

    UIImage *tmpImage = [UIImage imageWithData: request.responseData];

    //
    // Only replace fallback image if successfully created new one:
    //
    if (tmpImage)
    {
        super.image = tmpImage; // note use of super.image here ...

        //
        // If cache defined, store this image:
        //
        if (self.imageCache)
            [self.imageCache storeImage: tmpImage
                                 forURL: self.imageURL];
    }

    [self stopProgressIndicators];

    //
    // Do NOT reset self.downloadRequest property until AFTER callback (if any)
    // is executed:
    //
    [self didFinishLoad];

    self.downloadRequest = nil;
}

- (void) requestStarted: (MCFormDataRequest *) request
{
    if (self.downloadRequest != request)
        return;

    [self startProgressIndicators];
    [self didStartLoad];
}

@end
