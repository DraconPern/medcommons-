//
//  DictionaryAdditions.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DictionaryAdditions.h"

#pragma mark -
#pragma mark Public Class NSDictionary Additions
#pragma mark -

@implementation NSDictionary (MCProviderAdditions)

- (NSArray *) arrayForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj && [obj isKindOfClass: [NSArray class]])
            return obj;

    return nil;
}

- (BOOL) boolForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [(NSNumber *) obj boolValue];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return [str boolValue];
        }
    }

    return NO;
}

- (NSData *) dataForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj && [obj isKindOfClass: [NSData class]])
        return obj;

    return nil;
}

- (NSDate *) dateForKey: (id) key   // unixDateForKey ???
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [NSDate dateWithTimeIntervalSince1970: [(NSNumber *) obj doubleValue]];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return [NSDate dateWithTimeIntervalSince1970: [str doubleValue]];
        }
    }

    return nil;
}

- (NSDictionary *) dictionaryForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj && [obj isKindOfClass: [NSDictionary class]])
        return obj;

    return nil;
}

- (double) doubleForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [(NSNumber *) obj doubleValue];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return [str doubleValue];
        }
    }

    return 0.0;
}

- (float) floatForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [(NSNumber *) obj floatValue];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return [str floatValue];
        }
    }

    return 0.0f;
}

- (NSInteger) integerForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [(NSNumber *) obj integerValue];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return [str integerValue];
        }
    }

    return 0;
}

- (NSMutableDictionary *) mutableDictionaryForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj && [obj isKindOfClass: [NSMutableDictionary class]])
        return obj;

    return nil;
}

- (NSArray *) stringArrayForKey: (id) key
{
    NSArray *tmpArray = [self arrayForKey: key];

    if (tmpArray)
    {
        for (id obj in tmpArray)
        {
            if (![obj isKindOfClass: [NSString class]])
                return nil;
        }

        return tmpArray;
    }

    return nil;
}

- (NSString *) stringForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj && [obj isKindOfClass: [NSString class]])
    {
        NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

        if ([str length] > 0)
            return str;
    }

    return nil;
}

- (NSUInteger) unsignedIntegerForKey: (id) key
{
    id obj = [self objectForKey: key];

    if (obj)
    {
        if ([obj isKindOfClass: [NSNumber class]])
            return [(NSNumber *) obj unsignedIntegerValue];

        if ([obj isKindOfClass: [NSString class]])
        {
            NSString *str = [(NSString *) obj stringByTrimmingWhitespace];

            if ([str length] > 0)
                return (NSUInteger) [str integerValue];
        }
    }

    return 0;
}

- (NSURL *) URLForKey: (id) key
{
    NSString *str = [self stringForKey: key];

    return (str ?
            [NSURL URLWithString: str] :
            nil);
}

@end
