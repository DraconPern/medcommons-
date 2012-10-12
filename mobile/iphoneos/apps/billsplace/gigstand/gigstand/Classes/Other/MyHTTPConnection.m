//
//  This class was created by Nonnus,
//  who graciously decided to share it with the CocoaHTTPServer community.
//

#import "MyHTTPConnection.h"
#import "HTTPServer.h"
#import "HTTPResponse.h"
#import "AsyncSocket.h"
#import "GigStandAppDelegate.h"
#import "DataManager.h"
#import "ArchivesManager.h"
#import "DataStore.h"
#import "ArchivesManager.h"
#import "TunesManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"
#import "LogManager.h"
@implementation MyHTTPConnection

/**
 * Returns whether or not the requested resource is browseable.
 **/
- (BOOL)isBrowseable:(NSString *)path
{
	// Override me to provide custom configuration...
	// You can configure it for the entire server, or based on the current request
	
	return YES;
}

- (NSString *)dateAsString:(NSDate *)date
{
	//	NSLog (@"dateAsString:%@ ",date);
	// Example: Sun, 06 Nov 1994 08:49:37 GMT
	
	NSDateFormatter *df = [[[NSDateFormatter alloc] init] autorelease];
	[df setFormatterBehavior:NSDateFormatterBehavior10_4];
	[df setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	[df setDateFormat:@"EEE, dd MMM y HH:mm:ss 'GMT'"];
	
	// For some reason, using zzz in the format string produces GMT+00:00
	
	NSString *x = [df stringFromDate:date];
	//	NSLog (@"dateAsString:%@ returns %@",date,x);
	return x;
}
-(NSMutableString *) headerWithTitle:(NSString *)title
{	
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
	NSMutableString *outdata = [NSMutableString new];
	[outdata appendFormat:@"<h1><img src='/favicon' alt='noimg' /> %@</h1>", title];	
	[outdata appendFormat:@"<p>%@ running at %@:%d on %@ %@<br/>",
	 devicename,
	 [DataManager sharedInstance].myLocalIP, 
	 [DataManager sharedInstance].myLocalPort,
	 [DataManager sharedInstance].applicationName,
	 [DataManager sharedInstance].applicationVersion];
	[outdata appendString:@"\n <a href='/inbox'>inbox</a> <a href='/archives'>archives</a> <a href='/log'>log</a> <a href='/info'>info</a> <a href='/follow'>follow</a></p>"];
	return [outdata autorelease];
}
-(NSMutableString *) stdHeader
{
	
	NSMutableString *outdata = [NSMutableString new];	
	[outdata appendFormat:@"<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />"];
	[outdata appendString:@"<link rel='shortcut icon' href='/favicon' type='image/png' />"];
	[outdata appendString:@"<style>html {background-color:#eeeeee} body { background-color:#FFFFFF; font-family:Tahoma,Arial,Helvetica,sans-serif; font-size:18x; margin-left:15%; margin-right:15%; border:3px groove #006600; padding:15px; } </style>"];
	[outdata appendString:@"</head><body>"];
	return [outdata autorelease];
}

- (NSString *)createArchivePage:(NSString *)path
{
	//NSError *error;//	
	
	NSArray *putb = [TunesManager allTitlesFromArchive: path];
	
	NSMutableArray *array =   [NSMutableArray arrayWithCapacity:[putb count]];	
	
	[array sortUsingSelector:@selector(compare:)];
	
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>%@ on %@</title>",path,devicename];
	[outdata appendString:[self stdHeader]];
	[outdata appendString: [self headerWithTitle:[NSString stringWithFormat:@"Archive %@",path]]];
    [outdata appendFormat:@"<div style='border: solid 1px black; margin: 5px; padding: 5px;'><h3>These are the tunes in %@:</h3>",path];
    [outdata appendString:@"<p><table>"];
    for (NSString *fname in array)
		[outdata appendFormat:@"<tr><td><a href='/tunes/%@'>%@</a> </td></tr>\n",fname, fname];
    [outdata appendString:@"</table></p></div>"];
	[outdata appendString:@"<p>Please visit <a href='http://www.gigstand.net/' >www.gigstand.net</a> for more information</body></html>"];
    return [outdata autorelease];
}
- (NSString *)createListOfArchivesPage:(NSString *)path
{
	//NSError *error;//
	//    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:&error];
	//	
	NSArray *array = [ArchivesManager allArchives] ;
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>GigStand Archives on %@</title>",devicename];
	[outdata appendString:[self stdHeader]];
	[outdata appendString:[self headerWithTitle:[NSString stringWithFormat:@"Archives"]]];
    [outdata appendFormat:@"<div style='border: solid 1px black; margin: 5px; padding: 5px;'><h3>You can download from  %@:</h3>",devicename];
    [outdata appendString:@"<p><table>"];
	//[outdata appendFormat:@"<a href=\"..\">..</a><br />\n"];
    for (NSString *fname in array)
    {
		NSError *error;
		NSString *zname = [DataStore pathForArchive:fname];
		
		NSDictionary *fileDict = [[NSFileManager defaultManager] attributesOfItemAtPath:zname error:&error];
		//			//NSLog(@"fileDict: %@", fileDict);
		NSString *modDate = [[fileDict objectForKey:NSFileModificationDate] description];
		
		[outdata appendFormat:@"<tr><td><a href='/archives/%@'>%@</a> </td><td>  %8.1f K </td><td> %@ </td></tr>\n", 
		 fname, fname, [[fileDict objectForKey:NSFileSize] floatValue] / 1024,
		 modDate];
    }
    [outdata appendString:@"</table></p></div>"];
	[outdata appendString:@"<p>Please visit <a href='http://www.gigstand.net/' >www.gigstand.net</a> for more information</body></html>"];
    return [outdata autorelease];
}

- (NSString *)createInboxPage:(NSString *)path
{
	
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>GigStand Inbox on %@</title>",devicename];
	[outdata appendString:[self stdHeader]];
	[outdata appendString:[self headerWithTitle:[NSString stringWithFormat:@"Inbox"]]];
	
	if ([self supportsPOST:path withSize:0])
	{
		NSArray *inboxdocs = [[DataManager  newInboxDocumentsList] autorelease];//added monday
		
		[outdata appendFormat :@"<div style='border: solid 1px black; margin: 5px;padding: 5px;margin-top:20px;'><h3>You can upload to %@</h3>",devicename];
		[outdata appendString:@"Here's what's currently in the Inbox awaiting assimilation on the device:<p><table>"];
		//[outdata appendFormat:@"<a href=\"..\">..</a><br />\n"];
		for (NSString *fname in inboxdocs)
		{
			NSError *error;
			NSString *zname = [NSString stringWithFormat:@"%@/%@",[DataStore pathForItunesInbox],fname];////[DataStore pathInbox:fname];
			NSDictionary *fileDict = [[NSFileManager defaultManager] attributesOfItemAtPath:zname error:&error];
			NSString *modDate = [[fileDict objectForKey:NSFileModificationDate] description];
			[outdata appendFormat:@"<tr><td><a href='/inbox/%@'>%@</a> </td><td>  %8.1f K </td><td> %@ </td></tr>\n", 
			 fname, fname, [[fileDict objectForKey:NSFileSize] floatValue] / 1024,
			 modDate];
		}
		[outdata appendString:@"</table>\n</p>"];		
		[outdata appendString:@"<br/><br/><cite>Other choices for uploading include using a cable to connect thru iTunes, and sending email attachments.</cite><br/><br/>"];
		[outdata appendString:@"<form action=\"\" method=\"post\" enctype=\"multipart/form-data\" name=\"form1\" id=\"form1\">"];
		[outdata appendString:@"<label>upload file "];
		[outdata appendString:@"<input type=\"file\" name=\"file\" id=\"file\" />"];
		[outdata appendString:@"</label>"];
		[outdata appendString:@"<label>"];
		[outdata appendString:@"<input type=\"submit\" name=\"button\" id=\"button\" value=\"Submit\" />"];
		[outdata appendString:@"</label>"];
		[outdata appendString:@"</form></div>"];
	}
	
	[outdata appendString:@"<p>Please visit <a href='http://www.gigstand.net/' >www.gigstand.net</a> for more information</body></html>"];
    
	//NSLog(@"outData: %@", outdata);
    return [outdata autorelease];
}
- (NSString *)createTunesPage:(NSString *)tune 
{
	
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>GigStand Tune %@ on %@</title>",tune,devicename];
	[outdata appendString:[self stdHeader]];
	[outdata appendString:[self headerWithTitle:[NSString stringWithFormat:@"Tune %@",tune]]];
    [outdata appendFormat:@"<ul>\n"];
	TuneInfo *tn = [TunesManager findTuneInfo:tune];
	for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title])
	{
		[outdata appendFormat:@"<li><a href='/archives/%@' >%@</a></li>\n", ii.archive,ii.archive ];
	}
	
	[outdata appendFormat:@"</ul>\n"];	
	[outdata appendString:@"<p>Please visit <a href='http://www.gigstand.net/' >www.gigstand.net</a> for more information</body></html>"];
    return [outdata autorelease];
}
- (NSString *)createInfoPage:(NSString *)errorString
{
	
	NSString *devicename = [[[UIDevice currentDevice ] name] description];
	NSString *now =  [self dateAsString:[NSDate date]];
	NSString *then = [self dateAsString:[DataManager sharedInstance].starttime];
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>GigStand Info - %@</title>",devicename];
	[outdata appendFormat:@"<meta http-equiv='refresh' content='10' />"];
	[outdata appendString:[self stdHeader]];
	[outdata appendFormat:@"<p>%@ %@ running at %@:%d on %@ %@</p>",
	 now,
	 devicename,
	 [DataManager sharedInstance].myLocalIP, 
	 [DataManager sharedInstance].myLocalPort,
	 [DataManager sharedInstance].applicationName,
	 [DataManager sharedInstance].applicationVersion];
    
	if ([[TunesManager lastTitle] length] > 0)
		[outdata appendFormat:@"<p>Last tune:<a href='/'> %@</a> ",[TunesManager lastTitle]];
	else	
		[outdata appendString :@"<p>No tunes played "];
	
	[outdata appendFormat:@"; device started at %@</p>",then];
	
	return [outdata autorelease];
}
- (NSString *)createErrorPage:(NSString *)errorString
{
    NSMutableString *outdata = [NSMutableString new];
	[outdata appendString:@"<html><head>"];
	[outdata appendFormat:@"<title>GigStand Error - %@</title>",errorString];
	[outdata appendString:[self stdHeader]];
	[outdata appendString:[self headerWithTitle:[NSString stringWithFormat:@"GigStand Error - %@",errorString]]];
	[outdata appendString:@"<p>Please visit <a href='http://www.gigstand.net/' >www.gigstand.net</a> for more information</body></html>"];
    return [outdata autorelease];
}

