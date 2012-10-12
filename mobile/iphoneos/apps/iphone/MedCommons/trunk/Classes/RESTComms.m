//
//  RESTComms.m
//  ForensicFoto
//
//  Created by bill donner on 9/9/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//
#import "JSON.h"
#import "MedCommons.h"
#import "RESTComms.h"
#import "PatientStore.h"
#import "MCUploadController.h"
#import "DataManager.h"


@implementation RESTComms: NSObject 
#pragma mark Low Level Communications Routines
-(void) dealloc 
{
//	[storebitsService release];
//	[storemetaService release];
	BREADCRUMBS_POP;
	[super dealloc];
}
-(RESTComms *) init
{
	self = [super init];

	
	BREADCRUMBS_PUSH;
	return self;
}

-(NSString *) postIt:(NSString *)request whichService:(NSString  *)whichservice   withBitsFromPath:(NSString *)uniquePath
{
	
	LLC_LOG(@"*********HTTP POST application/x-www-form-urlencoded %@%@**",whichservice, request);
	NSError *error;
	NSData *data;
	NSMutableData *mdata;
	const char *utfRequest = [request UTF8String];
	mdata = [NSMutableData dataWithBytes:utfRequest	length:strlen(utfRequest)];
	if (uniquePath!=nil) {
		
		//LLC_LOG(@"*********POSTING TO %@ %@**",whichservice, request);
		data = [NSData dataWithContentsOfFile:uniquePath options:0 error:&error];	
		if (data == nil) {
			// an error occurred
			LLC_LOG(@"Error reading %@ \n  -- not posted %d %@",
				  uniquePath, error, [error localizedFailureReason]);
		} 
		else 	[mdata appendData:data]; // dadd bits to end of it
	}
	NSMutableURLRequest* post = [NSMutableURLRequest requestWithURL:[NSURL URLWithString: whichservice]];
	[post addValue: @"application/x-www-form-urlencoded" forHTTPHeaderField: @"Content-Type"];
	[post setHTTPMethod: @"POST"];
	[post setHTTPBody:mdata];
	NSURLResponse *response;
	BOOL old = [UIApplication sharedApplication].networkActivityIndicatorVisible; // turn this on then restore at bottom
	[UIApplication sharedApplication].networkActivityIndicatorVisible=YES;
	NSData *result = [NSURLConnection sendSynchronousRequest:post 
										   returningResponse:&response error:&error];
	NSString *rets = [[[NSString alloc] initWithData:result 
												  encoding:NSASCIIStringEncoding] autorelease]; //??
	[UIApplication sharedApplication].networkActivityIndicatorVisible=old;
	NSString	*first = [rets substringToIndex:20];
	NSLog (@"Payload up %d bytes response  %d >>>> %@\n", [mdata length],[rets length],first);
	return rets;
}


