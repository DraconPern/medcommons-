//
//  ArchivesManager.m -----
//  GigStand
//
//  Created by bill donner on 2/22/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "ArchivesManager.h"
#import "ArchiveInfo.h"
#import "ArchiveHeaderInfo.h"
#import "InstanceInfo.h"
#import "DataStore.h"
#import "DataManager.h"
#import "TunesManager.h"
#import "GigStandAppDelegate.h"
#import "SetListsManager.h"
#import "ProgressString.h"

@implementation ArchivesManager 
@synthesize dummy,otfarchive;

-(id) init
{
	self = [super init];
	if (self)
	{	
		otfarchive = [@"onthefly-archive" retain];
	}
	return self;
}


+(void) setup
{
	[ArchivesManager sharedInstance].dummy = 1;
}
+ (ArchivesManager *) sharedInstance;
{
	static ArchivesManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[ArchivesManager alloc] init];
		
	}
	
	return SharedInstance;
}
+ (NSArray *) allEnabledArchives;
{
	// just return list of names	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager allEnabledArchives"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat :@" enabled == TRUE"];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	/////////////
	////////////
	
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]];
	for (ArchiveInfo *ai in fetchedObjects)
		[putb	addObject:ai.archive];
	
	
	return [putb autorelease];
}
+ (NSArray *) allArchives;
{
	// just return list of names	
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager allArchives"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	     
	[fetchRequest release];
	/////////////
	////////////
	
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]];
	for (ArchiveInfo *ai in fetchedObjects)
		[putb	addObject:ai.archive];
	
	
	return [putb autorelease];
}
+ (NSArray *) allArchivesObjs;
{
	// return actual managed objects
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager allArchivesObjs"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	if (fetchedObjects == nil)
	{
	if (error) 
		NSLog (@"Allobjs error %@",error);
		return nil;
	}
	/////////////
	////////////
	if ([fetchedObjects count]>0)
	return fetchedObjects;
	
	return nil;
}
+ (NSUInteger) archivesCount;
{
	// return just count (optimized)
	/////////////
	/////////////
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager archivesCount"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSUInteger count = [context countForFetchRequest:fetchRequest error:&error];
	
	[fetchRequest release];
	return count;

}


+(ArchiveInfo *) findArchive: (NSString *)archive 
{
	NSError *error=nil;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager findArchive"];
	
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@", archive];
	[fetchRequest setPredicate:somePredicate];

	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	if (fetchedObjects == nil)
	{
	if (error) 
		NSLog (@"findArchive error %@",error);
		return nil;
	}	
	if ([fetchedObjects count]>0) {
		return (ArchiveInfo *)[fetchedObjects objectAtIndex:0];
	}
	
//	NSLog (@"======= findArchive fetched %d records from archive== '%@' ",[fetchedObjects count],archive);

	return nil;
}

+(ArchiveInfo *) insertArchive:(NSString *) archive;
{
NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager insertArchive"];
	ArchiveInfo  *ai = [NSEntityDescription
							  insertNewObjectForEntityForName:@"ArchiveInfo" 
							  inManagedObjectContext:context];
	
	ai.archive = archive;
	ai.logo = @"";	// set this to something non-nil
	ai.provenanceHTML = @""; // or core data will fail on actual flush
	return ai;
}


+(ArchiveInfo *) insertArchiveUnique:(NSString *) archive;
{
	ArchiveInfo *mo = [ArchivesManager findArchive:archive];
	if (mo==nil) 
	{
		// if the tune wasnt found
		mo= [ArchivesManager insertArchive:archive ];
	}
	return mo;
}

