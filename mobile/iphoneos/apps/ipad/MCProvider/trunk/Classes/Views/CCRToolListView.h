//
//  CCRToolListView.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AQGridView.h"      // <AQGridView/AQGridView.h>

@class CCRToolCell;

@interface CCRToolListView : AQGridView
{
@private

    NSMutableArray *cells_;
}

- (void) addCell: (CCRToolCell *) cell;

- (CCRToolCell *) cellWithIdentifier: (NSString *) identifier;

- (CCRToolCell *) cellWithImage: (UIImage *) image;

- (void) removeAllCells;

- (void) removeCellWithIdentifier: (NSString *) identifier;

- (CGSize) sizeThatFitsHorizontal: (CGSize) size;

- (CGSize) sizeThatFitsVertical: (CGSize) size;

@end
