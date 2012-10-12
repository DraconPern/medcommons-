//
//  ArchiveHeaderInfo.h
//  GigStand
//
//  Created by bill donner on 3/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface ArchiveHeaderInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSString * archive;
@property (nonatomic, retain) NSString * extension;
@property (nonatomic, retain) NSString * headerHTML;

@end



