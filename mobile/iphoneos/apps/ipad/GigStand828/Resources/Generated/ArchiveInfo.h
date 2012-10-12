//
//  ArchiveInfo.h
//  GigStand
//
//  Created by bill donner on 2/26/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface ArchiveInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSNumber * size;
@property (nonatomic, retain) NSString * provenanceHTML;
@property (nonatomic, retain) NSNumber * enabled;
@property (nonatomic, retain) NSString * logo;
@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSNumber * fileCount;

@end



