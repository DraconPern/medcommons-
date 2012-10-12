//
//  DataManager.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "DataManager.h"
#import "SettingsManager.h"
#import "DataStore.h"
#import "TitleNode.h"
#import "ZipArchive.h"
#import "Product.h"


#pragma mark -
#pragma mark Public Class DataManager
#pragma mark -

@implementation DataManager
@synthesize imageCache;
@synthesize fileSpaceTotal				= fileSpaceTotal_;

@synthesize totalFiles 				= totalFiles_;

@synthesize uniqueTunes				= uniqueTunes_;

@synthesize titlesDictionary;
@synthesize titleNodesGroupedByFirstLetter;
@synthesize allTitles;
@synthesize appColor;


//@synthesize recentItems;



//@synthesize collation;
@synthesize alphabetIndex;

@synthesize archives;
@synthesize archivelogos;

@synthesize archiveheaders;



#pragma mark UIAlertViewDelegate Methods

- (void) alertView: (UIAlertView *) avx
clickedButtonAtIndex: (NSInteger) idx
{
	
	// the simple dialogs used in here don't need to do anything conditional	
	[avx dismissWithClickedButtonIndex:0 animated:YES];
	
}

-(NSString *) allocInfoProgressString;
{
	
	NSUInteger zipcount=[DataManager zipcountItunesInbox];
	NSUInteger setlistcount=0;

	if (self->displayIncomingInfo)
		return [[NSString alloc] initWithFormat:@"now processing: %@",self->iname];
	if (self->progressString)
		return [[NSString alloc] initWithString:self->progressString];
	else return [[NSString alloc] initWithFormat:@"need to process:  %d archives and %d setlists",zipcount,setlistcount];
}
-(void) setProgressString:(NSString *)s;
{
	if (self->progressString) [self->progressString release];
	self->progressString = [[NSString alloc] initWithString:s];
}
-(void) showIncomingAsProgressString;
{
	if (self->progressString) [self->progressString release];
	self->progressString = nil;
}

- (void) dismissZipExpansionWaitIndicators;
{
	if (self->zipalert)
	{
		[self->zipalert dismissWithClickedButtonIndex:0 animated:YES];;
		[self->zipalert release];
		self->zipalert = nil;
		[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
	}
	
}
#pragma mark plist save and restore for favorites, recents, setlists 

-(NSUInteger)  itemCountForList:(NSString * ) path ; 
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

-(NSMutableArray *) allocLoadRefNodeItems: (NSString * ) path ; 
{
	
	NSMutableArray *sec = [[NSMutableArray alloc] init];
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath;
	
	plistPath = [DataStore pathForTuneListWithName:path] ;//stringByAppendingPathComponent:path];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"allocLoadRefNodeItems No plist today for %@:=(", plistPath);
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
	//	NSString *version = [temp objectForKey:@"version"];
	//	NSString *originalList = [temp objectForKey:@"listname"];
	//	NSLog (@"***** Processing list %@ version %@",originalList,version);
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	
	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		RefNode *rn = [[[RefNode alloc] initWithTitle:[titlesx objectAtIndex:n]  andWithArchive:[archivesx objectAtIndex:n] ] autorelease];
		
		[sec addObject:rn];
	}
	
	return sec;
}
-(NSMutableArray *) allocLoadRefNodeItemsFromString: (NSString * ) contents  ; 
{
	
	NSMutableArray *sec = [[NSMutableArray alloc] init];
	
	NSString *errorDesc = nil;
	
	NSPropertyListFormat format;
	
	//	NSLog(@"allocLoadRefNodeItemsFromString");
	NSData* plistXML=[contents dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
	if (!plistXML)
	{
		NSLog (@"cant convert set list items on %@", plistXML);
		return sec;
	}
	//	NSLog (@"did convert data is %@", plistXML);
	
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist from string: %@, format: %d", errorDesc, format);
		return sec;
	}
	// check to make sure these exists
	
	NSString *version = [temp objectForKey:@"version"];
	NSString *originalList = [temp objectForKey:@"listname"];
	NSLog (@"***** Processing incoming attachment list %@ version %@",originalList,version);
	
	if  ([temp objectForKey:@"titles"] && [temp objectForKey:@"archives"])
	{
		NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
		
		NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
		
		for (NSUInteger n=0; n<[titlesx count]; n++)
		{
			
			RefNode *rn = [[[RefNode alloc] initWithTitle:[titlesx objectAtIndex:n]  andWithArchive:[archivesx objectAtIndex:n] ] autorelease];
			
			[sec addObject:rn];
		}
	}
	
	return sec;
}


-(BOOL )readArchivesInfo
{
	// all done for side affect
	NSLog(@"Read archivesinfo");
	
	[[DataManager sharedInstance].archives removeAllObjects];
	
	[[DataManager sharedInstance].archivelogos removeAllObjects];
	
	[[DataManager sharedInstance].archiveheaders removeAllObjects];
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath;
	
	plistPath =  [DataStore pathForDBListWithName:@"archives"];
	
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"No archives found");
		return NO;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
		return NO;
	}
	
	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archivename"] ];
	
	NSMutableArray *logosx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archivelogo"]];
	
	NSMutableArray *headersx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archiveheaders"]];
	
	
	for (NSUInteger n=0; n<[archivesx count]; n++)
		
		if (![@"plists" isEqualToString: [archivesx objectAtIndex:n]])
			
		{
			[[DataManager sharedInstance].archives addObject:[archivesx objectAtIndex:n]];
			
			[[DataManager sharedInstance].archivelogos addObject:[logosx objectAtIndex:n]];
			
			[[DataManager sharedInstance].archiveheaders addObject:[headersx objectAtIndex:n]];
		}
	
	return YES;
	
}

