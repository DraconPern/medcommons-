//
//  GigBaseInfo.h
//  gigstand
//
//  Created by bill donner on 4/15/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface GigBaseInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSDate * dbPreviousStartTime;
@property (nonatomic, retain) NSDate * dbOperationalTime;
@property (nonatomic, retain) NSString * gigbaseVersion;
@property (nonatomic, retain) NSDate * dbStartTime;
@property (nonatomic, retain) NSString * gigstandVersion;

@end
