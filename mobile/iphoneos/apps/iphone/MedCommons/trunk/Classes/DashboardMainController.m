//
//  DashboardMainController.m
//  ForensicFoto
//
//  Created by bill donner on 9/13/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//

#import "JSON.h"
#import "MedCommons.h"
#import "PatientStore.h";
#import "RESTComms.h";
#import "DashboardMainController.h"
#import "DashboardPatient.h"
#import "MCPatientController.h"
#import "DashboardTableViewController.h"
#import "DataManager.h"

@implementation DashboardMainController

- (void)dealloc
{
	
	
	[super dealloc];
}
-(DashboardMainController *) init{
	// we start the Main and Landscape Controllers in the LoadView routine, otherwise they might get ahead of us 
	self = [super init];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	
	return self;
}

-(void)screenUpdate: (id)obj
{
	
}
-(void)modalOff:(id)foo
{
	
	[modalView	removeFromSuperview];
	[modalView release];
	
}
-(void)modalOn:(NSString *)s withDismissalAfter:(float_t)delta
{
	
	modalView = [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]] retain];
	modalView.opaque = NO;
	modalView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.5f];
	
	UILabel *label = [[[UILabel alloc] init] autorelease];
	label.text = s;
	label.textColor = [UIColor whiteColor];
	label.backgroundColor = [UIColor clearColor];
	label.opaque = NO;
	label.center = CGPointMake(100.0f,100.0f);
	[label sizeToFit];
	[modalView addSubview:label];
	[[[DataManager sharedInstance] ffMainWindow] addSubview:modalView];
}





-(NSString *) getAllDashboardsForProviderFromMedCommonsNoCache
{
	BOOL old = [UIApplication sharedApplication].networkActivityIndicatorVisible; // turn this on then restore at bottom
	[UIApplication sharedApplication].networkActivityIndicatorVisible=YES;
	NSString *loginService= [[NSString stringWithFormat:@"http:/%@/%@",[[DataManager sharedInstance] ffMCappliance],
							  @"acct/loginservice.php"] retain];
	// careful no ? in arg lists here
	RESTComms *llc = [[DataManager sharedInstance] ffRESTComms];
	
	NSString *response = [llc postIt:
						  [NSString stringWithFormat:@"email=%@&password=%@",
						   [[DataManager sharedInstance] ffMCusername],
						   [[DataManager sharedInstance] ffMCpassword]]
						whichService:loginService withBitsFromPath:nil];	
	
	// we did initialize successfully so save that
	NSString *zpath =  CACHEDDASHBOARDPATH;
	NSError *error;
	if ([response writeToFile:zpath atomically:YES  encoding:NSUTF8StringEncoding error:&error  ])	
		CONSOLE_LOG (@"Wrote dashboard to %@ ",zpath,response)
		
		else 
			CONSOLE_LOG (@"Failed to write dashboard to %@  error %@ ",zpath,error,response);
	[UIApplication sharedApplication].networkActivityIndicatorVisible=old ;
	if (realtablecontroller) [realtablecontroller.tableView reloadData]; //
	return response;
}
-(NSString *) getAllDashboardsForProvider
{
	
	NSString *zpath =  CACHEDDASHBOARDPATH;
	NSString *response;
	//NSError *error;
	if ([[NSFileManager defaultManager] fileExistsAtPath:zpath])				
		
	{
		response = [[NSString stringWithContentsOfFile:zpath encoding:NSUTF8StringEncoding error:nil] retain];	
		CONSOLE_LOG (@"Restored dashboard from local filesystem");
		//		
		
		//	
		
		//		CONSOLE_LOG (@"Found dashboard from %@ ",zpath,nil);
		return response;
		
	}
	//	
	CONSOLE_LOG (@"No dashboard on local filesystem");
	response = [self getAllDashboardsForProviderFromMedCommonsNoCache];
	
	[response retain]; // hang on to this
	return response;
}


