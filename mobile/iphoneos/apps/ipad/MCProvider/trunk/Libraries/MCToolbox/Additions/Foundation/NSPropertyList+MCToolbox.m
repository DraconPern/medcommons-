//
//  NSPropertyList+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 7/26/10.
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

#import "NSPropertyList+MCToolbox.h"

@interface NSArray (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSArray (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    NSMutableArray *plist = [NSMutableArray arrayWithCapacity: [self count]];

    for (id obj in self)
    {
        id tmpObj = [obj sanitizedPropertyList];

        if (tmpObj)
            [plist addObject: tmpObj];
    }

    return plist;
}

@end

@interface NSData (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSData (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    return self;
}

@end

@interface NSDate (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSDate (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    return self;
}

@end

@interface NSDictionary (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSDictionary (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    NSMutableDictionary *plist = [NSMutableDictionary dictionaryWithCapacity: [self count]];

    for (id key in self)
    {
        if ([key isKindOfClass: [NSString class]])
        {
            id tmpObj = [[self objectForKey: key] sanitizedPropertyList];

            if (tmpObj)
                [plist setObject: tmpObj
                          forKey: key];
        }
    }

    return plist;
}

@end

@interface NSNumber (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSNumber (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    return self;
}

@end

@interface NSObject (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSObject (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    return nil; // anything else is expunged ...
}

@end

@interface NSString (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList;

@end

@implementation NSString (MCToolbox_NSPropertyList)

- (id) sanitizedPropertyList
{
    return self;
}

@end

@implementation NSPropertyListSerialization (MCToolbox)

+ (id) sanitizePropertyList: (id) plist
{
    if ([self propertyList: plist
          isValidForFormat: NSPropertyListXMLFormat_v1_0])
        return plist;

    return [plist sanitizedPropertyList];
}

@end