//
//-(NSMutableArray *) allocReadFavorites
//{
//	return [self allocLoadRefNodeItems:@"favorites"];
//}


	

+(void) writeRefNodeItems:(NSArray *)items  toPropertyList:(NSString *)plistname;
{
	
	NSMutableArray *puta =[[[NSMutableArray alloc] init] autorelease];
	NSMutableArray *putb =[[[NSMutableArray alloc] init] autorelease];
	
	for (RefNode *rn in items)
	{
		[puta addObject:rn.title];
		[putb addObject:rn.archive];
		
	}
	NSString *error;
    NSString *plistPath;
	
	plistPath = [ DataStore pathForTuneListWithName :plistname];
	
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects: plistname, PLIST_VERSION_NUMBER, puta, putb,nil]
														  forKeys:[NSArray arrayWithObjects: 
																   @"listname",
																   @"version",
																   @"titles", 
																   @"archives",
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
+(void) writeRefNodeItems:(NSArray *)items  toDBList:(NSString *)dblistname;
{	
	NSMutableArray *puta =[[[NSMutableArray alloc] init] autorelease];
	NSMutableArray *putb =[[[NSMutableArray alloc] init] autorelease];
	
	for (RefNode *rn in items)
	{
		[puta addObject:rn.title];
		[putb addObject:rn.archive];
		
	}
	NSString *error;
    NSString *plistPath;
	
	plistPath = [ DataStore pathForDBListWithName :dblistname];
	
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects: dblistname, PLIST_VERSION_NUMBER, puta, putb,nil]
														  forKeys:[NSArray arrayWithObjects: 
																   @"listname",
																   @"version",
																   @"titles", 
																   @"archives",
																   nil]];
	
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
													  format:NSPropertyListXMLFormat_v1_0
														errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
	}
    else {
        NSLog(@"error %@", error);
        [error release];
    }
	
}
- (void)newSetListFromFile:(NSString *)path name:(NSString *)plistname;
{    
	// this is used to import a setlist from an email attachment
	
	NSStringEncoding encoding;
    NSError *err = nil;
	NSURL *URL = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@.%@",[DataStore pathForTuneLists],plistname,@"stl"]]; //careful here
    NSString *setlistbody = [NSString stringWithContentsOfURL :URL usedEncoding:&encoding error:&err];
    if (err) {
		NSLog (@"Couldnt import attachment %@ to plist %@ error %@",URL,plistname,err);
    	if (av) [av release];
		av = [[UIAlertView alloc] initWithTitle:@"There was a problem with this Import"
										message:@"no setlist was created"
									   delegate: nil
							  cancelButtonTitle: @"OK"
							  otherButtonTitles: nil] 
		;
		[av show];
		return;
    }
	
	if ([setlistbody length]>30) // minimal size for setlist?
	{
		// try to parse
		
		NSMutableArray *a = [[[DataManager sharedInstance] allocLoadRefNodeItemsFromString: setlistbody] autorelease]; // was complaining of leak on early leaving
		if ([a count] >0)
		{
			
			
			
			[DataManager writeRefNodeItems:a  toPropertyList:plistname];  // if we got some that's ok, otherwise put up an error
			NSLog (@"newSetListURL:%@ name:%@ count: %d",URL,plistname,[a count]);
			
			
			if (av) [av release];
			av = [[UIAlertView alloc] initWithTitle:@"A new setlist was created from the email"
											message:[NSString stringWithFormat:@"%@ has %d tunes",plistname,[a count]]
										   delegate: nil
								  cancelButtonTitle: @"OK"
								  otherButtonTitles: nil] 
			;
			[av show];
			return;
		}
		
	}
	
	// something is generically wrong here
	av = [[UIAlertView alloc] initWithTitle:@"There was a problem with this setlist"
									message:@"no setlist was created"
								   delegate: nil
						  cancelButtonTitle: @"OK"
						  otherButtonTitles: nil] 
	;
	[av show];
	
}

-(NSMutableArray *) allocReadRecents; // gets saved
{
	return [self allocLoadRefNodeItems:@"recents"];
}

-(void) updateRecents:(id)t;
{
	NSMutableArray *recents = [self allocReadRecents];
	[recents insertObject:t atIndex:0];
	
	if ([recents count] > 100) 
		[recents removeLastObject];
	[DataManager writeRefNodeItems:recents	toPropertyList:@"recents"];
	[recents release]; 
	
	
}

