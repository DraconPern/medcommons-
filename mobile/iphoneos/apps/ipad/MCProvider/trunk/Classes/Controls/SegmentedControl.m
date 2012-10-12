//
//  SegmentedControl.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SegmentedControl.h"

@implementation MySegmentedControl
@synthesize blocktypes;
@synthesize urls;
@synthesize titles;
@synthesize classes;
@synthesize methods;
@synthesize args;

- (MySegmentedControl *) initWithSegments: (NSArray *) _arr
                        andWithBlockTypes: (NSArray *) _bt
                            andWithTitles: (NSArray *) _titles
                              andWithUrls: (NSArray *) _urls
                           andWithClasses: (NSArray *) _classes
                           andWithMethods: (NSArray *) _methods
                              andWithArgs: (NSArray *) _args
{
    self = [super initWithItems:_arr ];
    blocktypes = [_bt retain];
    urls = [_urls retain];
    titles = [_titles retain];
    classes = [_classes retain];

    methods = [_methods retain];
    args = [_args retain];
    return self;
}

@end