+(BOOL) deleteArchive:(NSString *)archive;
{
	
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager deleteArchive"];
	
	ArchiveInfo *arch = [ArchivesManager findArchive:archive];
	if (arch==nil) return NO;
	
	// delete with CASCADE
	NSUInteger filesadded = 0;
	
	
	NSError *error;	
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"InstanceInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@ ",archive ];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	NSMutableDictionary *titlesToCheck = [[[NSMutableDictionary alloc] init] autorelease];
	
	for  (InstanceInfo *ii in fetchedObjects)
	{
		// delete the actual file from the file system 
		NSString *path = [DataManager deriveLongPath:ii.filePath forArchive:ii.archive];
		[[NSFileManager defaultManager] removeItemAtPath:path error: &error];
		if ([[NSFileManager defaultManager] fileExistsAtPath:path])
			NSLog (@"Error removing %@",path);
	
		NSString *temp =[NSString stringWithFormat:@"%@",ii.title];
		
		[context deleteObject:ii];
		// add to the dictionary of all titles that might need purging
		[titlesToCheck setObject:temp forKey:temp];
		//else NSLog (@"deleted file %@",path);
		
		
		filesadded++;	
		if ((filesadded/50)*50==filesadded)
			[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"small batch deletion at %d",filesadded]];
	
	}
	[context deleteObject:arch];
	
	// make sure titles still have instances, else purge them
	//NSLog (@"checking titles %@",titlesToCheck);
	
	for (NSString *title in titlesToCheck)
		
	{
		[TunesManager titlePurgeCheck: title];
		
		filesadded++;	
		if ((filesadded/50)*50==filesadded)
			[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"small batch deletion from titles check at %d",filesadded]];
		
	}
	
	[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"deleteArchive %@",archive,nil]];
	
	return YES;
}
			   
+(ArchiveHeaderInfo *) findArchiveHeader: (NSString *)archive forType: (NSString *)type ;
{
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager findArchiveHeader"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ArchiveHeaderInfo" inManagedObjectContext:context];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" archive == %@ && extension == %@",  archive,type ];
	[fetchRequest setPredicate:somePredicate];
	
	
	[fetchRequest setEntity:entity];
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findArchiveHeader error %@",error);
		return nil;
	}
	
	if ([fetchedObjects count]>0) 
		return (ArchiveHeaderInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
}

+(ArchiveHeaderInfo *) insertArchiveHeader:(NSString *)archive headerHTML:(NSString *)headerHTML forType:(NSString *)extension ;
{
	
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"ArchivesManager insertArchiveHeader"];
	ArchiveHeaderInfo *ahi = [NSEntityDescription
									 insertNewObjectForEntityForName:@"ArchiveHeaderInfo" 
									 inManagedObjectContext:context];
	
	
	ahi.archive = archive;
	ahi.extension = extension;
	ahi.headerHTML = headerHTML;

	return ahi;
}
////// these came from Archives Helper
#pragma mark Function or Subroutine Based Methods

+(NSString *)nameForOnTheFlyArchive
{
	return [ArchivesManager sharedInstance].otfarchive;
}


+(NSString *) shortName: (NSString * ) s
{
	NSArray *arr = [s componentsSeparatedByString:@"-"] ;//042511 removed it again 042311 added autorelease
    
	NSString *ss = (NSString *)[arr objectAtIndex:0]; // just take first component
	
	NSUInteger shortlen = (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)? 9: 12;
	if ([ss length]<=shortlen) return ss;	
	return	[ss substringToIndex:shortlen];
	
}
+(NSString *) headerdataFromArchive: (NSString *) archive type: (NSString *) extension;
{
	ArchiveHeaderInfo *ahi = [ArchivesManager findArchiveHeader:archive forType:extension];
	return ahi.headerHTML;
}

+(double) fileSize: (NSString *) archive;
{		//MUST MODIFY FOR COREDATA
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	unsigned long long ull = ([ai.size unsignedLongLongValue]);
	
	double mb = (double)ull	; // make this a double
	
	mb = mb/(1024.f*1024.f); // Convert to MB
	
	return mb;
}

