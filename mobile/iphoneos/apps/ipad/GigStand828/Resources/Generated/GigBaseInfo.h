//
//  GigBaseInfo.h
//  GigStand
//
//  Created by bill donner on 3/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface GigBaseInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSDate * dbPreviousStartTime;
@property (nonatomic, retain) NSDate * dbOperationalTime;
@property (nonatomic, retain) NSString * gigbaseVersion;
@property (nonatomic, retain) NSDate * dbStartTime;
@property (nonatomic, retain) NSString * gigstandVersion;
@property (nonatomic, retain) NSString * restartCount;

@end



