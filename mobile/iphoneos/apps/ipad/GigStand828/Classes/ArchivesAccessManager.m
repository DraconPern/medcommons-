//
//  ArchivesAccessManager.m -----
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "ArchivesAccessManager.h"
#import "ArchiveInfo.h"
#import "ArchiveHeader.h"


@implementation ArchivesAccessManager
@synthesize mo,dummy;

-(id) init
{
	self = [super init];
	if (self)
	{	}
	return self;
}


+(void) setup:(NSManagedObjectContext *) moc
{
	[TunesAccessManager sharedInstance].dummy = 1;
	[TunesAccessManager sharedInstance].mo = moc;
}
+ (ArchivesAccessManager *) sharedInstance;
{
	static ArchivesAccessManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[ArchivesAccessManager alloc] init];
		
	}
	
	return SharedInstance;
}

+ (NSArray *) allArchives;
{
	
	/// returns an array of NSStrings, not managed objects
	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context = [ArchivesAccessManager sharedInstance].mo;
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
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


+ (NSUInteger) archivesCount;
{
	return [[ArchivesAccessManager allTitles] count];
}

+(ArchiveInfo *) findArchive: (NSString *)archive ;
{;
	NSError *error=nil;
	NSManagedObjectContext *context = [ArchivesAccessManager sharedInstance].mo;
	//NSLog (@"In findTune context from mo is %@",context);
	
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@", tune];
	[fetchRequest setPredicate:somePredicate];

	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	if (fetchedObjects == nil)
	{
	if (error) 
		NSLog (@"findTune error %@",error);
		return nil;
	}
	
	if ([fetchedObjects count]>0) {
		return (ArchiveInfo *)[fetchedObjects objectAtIndex:0];
	}
	
	return nil;
}

+(ArchiveInfo *) insertArchive:(NSString *) archive;
{
	NSManagedObjectContext *context = [ArchivesAccessManager sharedInstance].mo;
	ArchiveInfo  *ai = [NSEntityDescription
							  insertNewObjectForEntityForName:@"ArchiveInfo" 
							  inManagedObjectContext:context];
	
	ai.title = archive;
	return ai;
}


+(ArchiveInfo *) insertArchiveUnique:(NSString *) archive;
{
	ArchiveInfo *mo = [ArchivesAccessManager findArchive:archive];
	if (mo==nil) 
	{
		// if the tune wasnt found
		mo = [ArchivesAccessManager insertArchive:archive];
	}
	return mo;
}
+(ArchiveHeaderInfo *) findArchiveHeader: (NSString *)archive forType: (NSString *)extension ;
{
	NSError *error;
	NSManagedObjectContext *context =[ArchivesAccessManager sharedInstance].mo;
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveHeaderInfo" inManagedObjectContext:context];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@ && extension == %@", archive,extension ];
	[fetchRequest setPredicate:somePredicate];
	
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findArchiveHeader error %@",error);
		return nil;
	}
	
	
////		NSLog (@"======= InstanceInfo fetched %d records from archive=='%@' AND filePath== '%@' ",[fetchedObjects count],archive, filepath);
	if ([fetchedObjects count]>0) 
		return (ArchiveHeaderInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
}


+(ArchiveHeaderInfo *) insertArchiveHeader:(NSString *)archive headerHTML:(NSString *)headerHTML forType:(NSString *)extension ;

{
		//NSLog (@"TunesAccessManager insertInstance");
	NSManagedObjectContext *context = [ArchivesAccessManager sharedInstance].mo;
	ArchiveHeaderInfo *ahi = [NSEntityDescription
									 insertNewObjectForEntityForName:@"ArchiveHeaderInfo" 
									 inManagedObjectContext:context];
	
	
	ahi.extension = extension;
	ahi.headerHTML = headerHTML;
	ahi.archive = archive;
	return ahi;
}





@end
