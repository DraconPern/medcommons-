//
//  SegmentMap.m
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "SegmentMap.h"
#import "SegmentedControl.h"



@implementation SegmentMap

@synthesize segmentBarItem;

- (SegmentMap *) initWithSegments: (NSArray *) _arr
                           ofSize: (float) width
                    andWithTarget: (id) who
                    andWithAction: (SEL) _action
                andWithBlockTypes: (NSArray *) _bt
                    andWithTitles:(NSArray *) _titles
                      andWithUrls:(NSArray *) _urls
                   andWithClasses: (NSArray *) _classes
                   andWithMethods:(NSArray *) _methods
                      andWithArgs: (NSArray *) _args
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
    segmentedControl.momentary = YES;

    self.segmentBarItem = [[[UIBarButtonItem alloc] initWithCustomView: segmentedControl] retain]; // autorelease ??? -- JGP

    [segmentedControl release];

    return self;
}

@end

