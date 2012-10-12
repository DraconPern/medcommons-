//
//  BonJourManager.m
//  GigStand
//
//  Created by bill donner on 2/15/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "BonJourManager.h"
#import "GigStandAppDelegate.h"
#import "SettingsManager.h"
#import "DataManager.h"
#import "localhostAddresses.h"
#import "TunesManager.h"

@implementation BonJourManager
#pragma mark Public Class Methods

+ (BonJourManager *) sharedInstance
{
	static BonJourManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[BonJourManager alloc] init];
	}
	
	return SharedInstance;
}
+(NSDictionary *) buildTXTDictionary;
{
	NSString *mess = [NSString stringWithFormat:@"%@:%d",
					  [GigStandAppDelegate sharedInstance].myLocalIP,
					  [GigStandAppDelegate sharedInstance].myLocalPort];
	NSData *data = [mess dataUsingEncoding: NSUTF8StringEncoding];
	
	NSString *mess2;
	if ([TunesManager lastTitle])
		mess2 = [TunesManager lastTitle]; 
	else 
		mess2 = @"<Not yet set>";	
	
	NSData *data2 = [mess2 dataUsingEncoding: NSUTF8StringEncoding];
	
	NSString *timenow = [NSString stringWithFormat:@"%@v%@",
						 [[[NSString stringWithFormat:@"%@",[NSDate date]] substringFromIndex:11] substringToIndex:8],
						 [DataManager sharedInstance].applicationVersion
						 ];
	NSData *data3 = [timenow dataUsingEncoding: NSUTF8StringEncoding];
	
	NSDictionary *dict = [NSDictionary  dictionaryWithObjectsAndKeys:
						  data,@"webserver",
						  data2,@"lasttitle",
						    data3,@"timestamp",
						  nil];
	return dict;
}

-(void) publishTXTFromLastTitle;
{
	BOOL bonjourWithPeers = [[SettingsManager sharedInstance] bonjourWithPeers];
	if (bonjourWithPeers)
	{
		NSDictionary *dict = [BonJourManager buildTXTDictionary];
		NSData *fdata = [NSNetService dataFromTXTRecordDictionary:dict];
		[self->netService setTXTRecordData: fdata]; // 
	//	[self->httpServer setTXTRecordDictionary:dict]; // stimulate it a bit
		[self->netService publish];//?? must be here or never shows up
	}
	
}

+(void) writeTXTData ;
{
	// this is done to push out the TXT record for us just once at startup
//	NSLog (@"writeTXTData ++++++++++++++ publishing TXT on  %@:%d +++++++++++++++++++",
//		   [GigStandAppDelegate sharedInstance].myLocalIP,
//		   [GigStandAppDelegate sharedInstance].myLocalPort);
	
	[[BonJourManager sharedInstance] publishTXTFromLastTitle];
}

+(void) starting;
{
[NSTimer scheduledTimerWithTimeInterval: 2.0f target:self selector:@selector(writeTXTData) userInfo:nil repeats:NO];
}
-(void) start 
{
	
	BOOL bonjourWithPeers = [[SettingsManager sharedInstance] bonjourWithPeers];
	if (bonjourWithPeers)
	{
		//NSLog(@"/////////re-start BonJour...");
		if (self->netService!=nil) [self->netService release];
		self->netService = [[[NSNetService alloc] initWithDomain:@"" type:@"_gigstand._tcp" 
															name:[UIDevice currentDevice].name	port:9090] retain];	
		self->netService.delegate = self;
		[BonJourManager writeTXTData];
	}	
}
//// incoming notification triggers the stargin of bonhour
- (void)localhostAdressesResolved:(NSNotification *) notification
{
	if(notification)
	{
		[addresses release];
		addresses = [[notification object] copy];
		//	NSLog(@"localhostAdressesResolved addresses: %@", addresses);
	}
	
	if(addresses == nil)
	{
		return;
	}
	
	NSMutableString *localIP = nil;	
	localIP = [addresses valueForKey:@"en0"];	
	if (!localIP)
	{
		localIP = [addresses valueForKey:@"en1"];
	}
	
	[GigStandAppDelegate sharedInstance].myLocalIP = [localIP copy];	
	NSLog (@"++++++++++++++ localhostAdressesResolved on  %@:%d +++++++++++++++++++",
		   [GigStandAppDelegate sharedInstance].myLocalIP,
		   [GigStandAppDelegate sharedInstance].myLocalPort);
	
	// now start bonjour
	[self start];
	
}
- (BonJourManager *) init
{
	self = [super init];
	if (self) 
	{
		
		
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(localhostAdressesResolved:) 
													 name:@"LocalhostAdressesResolved" object:nil];
		[localhostAddresses performSelectorInBackground:@selector(list) withObject:nil];
		
	}
	return self;	
}
#pragma mark  NSNetService Delegates

- (void ) netServiceDidPublish: (NSNetService *)sender
{
	NSLog (@"Netservice did publish %@", sender);
}
-(void) netService: (NSNetService *) sender didNotPublish: (NSDictionary *) errorDict
{
	NSLog (@"Netservice did not publish %@ -- %@", sender, errorDict);
}
@end
