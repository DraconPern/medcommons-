//
//  ListInfo.h
//  GigStand
//
//  Created by bill donner on 3/15/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface ListInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSDate * creationTime;
@property (nonatomic, retain) NSString * listName;

@end



