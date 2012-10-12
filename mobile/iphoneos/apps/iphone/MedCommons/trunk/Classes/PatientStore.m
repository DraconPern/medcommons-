//
//  PatientStore.m
//  ForensicFoto
//
//  Created by bill donner on 9/12/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//
//

//*******
//
// Uniform Storage Interface for all media and documents which include at this point:
//
// Subject Photos, Body Part Photos, and Videos//
//
// Photos are stored in the application's filesystem space under /tmp under the control of this class
//
// Videos are automagically stored in various places on the phone and we just point to them from inside this class
//
// For every video a normal photo as a thumbnail is also stored normally in /Documents
//
//***********
#import "PatientStore.h"
#import "HistoryCase.h"
#import "MedCommons.h"
#import "DataManager.h"
#import "GPSDevice.h"

@implementation PatientStore


@synthesize videospecs;
@synthesize photospecs;
@synthesize attrdicts;
@synthesize prefs;

-(void) dealloc
{
	[videospecs release];
	[photospecs release];
	[attrdicts release];
	[prefs	release];
	[super dealloc];
}

-(BOOL) readPatientStore // gets saved
{
	
			NSString *errorDesc = nil;
			NSPropertyListFormat format;
	
    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
			if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
				//plistPath = [[NSBundle mainBundle] pathForResource:@"Data" ofType:@"plist"];
				NSLog(@"No plist for this patient %@",mcid);
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
			}

			self.photospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"photospecs"]] retain];
	
	self.videospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"videospecs"]] retain];
	
	self.attrdicts = [[NSMutableArray arrayWithArray:[temp objectForKey:@"attrdicts"]] retain];
	
	self.prefs = [[NSMutableDictionary dictionaryWithDictionary:[temp objectForKey:@"prefs"]] retain];
	
	return YES;
}

-(void) writePatientStore // writes back 
{
		NSLog (@"write patient store for %@", mcid);
	
	//[NSKeyedArchiver archiveRootObject:self toFile:PERSONPATH]; // write entire tree in one go
	
	NSString *error;
  //  NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
							   [NSArray arrayWithObjects: photospecs,videospecs,attrdicts,prefs, nil]
														  forKeys:[NSArray arrayWithObjects: @"photospecs", @"videospecs", @"attrdicts", @"prefs" ,nil]];
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
#pragma mark remoteurlcache
//-(UIImage *) getImageFromRemoteURL:(NSString *)url
//{
//	//NSLog (@"requesting:%@",url);
//	UIImage *image = [imageCache objectForKey:url];
//	if (!image) 
//	{
//		// get image added to the cache
//	NSURLRequest *urlrequest = [NSURLRequest requestWithURL:[NSURL URLWithString:url]];
//	NSError *error;
//	NSURLResponse *urlresponse;
//	NSData *data = [NSURLConnection sendSynchronousRequest:urlrequest returningResponse:&urlresponse error:&error];
//	if (data) image = [UIImage imageWithData:data];	//??
//	else NSLog (@"Could not fetch data from  %@ %@ %@", url,error, urlresponse);
//		
//	[imageCache setObject:image forKey:url];
//		NSLog (@"--added remoteURL:%@",url);
//	return image;
//	}
//		NSLog (@"--found remoteURL:%@",url);
//	return image;
//}


#pragma mark Current case 
-(BOOL) isEmptySpec :(NSString *) spec
{
	return ([@"" isEqualToString:spec]);
}

