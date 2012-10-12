//
//  CCREpisodeActionItem.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/21/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRActionItem.h"

@interface CCREpisodeActionItem : CCRActionItem
{
@private

    NSArray *docList_;  // array of NSString
}

@property (nonatomic, copy, readwrite) NSArray *docList;

@end
