//
//  InstanceInfo.h
//  GigStand
//
//  Created by bill donner on 3/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface InstanceInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSDate * lastVisited;
@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSString * filePath;

@end



