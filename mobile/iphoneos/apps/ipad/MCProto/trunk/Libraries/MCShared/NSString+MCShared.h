//
//  NSString+MCShared.h
//  MCShared
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (MCShared)

- (NSString *) stringByURLDecoding;

- (NSString *) stringByURLEncoding;

@end
