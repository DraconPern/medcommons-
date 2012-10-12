//
//  MCRemoteImageView.h
//  MCToolbox
//
//  Created by J. G. Pusey on 6/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MCFormDataRequest;

@protocol MCImageCache;
@protocol MCRemoteImageViewDelegate;

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