+(BOOL) isArchiveEnabled: (NSString *) archive;
{			
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	NSNumber *booly = ai.enabled ;
	return [booly boolValue];
	
}
+(void) setArchiveEnabled:(BOOL) b forArchiveName: (NSString *) archive;
{		
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	NSNumber *booly2 = [NSNumber numberWithBool:b];
	ai.enabled = booly2;
	
}
+(NSUInteger ) fileCount: (NSString *) archive;
{
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	return [ai.fileCount intValue];
}
+(void ) bumpFileCount: (NSString *) archive;
{
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
    
	NSUInteger newcount =  [ai.fileCount intValue] + 1;
    
    ai.fileCount = [NSNumber numberWithUnsignedInteger:    newcount];
    
}
+(NSString *) archiveThumbnailSpec :(NSString *) archive
{
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	return ai.logo;
	
}

+(UIImage *) makeArchiveThumbnail:(NSString *) archive;
{
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	
	if ( [ai.logo length] > 5 ) // make sure there's something real in there
	{
		return [DataManager makeThumbFS:ai.logo 
										  size:[DataManager standardThumbSize]];
	}
	else {
		
		
		return     [DataManager makeThumbRS:
					@"MusicStand_72x72.png" // make sure this fails
											  size:[DataManager standardThumbSize]];
	}
}
#pragma mark Object Based Methods
+(NSString *) extractTitle:(NSString*) longtitle
{	// an actual source file
	NSArray *arr = [NSArray arrayWithObject: longtitle];										
	NSString *htitle = (NSString *)[arr objectAtIndex:0]; // just take first component
	// expand hungarian
	NSString *ptitle =[DataManager noHungarian:htitle];
	// //// ignore everythig after first dash -- this seems to be working well
	NSArray *arr2 = [ptitle componentsSeparatedByString:@"-"];
	NSString *title0 = (NSString *)[arr2 objectAtIndex:0]; // just take first component
	///  apply other title hacks here
	return title0;
}

+(void) parseAndAdd: (NSString *)pathtoremember title:(NSString *)longtitle
{
	NSString *title0 = [self extractTitle:longtitle];
	
	if ([title0 length]>0) // make sure there's really something there
	{
		NSArray *parts = [pathtoremember componentsSeparatedByString:@"/"];
		NSString *parchive = [parts objectAtIndex:0];
		NSString *pfilepath = [parts objectAtIndex:1];
		
		// Just add this right in							
		
		[TunesManager insertTuneUnique: title0 lastArchive:parchive lastFilePath:pfilepath];
		// even if that was a dupe, add an instance record
		
		[TunesManager insertInstanceUnique :title0  archive: parchive filePath:pfilepath];	
	}
}


