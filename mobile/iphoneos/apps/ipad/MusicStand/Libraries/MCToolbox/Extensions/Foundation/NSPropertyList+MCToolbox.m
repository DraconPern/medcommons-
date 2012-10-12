//
//  NSPropertyList+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 7/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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
