//
//  ScenarioManager.m
//  MCProvider
//
//  Created by Bill Donner on 4/16/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DataManager.h"
#import "DetailViewController+Bill.h"
#import "MasterViewController.h"
#import "ScenarioManager.h"
#import "SegmentedControl.h"
#import "SegmentMap.h"

@interface ScenarioManager ()

- (void) leftDetailSegmentHandler: (MySegmentedControl *) sender;

- (void) rightDetailSegmentHandler: (MySegmentedControl *) sender;

- (void) rootSegmentHandler: (MySegmentedControl *) sender;

@end

@implementation ScenarioManager

- (id) init
{
    return [super init];
}

+ (ScenarioManager *) sharedInstance
{
    static ScenarioManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[ScenarioManager alloc] init];

    return SharedInstance;
}

- (BOOL) setupScenarioButtons
{
    DataManager *dm = self.appDelegate.dataManager;
    NSDictionary *config = dm.masterPlist;
    // build the top right and left and bottom button decorators for the Root View

    //  build top left and right for detail view
    NSMutableArray      *details;
    NSMutableArray      *segments;
    NSMutableArray      *urls;
    NSMutableArray      *blocktypes;
    NSMutableArray      *titles;
    NSMutableArray      *classes;
    NSMutableArray      *methods;
    NSMutableArray      *args;
    NSMutableDictionary *odict;
    NSString            *size;


    segments = [NSMutableArray array];
    urls = [NSMutableArray array];
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    //size=@"0";

    if ([config objectForKey: @"leftRootSegment"])
    {
        odict = [config objectForKey: @"leftRootSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <leftRootSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }

        dm.smLeftMaster = [[[SegmentMap alloc] initWithSegments: segments
                                                         ofSize: [size intValue]
                                                  andWithTarget: self
                                                  andWithAction: @selector (rootSegmentHandler:)
                                              andWithBlockTypes: blocktypes
                                                  andWithTitles: titles
                                                    andWithUrls: urls
                                                 andWithClasses: classes
                                                 andWithMethods: methods
                                                    andWithArgs: args
                                               andWithMomentary: YES] retain];   // autorelease ??? -- JGP
    }
    else
        dm.smLeftMaster = nil;

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([config objectForKey:@"rightRootSegment"])
    {
        odict = [config objectForKey: @"rightRootSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <righttRootSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }

        dm.smRightMaster = [[[SegmentMap alloc] initWithSegments: segments
                                                          ofSize: [size intValue]
                                                   andWithTarget: self
                                                   andWithAction: @selector (rootSegmentHandler:)
                                               andWithBlockTypes: blocktypes
                                                   andWithTitles: titles
                                                     andWithUrls: urls
                                                  andWithClasses: classes
                                                  andWithMethods: methods
                                                     andWithArgs: args
                                                andWithMomentary: YES] retain];   // autorelease ??? -- JGP
    }
    else
        dm.smRightMaster = nil;

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([config objectForKey: @"bottomRootSegment"])
    {
        odict = [config objectForKey: @"bottomRootSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <bottomRootSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }

        dm.smBottomMaster = [[[SegmentMap alloc] initWithSegments: segments
                                                           ofSize: [size intValue]
                                                    andWithTarget: self
                                                    andWithAction: @selector (rootSegmentHandler:)
                                                andWithBlockTypes: blocktypes
                                                    andWithTitles: titles
                                                      andWithUrls: urls
                                                   andWithClasses: classes
                                                   andWithMethods: methods
                                                      andWithArgs: args
                                                 andWithMomentary: NO] retain];   // autorelease ??? -- JGP


    }
    else
        dm.smBottomMaster = nil;

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    {
        [segments addObject: @"<<<<"];

        size = @"50";
    }
    else
        size = @"0";

    if ([config objectForKey: @"leftDetailSegment"])
    {
        odict = [config objectForKey: @"leftDetailSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <leftDetailSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }

    dm.smLeftDetail = [[[SegmentMap alloc] initWithSegments: segments
                                                     ofSize: [size intValue]
                                              andWithTarget: self//dm.detailController  // these handled in the detailcontroller
                                              andWithAction: @selector(leftDetailSegmentHandler:)
                                          andWithBlockTypes: blocktypes
                                              andWithTitles: titles
                                                andWithUrls: urls
                                             andWithClasses: classes
                                             andWithMethods: methods
                                                andWithArgs: args
                                           andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    {
        [segments addObject: @">>>>"];

        size = @"50";
    }
    else
        size = @"0";

    if ([config objectForKey: @"leftDetailSegment"])
    {
        odict = [config objectForKey: @"leftDetailSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }

    dm.smLeftDetailReversed = [[[SegmentMap alloc] initWithSegments: segments
                                                             ofSize: [size intValue]
                                                      andWithTarget: self//dm.detailController  // these handled in the detailcontroller
                                                      andWithAction: @selector (leftDetailSegmentHandler:)
                                                  andWithBlockTypes: blocktypes
                                                      andWithTitles: titles
                                                        andWithUrls: urls
                                                     andWithClasses: classes
                                                     andWithMethods: methods
                                                        andWithArgs: args
                                                   andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    {
        [segments addObject: @"<<<<"];

        size = @"50";
    }
    else
        size = @"0";

    if ([config objectForKey: @"rightDetailSegment"])
    {
        odict = [config objectForKey: @"rightDetailSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <rightDetailSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }

    if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
        [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
        [segments addObject: @"Safari"];

    dm.smRightDetail = [[[SegmentMap alloc] initWithSegments: segments
                                                      ofSize: [size intValue]
                                               andWithTarget: self// dm.detailController  // these handled in the detailcontroller
                                               andWithAction: @selector (rightDetailSegmentHandler:)
                                           andWithBlockTypes: blocktypes
                                               andWithTitles: titles
                                                 andWithUrls: urls
                                              andWithClasses: classes
                                              andWithMethods: methods
                                                 andWithArgs: args
                                            andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    {
        [segments addObject: @">>>>"];

        size = @"50";
    }
    else
        size = @"0";

    if ([config objectForKey: @"leftDetailSegment"])
    {
        odict = [config objectForKey: @"leftDetailSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }

    dm.smLeftDetailReversed = [[[SegmentMap alloc] initWithSegments: segments
                                                             ofSize: [size intValue]
                                                      andWithTarget: self //dm.detailController  // these handled in the detailcontroller
                                                      andWithAction: @selector (leftDetailSegmentHandler:)
                                                  andWithBlockTypes: blocktypes
                                                      andWithTitles: titles
                                                        andWithUrls: urls
                                                     andWithClasses: classes
                                                     andWithMethods: methods
                                                        andWithArgs: args
                                                   andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];

    //        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
    //            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    //        {
    //            [segments addObject: @"<<<<"];
    //
    //            size = @"50";
    //        }
    //        else
    //            size = @"0";

    if ([config objectForKey: @"rightDetailSegment"])
    {
        odict = [config objectForKey: @"rightDetailSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <rightDetailSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }

    if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
        [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
        [segments addObject:@"Safari"];

    dm.smRightDetail = [[[SegmentMap alloc] initWithSegments: segments
                                                      ofSize: [size intValue]
                                               andWithTarget: self  // dm.detailController  // these handled in the detailcontroller
                                               andWithAction: @selector (rightDetailSegmentHandler:)
                                           andWithBlockTypes: blocktypes
                                               andWithTitles: titles
                                                 andWithUrls: urls
                                              andWithClasses: classes
                                              andWithMethods: methods
                                                 andWithArgs: args
                                            andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
    urls = [NSMutableArray array];          // ditto ... -- JGP
    titles = [NSMutableArray array];
    blocktypes = [NSMutableArray array];
    classes = [NSMutableArray array];
    methods = [NSMutableArray array];
    args = [NSMutableArray array];



    if ([config objectForKey: @"leftFullSegment"])
    {
        odict = [config objectForKey: @"leftFullSegment"];
        details = [odict objectForKey: @"Buttons"];
        size = [odict objectForKey: @"Width"];

        if (!size || !details)
        {
            [dm dieFromMisconfiguration: @"width and buttons missing in <leftFullSM/>"];

            return NO;
        }

        for (NSDictionary *dict in details)
        {
            if ([dict objectForKey: @"Action"])
                [blocktypes addObject: [dict objectForKey: @"Action"]];
            else
                [blocktypes addObject: @"missing-action"];

            if ([dict objectForKey: @"Label"])
                [segments addObject: [dict objectForKey: @"Label"]];
            else
                [segments addObject: @"missing-label"];

            if ([dict objectForKey: @"Title"])
                [titles addObject: [dict objectForKey: @"Title"]];
            else
                [titles addObject: @"missing-title"];

            if ([dict objectForKey: @"URL"])
                [urls addObject: [dict objectForKey: @"URL"]];
            else
                [urls addObject: @"no-url"];

            if ([dict objectForKey: @"class"])
                [classes addObject: [dict objectForKey: @"class"]];
            else
                [classes addObject: @""];

            if ([dict objectForKey: @"method"])
                [methods addObject: [dict objectForKey: @"method"]];
            else
                [methods addObject: @""];

            if ([dict objectForKey: @"args"])
                [args addObject: [dict objectForKey: @"args"]];
            else
                [args addObject: @""]; // bill - this was typo said "urls" instead of args
        }
    }


    dm.smLeftFull = [[[SegmentMap alloc] initWithSegments: segments
                                                   ofSize: [size intValue]
                                            andWithTarget: self  // dm.detailController  // these handled in the detailcontroller
                                            andWithAction: @selector (rootSegmentHandler:)
                                        andWithBlockTypes: blocktypes
                                            andWithTitles: titles
                                              andWithUrls: urls
                                           andWithClasses: classes
                                           andWithMethods: methods
                                              andWithArgs: args
                                         andWithMomentary: YES] retain];   // autorelease ??? -- JGP
                                                                           //    segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
                                                                           //    urls = [NSMutableArray array];          // ditto ... -- JGP
                                                                           //    titles = [NSMutableArray array];
                                                                           //    blocktypes = [NSMutableArray array];
                                                                           //    classes = [NSMutableArray array];
                                                                           //    methods = [NSMutableArray array];
                                                                           //    args = [NSMutableArray array];


    //
    //    if ([config objectForKey: @"rightFullSegment"])
    //    {
    //        odict = [config objectForKey: @"rightFullSegment"];
    //        details = [odict objectForKey: @"Buttons"];
    //        size = [odict objectForKey: @"Width"];
    //
    //        if (!size || !details)
    //        {
    //            [dm dieFromMisconfiguration: @"width and buttons missing in <rightFullSM/>"];
    //
    //            return NO;
    //        }
    //
    //        for (NSDictionary *dict in details)
    //        {
    //            if ([dict objectForKey: @"Action"])
    //                [blocktypes addObject: [dict objectForKey: @"Action"]];
    //            else
    //                [blocktypes addObject: @"missing-action"];
    //
    //            if ([dict objectForKey: @"Label"])
    //                [segments addObject: [dict objectForKey: @"Label"]];
    //            else
    //                [segments addObject: @"missing-label"];
    //
    //            if ([dict objectForKey: @"Title"])
    //                [titles addObject: [dict objectForKey: @"Title"]];
    //            else
    //                [titles addObject: @"missing-title"];
    //
    //            if ([dict objectForKey: @"URL"])
    //                [urls addObject: [dict objectForKey: @"URL"]];
    //            else
    //                [urls addObject: @"no-url"];
    //
    //            if ([dict objectForKey: @"class"])
    //                [classes addObject: [dict objectForKey: @"class"]];
    //            else
    //                [classes addObject: @""];
    //
    //            if ([dict objectForKey: @"method"])
    //                [methods addObject: [dict objectForKey: @"method"]];
    //            else
    //                [methods addObject: @""];
    //
    //            if ([dict objectForKey: @"args"])
    //                [args addObject: [dict objectForKey: @"args"]];
    //            else
    //                [args addObject: @""]; // bill - this was typo said "urls" instead of args
    //        }
    //    }


    dm.smRightFull = nil; // lets do it by hand
                          //[[[SegmentMap alloc] initWithSegments: segments
                          //                                                      ofSize: [size intValue]
                          //                                               andWithTarget: self  // dm.detailController  // these handled in the detailcontroller
                          //                                               andWithAction: @selector (rootSegmentHandler:)
                          //                                           andWithBlockTypes: blocktypes
                          //                                               andWithTitles: titles
                          //                                                 andWithUrls: urls
                          //                                              andWithClasses: classes
                          //                                              andWithMethods: methods
                          //                                                 andWithArgs: args
                          //                                            andWithMomentary: YES] retain];   // autorelease ??? -- JGP

    return YES;
}

#pragma mark -

- (void) rootSegmentIndex: (MySegmentedControl *) sender
                 atOrigin: (NSUInteger) origin
{
    // all the root guys come thru here because there is no special processing as with the detail segments
    NSUInteger  segIdx = sender.selectedSegmentIndex - origin;
    NSString    *blockType = [sender.blocktypes objectAtIndex: segIdx];
    AppDelegate *appDel = self.appDelegate;
    DataManager *dm = appDel.dataManager;

    if ([@"InvokeMethod" isEqual: blockType])
    {
        [dm invokeSavedActionBlockMethod: [sender.methods objectAtIndex: segIdx]
                                 inClass: [sender.classes objectAtIndex: segIdx]
                              withObject: [sender.args objectAtIndex: segIdx]];
    }
    else if ([@"ShowDetail" isEqual: blockType])
    {
        appDel.detailNavigationController.rootViewController = appDel.baseDetailViewController;

        [appDel.baseDetailViewController displayDetailWebView: [sender.urls objectAtIndex: segIdx]
                                              backgroundColor: [UIColor yellowColor]
                                                        title: [sender.titles objectAtIndex: segIdx]];
    }
    else if ([@"ReplaceScene" isEqual: blockType])
    {
        // change the scene and refresh the table
        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[sender.urls objectAtIndex: segIdx] stringByTrimmingWhitespace];
        NSUInteger itscene = [tscene intValue];

        if (itscene >= [scenes count])
        {
            [dm dieFromMisconfiguration: @"Destination ReplaceScene does not exist"];

            return;
        }

        dm.currentScene = itscene;

        UIViewController *vc = appDel.masterNavigationController.topViewController;

        if ([vc isKindOfClass: [MasterViewController class]])
        {
            MasterViewController *mvc = (MasterViewController *) vc;

            [mvc remotePoke: [sender.titles objectAtIndex: segIdx]];
        }
    }
    else if ([@"GotoScene" isEqual: blockType])
    {
        // change the scene and refresh the table

        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[sender.urls objectAtIndex: segIdx] stringByTrimmingWhitespace];
        NSUInteger itscene = [tscene intValue];

        if (itscene >= [scenes count])
        {
            [dm dieFromMisconfiguration: @"Destination GotoScene does not exist"];

            return;
        }

        dm.currentScene = itscene;

        UIViewController *vc = appDel.masterNavigationController.topViewController;

        if ([vc isKindOfClass: [MasterViewController class]])
        {
            MasterViewController *mvc = (MasterViewController *) vc;

            [mvc remotePoke: [sender.titles objectAtIndex: segIdx]];
        }

        appDel.detailNavigationController.rootViewController = appDel.baseDetailViewController;

        [appDel.baseDetailViewController displayDetailWebView: [sender.urls objectAtIndex:segIdx]
                                              backgroundColor: [UIColor greenColor]
                                                        title: [sender.titles objectAtIndex:segIdx]];
    }
    // ignore unknown blocktypes for now
}

- (void) rootSegmentHandler: (MySegmentedControl *) sender
{
    [self rootSegmentIndex: sender atOrigin: 0];
}

- (void) leftDetailSegmentHandler: (MySegmentedControl *) sender
{
    // these are fiddly because there is always an arrow spacer on the details
    //AppDelegate *appDel = self.appDelegate;
    //DataManager *dm = appDel.dataManager;
    //
    //    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
    //        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    //    {
    //        if (sender.selectedSegmentIndex == 0)
    //        {
    //            appDel.detailNavigationController.rootViewController = appDel.baseDetailViewController;
    //
    //            [appDel.baseDetailViewController toggleLeftPanel];
    //        }
    //        else
    //            [self rootSegmentIndex: sender
    //                          atOrigin: 1];
    //    }
    //    else
    [self rootSegmentIndex: sender
                  atOrigin: 0];

    //NSLog(@"leftDetail hit: %d  type: %@ url: %@   ",
    //            sender.selectedSegmentIndex, [sender.blocktypes objectAtIndex:sender.selectedSegmentIndex-1],[sender.urls objectAtIndex:sender.selectedSegmentIndex-1]);
}

- (void) rightDetailSegmentHandler: (MySegmentedControl *) sender
{
    //  NSLog(@"rightDetail hit: %d  type: %@ url: %@   ",
    //        sender.selectedSegmentIndex, [sender.blocktypes objectAtIndex:sender.selectedSegmentIndex],[sender.urls objectAtIndex:sender.selectedSegmentIndex]);
    AppDelegate *appDel = self.appDelegate;
    DataManager *dm = appDel.dataManager;

    if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
        [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
    {
        if ((NSUInteger) sender.selectedSegmentIndex == (sender.numberOfSegments - 1))
        {
            appDel.detailNavigationController.rootViewController = appDel.baseDetailViewController;

            [appDel.baseDetailViewController viewInSafari];
        }
        else
            [self rootSegmentIndex: sender
                          atOrigin: 0]; // otherwise handle normally
    }
    else
        [self rootSegmentIndex: sender
                      atOrigin: 0];
}


- (NSDictionary *) configureDetailViewFromSceneBlock: (NSUInteger) cb
{
    DataManager *dm = self.appDelegate.dataManager;

    // find the URL and Title from the particular scene

    NSDictionary *scene = [dm currentSceneContext];
    if (!scene) return nil;

    NSDictionary *block = [dm currentBlock: cb
                                  forScene: scene];

    if(!block)
        return nil;

    if (![block objectForKey: @"URL"])
    {
        [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                      @"Missing <URL/> in scene %@ block %d",
                                      [scene objectForKey: @"name"],
                                      cb]];

        return nil;
    }

    NSString *urlpart = [[block objectForKey: @"URL"] stringByTrimmingWhitespace];

    NSString *toptitlepart = [block objectForKey: @"Title"];


    if (!urlpart)
    {
        [dm dieFromMisconfiguration:[NSString stringWithFormat:
                                     @"Empty <URL/> in scene %@ block %d",
                                     [scene objectForKey: @"name"],
                                     cb]];

        return nil;
    }

    if (!toptitlepart)
    {
        [dm dieFromMisconfiguration:[NSString stringWithFormat:
                                     @"Missing <Title/> in scene %@ block %d",
                                     [scene objectForKey: @"name"],
                                     cb]];

        return nil;
    }

    return [NSDictionary dictionaryWithObjectsAndKeys: toptitlepart, @"Title", urlpart, @"URL", nil];
}

@end
