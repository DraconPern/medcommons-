//
//  MatesViewController.m
//  Nayberz
//
//  Created by Joe Conway on 7/27/09.
//  Copyright 2009 Big Nerd Ranch. All rights reserved.
//

#import "GigStandAppDelegate.h"
#import "MatesViewController.h"
#import "DataManager.h"
#import <netinet/in.h>
#import <arpa/inet.h>


@implementation MatesViewController

- (id)init 
{ 
	self =  [super init]; //[super initWithStyle:UITableViewStylePlain]; 
	if (self) 
	{
		displayList = [[NSMutableArray alloc] init];
		
		wsList = [[NSMutableArray alloc] init];
		// Create a net service browser 
		serviceBrowser = [[NSNetServiceBrowser alloc] init];
		
		// As the delegate, you will be told when services are found 
		[serviceBrowser setDelegate:self]; 
		// Start it up 
		[serviceBrowser searchForServicesOfType:@"_gigstand._tcp." 
									   inDomain:@""]; 
	}
    return self; 
} 
- (id)initWithStyle:(UITableViewStyle)style
{
	return [self init];
}
-(UIView *) buildUI
{
	
	
	CGRect theframe = self.parentViewController.view.bounds;//
	UIView *oview = [[[UIView alloc] initWithFrame: theframe ] autorelease];
	oview.backgroundColor = [UIColor clearColor];
	float fudge = [DataManager navBarHeight];
	theframe.origin.y+=fudge;
	theframe.size.height-=fudge;
	
	// outer view installed just to get background colors right
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: theframe
														 style: UITableViewStylePlain]
							autorelease];
	
	tmpView.tableHeaderView = nil;
	tmpView.backgroundColor =  [UIColor whiteColor]; 
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
	self.navigationItem.titleView = [DataManager makeTitleView:@"Mates Nearby" ];	
	[self setColorForNavBar ];
	
	[oview addSubview:tmpView];
	self->tableView = tmpView; // make everyone else happy too!
	return oview;
}

- (void) loadView
{
	self.view = [self buildUI];
}

- (NSInteger)tableView:(UITableView *)table numberOfRowsInSection:(NSInteger)section 
{ 
    return [self->displayList count]; 
} 

- (void) tableView: (UITableView *) tabView didSelectRowAtIndexPath: (NSIndexPath *) idxPath {
	
	NSString *message1 = [self->wsList objectAtIndex:[idxPath row]];    
	//	NSString *message1 = nil;
	//    // Try to get the TXT Record 
	//    NSData *data = [ns TXTRecordData]; 
	//    // Is the TXT data? (no TXT data in unresolved services) 
	//    if (data) { 
	//		
	//        // Convert it into a dictionary 
	//        NSDictionary *txtDict = [NSNetService dictionaryFromTXTRecordData:data]; 
	//        // Get the data that the publisher put in under the message key 
	//        NSData *mData1 = [txtDict objectForKey:@"webserver"]; 
	//        // Is there data? 
	//        if (mData1) { 
	//            // Make a string 
	//            message1 = [[NSString alloc] initWithData:mData1 encoding:NSUTF8StringEncoding]; 
	//            [message1 autorelease]; 
	//        } 
	//	}
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

- (UITableViewCell *)tableView:(UITableView *)tv cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{   
	NSString *message1 = [self->displayList objectAtIndex:[indexPath row]];    
	NSString *message2 = [self->wsList objectAtIndex:[indexPath row]];    
	//    NSString *message1 = nil; 
	//	NSString *message2=nil;
	//	
	//    // Try to get the TXT Record 
	//    NSData *data = [ns TXTRecordData]; 
	//    // Is the TXT data? (no TXT data in unresolved services) 
	//    if (data) { 
	//		
	//        // Convert it into a dictionary 
	//        NSDictionary *txtDict = [NSNetService dictionaryFromTXTRecordData:data]; 
	//        // Get the data that the publisher put in under the message key 
	//        NSData *mData1 = [txtDict objectForKey:@"webserver"]; 
	//        // Is there data? 
	//        if (mData1) { 
	//            // Make a string 
	//            message1 = [[NSString alloc] initWithData:mData1 encoding:NSUTF8StringEncoding]; 
	//            [message1 autorelease]; 
	//        } 
	//		NSData *mData2= [txtDict objectForKey:@"lasttitle"]; 
	//        // Is there data? 
	//        if (mData2) { 
	//            // Make a string 
	//            message2 = [[NSString alloc] initWithData:mData2 encoding:NSUTF8StringEncoding]; 
	//            [message2 autorelease]; 
	//        } 
	//    } 
	
	UITableViewCell *cell = [self->tableView dequeueReusableCellWithIdentifier:@"MatesTableViewCell"]; 
    if (!cell) { 
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle 
                                      reuseIdentifier:@"MatesTableViewCell"]; 
        [cell autorelease]; 
    } 
	
	NSString *message;
    // Did I fail to get a string? 
    if (!message1) { 
        // Use a default message 
        message = @"<No webserver>"; 
		cell.accessoryType = UITableViewCellAccessoryNone;
    } else
	{
		message = [NSString stringWithFormat:@"%@",message1];
		cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
	}
    [[cell textLabel] setText:message]; 
    [[cell detailTextLabel] setText:message2];
	return cell; 
} 
/*
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

/*
 // Override to allow orientations other than the default portrait orientation.
 - (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
 // Return YES for supported orientations
 return (interfaceOrientation == UIInterfaceOrientationPortrait);
 }
 */

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}

//
// this really should move into the base


