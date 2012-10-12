//
//  SnapshotInfo.h
//  gigstand
//
//  Created by bill donner on 4/15/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface SnapshotInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSDate * time;
@property (nonatomic, retain) NSString * filePath;
@property (nonatomic, retain) NSString * title;

@end
