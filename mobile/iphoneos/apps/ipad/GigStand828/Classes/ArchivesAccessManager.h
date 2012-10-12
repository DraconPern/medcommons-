//
//  ArchivesAccessManager.h
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <CoreData/CoreData.h>
@class ArchiveInfo,ArchiveHeaderInfo;


@interface ArchivesAccessManager : NSObject {
	NSManagedObjectContext *mo;
	NSInteger dummy;
}
@property (nonatomic, retain) NSManagedObjectContext *mo;

@property (nonatomic) NSInteger dummy;

+ (ArchivesAccessManager *) sharedInstance;

+(void) setup:(NSManagedObjectContext *) moc;

-(id) init;

+ (NSArray *) allArchives;

+ (NSUInteger) archivesCount;

+(ArchiveInfo *) findArchive: (NSString *)archive ;

+(ArchiveInfo *) insertArchive:(NSString *) archive;

+(ArchiveInfo *) insertArchiveUnique:(NSString *) archive;

+(ArchiveHeaderInfo *) findArchiveHeader: (NSString *)archive forType: (NSString *)type ;

+(ArchiveHeaderInfo *) insertArchiveHeader:(NSString *)archive headerHTML:(NSString *)headerHTML forType:(NSString *)extension ;
@end

