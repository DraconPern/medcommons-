//
//  ArchiveInfo.h
//  GigStand001
//
//  Created by bill donner on 3/25/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface ArchiveInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSNumber * size;
@property (nonatomic, retain) NSString * provenanceHTML;
@property (nonatomic, retain) NSNumber * enabled;
@property (nonatomic, retain) NSString * logo;
@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSNumber * fileCount;

@end
