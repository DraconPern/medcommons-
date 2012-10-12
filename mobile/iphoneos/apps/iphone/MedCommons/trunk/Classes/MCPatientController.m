//
//  MCPatientController.m
//  MedCommons
//
//  Created by bill donner on 12/28/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "DashboardPatient.h"
#import "MedCommons.h"
#import "WebPaneController.h"
#import "MCQAController.h"
#import "MCSOAPController.h"
#import "MCPatientController.h"
#import "MCSharingController.h"
#import "MCFaxController.h"
#import "MCShooterController.h"
#import "MCBlogViewer.h"
#import "DataManager.h"
#import "PatientStore.h"


@implementation MCPatientController

// this is a grouped table
- (id)init		 
{
	self = [super initWithStyle:(UITableViewStyle)UITableViewStyleGrouped];
	wrapper = [[DataManager sharedInstance] ffPatientWrapper];
	
	[DataManager sharedInstance].ffPatientStore	= [[PatientStore alloc] initWithMcid:wrapper.patientID];// switch to it baby
	[DataManager sharedInstance].ffMCmcid =wrapper.patientID; // also reset this each time
	NSLog(@"created new patientcontroller for %@",wrapper);
	BREADCRUMBS_PUSH;
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	if (![prefs objectForKey:@"blogEntryA"]) [prefs setValue:@"" forKey:@"blogEntryA"];	
	if (![prefs objectForKey:@"blogEntryP"]) [prefs setValue:@"" forKey:@"blogEntryP"];
	if (![prefs objectForKey:@"blogEntryS"]) [prefs setValue:@"" forKey:@"blogEntryS"];
	if (![prefs objectForKey:@"blogEntryD"]) [prefs setValue:@"" forKey:@"blogEntryD"];
	if (![prefs objectForKey:@"blogEntryC"]) [prefs setValue:@"" forKey:@"blogEntryC"];
	
	return self;
}




- (void)viewDidLoad {
	[super viewDidLoad];
	self.navigationItem.title = [wrapper nameForTitle];
	if ([[DataManager sharedInstance] selfMode]==YES) 
	{
		self.navigationItem.hidesBackButton = YES;
		self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle: @"logout"
																				   style:UIBarButtonItemStylePlain
																				  target:self 
																				  action:@selector (logout)] autorelease];
	}


	
TRY_RECOVERY;

}

