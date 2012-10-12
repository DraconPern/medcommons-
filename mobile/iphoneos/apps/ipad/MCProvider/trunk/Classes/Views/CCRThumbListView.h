//
//  CCRThumbListView.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AQGridView.h"      // <AQGridView/AQGridView.h>

@class CCRThumbCell;

@interface CCRThumbListView : AQGridView
{
@private

    CCRThumbCell   *activeCell_;
    NSMutableArray *cells_;
}

@property (nonatomic, retain, readwrite) CCRThumbCell *activeCell;

- (void) addCell: (CCRThumbCell *) cell;

- (CCRThumbCell *) cellWithIdentifier: (NSString *) identifier;

- (void) removeAllCells;

- (void) removeCellWithIdentifier: (NSString *) identifier;

- (CGSize) sizeThatFitsHorizontal: (CGSize) size;

- (CGSize) sizeThatFitsVertical: (CGSize) size;

@end