+(void) writeArchivesInfo
{
	// archives have their own formats
	NSMutableArray *puta =[[[NSMutableArray alloc] init] autorelease];
	NSMutableArray *putb =[[[NSMutableArray alloc] init] autorelease];
	
	NSMutableArray *putc =[[[NSMutableArray alloc] init] autorelease];
	
	
	for (NSUInteger index = 0; index < [[DataManager sharedInstance].archives count]; index++) 
	{
		
		
		{
			[puta addObject:[[DataManager sharedInstance].archives objectAtIndex:index]];
			[putb addObject:[[DataManager sharedInstance].archivelogos objectAtIndex:index]];
			
			[putc addObject:[[DataManager sharedInstance].archiveheaders objectAtIndex:index]];
		}
		
	}
	NSString *error;
	NSString *plistPath;
	
	plistPath =  [DataStore pathForDBListWithName:@"archives"];
	
	
	NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects:  @"archives", PLIST_VERSION_NUMBER,puta, putb,putc,nil]
														  forKeys:[NSArray arrayWithObjects: 
																   @"listname",
																   @"version",
																   @"archivename", 
																   @"archivelogo",
																   @"archiveheaders",
																   nil]];
	
	NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
																   format:NSPropertyListXMLFormat_v1_0
														 errorDescription:&error];
	if(plistData) {
		[plistData writeToFile:plistPath atomically:YES];
		
		NSLog (@"rewrote archives %@ size %d",[[plistPath  stringByDeletingPathExtension] lastPathComponent]  ,  [puta count]);
	}
	else {
		NSLog(@"error %@", error);
		[error release];
	}
	
}
+(void) writeAllTunes
{
	NSLog(@"writeAllTunes commencing");
	// list these all out explicitly so they are compatible with favorites,recents, etc
	NSMutableArray *puta =[[[NSMutableArray alloc] init] autorelease];
	NSMutableArray *putb =[[[NSMutableArray alloc] init] autorelease];	
	NSMutableArray *putc =[[[NSMutableArray alloc] init] autorelease];
	
	for (NSUInteger index = 0; index < [[DataManager sharedInstance].titleNodesGroupedByFirstLetter count]; index++) 
	{
		
		NSMutableArray *titlenodes = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:index];
		
		for (TitleNode *tn in titlenodes)
		{
			for (NSString *variant in tn.variants)
			{
				[puta addObject:tn.title];
				[putb addObject:variant];
				[putc addObject:[NSNumber numberWithInt:tn.lastvariantsegmentindex]];

			}
		}
	}
	NSString *error;
	NSString *plistPath;	
	plistPath =  [DataStore pathForDBListWithName:@"alltunes"];
	
	NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects:
								@"alltunes",
								PLIST_VERSION_NUMBER,
								puta, 
								putb,
								putc,
								nil]
														  forKeys:[NSArray arrayWithObjects:
																   @"listname",
																   @"version",
																   @"titles", 
																   @"archives",
																   
																   @"lastchosen",
																   nil]];
	NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
																   format:NSPropertyListXMLFormat_v1_0
														 errorDescription:&error];
	if(plistData) {
		[plistData writeToFile:plistPath atomically:YES];
		
		NSLog (@"rewrote alltunes size %d", [puta count]);
	}
	else {
		NSLog(@"error writing alltunes %@", error);
		[error release];
	}
	
	
}
-(void) sourceNil
{
}

-(NSString *) noHungarian:(NSString *)htitle
{
	const char *foo = [htitle UTF8String];
	char obuf[1000];
	NSUInteger opos = 0;
	NSUInteger len = strlen(foo);
	if (len>1000) len=1000;
	char tab = '\t';
	BOOL spaces = NO;
	BOOL firstx = YES; // 
	for (NSUInteger index = 0; index<len; index++)
	{
		char o = foo[index];
		if ((o==' ')||(o==tab)) // space or tab? just note it
		{	
			spaces = YES;
		}
		else 
		{
			
			// anything else gets copied
			if (YES==spaces)
			{	
				
				obuf[opos]=' '; // insert a space
				opos++;
				
			}
			else //if (NO==spaces) // can get changed in above conditional
			{
				// not coming off string of spaces
				if ((o>='A')&&(o<='Z')) // make each cap generate a new word
				{	
					if(NO==firstx)
					{
						obuf[opos]=' '; // insert a space
						opos++;
					}
				}
				firstx = NO;
				
			}
			
			spaces = NO; // not in a space anymore
			// in all cases if not a space then copy it over
			obuf[opos]=o;
			opos++;
			
		}
		
	}
	obuf[opos]='\0';
	// squeeze out beginning ending and extra blanks
	//	
	NSString* newStringq = [[NSString stringWithUTF8String:obuf] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] ;
	
	//						   stringByReplacingOccurrencesOfString:@"	" withString:@" "];// replace tabs with spaces
	//	NSString* newString = [newStringq
	//												stringByReplacingOccurrencesOfString:@"  " withString:@" "];// replace two strings with one
	//
	//NSLog (@"hungarian returns %@",newStringq);
	return newStringq;
}

