//
//  TunesManager.m -----
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "TunesManager.h"
#import "TuneInfo.h"
#import "InstanceInfo.h"
#import "GigBaseInfo.h"
#import "ArchivesManager.h"
#import "ArchiveInfo.h"
#import "DataManager.h"

@interface  TunesManager ()


@end

@implementation TunesManager
@synthesize mo,dummy,lastTitle;

-(id) init
{
	self = [super init];
	if (self)
	{	
		lastTitle = nil;
	}
	return self;
}


+(void) setup:(NSManagedObjectContext *) moc
{
	[TunesManager sharedInstance].dummy = 1;
	[TunesManager sharedInstance].mo = moc;
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
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@", archive];
	[fetchRequest setPredicate:somePredicate];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]];
	
	for (TuneInfo *ti in fetchedObjects)
		[putb addObject:ti.title];
	
	return [putb autorelease];
	
}
+ (NSArray *) allVariantsFromTitle:(NSString *)title;
{
	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
	
	return fetchedObjects;
}
+(TuneInfo *) findTune: (NSString *)tune
{
	//NSLog (@"TunesManager findTune");
	NSError *error=nil;
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
	//NSLog (@"In findTune context from mo is %@",context);
	
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
	if (ti==nil) 
		NSLog (@"findTuneInfo %@ failed",tune);
	
	
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
	//NSLog (@"TunesManager insertTune");
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
+(InstanceInfo *) findInstance: (NSString *)archive filePath: (NSString *)filepath forTune: (NSString *)tune;
{
	//NSLog (@"TunesManager findInstance");
	NSError *error;
	NSManagedObjectContext *context =[TunesManager sharedInstance].mo;
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
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
+(GigBaseInfo *) findGigBaseInfo;
{	NSError *error;
	NSManagedObjectContext *context =[TunesManager sharedInstance].mo;
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"GigBaseInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findInstance error %@",error);
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
	
	NSLog (@"gb - startTime:%@, previousTime: %@, dbversion: %@",gb.dbStartTime,gb.dbPreviousStartTime,	gb.gigbaseVersion);
	
}
+(void) dumpGigBaseInfo;
{
}
+(void) insertGigBaseInfo;
{
	NSDate *now = [NSDate date];
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
	
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
		     NSLog (@"# of existing GigBase records %d", [fetchedObjects count]);

	{
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
	NSManagedObjectContext *context = [TunesManager sharedInstance].mo;
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