#pragma mark  Once Only Startup Code for Building In Memory Stuctures
+(void) saveImageToOnTheFlyArchive:  (UIImage *) image;
{	
	NSDate *datenow = [NSDate date];
	NSString *dateString = [datenow description];
	// this **dateString** string will have **"yyyy-MM-dd HH:mm:ss +0530"**
	NSArray *arr = [dateString componentsSeparatedByString:@" "];
	NSString *date = [arr objectAtIndex:0];
	NSString *time = [arr objectAtIndex:1];
	
	NSString *hh = [time substringToIndex:2];
	NSString *mm = [[time substringFromIndex:3]substringToIndex:2];
	NSString *ss = [[time substringFromIndex:6]substringToIndex:2];
	
	// arr will have [0] -> yyyy-MM-dd, [1] -> HH:mm:ss, [2] -> +0530 (time zone)
	
	NSString *filepath = [NSString stringWithFormat:  @"%@%@%@-screenshot-%@.png", hh,mm,ss,date];
	NSString *topath = [[DataStore pathForOnTheFlyArchive] 
						stringByAppendingPathComponent:filepath];
	
	NSData *imageData = UIImagePNGRepresentation(image);
	if (imageData != nil) {
		
		[imageData writeToFile:topath atomically:YES];		
		
		NSString *longtitle   = [[topath  stringByDeletingPathExtension] lastPathComponent];
		
		NSString *title0 = [NSString stringWithFormat:@"%@-webcapture",[ArchivesManager extractTitle:longtitle]];
		
		//NSLog (@"saveImageToOnTheFlyArchive %@",title0);
		
		
		[TunesManager insertTuneUnique: title0 lastArchive:[ArchivesManager nameForOnTheFlyArchive] lastFilePath:filepath];
		// even if that was a dupe, add an instance record
		[TunesManager insertInstanceUnique :title0  archive:[ArchivesManager nameForOnTheFlyArchive] filePath:filepath];
          [ArchivesManager bumpFileCount:@"onthefly-archive"];
		[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"saveImageToOnTheFlyArchive %@",title0,nil]];
		
	}
	
}
+(void) copyFromInboxToOnTheFlyArchive: (NSString *) path  ofType:(NSString *) t withName:(NSString *) filepath;
{
	NSError *error;
	NSString *topath = [[DataStore pathForOnTheFlyArchive] 
						stringByAppendingPathComponent:filepath];
	BOOL success = [[NSFileManager defaultManager] copyItemAtPath:path
														   toPath:topath
															error:&error];
	if (!success) 
		NSLog (@"copyFromInboxToOnTheFlyArchive %@ returns %@", filepath,error.localizedDescription);
	
	else {
		NSString *longtitle   = [[path  stringByDeletingPathExtension] lastPathComponent];
		
		NSString *title0 = [ArchivesManager extractTitle:longtitle];
		
		[TunesManager insertTuneUnique: title0 lastArchive:[ArchivesManager nameForOnTheFlyArchive] lastFilePath:filepath];
		// even if that was a dupe, add an instance record
		[TunesManager insertInstanceUnique :title0  archive:[ArchivesManager nameForOnTheFlyArchive] filePath:filepath];	
        
        [ArchivesManager bumpFileCount:@"onthefly-archive"];
		
		[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"copyFromInboxToOnTheFlyArchive %@",title0,nil]];
		
	}
}