- (void)netServiceBrowser:(NSNetServiceBrowser *)browser 
           didFindService:(NSNetService *)aNetService 
               moreComing:(BOOL)moreComing 
{ 	
	if(! ([[aNetService name] isEqualToString: [[[UIDevice currentDevice ] name] description]]))
	{  
		// dont show self on list
		
		NSLog(@"netServiceBrowser %@ name %@ type %@", 
			  aNetService,[aNetService name],[aNetService type]); 
		NSUInteger row = [self->displayList indexOfObject:[aNetService name]]; 
		if (row == NSNotFound) 	
		{
			[self->displayList addObject: [aNetService name]];
			[self->wsList addObject: @"<not yet>"];
			
			//[self->displayList addObject:[aNetService name]]; 
			//    // Update the interface 
			//	    NSIndexPath *ip = [NSIndexPath indexPathForRow:[self->displayList count]-1 
			//											 inSection:0]; 
			//		[self->tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:ip] 
			//							   withRowAnimation:UITableViewRowAnimationRight]; 
			
			[self->tableView reloadData]; // repain the whole thing
			// Start resolution to get TXT record 
			[aNetService setDelegate:self]; 
			[aNetService resolveWithTimeout:30]; // was 30
		}
		
	}
} 
- (void)netServiceDidResolveAddress:(NSNetService *)aNetService 
{ 
	if ((NSNull *)aNetService == [NSNull null])
		
	{
		//NSLog(@"netServiceDidResolveAddress is null",aNetService);
		return;
	}
	//    // What row just resolved? 
	//    int row = [self->displayList indexOfObjectIdenticalTo:aNetService]; 
	//	if (NSNotFound == row)
	//	{
	//		NSLog(@"netServiceDidResolveAddress notfound for %@",aNetService);
	//		return;
	//	}
	//	
	
	//    NSIndexPath *ip = [NSIndexPath indexPathForRow:row inSection:0]; 
	//    NSArray *ips = [NSArray arrayWithObject:ip]; 
	//    // Reload that row 
	//    [self->tableView reloadRowsAtIndexPaths:ips 
	//						   withRowAnimation:UITableViewRowAnimationRight]; 
	
	
	NSArray *addrarrs = [aNetService addresses];
	for  (NSData *addrs in addrarrs)
		//if([addrs count] > 0)
	{
		const struct sockaddr_in *addy = [addrs bytes];
		char *str = inet_ntoa(addy->sin_addr);
		if (strcmp("0.0.0.0",str)!=0)
		{
			NSData *data = [aNetService TXTRecordData]; 
			// Is the TXT data? (no TXT data in unresolved services) 
			if (data) { 
				// Convert it into a dictionary 
				NSDictionary *txtDict = [NSNetService dictionaryFromTXTRecordData:data]; 
				// Get the data that the publisher put in under the message key 
				NSData *mData1 = [txtDict objectForKey:@"webserver"]; 
				// Is there data? 
				if (mData1) { 
					// Make a string 
					NSString * message1 = [[[NSString alloc] initWithData:mData1 encoding:NSUTF8StringEncoding]
										   autorelease ]; 
					// splice together at the colon
					NSRange foo = [message1		rangeOfString:@":"];
					NSString *port = [message1 substringFromIndex:foo.location+1];
					char *colon = strchr(str,':'); // get part before colon
					int size = colon-str;
					if (size >100 ) size=100;
					// copy bytes
					char h[100];
					int i=0;
					while (i<size-1)
					{
						h [i] = str[i];
						i++;
					}
					h [i] = '\0'; // tie up as c string
					NSString *blended = [NSString stringWithFormat:@"%s:%@",h,port ,nil];					
					NSLog(@"netServiceDidResolveAddress %s:%d to service %@ ws %@  ", str, ntohs(addy->sin_port),aNetService,blended);
					NSUInteger row = [self->wsList indexOfObject:blended]; 
					if (row == NSNotFound) 	
						[self->wsList addObject: blended];
					else
						[self->wsList replaceObjectAtIndex:row withObject:blended];
					
				} 
			}
			
		}
	}
	
	[self->tableView reloadData];
}

// Called when services are lost 
- (void)netServiceBrowser:(NSNetServiceBrowser *)browser 
         didRemoveService:(NSNetService *)aNetService 
               moreComing:(BOOL)moreComing 
{ 
	if(! ([[aNetService name] isEqualToString: [[[UIDevice currentDevice ] name] description]]))
	{
		// never added ourselves to this list
		//		NSLog(@"netServiceBrowser:didRemoveService %@", aNetService); 
		//		// Take it out of the array 
		//		NSUInteger row = [self->displayList indexOfObject:aNetService]; 
		//		if (row == NSNotFound) { 
		//			NSLog(@"unable to find the service in %@", self->displayList); 
		//			return; 
		//		} 
		//		[self->displayList removeObjectAtIndex:row]; 
		// Update the interface 
		//	NSIndexPath *ip = [NSIndexPath indexPathForRow:row inSection:0]; 
		//		[self->tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:ip] 
		
		//							   withRowAnimation:UITableViewRowAnimationRight]; 
		[self->tableView reloadData];
		
	}
} 

- (void)dealloc 
{
	[displayList	release];
	[wsList release];
	//[resolvedAddrs  release];//= [[NSMutableDictionary alloc] init];
	//[netServices release]; // = [[NSMutableArray alloc] init]; 
	[serviceBrowser release]; //= [[NSNetServiceBrowser alloc] init];
    //[tableView release];
    [super dealloc];
}


@end

