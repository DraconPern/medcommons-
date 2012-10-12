//
//  ArchiveHeaderInfo.h
//  GigStand001
//
//  Created by bill donner on 3/25/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface ArchiveHeaderInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSString * extension;
@property (nonatomic, retain) NSString * headerHTML;

@end
