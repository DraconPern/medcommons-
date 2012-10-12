//
//  GigSnifferController.m
//  GigStand
//
//  Hacked by Donner from Nayberz by Joe Conway on 7/27/09.
//  Copyright 2009 Big Nerd Ranch. All rights reserved.
//

#import "GigSnifferController.h"
#import <netinet/in.h>
#import <arpa/inet.h>


@implementation GigSnifferController

- (id)init 
{ 
    [super initWithStyle:UITableViewStylePlain]; 
	// Create an empty array 
    netServices = [[NSMutableArray alloc] init]; 
    // Create a net service browser 
    serviceBrowser = [[NSNetServiceBrowser alloc] init];
	
    // As the delegate, you will be told when services are found 
    [serviceBrowser setDelegate:self]; 
    // Start it up 
    [serviceBrowser searchForServicesOfType:@"_gigstand._tcp." inDomain:@""]; 
    return self; 
} 
- (id)initWithStyle:(UITableViewStyle)style
{
	return [self init];
}
- (NSInteger)tableView:(UITableView *)table numberOfRowsInSection:(NSInteger)section 
{ 
    return [netServices count]; 
} - (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath {
	
	NSNetService *ns = [netServices objectAtIndex:[idxPath row]];    
	NSString *message1 = nil;
    // Try to get the TXT Record 
    NSData *data = [ns TXTRecordData]; 
    // Is the TXT data? (no TXT data in unresolved services) 
    if (data) { 
		
        // Convert it into a dictionary 
        NSDictionary *txtDict = [NSNetService dictionaryFromTXTRecordData:data]; 
        // Get the data that the publisher put in under the message key 
        NSData *mData1 = [txtDict objectForKey:@"webserver"]; 
        // Is there data? 
        if (mData1) { 
            // Make a string 
            message1 = [[NSString alloc] initWithData:mData1 encoding:NSUTF8StringEncoding]; 
            [message1 autorelease]; 
        } 
	}
	// i
	if (message1)
	{
		//if we have an address of a webserver, get safari to open it
		NSString *urlas = [NSString stringWithFormat:@"http://%@/",message1];
		NSURL *url = [NSURL URLWithString:urlas];
		
		if (![[UIApplication sharedApplication] openURL:url])
			
			NSLog(@"%@%@",@"Failed to open url:",[url description]);
	}
	
}

- (UITableViewCell *)tableView:(UITableView *)tv 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{   
	NSNetService *ns = [netServices objectAtIndex:[indexPath row]];    
    NSString *message1 = nil; 
	NSString *message2=nil;
	NSString *message3=nil;
	
    // Try to get the TXT Record 
    NSData *data = [ns TXTRecordData]; 
    // Is the TXT data? (no TXT data in unresolved services) 
    if (data) { 
		
        // Convert it into a dictionary 
        NSDictionary *txtDict = [NSNetService dictionaryFromTXTRecordData:data]; 
        // Get the data that the publisher put in under the message key 
        NSData *mData1 = [txtDict objectForKey:@"webserver"]; 
        // Is there data? 
        if (mData1) { 
            // Make a string 
            message1 = [[NSString alloc] initWithData:mData1 encoding:NSUTF8StringEncoding]; 
			
        } 
		NSData *mData2= [txtDict objectForKey:@"lasttitle"]; 
        // Is there data? 
        if (mData2) { 
            // Make a string 
            message2 = [[NSString alloc] initWithData:mData2 encoding:NSUTF8StringEncoding]; 
			
        } 
		NSData *mData3= [txtDict objectForKey:@"timestamp"]; 
        // Is there data? 
        if (mData3) { 
            // Make a string 
            message3 = [[NSString alloc] initWithData:mData3 encoding:NSUTF8StringEncoding];
			;
			
        } 
    } 
	
	
	NSString *line1 = [NSString stringWithFormat:@"%@ %@",[ns name],message2];
	
	
	NSString *message;
    // Did I fail to get a string? 
    if (!message1&&!message2) { 
        // Use a default message 
        message = @"<No message>"; 
    } else
		message = [NSString stringWithFormat:@"%@ %@",[message3 substringToIndex:15],message1];
	
	
	
	UITableViewCell *cell = [[self tableView] dequeueReusableCellWithIdentifier:@"SnifferViewCell"]; 
    if (!cell) { 
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                      reuseIdentifier:@"SnifferViewCell"]; 
        [cell autorelease]; 
    } 
	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	
    [[cell textLabel] setText:line1]; 
    [[cell detailTextLabel] setText:message];
	[message1 release];
	[message2 release];
	[message3 release];
	
	return cell; 
	
} /*
   - (void)viewDidLoad {
   [super viewDidLoad];
   
   // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
   // self.navigationItem.rightBarButtonItem = self.editButtonItem;
   }
   */