-(void) addSourceWasNil
{
}
//-(NSInteger) readTitleSections
//{
//	NSLog(@"Read title sections");
//	NSInteger counter = 0;
//	
//	
//	NSString *errorDesc = nil;
//	NSPropertyListFormat format;
//	
//    NSString *plistPath;
//	
//	
//	plistPath =  [DataStore pathForDBListWithName:@"alltunes"];
//	
//	
//	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
//		NSLog(@"No alltunes plist today:=(");
//		return -1;
//	}
//	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
//	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
//										  propertyListFromData:plistXML
//										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
//										  format:&format
//										  errorDescription:&errorDesc];
//	if (!temp) {
//		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);//plists
//		return -1;
//	}
//	
//	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
//	
//	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
//	//NSUInteger fileloaderrors,filesduplicated,titlesadded;
//	//	fileloaderrors = filesduplicated = titlesadded = 0;
//	//	
//	
//	for (NSUInteger n=0; n<[titlesx count]; n++)
//	{
//		NSString *title = [titlesx objectAtIndex:n] ;
//		
//		NSString *archive = [archivesx objectAtIndex:n] ;
//		
//		// put this in the correct bucket in the sections array of arrays
//		NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[title substringToIndex:1]].location;
//		if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;
//		// Get the array for the section.
//		NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
//		//  Add the label to the sion.;
//		TitleNode *tn =  ([[DataManager sharedInstance].titlesDictionary objectForKey:title]);
//		if (tn)
//		{
//			// already exists, just add as a new segment at the end
//			NSUInteger segmentindex = [tn.variants count];
//			if (NO==[tn addSourceDocument:archive segment:segmentindex])
//			{
//				[self addSourceWasNil];
//				
//			}
//			
//		}
//		else
//		{
//			// doesnt exist, create the a new title node and add to the sections
//			tn = [[[TitleNode alloc ] initWithTitle:title  ] autorelease];
//			if (NO==[tn addSourceDocument:archive segment:0 ])
//			{
//				[self addSourceWasNil];
//				
//			}
//			
//			[sectionAlphaLetter addObject:tn];
//			[[DataManager sharedInstance].titlesDictionary setObject:tn forKey:title]; // put new titles into dictionary during re-read of plist
//			
//		}
//		counter++;
//	}
//	return counter;
//}
-(NSInteger) readAllTunes
{
	//NSLog(@"Read title sections");
	NSInteger counter = 0;
	
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath;
	
	
	plistPath =  [DataStore pathForDBListWithName:@"alltunes"];
	
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"No alltunes plist today:=(");
		return -1;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);//plists
		return -1;
	}
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	
	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
	
	
	NSMutableArray *lastchosenx = [NSMutableArray arrayWithArray:[temp objectForKey:@"lastchosen"]];
	
	//NSUInteger fileloaderrors,filesduplicated,titlesadded;
	//	fileloaderrors = filesduplicated = titlesadded = 0;
	//	
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		NSString *title = [titlesx objectAtIndex:n] ;
		
		NSString *archive = [archivesx objectAtIndex:n] ;
		
		NSNumber *lastchosen = [lastchosenx objectAtIndex:n];
		
		// put this in the correct bucket in the sections array of arrays
		NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[title substringToIndex:1]].location;
		if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;
		// Get the array for the section.
		NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
		//  Add the label to the sion.;
		TitleNode *tn =  ([[DataManager sharedInstance].titlesDictionary objectForKey:title]);
		if (tn)
		{
			// already exists, just add as a new segment at the end
			NSUInteger segmentindex = [tn.variants count];
			if (NO==[tn addSourceDocument:archive segment:segmentindex resetLast:NO])
			{
				[self addSourceWasNil];
				
			}
			
		}
		else
		{
			// doesnt exist, create the a new title node and add to the sections
			tn = [[[TitleNode alloc ] initWithTitle:title  ] autorelease];
			if (NO==[tn addSourceDocument:archive segment:[lastchosen intValue ] resetLast:YES])// set this in
			{
				[self addSourceWasNil];
				
			}
			[sectionAlphaLetter addObject:tn];
			[self->titlesDictionary setObject:tn forKey:title]; // put new titles into dictionary during re-read of plist			
		}
		counter++;
	}
	return counter;
}
#pragma mark  Once Only Startup Code for Building In Memory Stuctures