+(NSUInteger) convertDirectoryToArchive:(NSString *) archive ;
{
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"building: %@",archive]];
	[ArchivesManager insertArchive: archive];
	
	ArchiveInfo *ai = [ArchivesManager findArchive:archive];
	ai.enabled = [NSNumber numberWithBool:YES]; 
	ai.logo = @""; 
	ai.fileCount = [NSNumber numberWithUnsignedInt:0]; 
	ai.size = [NSNumber numberWithUnsignedLongLong:0];
	ai.provenanceHTML = @"";
	
	
	NSString *file;
	NSString *bigpath;
	NSUInteger filesadded = 0;
	unsigned long long size=0;
	
	bigpath = [DataStore pathForArchive: archive];
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager] enumeratorAtPath: bigpath];
	
	// segregate the files
	while ((file = [dirEnum nextObject]))
	{
		NSArray *parts = [file componentsSeparatedByString:@"/"];
		// skip these stinky MCOS directories that show up when jpgs are present
		
		NSDictionary *attrs = [dirEnum fileAttributes];
		//    	NSString *bigpathfile = [NSString stringWithFormat:@"%@/%@",bigpath, file];
		NSString *pathtoremember = [NSString stringWithFormat:@"%@/%@",archive, file];
		//		
		// count total size of all files scanned
		NSNumber *fs = [attrs objectForKey:@"NSFileSize"];		
		NSString *ftype = [attrs objectForKey:@"NSFileType"]; 
		
		//	NSLog (@"expandir %@ type %@",pathtoremember, ftype);
		if ([NSFileTypeRegular isEqualToString:ftype]&&([parts count] ==1 ))
		{
			// if the file starts with a ., like .DS_Store, just skip it
			if (!  ([@"." isEqualToString:[file substringToIndex:1]] || [@"_plist" isEqualToString:[file substringToIndex:6]] 
					|| [@"Inbox" isEqualToString:[file substringToIndex:5]]))
			{
				NSRange range = [file rangeOfString:@"-thumbnail.png"];// our own inserted thumbnail?
				if (range.location == NSNotFound)
				{
					//seems good to go
					size += [fs longLongValue];
					NSString *longtitle   = [[file stringByDeletingPathExtension] lastPathComponent]; 
					// if it's a special file then handle tharchiveat
					if ([@"--logo--" isEqualToString:longtitle])
					{
						// only remember the last of these
						NSString *logopath = [NSString stringWithFormat:@"%@/%@",archive, file];
						// just stash the filespec for anyone who wants it// assume no logo							
						ai.logo = logopath;		
					}
					else 
						if ([@"--header--" isEqualToString:longtitle])
						{
							// remember all of these 
							NSString *filetype = [file pathExtension];
							NSString *fullpath = [NSString stringWithFormat:@"%@/%@/%@",[DataStore pathForSharedDocuments], archive, file];
							// just stash  the contents of this for anyone who wants it
							NSError *error=nil;
							NSStringEncoding encoding;
							//	NSLog (@"reading header %@", fullpath);
							NSString *headerdata = [NSString stringWithContentsOfFile: fullpath usedEncoding:&encoding error: &error ];
							if (error)
							{
								NSLog (@"archiveheader path %@  error %@",fullpath,  [error localizedDescription]);
							}
							else 
							{
								if([headerdata length]>10) // ensure some heft
									
									[ArchivesManager insertArchiveHeader:archive  headerHTML:headerdata forType:filetype];
							}
						}
						else 	if ([@"--info--" isEqualToString:longtitle])
						{
							// remember all of these 
							NSString *xfullpath = [NSString stringWithFormat:@"%@/%@/%@",[DataStore pathForSharedDocuments], archive, file];
							NSError *xerror=nil;
							NSStringEncoding xencoding;
							//	NSLog (@"reading header %@", fullpath);
							NSString *infopagedata = [NSString stringWithContentsOfFile: xfullpath usedEncoding:&xencoding error: &xerror ];
							if (xerror)
							{
								NSLog (@"archiveinfopage path %@  error %@",xfullpath, [xerror localizedDescription]);
								
							}
							else {
								ai.provenanceHTML = infopagedata;
								if  ([infopagedata length]>10) // ensure some heft
									ai.provenanceHTML = infopagedata;
							}
						}
						else 
						{
							// a normal file, just gets added
							[ArchivesManager parseAndAdd:pathtoremember title:longtitle];
							filesadded++;	
							if ((filesadded/50)*50==filesadded)
							[[GigStandAppDelegate sharedInstance] saveContext:[NSString stringWithFormat:@"Small Batch Assimilation at %d",filesadded]];
                            
						}
					
				}	
			}
		}
	}	
	
	ai.fileCount = [NSNumber numberWithUnsignedInt:filesadded];
	ai.size = [NSNumber numberWithUnsignedLongLong:size];
	[[DataManager sharedInstance].progressString text:[NSString stringWithFormat:@"%@ with %d files size %lld",archive,filesadded, size]];
	
	return filesadded; 
	
}

