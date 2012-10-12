//
//  SetListsManager.m

//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//
#import "SettingsManager.h"
#import "DataManager.h"
#import "SetListsManager.h"
#import "DataStore.h"
#import "GigStandAppDelegate.h"
#import "ListItemInfo.h"
#import "ListInfo.h"

#pragma mark -
#pragma mark Public Class SetListsManager
#pragma mark -
@interface SetListsManager ()

+(ListInfo *) findList:(NSString *)listName;
@end
@implementation SetListsManager
@synthesize dummy;
+(void) setup
{
	[SetListsManager sharedInstance].dummy = 1;
}
+ (SetListsManager *) sharedInstance;
{
	static SetListsManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[SetListsManager alloc] init];
		
	}
	
	return SharedInstance;
}

#pragma mark Public Class Methods

#pragma mark Function or Subroutine Based Methods


+(NSMutableArray *) makeSetlistsScan
{	
	// Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager makeSetlistsScan"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"makeSetlistsScan error %@",error);
		return nil;
	}
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity: [fetchedObjects count]];
	for (ListInfo *li in fetchedObjects)
		[putb	addObject:li.listName];
		[putb sortUsingSelector:@selector(compare:)];
	return [putb autorelease]; // 042511 instruments made me do it
}
+(NSMutableArray *) makeSetlistsScanNoRecents;
{

	
	// Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager makeSetlistsScanNoRecents"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"makeSetlistsScanNoRecents error %@",error);
		return nil;
	}
	NSMutableArray *putb = [[NSMutableArray alloc] initWithCapacity: [fetchedObjects count]];
	for (ListInfo *li in fetchedObjects)
		if (![li.listName isEqualToString:@"recents"])
			[putb	addObject:li.listName];
	
	[putb sortUsingSelector:@selector(compare:)];
	return [putb autorelease]; // 042511 instruments made me do it
}



#pragma mark plist save and restore for favorites, recents, setlists 

+(ListItemInfo *) findListItem:(NSString *)title onList:(NSString *)listName;
{
	
	// Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager findListItem"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListItemInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ && title == %@ ",listName,title ];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findListItem error %@",error);
		return nil;
	}
	if ([fetchedObjects count]>0) 
		return (ListItemInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
	
}
+(NSUInteger)  itemCountForList:(NSString * ) listName  ; 
{
	
	// with Core Data
	
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager itemCountForList"];
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListItemInfo" inManagedObjectContext:context];
	[fetchRequest setEntity:entity];
	
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ ",listName ];
	[fetchRequest setPredicate:somePredicate];
	
	NSUInteger count = [context countForFetchRequest:fetchRequest error:&error];
	//[somePredicate release]; //042511 was now zombie 042311 instruments says leaking
	[fetchRequest release];
	/////////////
	////////////
	
	return count;
}

+(NSMutableArray *) listOfTunes: (NSString * ) listName  ; 
{

	
	// Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager listOfTunes"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListItemInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ ",listName ];
	[fetchRequest setPredicate:somePredicate];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]
                                        initWithKey:@"insertTime" ascending:YES];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    [sortDescriptor release];
    
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"listOfTunes error %@",error);
		return nil;
	}
	
	NSMutableArray *putb  = [[NSMutableArray alloc] initWithCapacity:[fetchedObjects count]];
	
	for (ListItemInfo *lii in fetchedObjects)
	{
		[putb addObject:lii.title];
	}
	return [putb autorelease];
}

+(NSString *) parseLineToTile: (NSString *) aline
{
    // returns nil if it doesn't parse
    // strip comment lines
NSArray* components = 
    [aline componentsSeparatedByString:@"##"];
  //  NSLog (@"aline %@ components %@", aline,components);
    NSString *first = [components objectAtIndex:0]; 
    if ([first length]==0) return nil;
    // now strip optional prefix line number
    NSArray *comp2 = [first componentsSeparatedByString:@"."];
    NSString *rest = ( [comp2 count] == 1) ?
    [[comp2 objectAtIndex:0] copy]:
    [[comp2 objectAtIndex:1] copy];
    NSArray *comp3 = [rest componentsSeparatedByString:@"-"];
    NSString *rest2 = [comp3 objectAtIndex:0];
    
    
    NSString *stripped = [rest2  stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    [rest release];
    return stripped; 
}

+(NSMutableArray *)listOfTunesFromFile: (NSString *) filePath;
{
    // read everything from text
    NSString* fileContents = 
    [NSString stringWithContentsOfFile:filePath 
                              encoding:NSUTF8StringEncoding error:nil];
    
    // first, separate by new line
    NSArray* allLinedStrings = 
    [fileContents componentsSeparatedByCharactersInSet:
     [NSCharacterSet newlineCharacterSet]];
    
    NSMutableArray *putb = [[NSMutableArray alloc] init ];
    //NSUInteger linecount = 0;
    
    for (NSString *aline in allLinedStrings)
    {
        NSString *oneline = [SetListsManager parseLineToTile:aline];
        if (oneline) {
//        NSLog (@"line %-3d %@", ++linecount, oneline);
        [putb addObject: oneline];
        }
    }
    return [putb autorelease];
}

+(ListItemInfo *) insertListItem:(NSString *)tune onList:(NSString *)listName top:(BOOL)onTop;
{
	
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager insertListItem"];
	ListItemInfo  *lii = [NSEntityDescription
						  insertNewObjectForEntityForName:@"ListItemInfo" 
						  inManagedObjectContext:context];
	
	lii.title = tune;
	lii.listName = listName;
	lii.insertTime = [NSDate date];
	return lii;
	
	
}
+(BOOL) removeOldestOnList:(NSString *)listName ;
{
    ListItemInfo *thislii = nil;
    NSDate *thisdate = [NSDate distantFuture];
    // Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager removeOldestOnList"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListItemInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ ",listName ];
	[fetchRequest setPredicate:somePredicate];
    
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]
                                        initWithKey:@"insertTime" ascending:YES];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    [sortDescriptor release];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"removeOldestOnList error %@",error);
		return NO;
	}
    for (ListItemInfo *lii in fetchedObjects)
        if ([lii.insertTime compare :  thisdate] < 0 )
        {
            thisdate = lii.insertTime;
            thislii = lii;
        }
    // ok delete this
    if (thislii == nil) return NO;
    
   // should remove loop above and replace with just this
   // thislii = fetchedObjects [0];
    
    [context deleteObject:thislii];
    
	[[GigStandAppDelegate sharedInstance] saveContext:@"removeOldestOnList"];
    return YES;
   
}