-(void) parse_mc_json_response:(NSString *)lastpostresponseline withGroupId:(NSString *)Groupid
{
	SBJSON *parser = [[SBJSON alloc] init];			
	mc_json_response = [[parser objectWithString:lastpostresponseline error:nil] retain];
	NSString *status = [mc_json_response objectForKey:@"status"];
	if (![@"ok" isEqualToString: status])  		
		LLC_LOG (@"postGenericRequestWithJSONResponse bad status %@",status);
	[parser release];
	
	
	NSDictionary *result = [mc_json_response objectForKey:@"result"];
	NSString *auth = [result objectForKey:@"auth"];
	NSDictionary *practice = [result objectForKey:@"practice"];
	NSArray *patients = [practice objectForKey:@"patients"];
	NSString *practicename = [practice objectForKey:@"practicename"];
	NSMutableArray *xpatients = [[NSMutableArray alloc] initWithCapacity:[patients count]];
	
	for (NSDictionary *patientAttribute in patients) { // must change soon to match on accid and put into the correct group
		BOOL background = NO;
		DashboardPatient *wrapper = 
		[[DashboardPatient alloc]  initWithFirstName:[patientAttribute objectForKey:@"PatientGivenName"]
											lastName:[patientAttribute objectForKey:@"PatientFamilyName"]
										   patientID:[patientAttribute objectForKey:@"PatientIdentifier"] 
										  patientSex:[patientAttribute objectForKey:@"PatientSex"]
										  patientDOB:[patientAttribute objectForKey:@"DOB"]
									   patientStatus:[patientAttribute objectForKey:@"order_status"]
									 patientDateTime:[patientAttribute objectForKey:@"CreationDateTime"]
									  patientPurpose:[patientAttribute objectForKey:@"Purpose"]
											photoURL:[patientAttribute objectForKey:@"photoUrl"] // new - replaces
											  danger:background];
		[xpatients addObject:wrapper];
		[wrapper release];
	}
	
	[DataManager sharedInstance].ffMCauth = auth;
	[DataManager sharedInstance].ffMCpracticename = practicename;
	[DataManager sharedInstance].ffMCpatientlist=  xpatients;
}
-(void) dashboardForGroup:(NSString *) accid
{
	[self parse_mc_json_response:lastValidMetaBoard withGroupId:accid];
	
}

-(void) loadMasterBoard:(BOOL) getlocal
{
	//////////
	///////// p r o c e s s r e s p o n s e from loginservice.php
	/////////
	if (getlocal == YES)
	lastValidMetaBoard = [ self getAllDashboardsForProvider]; 
	else {
		lastValidMetaBoard - [ self getAllDashboardsForProviderFromMedCommonsNoCache];
	}

	// this call does too much: [self loadAndReloadMetaBoard];
	[self dashboardForGroup:@"12345"]; // parse into a Medcommons Response
	
	[DataManager sharedInstance].ffMCpracticecursor = @"0";
	NSString *status = [mc_json_response objectForKey:@"status"];
	if (NO == [@"ok" isEqual:status]) 
	{
		CONSOLE_LOG (@"bad status from loginservice - %@", status);
		
		[DataManager sharedInstance].ffMCauth = @"";
		[DataManager sharedInstance].ffMCpracticename = @"";
		[DataManager sharedInstance].ffMCpatientlist=  nil;
		
		return;
	}
	
	
	//////////
	///////// everything set as side effects in master config table
	//////////
}
#pragma mark routines called from the tableviewcontroller
-(void) loadNextDashBoard
{
	// deeply sub optimal 
	NSArray *foo = [[DataManager sharedInstance] ffMCpatientlist]; 
	[foo release]; // remove existing dashboard
	
	// now step to next and rebuild
	int prcursor = [[DataManager sharedInstance].ffMCpracticecursor  integerValue];
	// bump to next dashboard
	if (prcursor < MAXGROUPS)
		[DataManager sharedInstance].ffMCpracticecursor = [NSString stringWithFormat:@"%d",++prcursor];
	else 
		[DataManager sharedInstance].ffMCpracticecursor = @"0";@"0";
	[self dashboardForGroup:@"12345"];
	
}
-(int) howManyDashBoards
{
	return MAXGROUPS;
	
}
-(void) getdashboards:(id)foo
{
	[self modalOn:@"reloading dashboards..." withDismissalAfter:0.0];
	[self getAllDashboardsForProviderFromMedCommonsNoCache]; // rewrite the disk file for side effect
	[self modalOff:nil];
	[self loadMasterBoard:NO];
	if (realtablecontroller) [realtablecontroller.tableView reloadData]; // force update
}



-(void) getboards_background_thread:(id)foo // this is the place to start the background going
{
	NSInvocationOperation *op = [[NSInvocationOperation alloc] initWithTarget:self selector:@selector(getdashboards:) object:nil];
	[[[DataManager sharedInstance ] ffSharedOperationQueue] addOperation:op];  
	[op release];
	[self performSelector:@selector (getboards_background_thread:) withObject:nil afterDelay:30.0f]; // wait 30 and go again
}


