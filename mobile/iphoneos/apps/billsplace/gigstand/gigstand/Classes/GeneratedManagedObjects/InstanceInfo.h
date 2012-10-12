//
//  InstanceInfo.h
//  GigStand001
//
//  Created by bill donner on 3/25/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface InstanceInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSDate * lastVisited;
@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSString * filePath;

@end
