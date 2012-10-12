//
//  SegmentMap.h
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SegmentMap : NSObject
{
@private

    UIBarButtonItem *segmentBarItem;
    //  NSArray *urls;
    //  NSArray *blocktypes;
    //SEL action; // ideally one for each entry in the segment, but not yet
}

@property (nonatomic, retain, readwrite) UIBarButtonItem *segmentBarItem;

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
                 andWithMomentary: (BOOL) _momentary;

@end