-(void)viewWillAppear:(BOOL)animated
{
	
	[self.tableView reloadData];
	[super viewWillAppear:animated];
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
	if (section==0) return 2;  // now one large lump
	if (section==1) return 3; // will expand to full list 
	
	if (section==2) return 2; // will expand to full list 
	
	return 0;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	static NSString *CellIdentifier = @"xsaaazy";
	
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if (cell == nil) {
		// cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];		
		cell = [[[UITableViewCell alloc] initWithFrame: CGRectZero reuseIdentifier:@"xsaaazy"] autorelease];
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
			cell.textLabel.text = @"Open HealthURL";
			
		}
		if (row==1)
		{
			cell.textLabel.text = @"Patient Photo";
			
		}
	}
	else
		if (section==1)
		{
			if (row==0)
			{
				PatientStore *ps = [[DataManager sharedInstance] ffPatientStore];
				NSInteger count = [ps countPartPics];
				if ([ps haveSubjectPhoto]) count++;
				if (count>0)
				cell.textLabel.text = [NSString stringWithFormat:@"Camera - %d unsent",count];else
				
				cell.textLabel.text = [NSString stringWithFormat:@"Camera "];
				
			}		
			else
				if (row==1)
				{
					NSInteger count=0;
					NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
					if ([prefs objectForKey:@"blogEntryA"]&&([[prefs objectForKey:@"blogEntryA"] length]>0)) count++;	
					
					if ([prefs objectForKey:@"blogEntryP"]&&([[prefs objectForKey:@"blogEntryP"] length]>0)) count++;	
					
					if ([prefs objectForKey:@"blogEntryS"]&&([[prefs objectForKey:@"blogEntryS"] length]>0)) count++;	
					
					if ([prefs objectForKey:@"blogEntryD"]&&([[prefs objectForKey:@"blogEntryD"] length]>0)) count++;	
					
					if ([prefs objectForKey:@"blogEntryC"]&&([[prefs objectForKey:@"blogEntryC"] length]>0)) count++;	
					
					if (count>0)
						cell.textLabel.text = [NSString stringWithFormat:@"SOAP - %d unsent",count];else
							
							cell.textLabel.text = [NSString stringWithFormat:@"SOAP "];
				}
			if (row==2)
			{
				cell.textLabel.text = @"QA";
			}
		} 
		else
			if (section==2)
			{
				if (row==0)
				{
					cell.textLabel.text = @"Share";
					
				}
				else
					if (row==1)
					{
						cell.textLabel.text = @"Fax Cover Sheets";
					}
			}
	return cell;
}
- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	return 44.0;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	// Navigation logic may go here. Create and push another view controller.
	
	int section = indexPath.section;
	int row = indexPath.row;
	////[DataManager sharedInstance].ffMCusername=@"";
	 /////[DataManager sharedInstance].ffMCpassword=@"";
	if (section==0)
	{
		
		if (row==0)
		{
			
			NSString* webURL = //[NSString stringWithFormat:@"http://%@/router/put/%@/?auth=%@",
			[NSString stringWithFormat:@"http://%@/%@/?auth=%@",
			 [DataManager sharedInstance].ffMCappliance,
			 wrapper.patientID,
			[DataManager sharedInstance].ffMCauth];
			
			[[UIApplication sharedApplication] openURL:[[NSURL alloc] initWithString:webURL]];
			
		}
		if (row==1)
		{
			NSString *url = [wrapper photoURL];
			if (url==nil) url = @"http://www.medcommons.net/";
			if ([@"" isEqual: url]) url = @"http://www.medcommons.net/";
			if ([url length] < 6) url = @"http://www.medcommons.net/";
			WebPaneController *txwebPaneController = [(WebPaneController *)[WebPaneController alloc] initWithURL:url
																									andWithTitle: [wrapper nameForTitle]];

			[self.navigationController pushViewController:txwebPaneController animated:YES];
			[txwebPaneController release];
			
		}
	}
	else
		if (section==1)
		{
			if (row==0)
			{
		
		
				 self.hidesBottomBarWhenPushed = YES;
				// generate a soap controller which will return results thru 
				MCShooterController *cameraController;	
			
				cameraController = 
				(MCShooterController *)[[MCShooterController alloc] init];
		
				[self.navigationController pushViewController:(UIViewController *)cameraController 	animated:YES];
				[cameraController release];	
				
			}		
			else
				if (row==1)
				{
					
					
					self.hidesBottomBarWhenPushed = YES;
					
					
					MCBlogViewer *xwebPaneController = [(MCBlogViewer *)[MCBlogViewer alloc] init ];
				
						[self.navigationController pushViewController:xwebPaneController animated:YES];
						[xwebPaneController release];
				
					
				}
			else			if (row==2)
			{
				
				self.hidesBottomBarWhenPushed = YES;
				// generate a qa controller which will return results thru 
				MCQAController *qaController;	
				

				qaController = 
				(MCQAController *)[[MCQAController alloc] init];
				MY_ASSERT(qaController!=nil);
				[self.navigationController  pushViewController:(UIViewController *)qaController 	animated:YES];
				[qaController release];	
				self.hidesBottomBarWhenPushed = NO;
			}
		} 
		else
			if (section==2)
			{
				if (row==0)
				{
					// generate a sharing controller
					MCSharingController *placesSharingController;	
					placesSharingController = 
					(MCSharingController *)[[MCSharingController alloc] init
																						  ] ;
					MY_ASSERT(placesSharingController!=nil);
					[self.navigationController  pushViewController:(UIViewController *)placesSharingController 	animated:YES];
					[placesSharingController release];		
					
				}
				else
					if (row==1)
					{
						MCFaxController *placesFaxController;	
						placesFaxController = 
						(MCFaxController *)[[MCFaxController alloc] init																	] ;
						MY_ASSERT(placesFaxController!=nil);
						[self.navigationController  pushViewController:(UIViewController *)placesFaxController 	animated:YES];
						[placesFaxController release];		
					}
			}
	
}




- (void)dealloc {
	[[[DataManager sharedInstance] ffPatientStore]	writePatientStore];//save this patient out to disk so we can restore him correctly
	NSString *s=@"-1";
	[[DataManager sharedInstance] setSelectedPatientIndex:s];
	[[NSUserDefaults standardUserDefaults] setValue:s forKey:@"patientcrumbs"]; // no one set yet
	NSLog (@"Leaving patient context, setting patientcrumbs to -1");
	
	BREADCRUMBS_POP;
	[DataManager sharedInstance].ffPatientWrapper= nil; // when we leave there is no current patient
	[super dealloc];
}


@end