-(void) convertDirectoryToArchive:(NSString *) str;
{
	
	[[DataManager sharedInstance].archives addObject:str]; 
	[[DataManager sharedInstance].archivelogos addObject:@""]; // assume no logo
	NSMutableDictionary *foo = [[[NSMutableDictionary alloc] init] autorelease] ; //***********deleak
	[[DataManager sharedInstance].archiveheaders addObject:foo]; // assume no logo
	
	NSString *file;
	//	NSString *name;
	NSString *bigpath;
	NSUInteger filesadded = 0;
	unsigned long long size=0;
	
	bigpath = [DataStore pathForExplodedZipFilesForKey: str];
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: bigpath];
	
	// segregate the files
	while ((file = [dirEnum nextObject]))
	{
		NSArray *parts = [file componentsSeparatedByString:@"/"];
		// skip these stinky MCOS directories that show up when jpgs are present
		
		NSDictionary *attrs = [dirEnum fileAttributes];
		//    	NSString *bigpathfile = [NSString stringWithFormat:@"%@/%@",bigpath, file];
		NSString *pathtoremember = [NSString stringWithFormat:@"%@/%@",str, file];
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
				
				size += [fs longLongValue];
				NSString *longtitle   = [[file stringByDeletingPathExtension] lastPathComponent]; 
				// if it's a special file then handle that
				if ([@"--logo--" isEqualToString:longtitle])
				{
					// only remember the last of these
					NSString *logopath = [NSString stringWithFormat:@"%@/%@",str, file];
					// just stash the filespec for anyone who wants it
					[[DataManager sharedInstance].archivelogos removeLastObject];
					[[DataManager sharedInstance].archivelogos addObject:logopath]; // assume no logo
					
				}
				else 
					if ([@"--header--" isEqualToString:longtitle])
					{
						// remember all of these 
						NSString *filetype = [file pathExtension];
						NSString *fullpath = [NSString stringWithFormat:@"%@/%@/%@",[DataStore pathForSharedDocuments], str, file];
						// just stash  the contents of this for anyone who wants it
						NSMutableDictionary *mdict = [[DataManager sharedInstance].archiveheaders  lastObject]; // what we just added 
						NSError *error;
						NSStringEncoding encoding;
						//	NSLog (@"reading header %@", fullpath);
						NSString *headerdata = [NSString stringWithContentsOfFile: fullpath usedEncoding:&encoding error: &error ];
						//	NSLog (@"header data %@",headerdata);
						[mdict setObject:headerdata forKey:filetype]; // assume no logo
					}
					else 
					{
						NSArray *arr;
						NSArray *far = [[[DataManager sharedInstance].archives lastObject] componentsSeparatedByString:@"-custom-"];
						if ([far count]==1) {
							// if the title has a '-' strip all that
							arr  = [longtitle componentsSeparatedByString:@"-"];
						}
						else 
						{
							arr = [NSArray arrayWithObject: longtitle];			
						}
						
						NSString *htitle = (NSString *)[arr objectAtIndex:0]; // just take first component
						// expand hungarian
						NSString *title =[self noHungarian:htitle];
						// put this in the correct bucket in the sections array of arrays
						NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[title substringToIndex:1]].location;
						if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;
						// Get the array for the section.
						NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
						//  Add the label to the sion.;
						TitleNode *tn =  ([[DataManager sharedInstance].titlesDictionary objectForKey:title]);
						if (tn)
						{
							// already exists, just add as a new segment at the end
							NSUInteger segmentindex = [tn.variants count];
							if (NO==[tn addSourceDocument:pathtoremember segment:segmentindex resetLast:NO])
							{
								[self sourceNil];
							}
						}
						else
						{
							// doesnt exist, create the a new title node and add to the sections
							tn = [[[TitleNode alloc ] initWithTitle:title  ] autorelease];
							if (NO==[tn addSourceDocument:pathtoremember segment:0  resetLast:YES])
							{
								[self sourceNil];
							}
							
							[sectionAlphaLetter addObject:tn];
							
							[self->titlesDictionary  setObject:tn forKey:title]; // put new titles into dictionary during re-read of plist
							
						}
						filesadded++;
					}
			}
		}	
	}
	
	//NSLog (@" rebuild: converted directory %@ to archive with %d files ",str,filesadded);
	NSUInteger i = [[DataManager sharedInstance].totalFiles unsignedIntValue];
	unsigned long long j = [[DataManager sharedInstance].fileSpaceTotal unsignedLongLongValue];
	
	
	[DataManager sharedInstance].totalFiles = [NSNumber numberWithUnsignedInt: i+filesadded];
	
	[DataManager sharedInstance].fileSpaceTotal = [NSNumber numberWithUnsignedLongLong: j+size];
	
}
+(BOOL) archiveExists: (NSString *)file
{
	for (NSUInteger index = 0; index< [[DataManager sharedInstance].archives count]; index++)
	{
		if ([file isEqualToString:[[DataManager sharedInstance].archives objectAtIndex:index]]) return YES;
	}
	return NO;
}


-(void) restoreArchives
{
	
	[DataManager sharedInstance].archives = [[NSMutableArray alloc] init];
	[DataManager sharedInstance].archivelogos = [[NSMutableArray alloc] init];
	[DataManager sharedInstance].archiveheaders = [[NSMutableArray alloc] init];		
	
	[DataManager writeRefNodeItems:[NSArray array]  toDBList:@"alltunes" ];  // empty plist is fine			
	[DataManager writeRefNodeItems:[NSArray array]  toDBList:@"archives" ];  // empty plist is fine
//	[DataManager writeRefNodeItems:[NSArray array]  toPropertyList:@"recents" ];   // empty plist is fine
//	[DataManager writeRefNodeItems:[NSArray array]  toPropertyList:@"favorites" ]; // empty plist is fine
//	
	
	
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForSharedDocuments]  error: NULL];
	if (paths){
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForSharedDocuments] 
																							   stringByAppendingPathComponent: path]
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeDirectory])
				{
					
					NSString *archive = path; //[parts objectAtIndex:0]; // behaving wierdly with macosx component
					//		NSLog(@"Evaluating archive %@ ",archive);
					if (!  [@"_" isEqualToString:[archive substringToIndex:1]] )
						// skip these stinky MCOS directories that show up when jpgs are present
						// directory gets expanded, everything else is skipped
					{
						
						
						
						if ([DataManager archiveExists:archive]==NO)
							[self convertDirectoryToArchive:archive] ;// hope this works
						
						
						//else NSLog(@"Not processing %@ due to locked settings",archive);
					}
					//else NSLog (@"Skipping plist folder during scan");
				}
			}
	}
	// write out the adjusted archive information and the alltunes db
	
	[DataManager writeAllTunes]; // unsorted and unkempt
	[DataManager writeArchivesInfo];
	
	
	return ;
	// END SLOW SCANNING WITH A NEWLY WRITTEN AllTunes.Plist and archivesplist FOR NEXT TIME
}

-(void) buildNewDB;

