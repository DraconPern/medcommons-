//
//  ImageCache.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <CommonCrypto/CommonDigest.h>
#import <MobileCoreServices/MobileCoreServices.h>

#import "DataManager.h"
#import "DataStore.h"
#import "ImageCache.h"

#pragma mark -
#pragma mark Public Class ImageCache
#pragma mark -

#pragma mark Internal Functions

static NSString *cacheKeyForURL (NSURL *url);

static NSData *extractImageData (NSURL *url,
                                 UIImage *image);

static NSString *cacheKeyForURL (NSURL *url)
{
    const char    *utf8String = [url.absoluteString UTF8String];
    unsigned char  digest [CC_MD5_DIGEST_LENGTH];

    CC_MD5 (utf8String,
            strlen (utf8String),
            digest);

    return [NSString stringWithFormat:
            @"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
            digest [0],
            digest [1],
            digest [2],
            digest [3],
            digest [4],
            digest [5],
            digest [6],
            digest [7],
            digest [8],
            digest [9],
            digest [10],
            digest [11],
            digest [12],
            digest [13],
            digest [14],
            digest [15]];
}

static NSData *extractImageData (NSURL *url,
                                 UIImage *image)
{
    NSString *dataUTI = [NSString preferredUTIForPathExtension: url.path.pathExtension];
    NSString *jpegUTI = (NSString *) kUTTypeJPEG;

    if ([dataUTI conformsToUTI: jpegUTI])
    {
        CACHE_LOG (@">>> Extracting JPEG representation of image <%@>", url);

        return UIImageJPEGRepresentation (image, 1.0f);
    }

    CACHE_LOG (@">>> Extracting PNG representation of image <%@>", url);

    return UIImagePNGRepresentation (image);
}

@interface ImageCache ()

@property (nonatomic, retain, readwrite) NSMutableDictionary *volatileCache;

- (NSData *) imageDataFromDurableCacheForKey: (NSString *) cacheKey;

- (NSData *) imageDataFromVolatileCacheForKey: (NSString *) cacheKey;

- (void) removeAllImageDataFromDurableCache;

- (void) removeAllImageDataFromVolatileCache;

- (void) removeImageDataFromDurableCacheForKey: (NSString *) cacheKey;

- (void) removeImageDataFromVolatileCacheForKey: (NSString *) cacheKey;

- (void) storeImageData: (NSData *) imageData
   toDurableCacheForKey: (NSString *) cacheKey;

- (void) storeImageData: (NSData *) imageData
  toVolatileCacheForKey: (NSString *) cacheKey;

@end

@implementation ImageCache

@synthesize volatileCache = volatileCache_;

#pragma mark Public Instance Methods

- (NSArray *) allKeys
{
    return [self.volatileCache allKeys];
}

- (UIImage *) imageForURL: (NSURL *) url
{
    //
    // Obtain cache key:
    //
    NSString *cacheKey = cacheKeyForURL (url);

    CACHE_LOG (@">>> Looking up image <%@> in cache, key: %@", url, cacheKey);

    //
    // Look up in volatile cache:
    //
    NSData *imageData = [self imageDataFromVolatileCacheForKey: cacheKey];

    //
    // If not found, look up in durable cache:
    //
    if (!imageData)
    {
        imageData = [self imageDataFromDurableCacheForKey: cacheKey];

        //
        // If loaded from durable cache, store to volatile cache:
        //
        if (imageData)
            [self storeImageData: imageData
           toVolatileCacheForKey: cacheKey];
    }

    UIImage *image = nil;

    //
    // If loaded, create actual image:
    //
    if (imageData)
    {
        image = [UIImage imageWithData: imageData];

        if (!image)
        {
            CACHE_LOG (@">>> Unable to create image <%@> from cached data", url);

            [self removeImageForURL: url];
        }
    }

    return image;
}

- (void) removeAllImages
{
    CACHE_LOG (@">>> Removing all images from cache");

    //
    // For now, remove entries from volatile cache only:
    //
    [self removeAllImageDataFromVolatileCache];
    //[self removeAllImageDataFromDurableCache];
}

