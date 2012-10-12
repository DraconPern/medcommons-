//
//  MCSharingController.m
//  MedCommons
//
//  Created by bill donner on 1/3/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MCSharingController.h"
#import "DashboardPatient.h"
#import "MedCommons.h"
#import "WebPaneController.h"
#import "DataManager.h"


@implementation MCSharingController


- (id)init 		
{
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
	self = [super initWithStyle:UITableViewStyleGrouped];
	{
		wrapper = [[DataManager sharedInstance] ffPatientWrapper];	
    }
	//BREADCRUMBS_PUSH;
    return self;
}



- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.title = @"Sharing"; //[NSString stringWithFormat:@"%@ %@",wrapper.firstName,wrapper.lastName];
	if ([[DataManager sharedInstance] selfMode]==YES) self.navigationItem.hidesBackButton = YES;
	//TRY_RECOVERY;
	
}


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


#pragma mark Table view methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section==0) return 1;
	if (section==1) return 3; // will expand to full list of offenses
	
	return 0;
}
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    // The header for the section is the region name -- get this from the region at the section index.
	if (section==0) return @"Generate message with tracking # and single use pin";
	if (section==1) return @"and then"; // will expand to full list of offenses
	return @"";
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"axxsszy";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
		// cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];		
		cell = [[[UITableViewCell alloc] initWithFrame: CGRectZero reuseIdentifier:@"xszy"] autorelease];
    }
    
    // Set up the cell...
	int row = indexPath.row;
	int section = indexPath.section;
	//CONSOLE_LOG (@"tableView cellforRowatIndexPath section %d row %d",section, row);
	
	[cell setHighlighted:NO animated:NO];
	if (section==1)
	{
	

		if (row==2)
		{
			cell.textLabel.text = @"Send Invitation By Fax";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
		if (row==1)
		{
			cell.textLabel.text = @"Send Invitation By SMS";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
		if (row==0)
		{
			cell.textLabel.text = @"Send Invitation By Email";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
	}
	else if (section==0) 
	{
		cell.textLabel.font = [UIFont systemFontOfSize:14];
//		if (row==0) cell.textLabel.text = [NSString stringWithFormat:@"A new tracking # and PIN will be allocated",nil]; 
		if (row==0) 
			
		{ 			
			CGRect pinSwitchFrame = CGRectMake(200, 10, 120, 50);
			showPinSwitch = [[UISwitch alloc] initWithFrame:pinSwitchFrame ];
			[showPinSwitch setOn:NO animated:NO];
			[cell.contentView addSubview:showPinSwitch];
			
			cell.textLabel.text = @"include PIN in message"; 
	
			
		}

	}
	
	
    return cell;
}
- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	return 44.0;
	
}

- (void) pushWebPane:(NSString *) webPane
{
	// generate a sharing invitation via email
//	NSLog(@"a %@", [[DataManager sharedInstance]   ffMCappliance]);
//		NSLog(@"b %@", webPane);
//		NSLog(@"c %@", [[DataManager sharedInstance]   ffMCprovidermcid]);
//		NSLog(@"d %@", [[DataManager sharedInstance]   ffMCpassword]);
//		NSLog(@"e %@", [[DataManager sharedInstance]   ffMCappliance]);
//		NSLog(@"f %@", [[DataManager sharedInstance]   ffMCusername]);
//		NSLog(@"g %@",  wrapper.patientID);
//		NSLog(@"h %@", [wrapper name]);
//		NSLog(@"i %d", showPinSwitch.on);
	NSString *barImgUrl= [NSString stringWithFormat:@"http://%@/%@/%@%@&pmcid=%@&password=%@&from=%@&mcid=%@&name=%@&switchA=%d",
						 [[DataManager sharedInstance]   ffMCappliance], 
						  MOBILE_PATH, webPane,	MOBILE_SHARE_SUFFIX,						  
				  
						  [[DataManager sharedInstance]  ffMCprovidermcid],		    
						  [[DataManager sharedInstance]  ffMCpassword],
						  [[DataManager sharedInstance]   ffMCusername], 
						   wrapper.patientID,[wrapper name], 
						   showPinSwitch.on];
	WebPaneController *webPaneController = [[WebPaneController alloc] initWithURL:barImgUrl andWithTitle: [wrapper nameForTitle]];
	[self.navigationController pushViewController:webPaneController animated:YES];
	[webPaneController release];
	
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    //Create and push another view controller.

	int section = indexPath.section;
	int row = indexPath.row;
	if (section ==1)
	{
		if (row==0)[self pushWebPane:@"emailshareiphone"];
		else 
			if (row==1)[self pushWebPane:@"smsshareiphone"];
			else
				if (row==2)[self pushWebPane:@"faxshareiphone"];
	}
}


/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */


/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
 
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
 }   
 else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }   
 }
 */


/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
 }
 */


/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */


- (void)dealloc {
	//BREADCRUMBS_POP;
    [super dealloc];
}


@end

