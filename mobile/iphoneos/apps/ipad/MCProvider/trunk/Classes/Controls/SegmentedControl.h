//
//  SegmentedControl.h
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MySegmentedControl : UISegmentedControl
{
@private

    NSArray *blocktypes;
    NSArray *urls;
    NSArray *titles;
    NSArray *classes;
    NSArray *methods;
    NSArray *args;
}

@property (nonatomic, retain, readwrite) NSArray *blocktypes;
@property (nonatomic, retain, readwrite) NSArray *urls;
@property (nonatomic, retain, readwrite) NSArray *titles;
@property (nonatomic, retain, readwrite) NSArray *classes;
@property (nonatomic, retain, readwrite) NSArray *methods;
@property (nonatomic, retain, readwrite) NSArray *args;

- (MySegmentedControl *) initWithSegments: (NSArray *) _arr
                        andWithBlockTypes: (NSArray *) _bt
                            andWithTitles: (NSArray *) _titles
                              andWithUrls: (NSArray *) _urls
                           andWithClasses: (NSArray *) _classes
                           andWithMethods: (NSArray *) _methods
                              andWithArgs: (NSArray *) _args ;

@end
