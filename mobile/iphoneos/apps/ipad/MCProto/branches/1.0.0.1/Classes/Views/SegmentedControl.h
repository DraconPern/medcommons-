//
//  SegmentedControl.h
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MySegmentedControl : UISegmentedControl
{
    NSArray *blocktypes;
    NSArray *urls;
    NSArray *titles;
    NSArray *classes;
    NSArray *methods;
    NSArray *args;
}

@property (nonatomic, retain) NSArray *blocktypes;
@property (nonatomic, retain) NSArray *urls;
@property (nonatomic, retain) NSArray *titles;
@property (nonatomic, retain) NSArray *classes;
@property (nonatomic, retain) NSArray *methods;
@property (nonatomic, retain) NSArray *args;

- (MySegmentedControl *) initWithSegments: (NSArray *) _arr
                        andWithBlockTypes: (NSArray *) _bt
                            andWithTitles: (NSArray *) _titles
                              andWithUrls: (NSArray *) _urls
                           andWithClasses: (NSArray *) _classes
                           andWithMethods: (NSArray *) _methods
                              andWithArgs: (NSArray *) _args ;

@end