+(void) updateTune:(NSString *)tune after: (NSString *) existing list: (NSString *) list;
{
  //  NSLog (@"updateTune %@ after %@ on list %@", tune,existing,list);
    ListItemInfo *liiafter = [SetListsManager findListItem:existing onList:list];
    NSDate *d = liiafter.insertTime;
    NSDate *newdate = [d dateByAddingTimeInterval:(NSTimeInterval)1];
    ListItemInfo *lii = [SetListsManager findListItem:tune onList:list];
    lii.insertTime = newdate; 
    
	[[GigStandAppDelegate sharedInstance] saveContext:@"updateTune after"];
}
+(void) updateTune:(NSString *)tune before: (NSString *) existing list: (NSString *) list;
{
  //  NSLog (@"updateTune %@ before %@ on list %@", tune,existing,list);
    ListItemInfo *liibefore = [SetListsManager findListItem:existing onList:list];
    NSDate *d = liibefore.insertTime;
    NSDate *newdate = [d dateByAddingTimeInterval:(NSTimeInterval)-1];
    ListItemInfo *lii = [SetListsManager findListItem:tune onList:list];
    lii.insertTime = newdate; 
    
	[[GigStandAppDelegate sharedInstance] saveContext:@"updateTune before"];
}

+(ListItemInfo *) insertListItemUnique:(NSString *)tune onList:(NSString *)listName top:(BOOL)onTop;
{
	ListItemInfo *lii = [SetListsManager findListItem:tune onList:listName];
	if (!lii) 
	{
		// if the tune wasnt found
		lii =  [SetListsManager insertListItem:tune onList: listName top:onTop];
	}
	return lii;
}
//[SetListsManager removeTune:tune list:self->name];
+(BOOL) removeTune:(NSString *) tune list:(NSString *) list;
{
    NSLog (@"removeTune %@ from list %@", tune,list);
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager removeTune"];

    
    ListItemInfo *lii = [SetListsManager findListItem:tune onList:list];
    if (lii==nil) return NO;
    [context deleteObject: lii];
    
	
	[[GigStandAppDelegate sharedInstance] saveContext:@"removeTune list"];
    return YES;
    
}
+(ListInfo *) insertList:(NSString *)list;
{
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager insertListItemUnique"];
	ListInfo  *li = [NSEntityDescription
					 insertNewObjectForEntityForName:@"ListInfo" 
					 inManagedObjectContext:context];
	
	li.listName = list;
	li.creationTime = [NSDate date];
	return li;
	
	
}
+(ListInfo *) findList:(NSString *)listName;
{
	
	// Do it with Core Data
	NSError *error;
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager findList"];
	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ ",listName];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	if (fetchedObjects == nil)
	{
		if (error) 
			NSLog (@"findList error %@",error);
		return nil;
	}
	if ([fetchedObjects count]>0) 
		return (ListInfo *)[fetchedObjects objectAtIndex:0];
	
	return nil;
	
}
+(ListInfo *) insertListUnique:(NSString *)list;
{
	ListInfo *li = [SetListsManager findList:list];
	if (!li) 
	{
		// if the list wasnt found
		li =  [SetListsManager insertList:list];
	}
	return li;
}

+(BOOL) deleteList:(NSString *)listName;
{
	NSManagedObjectContext *context =[[GigStandAppDelegate sharedInstance] managedObjectContext:@"SetListsManager deleteList"];
	
	ListInfo *li = [SetListsManager findList:listName];
	if (li==nil) return NO;
	
	
	
	// delete with CASCADE
	
	
	NSError *error;	NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
	NSEntityDescription *entity = [NSEntityDescription 
								   entityForName:@"ListItemInfo" inManagedObjectContext:context];
	
	[fetchRequest setEntity:entity];
	NSPredicate *somePredicate = [NSPredicate predicateWithFormat:@" listName == %@ ",listName ];
	[fetchRequest setPredicate:somePredicate];
	
	NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
	
	for  (ListItemInfo *lii in fetchedObjects)
		[context deleteObject:lii];
	
	
	
	[context deleteObject:li];
	return YES;
}
+(NSString *) picSpecForList:(NSString *) listName;
{
	return @"setlistpic.jpg";
}
	
+(void) updateRecents:(NSString *)newtune;
{	
	// Do it with Core Data Instead
	ListItemInfo *lii = [SetListsManager findListItem:newtune onList:@"recents"];
	if (lii==nil)
	{
		[SetListsManager insertListItemUnique:newtune 
								 onList:@"recents" top:YES];
	}
	
	return;
	
} 
//[SetListsManager makeSetList: self->iname items:theseitems];
+(void) makeSetList :(NSString *)list items:(NSArray *) items;
{
    
    [SetListsManager insertListUnique :list ];  // first shovel the list in there
    for (NSString *s in items)
    {
        [SetListsManager insertListItemUnique:s onList:list top:NO];
    }
    
}

@end