-(NSDictionary *) postGenericRequestWithJSONResponse:(NSString *)request toService:(NSString *)service 
{

	
	lastpostresponseline = [self postIt:request whichService:service withBitsFromPath:nil];
	//parse JSON returned from remote server	
	SBJSON *parser = [[SBJSON alloc] init];			
	NSDictionary *jdict = [[parser objectWithString:lastpostresponseline error:nil] retain];
	NSString *status = [jdict objectForKey:@"status"];
	if (![@"ok" isEqualToString: status])  		
		LLC_LOG (@"postGenericRequestWithJSONResponse bad status %@ %@",status,request);
	//LLC_LOG (@"--- Dictionary in PostBits %@ ",jdict);
	[parser release];
	return jdict;
}
-(NSString *) 	buildNameValuePair:(NSString *)name withValue:(NSString *)value
{
	return[ [NSString stringWithFormat:@"%@%@%@%@", @"Content-Disposition: form-data; name= \"",name,@"\"\r\n\r\n",value] retain]; 
}
-(void) postItSimon:(NSDictionary *)reqPairs metaPairs:(NSDictionary *)metaPairs whichService:(NSString  *)whichservice   withBitsFromPath:(NSString *)uniquePath
{
#define Boundary @"----FOO"
	
	NSString *str;
	NSURL *url = [NSURL URLWithString:	 whichservice];
	NSMutableURLRequest *req = [NSMutableURLRequest requestWithURL:url];
	[req setHTTPMethod:@"POST"];
	
	NSString *contentType = [NSString stringWithFormat:@"multipart/form-data, boundary=%@", Boundary];
	[req setValue:contentType forHTTPHeaderField:@"Content-type"];
	
	NSData *imageData = [NSData dataWithContentsOfFile:uniquePath options:0 error:nil];
	
	//adding the body:
	
	NSMutableData *postBody = [[NSMutableData alloc] init];
	
	// first boundary is different
    [postBody appendData:[[NSString stringWithFormat:@"--%@\r\n", Boundary] dataUsingEncoding:NSUTF8StringEncoding]];
	
	// add all the name value pairs in the incoming request dictionary
	BOOL first = TRUE;
	for (id key in reqPairs) {
		if (!first) 
			
			[postBody appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",Boundary] 
								  dataUsingEncoding:NSUTF8StringEncoding]];
		
		str = [self buildNameValuePair:key withValue:[reqPairs objectForKey:key]];
		[postBody appendData:[str dataUsingEncoding:NSUTF8StringEncoding]];
		[str release];
		first = FALSE; 
		
	}
	[postBody appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",Boundary] 
						  dataUsingEncoding:NSUTF8StringEncoding]];
	
	[postBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"imageFile\"; filename=\"%@\"\r\n", uniquePath] 
						  dataUsingEncoding:NSUTF8StringEncoding]];
	
	[postBody appendData:[@"Content-Type: image/png\r\n\r\n" 
						  dataUsingEncoding:NSUTF8StringEncoding]];
	
	//NSString *dstring = [[NSString alloc] initWithData:postBody encoding:NSUTF8StringEncoding];
	//NSLog (@"prestring %@",dstring);
	
	[postBody appendData:imageData];	
    [postBody appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",Boundary] dataUsingEncoding:NSUTF8StringEncoding]];
	
	// add all the name value pairs in the incoming meta dictionary
	first = TRUE;
	for (id key in metaPairs) {
		if (!first) 
			
			[postBody appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",Boundary] 
								  dataUsingEncoding:NSUTF8StringEncoding]];
		
		str = [self buildNameValuePair:key withValue:[metaPairs objectForKey:key]];
		[postBody appendData:[str dataUsingEncoding:NSUTF8StringEncoding]];
		[str release];
		first = FALSE; 
		
	}
	
	// last boundary is different
	[postBody appendData:[[NSString stringWithFormat:@"\r\n--%@--\r \n",Boundary] dataUsingEncoding:NSUTF8StringEncoding]];
	
	[req setHTTPBody:postBody];
	BOOL old = [UIApplication sharedApplication].networkActivityIndicatorVisible; // turn this on then restore at bottom
	[UIApplication sharedApplication].networkActivityIndicatorVisible=YES;
	
	NSError *error;
	NSURLResponse *response;
	NSData *result = [NSURLConnection sendSynchronousRequest: req returningResponse:&response error:&error];
	
	
	lastpostresponseline = [[[NSString alloc] initWithData:result encoding:NSASCIIStringEncoding] autorelease];
	[UIApplication sharedApplication].networkActivityIndicatorVisible=old;
	NSLog(@"===>>>>>medcommons response: %@", lastpostresponseline);
	
	[postBody release];
	
}

-(void) postBits:(NSString *)request withSlotNum: (int) slotNum  withBitsFromPath:(NSString *)uniquePath {
	//add GPS info about this photo to this request
	//make sure the bits part always comes last!
	NSDictionary *dict = [[DataManager sharedInstance].ffPatientStore attrsAtIndex:slotNum];
//#if defined(TESTINGMC)
	
	NSDictionary *reqdict = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:	@"iPhone-id", 
																 @"iPhone-date", @"photo_type", nil] 
														         forKeys:[NSArray arrayWithObjects:[[UIDevice currentDevice] uniqueIdentifier], 
																 [NSDate date], @"patient_photo", nil]];
	NSString *thisService = [NSString stringWithFormat:@"http://%@/router/put/%@?auth=%@",[ [DataManager sharedInstance] ffMCappliance],
							 [ [DataManager sharedInstance] ffMCmcid],[ [DataManager sharedInstance] ffMCauth]];
	CONSOLE_LOG(@"posting photos to MedCommons at %@",thisService);
	[self postItSimon:reqdict metaPairs:dict
		 whichService:
	     thisService
	 withBitsFromPath: uniquePath];
	
	
		
	
	