#pragma mark database initialization and recovery
+(void) setupDB;
{
	
	[TunesManager insertGigBaseInfo]; // push initial values in there 
	ArchiveInfo *ai = [ArchivesManager insertArchiveUnique: [ArchivesManager nameForOnTheFlyArchive]];
	NSString *logopath = [NSString stringWithFormat:@"%@/%@",[ArchivesManager nameForOnTheFlyArchive], @"--logo--.jpg"];
	// just stash the filespec for anyone who wants it// assume no logo	
    
	ai.logo = logopath;	
	ai.provenanceHTML = @"";
    
	[TunesManager insertTuneUnique: @"READ ME PLEASE" lastArchive:[ArchivesManager nameForOnTheFlyArchive] lastFilePath:@"readme.html"];		
	[TunesManager insertInstanceUnique: @"READ ME PLEASE"  archive:[ArchivesManager nameForOnTheFlyArchive] filePath:@"readme.html" ];	
	
	[SetListsManager insertListUnique:@"favorites"];
	[SetListsManager insertListUnique:@"recents"];
      [ArchivesManager bumpFileCount:[ArchivesManager nameForOnTheFlyArchive]];	
	[[GigStandAppDelegate sharedInstance] saveContext:@"setupDB"];
	
}
+(void)setupFS
{
	// the primary directory is created in the appdelegate as part of booting up the sqlite db
	
	NSString *otfDir = [DataStore pathForOnTheFlyArchive];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:otfDir])
	{
		NSLog (@"Creating %@ directory",otfDir);
		[[NSFileManager defaultManager] createDirectoryAtPath:[DataStore pathForOnTheFlyArchive] withIntermediateDirectories:NO attributes:nil error:nil];
		if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForOnTheFlyArchive]])
			NSLog (@"Could not create %@ directory",otfDir);
		else
		{
            NSLog (@"Created %@ directory",otfDir);
            

            NSString *readmesrc = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"readme.html"];
			NSString *readmedest = [[DataStore pathForOnTheFlyArchive] stringByAppendingPathComponent:@"readme.html"];
			NSString *logosrc = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"onthefly.jpg"] ;
			NSString *logodest = [[DataStore pathForOnTheFlyArchive] stringByAppendingPathComponent:@"--logo--.jpg"];
            
            NSData *readmebytes = [[NSFileManager defaultManager] contentsAtPath:readmesrc ];
            
            [[NSFileManager defaultManager] createFileAtPath:readmedest contents:readmebytes attributes:nil];
            if (![[NSFileManager defaultManager] fileExistsAtPath:readmedest])
                NSLog (@"Could not create dest readme at %@",readmedest);
            
            NSData *logobytes = [[NSFileManager defaultManager ] contentsAtPath:logosrc];
            
            [[NSFileManager defaultManager] createFileAtPath:logodest contents:logobytes attributes:nil];
            if (![[NSFileManager defaultManager] fileExistsAtPath:logodest])
                NSLog (@"Could not create dest logo at %@",logodest);

			
		}
	}
	else NSLog (@"Not Creating %@ directory",otfDir);
	
	NSString *tp = [DataStore pathForThumbnails];
	if (![[NSFileManager defaultManager] fileExistsAtPath:tp]) {
		
		NSLog (@"Creating %@ directory",tp);
		[[NSFileManager defaultManager] createDirectoryAtPath:tp
								  withIntermediateDirectories:NO attributes:nil error:nil];
		if (![[NSFileManager defaultManager] fileExistsAtPath:tp])
			NSLog (@"Could not create %@ directory",tp);
		
	}
	else  NSLog (@"Not Creating %@ directory",tp);
	
}

+(void) buildNewDB;

{	

	
	NSLog (@"setupFS commencing..."); 
	[ArchivesManager setupFS];	
	NSLog (@"setupFS finished...");
	
	NSLog (@"setupDB commencing...");
	[ArchivesManager setupDB]; 
	NSLog (@"setupDB finished...");
	

}

+(unsigned long long) totalFileSystemSize;
{
	unsigned long long totalsize = 0;
	
	for (ArchiveInfo *ai in [ArchivesManager allArchivesObjs	])
	{	
		totalsize += [ai.size unsignedLongLongValue];		
	}	
	return totalsize;
	
}

+ (void) dump;
{
	
	NSLog (@"$$$$$$$$$$ dumping archive entries $$$$$$");
	NSArray *aia = [ArchivesManager allArchivesObjs];
	for (ArchiveInfo *ai in aia)
		NSLog (@"$$$$$$$$$$$$ name %@ logo %@ enabled %d rc %d",ai.archive,ai.logo,[ai.enabled boolValue],[ai retainCount]);
}

@end
