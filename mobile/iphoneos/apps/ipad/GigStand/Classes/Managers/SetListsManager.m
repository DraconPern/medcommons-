//
//  DataManager.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import "SettingsManager.h"
#import "DataManager.h"
#import "SetListsManager.h"
#import "DataStore.h"

#pragma mark -
#pragma mark Public Class SetListsManager
#pragma mark -
@interface SetListsManager ()
@end
@implementation SetListsManager



#pragma mark Public Class Methods
- (SetListsManager *) init
{
	self = [super init];
	if (self) 
	{

	}
	return self;	
}
#pragma mark Function or Subroutine Based Methods
+(void) rewriteTuneList:(NSArray *)items  toPropertyList:(NSString *)plistname;
{
	
	NSMutableArray *puta =[[[NSMutableArray alloc] init] autorelease];
	//NSMutableArray *putb =[[[NSMutableArray alloc] init] autorelease];
	
	for (NSString *tune in items)
	{
		[puta addObject:tune];
	}
	
	NSString *error;
    NSString *plistPath;
	
	plistPath = [ DataStore pathForTuneListWithName :plistname];
	
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects: plistname, PLIST_VERSION_NUMBER, puta,// putb,
								nil]
														  forKeys:[NSArray arrayWithObjects: 
																   @"listname",
																   @"version",
																   @"titles", 
																
																   nil]];
	
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
																   format:NSPropertyListXMLFormat_v1_0
														 errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
		//NSLog (@"rewrote setlist %@ size %d",[[plistPath  stringByDeletingPathExtension] lastPathComponent]  ,  [items count]);
	}
    else {
        NSLog(@"error %@", error);
        [error release];
    }
	
}

+ (SetListsManager *) sharedInstance
{
	static SetListsManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[SetListsManager alloc] init];
	}
	
	return SharedInstance;
}


+(NSMutableArray *) newSetlistsScan
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		
		
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			
			if (![shortie isEqualToString:@".DS_Store"])
				
				[alllists addObject:shortie];
			//NSLog (@"added setlist %@",shortie);
		}
	}
	return alllists;
}
+(NSMutableArray *) newSetlistsScanNoRecents;
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			if (!(([shortie isEqualToString:@".DS_Store"]) ||
				  ([shortie isEqualToString:@"recents"])))
				[alllists addObject:shortie];
			//NSLog (@"added setlist %@",shortie);
		}
	}
	return alllists;
}



#pragma mark plist save and restore for favorites, recents, setlists 


+(NSUInteger)  itemCountForList:(NSString * ) path ; 
{
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath;
	
	plistPath = [DataStore pathForTuneListWithName:path] ;//stringByAppendingPathComponent:path];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"itemCountForList No plist today for %@:=(", plistPath);
		return 0;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@" itemCountForList Error reading plist: %@, format: %d", errorDesc, format);
		return 0;
	}
	
	NSArray *titles = [temp objectForKey:@"titles"];
	return [titles count];
}

+(NSMutableArray *) allocListOfTunes: (NSString * ) path ; 
{
	// returns a list of tune titles now, 
	
	NSMutableArray *sec = [[NSMutableArray alloc] init];
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath;
	
	plistPath = [DataStore pathForTuneListWithName:path] ;//stringByAppendingPathComponent:path];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"allocListOfTunes No plist today for %@:=(", plistPath);
		return sec;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
		return sec;
	}
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		[sec addObject:[titlesx		objectAtIndex:n]];
	}
	
	return sec;
}

+(void) updateRecents:(NSString *)newtune;
{
// this is very brutal as it rewrites the whole list each time
	NSMutableArray *recents = [SetListsManager allocListOfTunes:@"recents"];
	BOOL found=NO;
	for (NSString *tune in recents)
		if ([newtune isEqualToString:tune])  {found=YES; break; }// check its not already in there
	if (found==NO) 
	{
		[recents insertObject:newtune atIndex:0];		
		if ([recents count] > 100) 
			[recents removeLastObject];
		[SetListsManager rewriteTuneList:recents	toPropertyList:@"recents"];
	}
	[recents release]; 
}




@end