{	
	
	// make directories for tucking things away only if we need them
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForSharedDocuments]]) {

	NSLog (@"Creating Documents/DONOTDISTURB directory");
	[[NSFileManager defaultManager] createDirectoryAtPath:[DataStore pathForSharedDocuments] withIntermediateDirectories:NO attributes:nil error:nil];
	if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForSharedDocuments]])
		NSLog (@"Could not create Documents/DONOTDISTURB directory");
		
	}
	
	// unconditionally overwrite the _db files each time
	
	[[NSFileManager defaultManager] removeItemAtPath:[DataStore pathForDBLists]  error:nil];
	NSLog (@"Re-Creating Documents/DONOTDISTURB/_db directory");
	[[NSFileManager defaultManager] createDirectoryAtPath:[DataStore pathForDBLists] withIntermediateDirectories:NO attributes:nil error:nil];
	if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForDBLists]])
		NSLog (@"Could not create Documents/DONOTDISTURB/_db directory");
	
	
	//leave the lists alone since these express user preferences
	if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForTuneLists]]) {
		
	NSLog (@"Creating Documents/DONOTDISTURB/_plists directory");
	[[NSFileManager defaultManager] createDirectoryAtPath:[DataStore pathForTuneLists] withIntermediateDirectories:NO attributes:nil error:nil];
	if (![[NSFileManager defaultManager] fileExistsAtPath:[DataStore pathForTuneLists]])
		NSLog (@"Could not create Documents/DONOTDISTURB/_plists directory");
		
	}
	
	
	NSLog (@"buildNewDB started");
	[self restoreArchives];
	
	NSLog (@"buildNewDB archives restored");

}

+(void) initTitleNodesByFirstLetter
{
	
	NSUInteger sectionTitlesCount = 27;//[[[DataManager sharedInstance].collation  sectionTitles] count];
	
	NSMutableArray *newtitleNodesGroupedByFirstLetter = [[[NSMutableArray alloc] initWithCapacity:sectionTitlesCount] autorelease];
	
	// Set up the sections array: elements are mutable arrays that will contain the titles for that section.
	for (NSUInteger index = 0; index < sectionTitlesCount; index++) {
		NSMutableArray *array = [[NSMutableArray alloc] init];
		[newtitleNodesGroupedByFirstLetter addObject:array];
		[array release];
	}
	
	[DataManager sharedInstance].titleNodesGroupedByFirstLetter =  newtitleNodesGroupedByFirstLetter;  // now initialized
}
+(void) finishDBSetup;
{
	
	// Now that all the data's in place, each section array needs to be sorted.
	
	UILocalizedIndexedCollation *collation = [UILocalizedIndexedCollation currentCollation] ;
	NSUInteger sectionTitlesCount = [[collation  sectionTitles] count];
	
	for (NSUInteger index = 0; index < sectionTitlesCount; index++)
	{
		NSMutableArray *titlenodes = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:index];
		///////////////  IT IS THE getName Selector which prevents this from moving out of the same text file that TitleNode is defined in /////////
		NSArray *na = [collation sortedArrayFromArray:titlenodes collationStringSelector:@selector(title)];
		// Replace the existing array with the sorted array.
		[[DataManager sharedInstance].titleNodesGroupedByFirstLetter replaceObjectAtIndex:index withObject:na];
		for (TitleNode  *tn in na)
		{
			Product *p = [Product allocProductWithType:@"Chords" name: tn.title];
			[[DataManager sharedInstance].allTitles addObject: p];
			[p release];
		}
		
	}
	
	
	// finally lets announce how many unique titles we have added
	
	[DataManager sharedInstance].uniqueTunes = [NSNumber numberWithUnsignedInt:[[DataManager sharedInstance].titlesDictionary count]];
	
	NSLog (@"buildNewDB finished in finishDBSetup");
}
+(NSInteger) recoverDB;
{
	// SLOW FILE SCANNING UNECESSARY IF WE HAVE THE AllTunes.Plist
	
	BOOL haveArchive = [[DataManager sharedInstance] readArchivesInfo]; 
	if (haveArchive == YES)
	{
		NSInteger count = [[DataManager sharedInstance].titlesDictionary count];//[[DataManager sharedInstance] readTitleSections];
		NSLog(@"buildDB restored from %d files",count);
		[DataManager sharedInstance].totalFiles = [NSNumber numberWithInt:  count];
		return count;

	}
	return 0;
}
+(void) onceOnlyMasterIndexInitialization;
{
	
	NSLog (@"onceOnlyMasterIndexInitialization");
	
	// START MEMORY BASED INITIALIZATION
	//
	
	
	[DataManager initTitleNodesByFirstLetter];
	[DataManager sharedInstance].allTitles = [[NSMutableArray alloc] init]; // this is the structure used by SearchContreoller
	[DataManager sharedInstance].titlesDictionary = [[NSMutableDictionary alloc] init]; // this is the structure used by SearchContreoller
	[DataManager sharedInstance].archives = [[NSMutableArray alloc] init];	
	[DataManager sharedInstance].archivelogos =[[NSMutableArray alloc] init];	
	[DataManager sharedInstance].archiveheaders =[[NSMutableArray alloc] init];
	
	// END MEMORY BASED INITIALIZATION
	

	
}

+(void) cleanUpOldDB;
{
	if 	([DataManager sharedInstance].titlesDictionary)
	{
		
		
		[[DataManager sharedInstance].titlesDictionary removeAllObjects];
		
		[[DataManager sharedInstance].allTitles removeAllObjects];
		
		[[DataManager sharedInstance].allTitles removeAllObjects];
		
		[[DataManager sharedInstance].archives removeAllObjects];
		
		[[DataManager sharedInstance].archivelogos removeAllObjects];
		
		[[DataManager sharedInstance].archiveheaders removeAllObjects];
		
		[DataManager initTitleNodesByFirstLetter];
		
		
	    [DataManager sharedInstance].uniqueTunes = [NSNumber numberWithUnsignedInt:0];
		[DataManager sharedInstance].totalFiles = [NSNumber numberWithUnsignedInt:0];
		[DataManager sharedInstance].fileSpaceTotal = [NSNumber numberWithUnsignedLongLong :0];
		
		
	}
}




