//
//  CCRActionController.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@class CCRActionItem;

@interface CCRActionController : MCActionController
{
@private

    NSMutableArray *items_;
}

- (void) addItem: (CCRActionItem *) item;

- (CCRActionItem *) itemWithIdentifier: (NSString *) identifier;

- (void) removeAllItems;

- (void) removeItemWithIdentifier: (NSString *) identifier;

@end
