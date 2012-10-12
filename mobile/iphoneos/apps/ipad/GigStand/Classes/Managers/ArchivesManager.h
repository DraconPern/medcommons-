//
//  TunesManager.h
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <CoreData/CoreData.h>
@class ArchiveInfo,ArchiveHeaderInfo;


@interface ArchivesManager : NSObject {
	NSManagedObjectContext *mo;
	NSInteger dummy;
	NSString *otfarchive; // the name 
}
@property (nonatomic, retain) NSManagedObjectContext *mo;

@property (nonatomic) NSInteger dummy;

@property (nonatomic, readonly, retain) NSString *otfarchive;



+ (ArchivesManager *) sharedInstance;

+(void) setup:(NSManagedObjectContext *) moc;

-(id) init;

+ (NSArray *) allEnabledArchives;
+ (NSArray *) allArchives;
+ (NSArray *) allArchivesObjs;

+ (NSUInteger) archivesCount;

+(ArchiveInfo *) findArchive: (NSString *)archive ;


+(ArchiveInfo *) insertArchive:(NSString *) archive;

+(ArchiveInfo *) insertArchiveUnique:(NSString *) archive;

+(ArchiveHeaderInfo *) findArchiveHeader: (NSString *)archive forType: (NSString *)type ;

+(ArchiveHeaderInfo *) insertArchiveHeader:(NSString *)archive headerHTML:(NSString *)headerHTML forType:(NSString *)extension ;

// these came from ArchivesManager


// archives singleton object is gated thru these routines

+(BOOL) isArchiveEnabled: (NSString *) archive;
+(void) setArchiveEnabled:(BOOL) b forArchiveName: (NSString *) archive;

+(NSString *) archiveThumbnailSpec :(NSString *) archive;
+(double) fileSize: (NSString *) archive;
+(NSUInteger ) fileCount: (NSString *) archive;
+(NSString *) headerdataFromArchive: (NSString *) archive type: (NSString *) ext;

+(UIImage *) newArchiveThumbnail:(NSString *) archive; 

// utility routine that doesnt need db
+(NSString *) shortName: (NSString * ) archive;
+ (void) dump;
// db startup, etc
+(unsigned long long) totalFileSystemSize;
+(void) factoryReset;
+(NSUInteger) convertDirectoryToArchive:(NSString *) archive ;
+(void) buildNewDB;
+(void) setupDB;
+(NSString *)nameForOnTheFlyArchive;
+(void) copyFromInboxToOnTheFlyArchive: (NSString *) path  ofType:(NSString *) t withName:(NSString *) name;
+(void) saveImageToOnTheFlyArchive:  (UIImage *) image;


@end