- (int) countPartPics
{
	int numpics =0; 
	
	for (int i=1; i<kHowManySlots; i++)
	{
		if(![self isEmptySpec:[photospecs objectAtIndex:i]]) 	
			++numpics;	
	}	
	return numpics;
}
- (int) countVideos
{
	int numpics =0; 
	
	for (int i=1; i<kHowManySlots; i++)
	{
		if(![self isEmptySpec:[videospecs objectAtIndex:i]]) 	
			++numpics;	
	}	
	return numpics;
}
-(void) dumpPatientStore
{
	for (int j=0; j<kHowManySlots; j++)
	{
		
		if(![self isEmptySpec:[photospecs objectAtIndex:j]]) 
			NSLog (@"part photo %d:  %@",j,[photospecs objectAtIndex:j]);		
		if(![self isEmptySpec:[videospecs objectAtIndex:j]]) 
			NSLog (@"video %d:  %@",j,[videospecs objectAtIndex:j]);
		if([attrdicts objectAtIndex:j]) 
			NSLog (@"attrs %d:  %@",j,[attrdicts objectAtIndex:j]);
		
	}
	
}
-(void) swapPhotoPart:(int)j withPart:(int)k
{
	id temp = [photospecs objectAtIndex:k+1];
	id temv = [videospecs objectAtIndex:k+1];
	id tema = [attrdicts objectAtIndex:k+1];
	
	[photospecs replaceObjectAtIndex:k+1 withObject:[photospecs objectAtIndex:j+1]];
	[photospecs replaceObjectAtIndex:j+1 withObject:temp];
	
	[videospecs replaceObjectAtIndex:k+1 withObject:[videospecs objectAtIndex:j+1]];
	[videospecs replaceObjectAtIndex:j+1 withObject:temv];
	
	[attrdicts replaceObjectAtIndex:k+1 withObject:[attrdicts objectAtIndex:j+1]];
	[attrdicts replaceObjectAtIndex:j+1 withObject:tema];
	
}
-(void) swapSubjectPhotoWithPart: (int)j
{
	id temp = [photospecs objectAtIndex:0];
	[photospecs replaceObjectAtIndex:0 withObject:[photospecs objectAtIndex:j+1]];
	[photospecs replaceObjectAtIndex:j+1 withObject:temp];
}
-(void) trashPhotoPart:(int)j
{
	NSError *error;
	[[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																										[photospecs objectAtIndex:j+1]]]	error:&error];
	[photospecs   removeObjectAtIndex:	j+1  ];
	[photospecs   addObject:@""   ];
	
	[[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																										[videospecs objectAtIndex:j+1]]]	error:&error];
	[videospecs   removeObjectAtIndex:	j +1 ];
	[videospecs   addObject:@""   ];
	
	[attrdicts   removeObjectAtIndex:	j +1 ];
	[attrdicts addObject:[[NSDictionary alloc]init]];
	

}
-(void) trashSubjectPhoto
{
	NSError *error;
	[[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																										[photospecs objectAtIndex:0]]]	error:&error];
	

	
	
	[photospecs replaceObjectAtIndex:0 withObject:[NSString stringWithString:@""]];	
	[attrdicts   removeObjectAtIndex:	0];
}


-(PatientStore *) initWithMcid:(NSString *)_mcid
{
	
		self = [super init];
		mcid = _mcid;

	// create it afresh
	prefs = [[NSMutableDictionary alloc] init];
	


	//
	// now read whatever we have
	//
	
	NSLog (@"reading patient store for %@", mcid);
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"No plist for this patient %@",mcid);
		// ok, no plist, so lets just initialize
		photospecs = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone photos
		for (int j=0; j<kHowManySlots; j++) 
		{
			[photospecs addObject:@""]; // set to nothing for now, will overwrite
			
		}
		
		videospecs = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone videos
		for (int j=0; j<kHowManySlots; j++) 
		{
			[videospecs addObject:@""]; // set to nothing for now, will overwrite
			
		}
		
		attrdicts = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone videos
		for (int j=0; j<kHowManySlots; j++) 
		{
			[attrdicts addObject:[[NSMutableDictionary alloc] init]] ; // set to nothing for now, will overwrite
			
		}
		
		
		for (int j=0; j<kHowManySlots; j++) 
		{
			// make sure all of the files exist and cleanup if the system has removed them since the last time we ran
			if (![@"" isEqualToString:[photospecs objectAtIndex:j]])
			{
				if (![[NSFileManager defaultManager] fileExistsAtPath:
					  [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																  [photospecs objectAtIndex:j]]]])
				{
					[photospecs   replaceObjectAtIndex:	j withObject:@""   ];
					[videospecs   replaceObjectAtIndex:	j withObject:@""   ];
				}
			}
		}
			// end of no existing plist
	}
	else 
	{
		// we have the plist, lets read it and fix things up
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
	}
	
	self.photospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"photospecs"]] retain];
	
	self.videospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"videospecs"]] retain];
	
	self.attrdicts = [[NSMutableArray arrayWithArray:[temp objectForKey:@"attrdicts"]] retain];
	
	self.prefs = [[NSMutableDictionary dictionaryWithDictionary:[temp objectForKey:@"prefs"]] retain];
		
		NSLog(@"Did not restore",nil);
	
	}
	// OK at this point we are ok either way 
		
	return self;	
}

