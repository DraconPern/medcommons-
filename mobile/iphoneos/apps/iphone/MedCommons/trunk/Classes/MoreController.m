//
//  MoreController.m
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MoreController.h"

#import "DashboardPatient.h"
#import "HelpViewController.h"
#import "MedCommons.h"
#import "WebPaneController.h"

#import "HistoryTable.h"
#import "CustomViews.h"
#import "PatientStore.h"
#import "DataManager.h"

@implementation MoreController


- (id)init

{
	// Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
	self = [super initWithStyle:(UITableViewStyle)UITableViewStyleGrouped];
	{
		patientStore = [[DataManager sharedInstance] ffPatientStore];		
		customViews = [[DataManager sharedInstance] ffCustomViews];

	
	}
//	BREADCRUMBS_PUSH;
			
	return self;
}


- (void)viewDidLoad {
	[super viewDidLoad];
	self.navigationItem.title = @"MedCommons";
	if ([[DataManager sharedInstance] selfMode]==YES) 
	{
		self.navigationItem.hidesBackButton = YES;
		
		self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle: @"logout"
																				   style:UIBarButtonItemStylePlain
																				  target:self 
																				  action:@selector (logout)] autorelease];
		
	}
	
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


#pragma mark Table view methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
	return 3;
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	if (section==0) return 1;  // now one large lump
	if (section==1) return 4; // will expand to full list 
	if (section==2) return 2; // will expand to full list 
	
	return 0;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	static NSString *CellIdentifier = @"xxxsaaazy";
	
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if (cell == nil) {
		// cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];		
		cell = [[[UITableViewCell alloc] initWithFrame: CGRectZero reuseIdentifier:@"xxxsaaazy"] autorelease];
	}
	
	// Set up the cell...
	int row = indexPath.row;
	int section = indexPath.section;
	//CONSOLE_LOG (@"tableView cellforRowatIndexPath section %d row %d",section, row);
	
	cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
	[cell setHighlighted:NO animated:NO];
	if (section==0)
	{
		
		if (row==0)
		{
			cell.textLabel.text = @"Groups";
			
		}
	}
	else
		if (section==1)
		{
			if (row==0)
			{
				cell.textLabel.text = @"Help";
				
			}		
			else
				if (row==1)
				{
					cell.textLabel.text = @"Settings";
				}
			if (row==2)
			{
				cell.textLabel.text = @"Logout";
			}
			if (row==3)
			{
				cell.textLabel.text = @"www.medcommons.net";
			}
		} 
		else	
			if (section==2)
		{
			if (row==0) cell.textLabel.text = @"History";
				if (row==1) cell.textLabel.text = @"Comms Test";
			
			
		}


	return cell;
}
- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	return 42.0;
	//	int section = indexPath.section;
	//	//	int row = indexPath.row;
	//	if (section==1) return 44.0;
	//	NSString *foo = [wrapper summary];
	//	CGSize s = [foo sizeWithFont:[UIFont systemFontOfSize:14] constrainedToSize:CGSizeMake(296,300) lineBreakMode:UILineBreakModeWordWrap];
	//	float height =s.height+10.0f+10.0f;
	//	return height;	
}


-(void)  actionSheet: (UIActionSheet *) alertView clickedButtonAtIndex:(NSInteger) buttonIndex
{
	if( (alert_state ==2)&&(buttonIndex == 0)) 	
	{
		[customViews  customMainViewResetPrefs]; 
		[patientStore cleanup];
	}
	if( (alert_state ==2)&&(buttonIndex == 1)) 
	{
		[customViews  customMainViewResetPrefs]; 
		[patientStore cleanup];
	}
	if ((alert_state ==4)&&(buttonIndex == 0)) 
	{
		[DataManager sharedInstance].ffMCusername=@"";
		[DataManager sharedInstance].ffMCpassword=@"";
	
		[[NSUserDefaults standardUserDefaults] setValue:@"" forKey:@"username"];
		[[NSUserDefaults standardUserDefaults] setValue:@"" forKey:@"password"];
		[[NSUserDefaults standardUserDefaults] setBool: NO forKey:@"testuser"];
		[[NSUserDefaults standardUserDefaults] synchronize];
		exit(1);
	}
	
	[actionSheet release];
}

	

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	// Navigation logic may go here. Create and push another view controller.
	
	int section = indexPath.section;
	int row = indexPath.row;
	
	if (section==0)
	{
		
		if (row==0)
		{
		//	use this to trigger a dump to logfile of interesting stuff
			[[ [DataManager sharedInstance] ffPatientStore] dumpPatientStore];
		
			
		}
	}
	else
		if (section==1)
		{
			if (row==0)
			{
				
				HelpViewController *hvc  = [[HelpViewController alloc] init];
				[self.navigationController pushViewController:(UIViewController *)hvc 	animated:YES];
				[hvc release];	
		
				
			}		
			else
				if (row==1)
				{
					
					WebPaneController *cxvc  = [[WebPaneController alloc] 
											   initWithURL:[[NSString alloc] initWithFormat:@"http://ci.myhealthespace.com/midatastore/splash.html",nil] 
											   andWithTitle: @"Workspace"];
					[self.navigationController pushViewController:(UIViewController *)cxvc 	animated:YES];
					[cxvc release];	
				}
				else			if (row==2)
				{
					// logout
					alert_state = 4;
					
					actionSheet= [[UIActionSheet alloc] //initWithTitle:@"You have all the required data fields but you may want to supply additional data"
								  initWithTitle:@"Are You Sure You Want to Logout?\n You will need to visit Settings to Logon again" 
								  delegate:self cancelButtonTitle: @"Cancel"	destructiveButtonTitle:nil				
								  otherButtonTitles:@"Logout",
								  //@"Add More Fields",
								  nil];
					
					[actionSheet showFromTabBar:(UITabBar *)self.navigationController.view ];	
		}  else if (row==3)
		{
		
				WebPaneController *xvc  = [[WebPaneController alloc] 
										   initWithURL:[[NSString alloc] initWithFormat:@"http://www.medcommons.net/",nil] 
										   andWithTitle: @"medcommons"];
				[self.navigationController pushViewController:(UIViewController *)xvc 	animated:YES];
				[xvc release];	
				
				
		}

	
}
	else 
		if (section==2)
		{
			if (row==0) {
			HistoryTable *tvc  = [(HistoryTable *)[HistoryTable alloc] init];
			[self.navigationController pushViewController:(UIViewController *)tvc 	animated:YES];
			[tvc release];	
			}
			else if (row==1)
			{
				WebPaneController *xxvc  = [[WebPaneController alloc] 
										   initWithURL:[[NSString alloc] initWithFormat:	@"http://ci.myhealthespace.com/probe/selftest.html",nil] 
										   andWithTitle: @"Comms Test"];
				[self.navigationController pushViewController:(UIViewController *)xxvc 	animated:YES];
				[xxvc release];	
				
			}
		}
}


- (void)dealloc {
	//BREADCRUMBS_POP;
	[super dealloc];
}


@end

