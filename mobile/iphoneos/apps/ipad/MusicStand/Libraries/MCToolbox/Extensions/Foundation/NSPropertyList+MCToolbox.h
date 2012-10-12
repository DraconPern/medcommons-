//
//  NSPropertyList+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 7/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSPropertyListSerialization (MCToolbox)

+ (id) sanitizePropertyList: (id) plist;

@end
