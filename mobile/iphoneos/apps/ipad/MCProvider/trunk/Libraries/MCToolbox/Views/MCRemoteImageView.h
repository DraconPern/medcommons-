//
//  MCRemoteImageView.h
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

#import <UIKit/UIKit.h>

@class MCFormDataRequest;

@protocol MCImageCache;
@protocol MCRemoteImageViewDelegate;

/**
 * The MCRemoteImageView class extends UIImageView to ...
 */
@interface MCRemoteImageView : UIImageView
{
@private

    UIActivityIndicatorView        *activityIndicator_;
    UIActivityIndicatorViewStyle    activityIndicatorStyle_;
    id <MCRemoteImageViewDelegate>  delegate_;
    MCFormDataRequest              *downloadRequest_;
    id <MCImageCache>               imageCache_;
    NSURL                          *imageURL_;
    UIProgressView                 *progressBar_;
    UIProgressViewStyle             progressBarStyle_;
    //
    // Flags:
    //
    BOOL                            showsActivityIndicator_;
    BOOL                            showsProgressBar_;
}

@property (nonatomic, assign, readwrite)                    UIActivityIndicatorViewStyle    activityIndicatorStyle;
@property (nonatomic, assign, readwrite)                    id <MCRemoteImageViewDelegate>  delegate;
@property (nonatomic, retain, readwrite)                    id <MCImageCache>               imageCache;
@property (nonatomic, retain, readonly)                     NSURL                          *imageURL;
@property (nonatomic, assign, readonly, getter = isLoading) BOOL                            loading;
@property (nonatomic, assign, readwrite)                    UIProgressViewStyle             progressBarStyle;
@property (nonatomic, assign, readwrite)                    BOOL                            showsActivityIndicator;
@property (nonatomic, assign, readwrite)                    BOOL                            showsProgressBar;

- (void) loadImageFromURL: (NSURL *) URL;

- (void) loadImageFromURL: (NSURL *) URL
            fallbackImage: (UIImage *) image;

- (void) loadImageFromURL: (NSURL *) URL
            fallbackImage: (UIImage *) image
     shouldOverwriteImage: (BOOL) overwrite;

- (void) stopLoading;

@end

@protocol MCImageCache <NSObject>

@required

- (UIImage *) imageForURL: (NSURL *) URL;

- (void) removeImageForURL: (NSURL *) URL;

- (void) storeImage: (UIImage *) image
             forURL: (NSURL *) URL;

@end

@protocol MCRemoteImageViewDelegate <NSObject>

@optional

- (void) remoteImageView: (MCRemoteImageView *) riv
    didFailLoadWithError: (NSError *) error;

- (void) remoteImageViewDidFinishLoad: (MCRemoteImageView *) riv;

- (void) remoteImageViewDidStartLoad: (MCRemoteImageView *) riv;

@end
