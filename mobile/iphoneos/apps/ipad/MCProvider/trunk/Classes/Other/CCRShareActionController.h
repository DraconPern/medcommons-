//
//  CCRShareActionController.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/29/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ShareActionController.h"

@class CCRActionItem;

@interface CCRShareActionController : ShareActionController
{
@private

    NSMutableArray *items_;
}

- (void) addItem: (CCRActionItem *) item;

- (CCRActionItem *) itemWithIdentifier: (NSString *) identifier;

- (void) removeAllItems;

- (void) removeItemWithIdentifier: (NSString *) identifier;

@end