- (void) removeImageForURL: (NSURL *) url
{
    //
    // Obtain cache key:
    //
    NSString *cacheKey = cacheKeyForURL (url);

    CACHE_LOG (@">>> Removing image <%@> from cache, key: %@", url, cacheKey);

    //
    // Remove from volatile cache:
    //
    [self removeImageDataFromVolatileCacheForKey: cacheKey];

    //
    // Remove from durable cache:
    //
    [self removeImageDataFromDurableCacheForKey: cacheKey];
}

- (void) storeImage: (UIImage *) image
             forURL: (NSURL *) url
{
    //
    // Obtain cache key:
    //
    NSString *cacheKey = cacheKeyForURL (url);

    CACHE_LOG (@">>> Storing image <%@> to cache, key: %@", url, cacheKey);

    //
    // Extract image data:
    //
    NSData *imageData = extractImageData (url, image);

    if (imageData)
    {
        //
        // Store to volatile cache:
        //
        [self storeImageData: imageData
       toVolatileCacheForKey: cacheKey];

        //
        // Store to durable cache:
        //
        [self storeImageData: imageData
        toDurableCacheForKey: cacheKey];
    }
}

#pragma mark Private Instance Methods

- (NSData *) imageDataFromDurableCacheForKey: (NSString *) cacheKey
{
    NSString      *path = [DataStore pathForCacheEntryWithKey: cacheKey];
    NSFileManager *fm = [NSFileManager defaultManager];
    NSData        *imageData = nil;

    if ([fm fileExistsAtPath: path])
    {
        NSError *error = nil;

        CACHE_LOG (@">>> Loading image data <%@> from durable cache, path: %@",
                   cacheKey,
                   path);

        imageData = [NSData dataWithContentsOfFile: path
                                           options: 0
                                             error: &error];

        if (!imageData)
        {
            CACHE_LOG (@"Unable to load image data <%@> from durable cache, path: %@, error: %@",
                       cacheKey,
                       path,
                       error);
        }
    }

    return imageData;
}

- (NSData *) imageDataFromVolatileCacheForKey: (NSString *) cacheKey
{
    return [self.volatileCache objectForKey: cacheKey];
}

- (void) removeAllImageDataFromDurableCache
{
}

- (void) removeAllImageDataFromVolatileCache
{
    if ([self.volatileCache count] > 0)
    {
        CACHE_LOG (@">>> Removing all image data from volatile cache, size: %u",
                   [self.volatileCache count]);

        [self.volatileCache removeAllObjects];
    }
}

- (void) removeImageDataFromDurableCacheForKey: (NSString *) cacheKey
{
    NSString      *path = [DataStore pathForCacheEntryWithKey: cacheKey];
    NSFileManager *fm = [NSFileManager defaultManager];

    if ([fm fileExistsAtPath: path])
    {
        NSError *error = nil;

        CACHE_LOG (@">>> Removing image data <%@> from durable cache, path: %@",
                   cacheKey,
                   path);

        if (![fm removeItemAtPath: path
                            error: &error])
        {
            CACHE_LOG (@">>> Unable to remove image data <%@> from durable cache, path: %@, error: %@",
                       cacheKey,
                       path,
                       error);
        }
    }
}

- (void) removeImageDataFromVolatileCacheForKey: (NSString *) cacheKey
{
    CACHE_LOG (@">>> Removing image data <%@> from volatile cache", cacheKey);

    [self.volatileCache removeObjectForKey: cacheKey];
}

- (void) storeImageData: (NSData *) imageData
   toDurableCacheForKey: (NSString *) cacheKey
{
    NSString *path = [DataStore pathForCacheEntryWithKey: cacheKey];
    NSError  *error = nil;

    CACHE_LOG (@">>> Storing image data <%@> to durable cache, path: %@",
               cacheKey,
               path);

    if (![imageData writeToFile: path
                        options: NSAtomicWrite
                          error: &error])
    {
        CACHE_LOG (@">>> Unable to store image data <%@> to durable cache, path: %@, error: %@",
                   cacheKey,
                   path,
                   error);
    }
}

- (void) storeImageData: (NSData *) imageData
  toVolatileCacheForKey: (NSString *) cacheKey
{
    CACHE_LOG (@">>> Storing image data <%@> to volatile cache", cacheKey);

    [self.volatileCache setObject: imageData
                           forKey: cacheKey];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->volatileCache_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
        self->volatileCache_ = [[NSMutableDictionary alloc] init];

    return self;
}

@end
