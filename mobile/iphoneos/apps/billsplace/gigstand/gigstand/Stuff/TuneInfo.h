//
//  TuneInfo.h
//  gigstand
//
//  Created by bill donner on 4/15/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface TuneInfo : NSManagedObject {
@private
}
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * lastArchive;
@property (nonatomic, retain) NSString * lastFilePath;

@end
