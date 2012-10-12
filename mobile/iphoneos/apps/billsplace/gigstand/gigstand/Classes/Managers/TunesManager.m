//
//  TunesManager.m -----
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//
#import "GigStandAppDelegate.h"
#import "TunesManager.h"
#import "TuneInfo.h"
#import "InstanceInfo.h"
#import "SnapshotInfo.h"
#import "GigBaseInfo.h"
#import "ArchivesManager.h"
#import "ArchiveInfo.h"
#import "DataManager.h"


@interface  TunesManager ()


@end

@implementation TunesManager
@synthesize dummy,lastTitle;

-(id) init
{
	self = [super init];
	if (self)
	{	
		lastTitle = nil;
	}
	return self;
}


+(void) setup
{
	[TunesManager sharedInstance].dummy = 1;
}
+ (TunesManager *) sharedInstance;
{
	static TunesManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[TunesManager alloc] init];
		
	}
	
	return SharedInstance;
}

+ (NSArray *) allTitles;
{
	
	/// returns an array of NSStrings, not managed objects
	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager allTitles"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"TuneInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]];
	
	for (TuneInfo *ti in fetchedObjects)
		[putb addObject:ti.title];
	
	return [putb autorelease];
}
+ (NSUInteger) tuneCount;
{
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager tuneCount"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"TuneInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSUInteger count = [context countForFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	return count;
}




+ (NSArray *) allTitlesFromArchive:(NSString *)archive;
{
	/// returns an array of NSStrings, not managed objects
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager allTitlesFromArchive"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];

	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@", archive];
	[fetchRequest setPredicate:somePredicate];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];

	NSMutableArray *putb = [[[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]] autorelease];

	for (InstanceInfo *ii in fetchedObjects)
	{ 
			[putb addObject:ii.title];
	}
	// weed out dupes
	
	[putb sortUsingSelector:@selector(compare:)];
	
	NSMutableArray *putc = [[NSMutableArray alloc] initWithCapacity:[putb count]];
	for (NSUInteger i=0; i<[putb count]; i++)
	{
		NSString *o = [putb objectAtIndex:i];
		NSString *p = [putc lastObject];
		if (! [o isEqualToString: p]) 		[putc addObject:o];
	}
	
	return [putc autorelease]; //cleanedArray;
}
+ (NSArray *) allVariantsFromTitle:(NSString *)title;
{
	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager allVariantsFromTitle"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" title == %@", title];
	[fetchRequest setPredicate:somePredicate];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"allVariantsFromTitle error %@",error);
		return nil;
	}
	if ([fetchedObjects count]==0) return nil;
	
	//return [fetchedObjects autorelease];  // seemed to be leaking
    return fetchedObjects;
}
+(TuneInfo *) findTune: (NSString *)tune
{
	//NSLog (@"TunesManager findTune");
	NSError *error=nil;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager findTune"];
	
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"TuneInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" title == %@", tune];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findTune error %@",error);
		return nil;
	}
	
	////	NSLog (@"======= TitleInfo fetched %d records from title== '%@' ",[fetchedObjects count],tune);
	
	if ([fetchedObjects count]>0) {
		return (TuneInfo *)[fetchedObjects objectAtIndex:0];
	}
	
	return nil;
}
+(TuneInfo *) findTuneInfo:(NSString *)tune;
{
	
	//Shadow into CoreData :::
	
	
	TuneInfo *ti = [TunesManager findTune:tune];
	//if (ti==nil) 
//		NSLog (@"findTuneInfo %@ failed",tune);
//	
	
	return ti;
}

+(void) addTuneInfo:(NSString *)title withLongPath:(NSString *)longpath;
{
	
	//Shadow into CoreData :::
	NSArray *parts = [longpath componentsSeparatedByString:@"/"];
	NSString *parchive = [parts objectAtIndex:0];
	NSString *pfilepath = [parts objectAtIndex:1];
	[TunesManager insertTuneUnique:title lastArchive:parchive lastFilePath:pfilepath];
	[TunesManager insertInstanceUnique:title  archive:parchive filePath:pfilepath];	
	
}	

+(TuneInfo *) tuneInfo:(NSString *) tune;
{
	
	TuneInfo *ti = [TunesManager findTune:tune];
	if (ti==nil) 
		NSLog (@"tuneInfo %@ failed",tune);
	
	NSAssert1 ((ti!=nil),@"Could not find tune %@ in TuneInfo table",tune);
	
	return ti;
	
}

+(TuneInfo *) insertTune:(NSString *) tune lastArchive:(NSString *) lastarchive lastFilePath:(NSString *) lastfilepath;
{
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager insertTune"];
	TuneInfo  *ti = [NSEntityDescription
					 insertNewObjectForEntityForName:@"TuneInfo" 
					 inManagedObjectContext:context];
	
	ti.title = tune;
	ti.lastArchive = lastarchive;
	ti.lastFilePath = lastfilepath;
	return ti;
}