-(BOOL) haveSubjectPhoto
{
	return (![self isEmptySpec:[photospecs objectAtIndex:0]]);
	//return ([[NSFileManager defaultManager] fileExistsAtPath:FIRSTIMAGEPATH]) ;
}

-(void) setSubjectPhotoSpec: (NSString *) url withPhotoAttrs: (NSDictionary *) attrs
{
	GPSDevice *gps =[[DataManager sharedInstance] ffGPSDevice];
	
	// this is where the last gps reading gets saved with the pictures attributes
	[photospecs  replaceObjectAtIndex:0 withObject:[url copy]]; // make a copy
	NSMutableDictionary *dict = [attrdicts objectAtIndex:0];
	
	//NSLog(@"--- %d PatientStore setSubjectPhotoSpec on slot %d ",[[attrdicts objectAtIndex:0] retainCount], 0);
	[dict addEntriesFromDictionary: attrs];	
	[dict setObject:gps.lastMeasuredVerticalAccuracy forKey:@"vertical-accuracy"];	
	[dict setObject:gps.lastMeasuredHorizontalAccuracy forKey:@"horizontal-accuracy"];	
	[dict setObject:gps.lastMeasuredLatitude forKey:@"latitude"];	
	[dict setObject:gps.lastMeasuredLongitude	forKey:@"longitude"];
	//NSLog(@"After setting subject photo dumping image store");
	//[self dumpPatientStore];
}


- (NSString *) subjectPhotoSpec
{
	return [photospecs objectAtIndex:0];
}


-(NSString *) photoSpecAtIndex: (int) i
{
	return [photospecs objectAtIndex:i+1];
}

-(NSString *) videoSpecAtIndex: (int) i
{
	return [videospecs objectAtIndex:i+1];
}


- (NSString *) fullSubjectPhotoSpec
{
	NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[photospecs objectAtIndex:0]];
	//NSLog (@"subject photo is %@",plistPath);
	return plistPath;
}


-(NSString *) fullPhotoSpecAtIndex: (int) i
{
	NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[photospecs objectAtIndex:i+1]];
	//	NSLog (@"parts photo is %@",plistPath);
	return plistPath;
}
-(NSString *) fullVideoSpecAtIndex: (int) i
{
	
    NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[videospecs objectAtIndex:i+1]];
	return plistPath;
}



-(BOOL) havePhotoAtIndex: (int )i
{
	return (![self isEmptySpec: [photospecs objectAtIndex:i+1]]);
}
-(void) setPhotoSpec: (NSString *) url atIndex: (int) i withPhotoAttrs: (NSDictionary *) attrs
{
	//
	// this is where we will absorb the gps info and att to the attrs, it is right at the point the picture is taken
	//
	NSLog (@"writing %@ to index %d",url,i);
	
	[photospecs  replaceObjectAtIndex:	i+1 withObject:url ];	
	NSMutableDictionary *dict = [attrdicts objectAtIndex:i+1];
	
	////NSLog(@"--- %d PatientStore setPhotoSpec on slot %d ",[[attrdicts objectAtIndex:i+1] retainCount], i+1);
	[dict addEntriesFromDictionary: attrs];	
	
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore];// snap it to disk

}
-(void) addAttrs:(NSDictionary *) attrs atIndex: (int) slot
{
	NSMutableDictionary *dict = [attrdicts objectAtIndex:slot];
	
	////NSLog(@"--- %d PatientStore addAttrs on slot %d ",[[attrdicts objectAtIndex:slot] retainCount], slot);
	[dict addEntriesFromDictionary: attrs];
}
-(NSDictionary *) attrsAtIndex: (int) slot
{
	NSDictionary *dict = [attrdicts objectAtIndex:slot]; // return it as read only
	return dict;

}
-(BOOL) haveVideoAtIndex: (int)i
{
	return (![self isEmptySpec: [videospecs objectAtIndex:i+1]]);
}
-(void) setVideoSpec: (NSString *) url atIndex: (int) i withVideoAttrs: (NSDictionary *) attrs
{
	[videospecs  replaceObjectAtIndex:	i+1 withObject:url ];

	[[[DataManager sharedInstance] ffPatientStore].prefs setObject:videospecs forKey:@"videospecs"];
	
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore];// snap it to disk
	NSMutableDictionary *dict = [attrdicts objectAtIndex:i+1];
	
	NSLog(@"--- %d PatientStore setVideoSpec on slot %d ",[[attrdicts objectAtIndex:i+1] retainCount], i+1);
	[dict addEntriesFromDictionary: attrs];
	//NSLog(@"After setting video dumping image store");
	//[//self dumpPatientStore];
}

