//
//  GigSnifferDelegate.m
//  Nayberz
//
//  Created by Joe Conway on 7/27/09.
//  Copyright Big Nerd Ranch 2009. All rights reserved.
//

#import "GigSnifferDelegate.h"
#import "GigSnifferController.h"

@implementation GigSnifferDelegate

@synthesize window;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{


	GigSnifferController *gigSnifferController = [[[GigSnifferController alloc] init] autorelease];
	gigSnifferController.navigationItem.title = @"GigSniffer";
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController:gigSnifferController] autorelease];
	
	[window setRootViewController:nav];
    [window makeKeyAndVisible];
	
	return YES;
}
//- (void)applicationDidEnterBackground:(UIApplication *)application
//{
//	[netService stop];
//}
//- (void)applicationWillEnterForeground:(UIApplication *)application
//{
//	[netService publish];
//}
//
//- (void)setMessage:(NSString *)str forNetService:(NSNetService *)service
//{
//    // Pack the string into an NSData 
//    NSData *d = [str dataUsingEncoding:NSUTF8StringEncoding]; 
//    // Put the data in a dictionary 
//    NSDictionary *txtDict = [NSDictionary dictionaryWithObject:d forKey:@"message"]; 
//    // Pack the dictionary into an NSData 
//    NSData *txtData = [NSNetService dataFromTXTRecordDictionary:txtDict]; 
//    // Put that data into the net service 
//    [service setTXTRecordData:txtData]; 
//}
//- (void)netServiceDidPublish:(NSNetService *)sender 
//{ 
//    NSLog(@"published: %@", sender); 
//} 
//- (void)netService:(NSNetService *)sender didNotPublish:(NSDictionary *)errorDict 
//{ 
//    NSLog(@"not published: %@ -> %@", sender, errorDict); 
//} 
//- (void)applicationWillTerminate:(UIApplication *)application 
//{ 
//    [netService stop]; 
//} 


- (void)dealloc {
    [window release];
    [super dealloc];
}


@end