+(TuneInfo *) insertTuneUnique:(NSString *) tune lastArchive:(NSString *) lastarchive lastFilePath:(NSString *) lastfilepath;
{
	//NSLog (@"TunesManager insertTuneUnique");
	TuneInfo *mo = [TunesManager findTune:tune];
	
	
	if (mo==nil) 
	{
		// if the tune wasnt found
		mo = [TunesManager insertTune:tune lastArchive:lastarchive lastFilePath:lastfilepath];
	}
	return mo;
}
+(void) titlePurgeCheck:(NSString *)title;
{
	NSUInteger counter = [[TunesManager allVariantsFromTitle:title] count];
	//NSLog (@"%@ count %D", title, counter);
	
	// if there are no instances for the title record then delete it	
	if (counter ==0)
	{
		NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager titlePurgeCheck"];
		
		TuneInfo *ti = [TunesManager findTune:title];
		if (ti!=nil) 
			[context deleteObject:ti];
	}	
}
+(InstanceInfo *) findInstance: (NSString *)archive filePath: (NSString *)filepath forTune: (NSString *)tune;
{
	//NSLog (@"TunesManager findInstance");
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager findInstance"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" filePath == %@ && archive == %@ && title == %@", filepath, archive,tune ];
	[fetchRequest setPredicate:somePredicate];
	
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findInstance error %@",error);
		return nil;
	}
	
	
	////		NSLog (@"======= InstanceInfo fetched %d records from archive=='%@' AND filePath== '%@' ",[fetchedObjects count],archive, filepath);
	if ([fetchedObjects count]>0) 
		return (InstanceInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
}

+(InstanceInfo *) insertInstance:(NSString *)tune  archive:(NSString *)archive filePath:(NSString *)filepath;
{
	//NSLog (@"TunesManager insertInstance");
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager insertInstance"];
	InstanceInfo *ii = [NSEntityDescription
						insertNewObjectForEntityForName:@"InstanceInfo" 
						inManagedObjectContext:context];
	
	ii.lastVisited = [NSDate date];
	ii.archive= archive;
	ii.filePath = filepath ;
	ii.title =tune ;
	
	return ii;
}

+(InstanceInfo *) insertInstanceUnique:(NSString *)tune  archive:(NSString *)archive filePath:(NSString *)filepath;
{
	//NSLog (@"TunesManager insertInstanceUnique");
	InstanceInfo *mo = [TunesManager findInstance:archive filePath:filepath forTune:tune];
	if (!mo) 
	{
		// if the tune wasnt found
		mo =  [TunesManager insertInstance:tune archive: archive filePath:filepath];
	}
	return mo;
}
+ (NSUInteger) instancesCount;
{
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager instancesCount"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSUInteger count = [context countForFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	return count;
}
+ (NSUInteger) snapshotCount;
{
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager snapshotCount"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"SnapshotInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSUInteger count = [context countForFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	return count;
    
}
+(SnapshotInfo *) findSnapshotInfo: (NSString *)tune;
{
	//NSLog (@"TunesManager findInstance");
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager findSnapshotInfo"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"SnapshotInfo" inManagedObjectContext:context];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" title == %@", tune ];
	[fetchRequest setPredicate:somePredicate];
	
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findSnapshotInfo error %@",error);
		return nil;
	}
	
	
	////		NSLog (@"======= InstanceInfo fetched %d records from archive=='%@' AND filePath== '%@' ",[fetchedObjects count],archive, filepath);
	if ([fetchedObjects count]>0) 
		return (SnapshotInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
}

+(NSArray *) allSnapshotInfos;
{	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager findSnapshotInfo"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"SnapshotInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findSnapshotInfo error %@",error);
		return nil;
	}
	
    if ([fetchedObjects count]==0) return nil;
	
	return fetchedObjects;
    
}


+(void) insertSnapshotInfo:(NSString *)filepath title:(NSString *)titl;
{
  
	NSDate *now = [NSDate date];
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager insertSnapshotInfo"];
	
	// see if we already have stuff there
	NSError *error;
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"SnapshotInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	[fetchRequest release];
	if (fetchedObjects == nil)
	{
		if (error) 
		{
			NSLog (@"insertSnapshotInfo error %@",error);
			return;
		}
	}
//	else if ([fetchedObjects count]>0)
//	{
//		SnapshotInfo *gb = (SnapshotInfo *) [fetchedObjects lastObject];
//
//		gb.time = now;
//	}
	else 
        
	{
		// no previous gigbase records, so insert one
		SnapshotInfo *gb = [NSEntityDescription
						   insertNewObjectForEntityForName:@"SnapshotInfo" 
						   inManagedObjectContext:context];
		gb.time = now;
        gb.filePath = filepath;
        gb.title = titl;		
	}
	
	[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"insertSnapshotInfo %@",titl]];
  
}