-(NSString *)  newSubjectPhotoSpec
{	
		NSString *ss = [[[DataManager sharedInstance] ffNextFileIndex] bump];
	
	NSString *x =  [NSString stringWithFormat:@"Documents/%@-%@.%@",  BASE_PATH, ss, BASE_TYPE,nil ] ;	
	CAM_LOG(@"------------New subject photo spec is %@",ss);
	return x;
}
-(NSString *)  findFreePatientStorePath
{
	NSString *ss = [[[DataManager sharedInstance] ffNextFileIndex] bump];
	
		NSString *x =  [NSString stringWithFormat:@"Documents/%@-%@.%@", BASE_PATH, ss, BASE_TYPE,nil ] ;
	    CAM_LOG(@"------------New body part spec is %@",ss);
		return x;
	
}

-(int)  nextFreeIndex
{
	// back up the return value by one so when it is passed back into the store function it moves to the right spot -- yuck
	for (int j=1; j<kHowManySlots; j++)
		if([self isEmptySpec:[photospecs objectAtIndex:j]])
		{ /////NSLog (@"nextFreeIndex returns", j-1);
			return j-1;
		}
			
			
	return -1;
}



-(void) cleanup
{
	//********************* LOOP THRU Files ***********************

	NSError *error;
	
	for (int i = 0; i<kHowManySlots; i++)
	{ //do this agressively
		if (![[NSFileManager defaultManager] fileExistsAtPath:
			  [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																 [photospecs objectAtIndex:i]]]])
			[[NSFileManager defaultManager] removeItemAtPath:
			  [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
																 [photospecs objectAtIndex:i]]] error:&error] ;
		[photospecs   replaceObjectAtIndex:	i withObject:@""   ];
		// clean out the video references, should we delete these files?
	    [videospecs   replaceObjectAtIndex:	i withObject:@""   ];
		[attrdicts	   replaceObjectAtIndex: i  withObject: [[NSMutableDictionary alloc] init]]; // probably leaking
	}
	//*********************Remove ***********************
		[[[DataManager sharedInstance] ffPatientStore] writePatientStore];// snap it to disk
	
}
//#pragma mark testing
//-(void) loadTestCase
//{
//	[customViews customLoadTestCase:self]; 
//
//	
//	
//	
//	//********************* Copy Files from Resources into Files System ***********************
//	int i = 0;
//	//NSError *error;
//	NSData *png = (NSData *) UIImagePNGRepresentation([UIImage imageNamed:@"mcImage.png"]);
//	[png  writeToFile: FIRSTIMAGEPATH atomically:YES ];
//
//		[photospecs   replaceObjectAtIndex:	i++ withObject:FIRSTIMAGEPATH   ];
//	
//	NSString* uniquePath = [NSString stringWithFormat:@"%@/%@-%d.%@", DOCSFOLDER,BASE_PATH, 0, BASE_TYPE]; // get the next possible file
//	while ( i<=6 ) { //do this agressively
//		NSData *png = (NSData *) UIImagePNGRepresentation([UIImage imageNamed:[NSString stringWithFormat:@"mcImage-%d.png",i]]);
//		[png  writeToFile: uniquePath atomically:YES ];	
//		[photospecs   replaceObjectAtIndex:	i withObject:uniquePath   ];
//		NSDate *today = [NSDate date]; // get precise time of picture
//		NSInteger size = [png length];
//		[[attrdicts  objectAtIndex: i]   addEntriesFromDictionary: [NSDictionary dictionaryWithObjectsAndKeys:today,@"shoot-time",uniquePath,@"local-file",
//																	@"photo-testcase" ,@"media-type",
//															[NSString stringWithFormat:@"%d",size],@"size",
//																	[NSString stringWithFormat:@"%d",i],@"slot",nil] ];
//		uniquePath = [NSString stringWithFormat:@"%@/%@-%d.%@", DOCSFOLDER, BASE_PATH, ++i, BASE_TYPE]; // get the next possible file
//	}
//	//*********************Remove ***********************
//
//	[self storeMediaState];
//}
//	

@end
