//
//  TuneInfo.h
//  GigStand
//
//  Created by bill donner on 3/15/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <CoreData/CoreData.h>


@interface TuneInfo :  NSManagedObject  
{
}

@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * lastArchive;
@property (nonatomic, retain) NSString * lastFilePath;

@end



