//
//  SegmentMap.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SegmentMap.h"
#import "SegmentedControl.h"



@implementation SegmentMap

@synthesize segmentBarItem;

- (SegmentMap *) initWithSegments: (NSArray *) _arr
                           ofSize: (CGFloat) width
                    andWithTarget: (id) who
                    andWithAction: (SEL) _action
                andWithBlockTypes: (NSArray *) _bt
                    andWithTitles:(NSArray *) _titles
                      andWithUrls:(NSArray *) _urls
                   andWithClasses: (NSArray *) _classes
                   andWithMethods:(NSArray *) _methods
                      andWithArgs: (NSArray *) _args
                 andWithMomentary: (BOOL) _momentary
{
    self = [super init];

    MySegmentedControl *segmentedControl = [[MySegmentedControl alloc] initWithSegments: _arr
                                                                      andWithBlockTypes: (NSArray *) _bt
                                                                          andWithTitles: (NSArray *) _titles
                                                                            andWithUrls: (NSArray *) _urls
                                                                         andWithClasses: (NSArray *) _classes
                                                                         andWithMethods: (NSArray *) _methods
                                                                            andWithArgs: (NSArray *) _args ];

    [segmentedControl addTarget: who
                         action: _action
               forControlEvents: UIControlEventValueChanged];

    segmentedControl.frame = CGRectMake (0.0f, 0.0f, width, 40.0f);
    segmentedControl.segmentedControlStyle = UISegmentedControlStyleBar;
    segmentedControl.momentary = _momentary;

    self.segmentBarItem = [[[UIBarButtonItem alloc] initWithCustomView: segmentedControl] retain]; // autorelease ??? -- JGP

    [segmentedControl release];

    return self;
}

@end

