//
//  ListItemInfo.h
//  GigStand
//
//  Created by bill donner on 3/15/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface ListItemInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * listName;
@property (nonatomic, retain) NSDate * insertTime;

@end