+(BOOL) removeOldestSnapshot;
{
      SnapshotInfo *thislii = nil;
    NSDate *thisdate = [NSDate distantFuture];
    NSString *thispath;
    // Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager removeOldestSnapshot"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"SnapshotInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];

	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"removeOldestSnapshot error %@",error);
		return NO;
	}
    for (SnapshotInfo *lii in fetchedObjects)
        if ([lii.time compare:  thisdate] < 0)
        {
            thisdate = lii.time;
            thislii = lii;
            thispath = lii.filePath;
        }
    // ok delete this
    if (thislii == nil) return NO;
    // delete the actual file from the file system 
    [[NSFileManager defaultManager] removeItemAtPath:thispath error: &error];
    if ([[NSFileManager defaultManager] fileExistsAtPath:thispath])
        NSLog (@"Error removing %@",thispath);
    [context deleteObject:thislii];
    
	[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"removeOldestSnapshot from %@",thisdate]];
    return YES;
    
}
+(GigBaseInfo *) findGigBaseInfo;
{	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager findGigBaseInfo"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"GigBaseInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findGigBaseInfo error %@",error);
		return nil;
	}
	
	if ([fetchedObjects count]>0) 
		return (GigBaseInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
}
+(void) updateGigBaseInfo;
{
	NSDate *now = [NSDate date];
	GigBaseInfo *gb = [TunesManager findGigBaseInfo];
	gb.dbPreviousStartTime = gb.dbStartTime;
	gb.dbStartTime = now;
	
	[[GigStandAppDelegate sharedInstance] saveContext:@"updateGigBaseInfo"];
	NSLog (@"gb - startTime:%@, previousTime: %@, dbversion: %@",gb.dbStartTime,gb.dbPreviousStartTime,	gb.gigbaseVersion);
	
}
+(void) dumpGigBaseInfo;
{
}
+(void) insertGigBaseInfo;
{
	NSDate *now = [NSDate date];
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager insertGigBaseInfo"];
	
	// see if we already have stuff there
	NSError *error;
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"GigBaseInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	[fetchRequest release];
	if (fetchedObjects == nil)
	{
		if (error) 
		{
			NSLog (@"findGigBase error %@",error);
			return;
		}
	}
	else if ([fetchedObjects count]>0)
	{
		GigBaseInfo *gb = (GigBaseInfo *) [fetchedObjects lastObject];
		gb.dbPreviousStartTime = gb.dbStartTime;
		gb.dbStartTime = now;
		gb.gigstandVersion = [DataManager sharedInstance].applicationVersion;
		NSLog (@"gb existing %d - startTime:%@, previousTime: %@, dbversion: %@",[fetchedObjects count],
			   gb.dbStartTime,gb.dbPreviousStartTime,	gb.gigbaseVersion);
	}
	else 
	
	{
		// no previous gigbase records, so insert one
		GigBaseInfo *gb = [NSEntityDescription
						   insertNewObjectForEntityForName:@"GigBaseInfo" 
						   inManagedObjectContext:context];
		gb.dbStartTime = now;
		gb.dbPreviousStartTime = [NSDate distantPast];
		gb.gigbaseVersion = @"0.1.3";
		gb.gigstandVersion = [DataManager sharedInstance].applicationVersion;
		gb.dbOperationalTime = [NSDate distantFuture]; // this should get reset 
		NSLog (@"gb - startTime:%@, previousTime: %@, dbversion: %@",gb.dbStartTime,gb.dbPreviousStartTime,	gb.gigbaseVersion);
		
	}
	
	[[GigStandAppDelegate sharedInstance] saveContext:@"insertGigBaseInfo"];
}
////
////
//// TEST CODE HERE
////
////
////

+(void) dumpRelatedInstances:(NSString *)tune
{
	/////////////
	/////////////
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"TunesManager dumpRelatedInstances"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" title == %@", tune];
	[fetchRequest setPredicate:somePredicate];
	
	
	[fetchRequest setEntity:entity];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	if (fetchedObjects == nil) NSLog (@" ??has no instances");
	else 
	{
		NSLog(@">%@ has %d instances",tune,[fetchedObjects count]);
		for (InstanceInfo *details in fetchedObjects) {
			NSLog (@">>instance Info:");	
			NSLog(@"	>archive: %@", details.archive);
			
			NSLog(@"	>filePath: %@", details.filePath);
			
			NSLog(@"	>lastVisited: %@", details.lastVisited);
			
		} 
	}
	[fetchRequest release];
	/////////////
	////////////
}
+(NSString *) lastTitle;
{
	// runs in memory
	return [TunesManager sharedInstance].lastTitle;
}
+(void) setLastTitle:(NSString *)title;
{
	// runs in memory
	[TunesManager sharedInstance].lastTitle=title;
}

@end
