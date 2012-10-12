//
//  MCFaxController.m
//  MedCommons
//
//  Created by bill donner on 1/3/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MCFaxController.h"

#import "DashboardPatient.h"
#import "MedCommons.h"
#import "WebPaneController.h"
#import "DataManager.h"


@implementation MCFaxController


- (id)init
{
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
	self = [super initWithStyle:(UITableViewStyle)UITableViewStyleGrouped ];
	{
				wrapper = [[DataManager sharedInstance] ffPatientWrapper];
    }
	BREADCRUMBS_PUSH;    return self;
}



- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.title = @"Fax Cover Sheets"; //[NSString stringWithFormat:@"%@ %@",wrapper.firstName,wrapper.lastName];
		if ([[DataManager sharedInstance] selfMode]==YES) self.navigationItem.hidesBackButton = YES;
	
	TRY_RECOVERY;
	
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
    if (section==0) return 2;
	if (section==1) return 3; // will expand to full list of offenses
	
	return 0;
}
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    // The header for the section is the region name -- get this from the region at the section index.
	if (section==0) return @"Optionally share by adding tracking # and single use pin";
	if (section==1) return @"and then"; // will expand to full list of offenses
	return @"";
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"xxsszyxx";
    
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
			cell.textLabel.text = @"Fax a Fax Cover";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
		if (row==1)
		{
			cell.textLabel.text = @"Email a Fax Cover";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
		if (row==0)
		{
			cell.textLabel.text = @"Print a Fax Cover";
			cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
			
		}
	}
	else if (section==0) 
	{
		cell.textLabel.font = [UIFont systemFontOfSize:14];
		
		if (row==0) 
		{ 			
			CGRect pinSwitchFrame = CGRectMake(200, 10, 120, 50);
			showPinSwitcha = [[UISwitch alloc] initWithFrame:pinSwitchFrame ];
			[showPinSwitcha setOn:NO animated:NO];
			[cell.contentView addSubview:showPinSwitcha];
			
			cell.textLabel.text = @"add Tracking #"; 
			
			
		}
		if (row==1) {
			
			{ 			
				CGRect pinSwitchFrame = CGRectMake(200, 10, 120, 50);
				showPinSwitchb = [[UISwitch alloc] initWithFrame:pinSwitchFrame ];
				[showPinSwitchb setOn:NO animated:NO];
				[cell.contentView addSubview:showPinSwitchb];
				
				cell.textLabel.text = @"add Tracking # and PIN"; 
				
				
			}
		}
	}
	
	
    return cell;
}
- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	//	//int row = indexPath.row;
	//	int section = indexPath.section;
	//	int row = indexPath.row;
	//	
	//	
	//	if (section==0) return 44.0;
	//	
	//	CONSOLE_LOG (@"tableView heightForRowAtIndexPath section %d row %d text %@ ",section, row, wrapper.description);
	//	
	//	//CGSize s = [wrapper.description sizeWithFont:[UIFont systemFontOfSize:14] constrainedToSize:CGSizeMake(280, 500)];
	//	CGSize s = [wrapper.patientPurpose sizeWithFont:[UIFont systemFontOfSize:14] 
	//								  constrainedToSize:CGSizeMake(296,500) lineBreakMode:UILineBreakModeWordWrap];
	//	float height = s.height+24.0f;
	//	CONSOLE_LOG(@"height is %f",height);
	//	
	//	return height;
	return 44.0;
	
}
- (void) pushWebPane:(NSString *) webPane
{
	NSString *barImgUrl= [NSString stringWithFormat:@"http://%@/%@/%@%@&pmcid=%@&password=%@&from=%@&mcid=%@&name=%@&coverid=%@&switchA=%d&switchB=%d",
						  [[DataManager sharedInstance]   ffMCappliance],MOBILE_PATH,webPane,	MOBILE_SHARE_SUFFIX,	 
						   [[DataManager sharedInstance]  ffMCprovidermcid],		    [[DataManager sharedInstance]  ffMCpassword]
						 ,
						 [[DataManager sharedInstance]   ffMCusername],             wrapper.patientID,[wrapper name], 
						  @"1", showPinSwitcha.on,showPinSwitchb.on 
						  ];
	WebPaneController *webPaneController = [[WebPaneController alloc] initWithURL:barImgUrl andWithTitle: [wrapper nameForTitle]];
	
	
	[self.navigationController pushViewController:webPaneController animated:YES];
	[webPaneController release];
	
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Create and push another view controller.
	
	
	
	
	int section = indexPath.section;
	
	int row = indexPath.row;
	if (section ==1)
	{
		
		if (row==0)[self pushWebPane:@"printcoveriphone"];
		else 
			if (row==1)[self pushWebPane:@"emailcoveriphone"];
			else
				if (row==2)[self pushWebPane:@"faxcoveriphone"];
		
	}
}


- (void)dealloc {
	BREADCRUMBS_POP;
    [super dealloc];
}


@end

