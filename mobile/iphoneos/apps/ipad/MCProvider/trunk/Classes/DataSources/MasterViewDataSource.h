//
//  MasterViewDataSource.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MasterViewDataSource : NSObject <UITableViewDataSource>
{
@private

    BOOL withGroup;
}

- (id) initWithGroup: (BOOL) _withGroup;

@end