#pragma mark MAIN ROUTINE FOR ENTIRE IPHONE PROGAM - PAINTS THE FIRST PAGE SEEN
-(void)loadView
{
	[super loadView]; // must must
	
	realtablecontroller = nil ; // assume its going to be a single patient
	//nav.navigationBar.barStyle = UIBarStyleBlackTranslucent;
	self.navigationController.navigationBar.barStyle = UIBarStyleBlackTranslucent;
	
	// make a view with just the top navigation and almost nothing more, the main part will be overridden
	CGRect contentRect = [[UIScreen mainScreen] applicationFrame];	
	
	UIView *outerView = [[UIView alloc] initWithFrame:contentRect];
	outerView.backgroundColor = [UIColor blackColor];
	
	self.view = outerView;	
	
	[self loadMasterBoard:YES];
	NSString *status = [mc_json_response objectForKey:@"status"];
	
	if ([@"ok" isEqualToString:status])
	{
		UIViewController *tablecontroller;
		NSDictionary *result = [mc_json_response objectForKey:@"result"];
		NSString *auth = [result objectForKey:@"auth"];
		[DataManager sharedInstance].ffMCauth = auth;
		
		id  practice = [result objectForKey:@"practice"];
		
		//if ((practice)&&([practice count]>0))
		if ( [practice isKindOfClass: [NSDictionary class] ])
		{
			// ok we have one or more groups , response will get reparsed inside the tableviewcontrolle			
			[[DataManager sharedInstance] setSelfMode:NO]; //put us into regular mode
			[DataManager sharedInstance].ffMCprovidermcid = [result objectForKey:@"accid"];
		}
		else {
			[[DataManager sharedInstance] setSelfMode:YES]; //put us into SelfMode
			[DataManager sharedInstance].ffMCprovidermcid = @"";
		}
		
		////// SEE IF WE HAVE SOMETHING IN THE RECOVERYBREADCRUMBS //////////

		NSString *s = [[[DataManager sharedInstance] ffBreadCrumbs] popRecoveryCrumb];
		if (!s)
		{
            // we are not in a recovery
		   	if ( [practice isKindOfClass: [NSDictionary class] ])
			{
				realtablecontroller = [[DashboardTableViewController alloc]  
															init]; // 
				tablecontroller = realtablecontroller; // mark this so it can be updated
			}
			else {
				DashboardPatient *wrapper = 
				[[DashboardPatient alloc]  initWithFirstName:[result objectForKey:@"fn"]
													lastName:[result objectForKey:@"ln"]
												   patientID:[result objectForKey:@"accid"] 
												  patientSex:@""
												  patientDOB:@""
											   patientStatus:@""
											 patientDateTime:@""
											  patientPurpose:@""
													photoURL:@""
													  danger:YES];
				
				
				[[NSUserDefaults standardUserDefaults] setValue:@"1" forKey:@"patientcrumbs"];
				[DataManager sharedInstance].ffPatientWrapper = wrapper; // stash in global on way in
				
				tablecontroller = 
				(UIViewController *)[[MCPatientController alloc] init] ;
			
			}
			
			BREADCRUMBS_LOG (@">>>not restoring starting clean with no patient set");
			[[NSUserDefaults standardUserDefaults] setValue:@"-1" forKey:@"patientcrumbs"]; // no patient yet 
			[self.navigationController pushViewController:tablecontroller animated:YES]; // ok to animate, we are going slowly
			[tablecontroller release];
		} else {
			// we are in a recovery
			BREADCRUMBS_LOG (@">>>restoring %@ from existing patient %@",s,[[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"]);
			UIViewController *viewc = [[NSClassFromString(s) alloc] init];
			[self.navigationController pushViewController:viewc 	animated:NO];
			[viewc release];
			// fall out to outer level
		}

	} // ok status
	else {
		// status was not good 
		CGRect labelsubjectLongNameFrame =	CGRectMake(5.0f, 124.0f, 310.0f, 250.0f);
		UILabel *labelsubjectLongName = [[UILabel alloc] initWithFrame:labelsubjectLongNameFrame] ;
		labelsubjectLongName.textAlignment = UITextAlignmentCenter;
		labelsubjectLongName.font = [UIFont fontWithName:@"Arial" size:20];
		labelsubjectLongName.textColor = [UIColor whiteColor];
		labelsubjectLongName.backgroundColor = [UIColor lightGrayColor];
		labelsubjectLongName.text = @"please check your settings";
		
		self.navigationItem.title = @"Not Logged On to MedCommons";
		[outerView addSubview:labelsubjectLongName];
	}
	[outerView release];
	[self getboards_background_thread:nil]; // get the background cooking, let's see what happens
}

// override to allow orientations other than the default portrait orientation
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	// return YES for supported orientations
	return UIInterfaceOrientationIsPortrait(interfaceOrientation);
}

@end

