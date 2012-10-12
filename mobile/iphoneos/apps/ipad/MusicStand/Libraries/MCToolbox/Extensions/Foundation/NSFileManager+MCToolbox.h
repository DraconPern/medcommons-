//
//  NSFileManager+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 5/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSFileManager (MCToolbox)

+ (NSDate *) fileModificationDateOfItemAtPath: (NSString *) path;

@end