//#endif
	
	
	
		NSString *muts = [NSString stringWithFormat:@"%@&ds=%@&latitude=%@&longitude=%@&horizontalaccuracy=%@&verticalaccuracy=%@&bits=",request,
					  [[DataManager sharedInstance] ffAppDataStore],[dict objectForKey:@"latitude"], [dict objectForKey:@"longitude"],
					  [dict objectForKey:@"horizontal-accuracy"], [dict objectForKey:@"vertical-accuracy"] ,nil];
	lastpostresponseline = [self postIt:muts whichService:[DataManager sharedInstance].ffAppBitsPath withBitsFromPath:uniquePath];
	//parse JSON returned from remote server	
	SBJSON *parser = [[SBJSON alloc] init];			
	NSDictionary *jdict = [[parser objectWithString:lastpostresponseline error:nil] retain];
	NSString *status = [jdict objectForKey:@"status"];
	if ([@"ok" isEqualToString: status])  
		[[DataManager sharedInstance].ffPatientStore addAttrs:jdict atIndex:slotNum ];	// save entire response as well as passed in attrs
	else	
		LLC_LOG (@"bits post received bad status %@",status);
		
	[jdict release];
	[parser release];
	
}
-(void) postMeta:(NSString *)request  andGeneralAttrs:(NSDictionary *) genAttrs {
// this should only be called in a patient context, it sends along all this extra blog metadata, etc
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs; //[NSUserDefaults standardUserDefaults];

	NSString *muts = [NSString stringWithFormat:@"%@&ds=%@&soapA=%@&soapP=%@&soapS=%@&soapD=%@&soapC=%@",request,
					  [[DataManager sharedInstance] ffAppDataStore] ,[prefs objectForKey:@"blogEntryA"] ,
					  [prefs objectForKey:@"blogEntryP"] ,
					  [prefs objectForKey:@"blogEntryS"] ,
					  [prefs objectForKey:@"blogEntryD"] ,
					 [prefs objectForKey:@"blogEntryC"] ,
					  nil];
//#else
//	NSString *muts = [NSString stringWithFormat:@"%@&ds=%@",request,
//					  [[DataManager sharedInstance] ffAppDataStore]	,				  nil];
//#endif
	
	lastpostresponseline= [self postIt:muts whichService:[[DataManager sharedInstance] ffAppMetaPath]withBitsFromPath:nil];
	NSMutableDictionary *mut = [NSMutableDictionary dictionaryWithDictionary:genAttrs];
	[mut setObject:[[UIDevice currentDevice] model] forKey:@"device-type"];
	[mut setObject:[[UIDevice currentDevice] name] forKey:@"owner-name"];
	[mut setObject:[[UIDevice currentDevice] uniqueIdentifier] forKey:@"phone-udid"];

	[mut setObject:[prefs objectForKey:@"blogEntryA"] forKey:@"soap-A"];
	[mut setObject:[prefs objectForKey:@"blogEntryP"] forKey:@"soap-P"];
	[mut setObject:[prefs objectForKey:@"blogEntryS"] forKey:@"soap-S"];
	[mut setObject:[prefs objectForKey:@"blogEntryD"] forKey:@"soap-D"];	
	[mut setObject:[prefs objectForKey:@"blogEntryC"] forKey:@"soap-C"];	

	
	//	LLC_LOG (@"--- %d Mutable Dictionary in PostMeta ",[mut retainCount]);
	//parse JSON returned from remote server	
	SBJSON *parser = [[SBJSON alloc] init];			
	NSDictionary *jdict = [[parser objectWithString:lastpostresponseline error:nil] retain];
	NSString *status = [jdict objectForKey:@"status"];
	if ([@"ok" isEqualToString: status])  
		 [[DataManager sharedInstance]  saveToHistory: jdict 
									 withGeneralAttrs:mut 
							   andWithPhotoAttributes: [[[DataManager sharedInstance] ffPatientStore] attrdicts]];// save entire response as well as passed in attrs
	else	
		LLC_LOG (@"meta post received bad status %@",status);
	
	//LLC_LOG (@"--- %d Dictionary in PostMeta ",[jdict retainCount]);
	[jdict release];
	[parser release];
}