/*
 - (void)viewWillAppear:(BOOL)animated {
 [super viewWillAppear:animated];
 }
 */
/*
 - (void)viewDidAppear:(BOOL)animated {
 [super viewDidAppear:animated];
 }
 */
/*
 - (void)viewWillDisappear:(BOOL)animated {
 [super viewWillDisappear:animated];
 }
 */
/*
 - (void)viewDidDisappear:(BOOL)animated {
 [super viewDidDisappear:animated];
 }
 */


 // Override to allow orientations other than the default portrait orientation.
 - (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
 // Return YES for supported orientations
 return (YES);
 }
 

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}

- (void)netServiceBrowser:(NSNetServiceBrowser *)browser 
           didFindService:(NSNetService *)aNetService 
               moreComing:(BOOL)moreComing 
{ 
    NSLog(@"adding %@", aNetService); 
    // Add it to the array 
    [netServices addObject:aNetService]; 
    // Update the interface 
    NSIndexPath *ip = [NSIndexPath indexPathForRow:[netServices count] - 1 
										 inSection:0]; 
    [[self tableView] insertRowsAtIndexPaths:[NSArray arrayWithObject:ip] 
                            withRowAnimation:UITableViewRowAnimationRight]; 
    // Start resolution to get TXT record 
    [aNetService setDelegate:self]; 
    [aNetService resolveWithTimeout:30]; 
} 
- (void)netServiceDidResolveAddress:(NSNetService *)sender 
{ 
    // What row just resolved? 
    int row = [netServices indexOfObjectIdenticalTo:sender]; 
    NSIndexPath *ip = [NSIndexPath indexPathForRow:row inSection:0]; 
    NSArray *ips = [NSArray arrayWithObject:ip]; 
    // Reload that row 
    [[self tableView] reloadRowsAtIndexPaths:ips 
                            withRowAnimation:UITableViewRowAnimationRight]; 
	
	NSArray *addrs = [sender addresses];
	if([addrs count] > 0)
	{
		NSData *firstAddress = [addrs objectAtIndex:0];
		const struct sockaddr_in *addy = [firstAddress bytes];
		char *str = inet_ntoa(addy->sin_addr);
		NSLog(@"%s:%d", str, ntohs(addy->sin_port));
	}
}
// Called when services are lost 
- (void)netServiceBrowser:(NSNetServiceBrowser *)browser 
         didRemoveService:(NSNetService *)aNetService 
               moreComing:(BOOL)moreComing 
{ 
    NSLog(@"removing %@", aNetService); 
    // Take it out of the array 
    NSUInteger row = [netServices indexOfObject:aNetService]; 
    if (row == NSNotFound) { 
        NSLog(@"unable to find the service in %@", netServices); 
        return; 
    } 
    [netServices removeObjectAtIndex:row]; 
    // Update the interface 
    NSIndexPath *ip = [NSIndexPath indexPathForRow:row inSection:0]; 
    [[self tableView] deleteRowsAtIndexPaths:[NSArray arrayWithObject:ip] 
                            withRowAnimation:UITableViewRowAnimationRight]; 
} 

- (void)dealloc {
    [super dealloc];
}


@end