- (BOOL)supportsMethod:(NSString *)method atPath:(NSString *)relativePath
{
	if ([@"POST" isEqualToString:method])
	{
		return YES;
	}
	
	return [super supportsMethod:method atPath:relativePath];
}


/**
 * Returns whether or not the server will accept POSTs.
 * That is, whether the server will accept uploaded data for the given URI.
 **/
- (BOOL)supportsPOST:(NSString *)path withSize:(UInt64)contentLength
{
	dataStartIndex = 0;
	multipartData = [[NSMutableArray alloc] init];
	postHeaderOK = FALSE;
	return YES;
}


/**
 * This method is called to get a response for a request.
 * You may return any object that adopts the HTTPResponse protocol.
 * The HTTPServer comes with two such classes: HTTPFileResponse and HTTPDataResponse.
 * HTTPFileResponse is a wrapper for an NSFileHandle object, and is the preferred way to send a file response.
 * HTTPDataResopnse is a wrapper for an NSData object, and may be used to send a custom response.
 **/
- (NSObject<HTTPResponse> *)httpResponseForMethod:(NSString *)method URI:(NSString *)pathx
{
	
	// The incoming URL must be manipulated to produce a similar URL in the top level Documents directory
	//  The URL is copied over literally
	
	
	NSString *path = (NSString *)CFURLCreateStringByReplacingPercentEscapesUsingEncoding(kCFAllocatorDefault,
																						 (CFStringRef)pathx,CFSTR(""),kCFStringEncodingUTF8);
	
	NSData *requestData = [(NSData *)CFHTTPMessageCopySerializedMessage(request) autorelease];
	
	NSString *requestStr = [[[NSString alloc] initWithData:requestData encoding:NSASCIIStringEncoding] autorelease];
	NSLog(@"========= HTTPRequest %@ %@===================\n%@", method,path, requestStr);
	//NSLog(@"========= Response ===================================");
	
	if ([[method lowercaseString] isEqualToString: @"get"])
	{
			if ([path isEqualToString:@"/"] ||[path isEqualToString:@"/follow"])
			{
				// just try
				if ([[TunesManager lastTitle] length]>0)
				{
					NSData *browseData = [[self createTunesPage:[TunesManager lastTitle]] dataUsingEncoding:NSUTF8StringEncoding];
					[path release];
					return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
				}
				else {
					NSData *browseData = [[self createErrorPage:@"Still awaiting tune selection"] dataUsingEncoding:NSUTF8StringEncoding];
					[path release];
					return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
				}
			}
		else		
			if ([path isEqualToString:@"/favicon"]||[path isEqualToString:@"/favicon.ico"])
			{
				// wants one favicon file - not working on real device when type==png
				
				NSString     *filePath = [[NSBundle mainBundle] pathForResource: @"favicon" ofType: @"ico" ];	
				
				if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
				{
					[path release];
					return [[[HTTPFileResponse alloc] initWithFilePath:filePath] autorelease];
				}
				else {
					NSLog (@"failed looking for favicon file %@",filePath);
						NSData *browseData = [[self createErrorPage:[NSString stringWithFormat:@"failed looking for favicon file %@",filePath] ]
										  dataUsingEncoding:NSUTF8StringEncoding];
					[path release];
					return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
				}
			}
			else 
				if ([path isEqualToString:@"/tunes"])
				{
					NSData *browseData = [[self createErrorPage:[NSString stringWithFormat:@"syntax: /tunes/<tune name>/"] ]
										  dataUsingEncoding:NSUTF8StringEncoding];
					[path release];
					return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
					
				}
				else 
					if ([path isEqualToString:@"/info"])
					{
						// just try
						
						NSData *browseData = [[self createInfoPage:@"info"] dataUsingEncoding:NSUTF8StringEncoding];
						[path release];
						return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
						
						
					}
				
						else 
							if ([path isEqualToString:@"/archives"])
							{
								NSData *browseData = [[self createListOfArchivesPage:path] dataUsingEncoding:NSUTF8StringEncoding];
								[path release];
								return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
							} 
							else 
								if ([path isEqualToString:@"/inbox"])
								{
									NSData *browseData = [[self createInboxPage:path] dataUsingEncoding:NSUTF8StringEncoding];
									[path release];
									return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
								} 
								else 
									if ([path isEqualToString:@"/log"])
									{
										// wants one particular file from archive
										NSString *filePath =  [LogManager pathForCurrentLog];
										if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
										{
											[path release];
											return [[[HTTPFileResponse alloc] initWithFilePath:filePath] autorelease];
										}
									} 
									else 
									{
										NSArray *parts = [path componentsSeparatedByString:@"/"];
										//	NSLog (@"parts is %@",parts);
										if ([parts count]>=3)
										{
											if ([@"tunes" isEqualToString:[parts objectAtIndex:1]])
											{
												NSData *browseData = [[self createTunesPage:[parts objectAtIndex:2]] dataUsingEncoding:NSUTF8StringEncoding];
												[path release];
												return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
											} 
											else 
												if ([@"archives" isEqualToString:[parts objectAtIndex:1]])
												{
													// ok, distinguish between a list of files and a particular file
													if ([parts count]==3)
													{
														// wants list of archives
														NSData *browseData = [[self createArchivePage:[parts objectAtIndex:2]] dataUsingEncoding:NSUTF8StringEncoding];
														[path release];
														return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
													}
													else {
														// wants one particular file from archive
														NSString *filePath = [NSString stringWithFormat:@"%@/%@",
																			  [DataStore pathForArchive:[parts objectAtIndex:2]],[parts objectAtIndex:3]];
														
														if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
														{
															[path release];
															return [[[HTTPFileResponse alloc] initWithFilePath:filePath] autorelease];
														}
														else {
															NSLog (@"failed looking for archive file %@",filePath);
															NSData *browseData = [[self createErrorPage:[NSString stringWithFormat:@"failed looking for archive file %@",filePath] ]
																				  dataUsingEncoding:NSUTF8StringEncoding];
															[path release];
															return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
														}
													}
													
												} 
												else
													if ([@"inbox" isEqualToString:[parts objectAtIndex:1]])
													{
														// just return the file directly from inbox
														
														NSString *filePath = [NSString stringWithFormat:@"%@/%@",
																			  [DataStore pathForItunesInbox],[parts objectAtIndex:2]];
														
														if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
														{
															[path release];
															return [[[HTTPFileResponse alloc] initWithFilePath:filePath] autorelease];
														}
														else {
															NSLog (@"failed looking for inbox file %@",filePath);
															
															NSData *browseData = [[self createErrorPage:[NSString stringWithFormat:@"failed looking for inbox file %@",filePath] ]
																				  dataUsingEncoding:NSUTF8StringEncoding];
															[path release];
															return [[[HTTPDataResponse alloc] initWithData:browseData] autorelease];
														}
														
													}
													else
													{
														
														NSString     *filePath = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments ],path ];	
														
														if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
														{
															[path release];
															return [[[HTTPFileResponse alloc] initWithFilePath:filePath] autorelease];
														}
														
														
														
													}
										}
									}
	}
	
	
	
	
	[path release];
	//////////////
	// Process POST data
	////////////
	if (requestContentLength > 0)  
	{
		NSLog(@"processing post data: %i", requestContentLength);
		
		if ([multipartData count] < 2) return nil;
		
		NSString* postInfo = [[NSString alloc] initWithBytes:[[multipartData objectAtIndex:1] bytes]
													  length:[[multipartData objectAtIndex:1] length]
													encoding:NSUTF8StringEncoding];
		
		NSArray* postInfoComponents = [postInfo componentsSeparatedByString:@"; filename="];
		postInfoComponents = [[postInfoComponents lastObject] componentsSeparatedByString:@"\""];
		postInfoComponents = [[postInfoComponents objectAtIndex:1] componentsSeparatedByString:@"\\"];
		NSString* filename = [postInfoComponents lastObject];
		
		if (![filename isEqualToString:@""]) //this makes sure we did not submitted upload form without selecting file
		{
			UInt16 separatorBytes = 0x0A0D;
			NSMutableData* separatorData = [NSMutableData dataWithBytes:&separatorBytes length:2];
			[separatorData appendData:[multipartData objectAtIndex:0]];
			int l = [separatorData length];
			int count = 2;	//number of times the separator shows up at the end of file data
			
			NSFileHandle* dataToTrim = [multipartData lastObject];
			NSLog(@"data: %@", dataToTrim);
			
			for (unsigned long long i = [dataToTrim offsetInFile] - l; i > 0; i--)
			{
				[dataToTrim seekToFileOffset:i];
				if ([[dataToTrim readDataOfLength:l] isEqualToData:separatorData])
				{
					[dataToTrim truncateFileAtOffset:i];
					i -= l;
					if (--count == 0) break;
				}
			}
			
			NSLog(@"NewFileUploaded");
			[[NSNotificationCenter defaultCenter] postNotificationName:@"NewFileUploaded" object:nil];
		}
		
		//		for (NSUInteger n = 1; n < [multipartData count] - 1; n++)
		//			NSLog(@"%@", [
		//						  [NSString alloc] initWithBytes:[[multipartData objectAtIndex:n] bytes] 
		//						  length:[[multipartData objectAtIndex:n] length] encoding:NSUTF8StringEncoding]);
		
		[postInfo release];
		[multipartData release];
		requestContentLength = 0;
		
	}
	
	return nil;
}


