//
//  ImageCache.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@interface ImageCache : NSObject <MCImageCache>
{
@private

    NSMutableDictionary *volatileCache_;
}

- (NSArray *) allKeys;

- (void) removeAllImages;

@end