- (void) doPosts:  (NSString *)finalRequest withId:(NSString *)unique andTimeStamp:(double)dtoday andGeneralAttrs:(NSDictionary *) genAttrs andMasterController: (MCUploadController *) masterController
{
	//******** CHECK TO MAKE SURE WE ARE GOOD TO GO OTHERWISE JUST FORGET IT
	
	
	float totransmit = 1.0f+[[DataManager sharedInstance].ffPatientStore countPartPics] + [[DataManager sharedInstance].ffPatientStore countVideos];//force floating point	
	
	///////MY_ASSERT (totransmit>1.0f); // confirm there is indeed something to transmit
	
	//********************** TRANSMIT **********************
	NSString *request = [NSString stringWithFormat:@"&uid=%@&iphone_time=%f&iphone_path=%@&photo_type=subject_photo&file_suffix=%@" // make sure these start with &
						 ,unique,dtoday,[[DataManager sharedInstance].ffPatientStore subjectPhotoSpec]
						 ,[NSString stringWithFormat:@"%f.person.%@.%@", dtoday,BASE_PATH,BASE_TYPE]];	
	
	if ([[DataManager sharedInstance].ffPatientStore haveSubjectPhoto])
		[self postBits: request withSlotNum: 0 withBitsFromPath: [[DataManager sharedInstance].ffPatientStore fullSubjectPhotoSpec]]; 
	int i = 1;
	
	// new part - send any photos
	for (int j=1; j<kHowManySlots-1; j++)
	{
		NSString *q = [[DataManager sharedInstance].ffPatientStore photoSpecAtIndex: j-1];
		if(q&&(![@"" isEqualToString:q]))	
		{ 
			// upload photo
			NSString *fs = [NSString stringWithFormat:@"%f.part-%d.mcImage.png",dtoday,j-1];
			request = [NSString stringWithFormat:@"&uid=%@&iphone_time=%f&iphone_path=%@&photo_type=parts_photo&file_suffix=%@"
					   ,unique,dtoday,q,fs ];	
			//LLC_LOG (@"Video posting part bits request with %@",request);
			[self postBits:request withSlotNum: j withBitsFromPath:[[DataManager sharedInstance].ffPatientStore fullPhotoSpecAtIndex:j-1]]; 
			[masterController incrementProgressBar: (i++/totransmit) ];
		}
	}
	// new part - send any videos
	for (int j=1; j<kHowManySlots-1; j++)
	{
		NSString *q = [[DataManager sharedInstance].ffPatientStore videoSpecAtIndex: j-1];
		if(q && (![@"" isEqualToString:q]))
		{ 
			// upload video
			NSString *fs = [NSString stringWithFormat:@"%f.part-%d.mcVideo.MOV",dtoday,j];
			request = [NSString stringWithFormat:@"&uid=%@&iphone_time=%f&iphone_path=%@&photo_type=video&file_suffix=%@"
					   ,unique,dtoday,q  ,fs ];	
			//LLC_LOG (@"Video posting video bits request with %@",request);
			[self postBits:request withSlotNum: j withBitsFromPath:[[DataManager sharedInstance].ffPatientStore fullVideoSpecAtIndex:j-1]]; 
			[masterController incrementProgressBar: (i++/totransmit) ];
		}
	}
	
	// last bit - post the metadata	
	//LLC_LOG (@"Meta posting meta request with %@",finalRequest);
	[self postMeta:finalRequest andGeneralAttrs:(NSDictionary *) genAttrs];
	
	
	
	
	//[photoAttrs release];
	
}

@end
