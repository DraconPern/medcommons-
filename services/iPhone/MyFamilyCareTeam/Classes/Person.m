//
//  Person.m
//  MyFamilyCareTeam
//
//  Created by bill donner on 5/5/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "URLCacheConnection.h"
#import "Person.h"



@implementation Person

@synthesize webLink = _webLink; // Holds the elders first name
@synthesize firstname = _firstname; // Holds the elders first name
@synthesize lastname = _lastname; // Holds the elders last name
@synthesize mcid = _mcid; // Holds the medcommons id
@synthesize sex = _sex; // 
@synthesize sponsorfbid = _sponsorfbid; // 
@synthesize familyfbid = _familyfbid; // who opened up this team and is manager
@synthesize oauth_token = _oauth_token; // 
@synthesize oauth_secret = _oauth_secret; //
@synthesize applianceurl = _applianceurl; // 
@synthesize photoUrl = _photoUrl; // 
@synthesize gw_modified_date_time = _gw_modified_date_time; // should be nicely formatted
@synthesize alertlevel = _alertlevel; // shows implicit status by coloring the display
@synthesize photoFileSpec = _photoFileSpec;
@synthesize photoState = _photoState;

- (NSString *)description
{
    // Override of -[NSObject description] to print a meaningful representation of self.
    return [NSString stringWithFormat:@"%@ %@", self.firstname, self.lastname];
}



/*
 ------------------------------------------------------------------------
 URLCacheConnectionDelegate protocol methods
 ------------------------------------------------------------------------
 */

#pragma mark -
#pragma mark URLCacheConnectionDelegate methods, these come back in patient context
- (void) connectionDidFail:(URLCacheConnection *)theConnection
{	
	//[self stopAnimation];
	//[self buttonsEnabled:YES];
	self.photoState = @"load failed"; // 1; // okay mark it
	NSLog(@"%@ of %@ to %@ ", self.photoState, self.photoUrl, self.photoFileSpec);
	[theConnection release];
}


- (void) connectionDidFinish:(URLCacheConnection *)theConnection
{	/*
 if ([[NSFileManager defaultManager] fileExistsAtPath:filePath] == YES) {
 
 
 
 [self getFileModificationDate];
 NSComparisonResult result = [theConnection.lastModified compare:fileDate];
 if (result == NSOrderedDescending) {
 
 if (![[NSFileManager defaultManager] removeItemAtPath:filePath error:&error]) {
 URLCacheAlertWithError(error);
 }
 
 }
 }
 
 if ([[NSFileManager defaultManager] fileExistsAtPath:filePath] == NO) {
 */
	[[NSFileManager defaultManager] createFileAtPath:self.photoFileSpec 
											contents:theConnection.receivedData 
										  attributes:nil];
	/*
	 statusField.text = NSLocalizedString (@"Newly cached image", 
	 @"Image not found in cache or new image available.");
	 }
	 else {
	 statusField.text = NSLocalizedString (@"Cached image is up to date",
	 @"Image updated and no new image available.");
	 }
	 
	 // reset the file's modification date to indicate that the URL has been checked 
	 
	 NSDictionary *dict = [[NSDictionary alloc] initWithObjectsAndKeys:[NSDate date], NSFileModificationDate, nil];
	 if (![[NSFileManager defaultManager] setAttributes:dict ofItemAtPath:filePath error:&error]) {
	 URLCacheAlertWithError(error);
	 }
	 [dict release];
	 */
	/*
	 [self stopAnimation];
	 [self buttonsEnabled:YES];
	 [self displayCachedImage];
	 
	 */
	
	
	NSLog(@"Photo Load of %@ to %@ succeeded", self.photoUrl, self.photoFileSpec);
	self.photoState = @"load complete"; // 1; // okay mark it
	
	
	[theConnection release];
}
@end