-(void) factoryReset;
{
	[DataManager cleanUpOldDB];
	[DataManager deleteAllArchives];
	first = YES; // get things going from scratch again
}

+(NSUInteger) zipcountItunesInbox;
{
	//BOOL any = NO;
	NSUInteger zipcount=0;
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForItunesInbox]  error: NULL];
	if (paths){
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path]
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
				{
					// if it's a zip file then unpack it
					NSString *zip = @".zip";
					NSRange range = [[[DataStore pathForItunesInbox] stringByAppendingPathComponent: path] rangeOfString:zip
																												 options:(NSCaseInsensitiveSearch)];
					if (range.location != NSNotFound)
					{
						//NSLog (@"Processing new zip %@", [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path], attrs);
						NSString *doctags = @"/Documents/";
						NSRange range2 = [[[DataStore pathForItunesInbox] stringByAppendingPathComponent: path] rangeOfString:doctags
																													  options:(NSCaseInsensitiveSearch)];
						if (range2.location != NSNotFound)
						{
							zipcount++;
						}
					}
				}
			}
	}
	return zipcount;
}
-(void) doOne;// :(NSString *)incoming iName:(NSString *)iname;
{
	ZipArchive *za = [[ZipArchive alloc] init];
	if ([za UnzipOpenFile: self->incoming]) {
		BOOL ret = [za UnzipFileTo: [DataStore pathForExplodedZipFilesForKey:self->iname] overWrite: YES];
		if (ret)
		{
			[za UnzipCloseFile];
			
		}
		// delete the zip file once we have folders
		[[NSFileManager defaultManager] removeItemAtPath:incoming  error:NULL];
		NSLog (@" removing file fullpath %@", incoming);
		
	}
	[za release];
	[self->incoming release];
	[self->iname release];
	
//	[self dismissZipExpansionWaitIndicators];
	self->displayIncomingInfo = NO;
}
-(BOOL) processIncomingFromItunes;
{
	//BOOL any = NO;
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForItunesInbox]  error: NULL];
	if (paths){
		//NSMutableArray *docs = [NSMutableArray arrayWithCapacity: [paths count]];
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path]
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
				{
					// if it's a zip file then unpack it
					NSString *zip = @".zip";
					NSRange range = [[[DataStore pathForItunesInbox] stringByAppendingPathComponent: path] rangeOfString:zip
																												 options:(NSCaseInsensitiveSearch)];
					if (range.location != NSNotFound)
					{
						//NSLog (@"Processing new zip %@", [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path], attrs);
						NSString *doctags = @"/Documents/";
						NSRange range2 = [[[DataStore pathForItunesInbox] stringByAppendingPathComponent: path] rangeOfString:doctags
																													  options:(NSCaseInsensitiveSearch)];
						if (range2.location != NSNotFound)
						{
							//[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
//							
//							zipalert = [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"Importing %@\nPlease Wait",
//																		   path]
//																  message:nil delegate:nil 
//														cancelButtonTitle:(NSString *)nil 
//														otherButtonTitles:(NSString *)nil] ;
//							[zipalert show];
//							UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
//							
//							// Adjust the indicator so it is up a few pixels from the bottom of the alert
//							indicator.center = CGPointMake(zipalert.bounds.size.width / 2 +90 , zipalert.bounds.size.height /2 +70);
//							[indicator startAnimating];
//							[zipalert addSubview:indicator];
//							[indicator release];
							
							// ok do one file
							int len = range.location - range2.location - 11;
							NSRange xrange= NSMakeRange(range2.location+11,len);
							
							// preserve these values into the timer-completion 
							self->incoming = [[NSString alloc] initWithString:[[DataStore pathForItunesInbox] stringByAppendingPathComponent: path]];
							self->iname = [[NSString alloc] initWithString:[incoming substringWithRange:xrange]];
					
							//[self doOne];
							
							self->displayIncomingInfo = YES;
							
							[self performSelector:@selector(doOne) withObject:nil afterDelay:.001f];

							return YES;
							
						}
					}
					
					else 
					{   // not a zip file - dont even consider it now
						//[docs addObject: [MCDocument documentWithPath: [self.iTunesInboxPath stringByAppendingPathComponent: path] attributes: attrs]];
					}
				}// not a regular file (in this case a zip file or something else)
				
			}// not .DS_store and for loop
	} // have some paths
	return NO;
}


+(void) deleteAllArchives
{
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath: [DataStore pathForSharedDocuments] error: NULL];
	if (paths){
		for (NSString *path in paths)
		{ NSLog (@"Evaluating %@",path);
			if (![path isEqualToString:@".DS_Store"])
			{
				if (![path isEqualToString:@"_plists"])
				{
				[[NSFileManager defaultManager] removeItemAtPath:[[DataStore pathForSharedDocuments] stringByAppendingPathComponent: path] error:NULL];
				NSLog (@" removing file fullpath %@", [[DataStore pathForSharedDocuments] stringByAppendingPathComponent: path]);
				}
				
				
			}// not .DS_store and for loop
		}
	} // have some paths
}
#pragma mark Public Class Methods

