//
//  DictionaryAdditions.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NSDictionary (GigStandAdditions)

- (NSArray *) arrayForKey: (id) key;

- (BOOL) boolForKey: (id) key;

- (NSData *) dataForKey: (id) key;

- (NSDate *) dateForKey: (id) key;

- (NSDictionary *) dictionaryForKey: (id) key;

- (double) doubleForKey: (id) key;

- (float) floatForKey: (id) key;

- (NSInteger) integerForKey: (id) key;

- (NSMutableDictionary *) mutableDictionaryForKey: (id) key;

- (NSArray *) stringArrayForKey: (id) key;

- (NSString *) stringForKey: (id) key;

- (NSUInteger) unsignedIntegerForKey: (id) key;

- (NSURL *) URLForKey: (id) key;

@end
