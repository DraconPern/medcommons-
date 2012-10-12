//
//  LogManager.m
//  GigStand
//
//  Created by bill donner on 3/17/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "LogManager.h"
#import "DataManager.h"
#define LOG_FILES 2

@implementation LogManager
@synthesize dummy,logfilespecs;

-(id) init
{
	self = [super init];
	if (self)
	{	
		if ([DataManager sharedInstance].inSim == NO)
		{
		// get these just right
		logfilespecs = [[NSMutableArray alloc] initWithCapacity:LOG_FILES];
		NSString *path1 = [NSString stringWithFormat:@"%@/tmp/stderr%d.txt",NSHomeDirectory(),0];
		NSString *path2 = [NSString stringWithFormat:@"%@/tmp/stderr%d.txt",NSHomeDirectory(),1];
		NSDate* fileDate1;
		NSDictionary* attr1 = [[NSFileManager defaultManager] attributesOfItemAtPath:path1 error:NULL];
			fileDate1 = (NSDate*)[attr1 objectForKey:NSFileModificationDate];
			if( !fileDate1 ) 
			{
				fileDate1 = [NSDate distantPast];
			}
		NSDate* fileDate2;
		NSDictionary* attr2 = [[NSFileManager defaultManager] attributesOfItemAtPath:path2 error:NULL];
		fileDate2 = (NSDate*)[attr2 objectForKey:NSFileModificationDate];
		if( !fileDate2 ) 
		{
			fileDate2 = [NSDate distantPast];
		}
		
		if ([fileDate1 laterDate:fileDate2])
		{ 
			// path1 is oldest
			NSLog (@"%@ with date %@ has later date than %@ with date %@", 
				                                   path1,fileDate1,path2,fileDate2);
			[logfilespecs addObject:path2];
			[logfilespecs addObject:path1];
		}
		else 
		{
			//path 2 is oldests			
			NSLog (@"%@ with date %@ has earlier date than %@ with date %@", 
				                                    path1,fileDate1,path2,fileDate2);
			[logfilespecs addObject:path1];
			[logfilespecs addObject:path2];			
		}
		
		NSLog (@"Logs are init as %@",logfilespecs);
		
		NSString *temp = [logfilespecs lastObject];
		
		freopen([temp fileSystemRepresentation], "a+", stderr); // clear log
		}
	}
	return self;
}


+(void) setup
{
	[LogManager sharedInstance].dummy = 1;
}
+ (LogManager *) sharedInstance;
{
	static LogManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[LogManager alloc] init];		
	}	
	return SharedInstance;
}
+(NSString *) pathForCurrentLog
{
	if ([DataManager sharedInstance].inSim == NO)
	return [[LogManager sharedInstance].logfilespecs objectAtIndex:0];
	else return nil;
}
+(NSString *) pathForPreviousLog
{
	if ([DataManager sharedInstance].inSim == NO)
	return [[LogManager sharedInstance].logfilespecs objectAtIndex:1]; //  this is the 2nd to last
	else return nil;
}
+(void) clearCurrentLog
{
	if ([DataManager sharedInstance].inSim == NO)
	[LogManager rotateLogs]; else
		return;
	//freopen([[LogManager pathForCurrentLog] fileSystemRepresentation], "w+", stderr); // clear log
}
+(void) rotateLogs
{
	//NSArray *old = [NSArray arrayWithArray:[LogManager sharedInstance].logfilespecs];
		if ([DataManager sharedInstance].inSim == NO)
		{
	NSString *temp = [[LogManager sharedInstance].logfilespecs lastObject];	
	freopen([temp fileSystemRepresentation], "a+", stderr); // clear log
	[[LogManager sharedInstance].logfilespecs insertObject:temp atIndex:0];
	[[LogManager sharedInstance].logfilespecs removeLastObject];
		}
	//NSLog (@"Logs were %@ rotated to %@",old,[LogManager sharedInstance].logfilespecs);
}
@end