- (DataManager *) init
{
	self = [super init];
	if (self) {
		imageCache_ = nil; // just quiet down the analyzer if we can ASK JGP
		first = YES;
		alphabetIndex = @"ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
		
		appColor = [UIColor colorWithRed:(CGFloat).467f green:(CGFloat).125f blue:(CGFloat).129 alpha:(CGFloat)1.0f];	
		
		titlesDictionary =  nil;
		allTitles = nil;
		uniqueTunes_ = [NSNumber numberWithUnsignedInt:0];
		totalFiles_ = [NSNumber numberWithUnsignedInt:0];
		fileSpaceTotal_ = [NSNumber numberWithUnsignedLongLong :0];
		self->displayIncomingInfo = NO;
	}
	return self;
	
}


+ (DataManager *) sharedInstance
{
	static DataManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[DataManager alloc] init];
	}
	
	return SharedInstance;
}


+(NSString *) reWind: (NSString * ) s;
{
	NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[s substringToIndex:1]].location;
	if ((sectionNumber<0) || (sectionNumber>26)) sectionNumber = 26;
	while(YES)
	{ // assert there is always one entry in the table and eventually will be found
		if (sectionNumber == 0) sectionNumber = 26;
		else sectionNumber--;
		
		// Get the array for the section.
		NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
		if ([sectionAlphaLetter count] > 0) {
			TitleNode *tn = [sectionAlphaLetter objectAtIndex:0];	
			return tn.title;
		}
	}
}
+(NSString *) fastForward: (NSString * ) s;
{
	NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[s substringToIndex:1]].location;
	if ((sectionNumber<0) || (sectionNumber>26))sectionNumber = 26;
	while(YES)
	{ // assert there is always one entry in the table and eventually will be found
		if (sectionNumber == 26) sectionNumber = 0;
		else sectionNumber++;
		// Get the array for the section.
		NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
		if ([sectionAlphaLetter count] > 0) {
			TitleNode *tn = [sectionAlphaLetter objectAtIndex:0];	
			return tn.title;
		}
	}
}

+(NSString *) goBack: (NSString * ) s;
{
	NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[s substringToIndex:1]].location;
	if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;	
	// Get the array for the section.
	NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
	NSMutableString *previousTitle =(NSMutableString *)(( (TitleNode *)[sectionAlphaLetter lastObject]).title) ;
	for (TitleNode *tn in sectionAlphaLetter)
	{
		if ([tn.title isEqualToString:s])
		{
			// found our spot, back it up
			return previousTitle;
		}
		else {
			previousTitle = (NSMutableString*) tn.title;
		}
		
		
	}
	return @"bad goBack";
}


+(NSString *) goForward: (NSString * ) s;
{
	NSInteger sectionNumber = [[DataManager sharedInstance].alphabetIndex rangeOfString:[s substringToIndex:1]].location;
	if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;	
	// Get the array for the section.
	NSMutableArray *sectionAlphaLetter = [[DataManager sharedInstance].titleNodesGroupedByFirstLetter objectAtIndex:sectionNumber];
	NSMutableString *previousTitle =(NSMutableString *)(( (TitleNode *)[sectionAlphaLetter lastObject]).title) ;
	for (TitleNode *tn in sectionAlphaLetter)
	{
		if ([previousTitle isEqualToString:s])
		{
			// found our spot, back it up
			return tn.title;
		}
		else {
			previousTitle = (NSMutableString*) tn.title;
		}
		
		
	}
	return @"bad goForward";
}



+(NSString *) shortNameFromArchiveName: (NSString * ) s
{
	NSArray *arr = [s componentsSeparatedByString:@"-"];
	NSString *ss = (NSString *)[arr objectAtIndex:0]; // just take first component
	if ([ss length]<=8) return ss;
	
	return	[ss substringToIndex:8];
	
}
+(NSString *) shortNameFromArchiveIndex: (NSUInteger ) s
{
	NSString *s2 = [[DataManager sharedInstance].archives objectAtIndex:s];
	return [self shortNameFromArchiveName:s2];
	
}
+(NSString *) archiveNameFromPath:(NSString *)path
{
	
	NSArray *arr = [path componentsSeparatedByString:@"/"];
	return (NSString *)[arr objectAtIndex:0]; // just take first component
}	

+(NSUInteger) indexFromArchiveName: (NSString *) archive
{
	NSArray *arr = [archive componentsSeparatedByString:@"/"];
	NSString *ss = (NSString *)[arr objectAtIndex:0]; // just take first component
	
	for (NSUInteger i=0; i<[[DataManager sharedInstance].archives count]; i++)
	{
		NSString *testing = [[DataManager sharedInstance].archives objectAtIndex:i];
		//	NSLog (@"testing %@ against archive %@",testing,ss);
		if ([testing isEqualToString:ss]) return i;
	}
	return [[DataManager sharedInstance].archives count]; // will raise oob
}

+(NSArray *) allocItemsFromArchive: (NSString *)archive;
{
	// returns array of producs, just like [DataManager sharedInstance].allTitles
	NSMutableArray *puta = [[NSMutableArray alloc] init] ;
	
	for (Product *aproduct in [DataManager sharedInstance].allTitles)
	{
		TitleNode	*tn = [[DataManager sharedInstance].titlesDictionary objectForKey: aproduct.name];
		
		for (NSString *variant in tn.variants)
		{
			if ([[DataManager archiveNameFromPath:variant] isEqualToString: archive])
				[puta addObject:aproduct];
			
		}		
	}
	
	return puta;
}
@end