/**
 * This method is called to handle data read from a POST.
 * The given data is part of the POST body.
 **/
- (void)processDataChunk:(NSData *)postDataChunk
{
	// Override me to do something useful with a POST.
	// If the post is small, such as a simple form, you may want to simply append the data to the request.
	// If the post is big, such as a file upload, you may want to store the file to disk.
	// 
	// Remember: In order to support LARGE POST uploads, the data is read in chunks.
	// This prevents a 50 MB upload from being stored in RAM.
	// The size of the chunks are limited by the POST_CHUNKSIZE definition.
	// Therefore, this method may be called multiple times for the same POST request.
	
	//NSLog(@"processPostDataChunk");
	
	if (!postHeaderOK)
	{
		UInt16 separatorBytes = 0x0A0D;
		NSData* separatorData = [NSData dataWithBytes:&separatorBytes length:2];
		
		int l = [separatorData length];
		
		for (NSUInteger i = 0; i < [postDataChunk length] - l; i++)
		{
			NSRange searchRange = {i, l};
			
			if ([[postDataChunk subdataWithRange:searchRange] isEqualToData:separatorData])
			{
				NSRange newDataRange = {dataStartIndex, i - dataStartIndex};
				dataStartIndex = i + l;
				i += l - 1;
				NSData *newData = [postDataChunk subdataWithRange:newDataRange];
				
				if ([newData length])
				{
					[multipartData addObject:newData];
				}
				else
				{
					postHeaderOK = TRUE;
					NSString* postInfo = [[NSString alloc] initWithBytes:[[multipartData objectAtIndex:1] bytes] length:[[multipartData objectAtIndex:1] length] encoding:NSUTF8StringEncoding];
					NSArray* postInfoComponents = [postInfo componentsSeparatedByString:@"; filename="];
					postInfoComponents = [[postInfoComponents lastObject] componentsSeparatedByString:@"\""];
					postInfoComponents = [[postInfoComponents objectAtIndex:1] componentsSeparatedByString:@"\\"];
					NSString* filename = [[[server documentRoot] path] stringByAppendingPathComponent:[postInfoComponents lastObject]];
					NSRange fileDataRange = {dataStartIndex, [postDataChunk length] - dataStartIndex};
					[[NSFileManager defaultManager] createFileAtPath:filename contents:[postDataChunk subdataWithRange:fileDataRange] attributes:nil];
					NSFileHandle *file = [NSFileHandle fileHandleForUpdatingAtPath:filename] ;//retain]; // removed retain on monday					
					if (file)
					{
						[file seekToEndOfFile];
						[multipartData addObject:file];
					}					
					[postInfo release];					
					break;
				}
			}
		}
	}
	else
	{
		[(NSFileHandle*)[multipartData lastObject] writeData:postDataChunk];
	}
}

@end
