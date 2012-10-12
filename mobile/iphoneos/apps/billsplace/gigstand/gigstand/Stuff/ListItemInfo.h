//
//  ListItemInfo.h
//  gigstand
//
//  Created by bill donner on 4/15/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface ListItemInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSDate * insertTime;
@property (nonatomic, retain) NSString * listName;

@end
