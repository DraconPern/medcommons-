//
//  DashboardTableViewController.m
//  MedCommons
//
//  Created by bill donner on 12/27/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "MCPatientController.h"
#import "DashboardTableViewController.h"
#import "MoreController.h"

#import "JSON.h"
#import "DashboardPatient.h"
#import "MedCommons.h"
#import "RESTComms.h"
#import "PatientStore.h"
#import "CustomViews.h"
#import "DashboardMainController.h"

#import "DataManager.h"


#pragma mark Configuring table view cells

#define PATIENTNAME_TAG 1
#define PATIENTID_TAG 2
#define PATIENTSEX_TAG 3
#define PATIENTDOB_TAG 4
#define PATIENTSTATUS_TAG 5
#define PATIENTPURPOSE_TAG 6
#define PATIENTDATETIME_TAG 7
#define IMAGE_TAG 8
#define PATIENTHEADER_TAG 9


#define LEFT_COLUMN_OFFSET 10.0
#define LEFT_COLUMN_WIDTH 140.0

#define MIDDLE_COLUMN_OFFSET 150.0
#define MIDDLE_COLUMN_WIDTH 110.0

#define RIGHT_COLUMN_OFFSET 280.0
#define LARGE_FONT_SIZE 16.0
#define MAIN_FONT_SIZE 14.0
#define SMALL_FONT_SIZE 12.0
#define LABEL_HEIGHT 20.0

#define IMAGE_SIDE 20.0
#define ROW_HEIGHT 64



@implementation DashboardTableViewController
@synthesize	patientsArray, sectionsArray, collation;
@synthesize myAuth;
@synthesize practiceGroupName;


-(NSArray *) countsForMcid:(NSString *)_mcid
{
	NSMutableArray *retval = [[NSMutableArray alloc] init]; // prepare array of strings
	
	NSLog (@"counting patient store for %@", _mcid);
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",_mcid]];
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"countsForMcid has no plist for this patient %@",_mcid);
		retval = [NSArray arrayWithObjects :@"0",@"0",@"0",@"0",nil];// ok, no plist, so lets just initialize
		
	}
	else 
	{
		// we have the plist, lets read it and fix things up
		NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
		NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
											  propertyListFromData:plistXML
											  mutabilityOption:NSPropertyListMutableContainersAndLeaves
											  format:&format
											  errorDescription:&errorDesc];
		if (!temp) {
			NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
		}
		NSArray *photos = [temp objectForKey:@"photospecs"];
		NSInteger unsentpiccount = 0;
		for (NSString *photo in photos)
			if ([photo length]>0) unsentpiccount++;
		NSArray *vids = [temp objectForKey:@"videospecs"];
		NSInteger unsentvidcount = 0;
		for (NSString *vid in vids)
			if ([vid length]>0) unsentvidcount++;
		NSInteger unsentsoapcount=0;
		NSMutableDictionary *prefs = [temp objectForKey:@"prefs"];
		
		if ([prefs objectForKey:@"blogEntryA"]&&([[prefs objectForKey:@"blogEntryA"] length]>0)) unsentsoapcount++;	
		
		if ([prefs objectForKey:@"blogEntryP"]&&([[prefs objectForKey:@"blogEntryP"] length]>0)) unsentsoapcount++;	
		
		if ([prefs objectForKey:@"blogEntryS"]&&([[prefs objectForKey:@"blogEntryS"] length]>0)) unsentsoapcount++;	
		
		if ([prefs objectForKey:@"blogEntryD"]&&([[prefs objectForKey:@"blogEntryD"] length]>0)) unsentsoapcount++;	
		
		if ([prefs objectForKey:@"blogEntryC"]&&([[prefs objectForKey:@"blogEntryC"] length]>0)) unsentsoapcount++;	
		
		NSString *qastate = @"";
		if ( [prefs objectForKey:@"QAState"]) 
			qastate =  [prefs objectForKey:@"QAState"];

		
		// return these integers as strings, we could instead return a nice UIView
		
		
		
		
		retval = [NSArray arrayWithObjects:
				  [NSString stringWithFormat:@"%d",unsentpiccount],
				  [NSString stringWithFormat:@"%d",unsentvidcount],
				  [NSString stringWithFormat:@"%d",unsentsoapcount],
				   qastate, // and whatever this was marked as 
				  nil];		
	}
	[retval retain];// up to caller to free this
	return retval;
	
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
		[DataManager sharedInstance].ffMCusername =@"";
			[DataManager sharedInstance].ffMCpassword =@"";
		
		[[NSUserDefaults standardUserDefaults] setValue:@"" forKey:@"username"];
		[[NSUserDefaults standardUserDefaults] setValue:@"" forKey:@"password"];
		[[NSUserDefaults standardUserDefaults] setBool: NO forKey:@"testuser"];
		[[NSUserDefaults standardUserDefaults] synchronize];
		exit(1);
	}
	
	[actionSheet release];
}
-(void) showActionSheet
{
	[actionSheet showInView:self.view ];//self.view];
}
-(void) logout
{
	alert_state = 4;
	
	actionSheet= [[UIActionSheet alloc] //initWithTitle:@"You have all the required data fields but you may want to supply additional data"
				  initWithTitle:@"Are You Sure You Want to Logout?\n You will need to visit Settings to Logon again" 
				  delegate:self cancelButtonTitle: @"Cancel"	destructiveButtonTitle:nil				
				  otherButtonTitles:@"Logout",
				  //@"Add More Fields",
				  nil];
	
	[self showActionSheet];
	
	
	
	
}

-(void) newsubject 
{

		alert_state = 2;
		
		
		
		actionSheet = [[UIActionSheet alloc] initWithTitle:@"Are you sure? You will lose the info for the current subject" 
												  delegate:self cancelButtonTitle: @"Cancel"
									destructiveButtonTitle: @"New Subject"
										 otherButtonTitles:@"New Series for Subject",nil];
		[self showActionSheet];

}
-(void) nextDashboard :(id) object
{
	[mainOuterController loadNextDashBoard]; // change the plumbin
	[self.tableView reloadData]; // we hope
}
-(void) more :(id) object
{
	
	MoreController *hvc  = [(MoreController *)[MoreController alloc] init];
	[self.navigationController pushViewController:(UIViewController *)hvc 	animated:YES];
	[hvc release];	
}
#pragma mark -

- (UITableViewCell *)tableViewCellWithReuseIdentifier:(NSString *)identifier {
	
	/*
	 Create an instance of UITableViewCell and add tagged subviews for the name, local time, and quarter image of the time zone.
	 */
	
	UITableViewCell *cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier] autorelease];
	
	/*
	 Create labels for the text fields; set the highlight color so that when the cell is selected it changes appropriately.
	 */
	UILabel *label;
	CGRect rect;
	
	// Create a label for the name.
	rect = CGRectMake(LEFT_COLUMN_OFFSET, 3, LEFT_COLUMN_WIDTH, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTNAME_TAG;
	label.font = [UIFont boldSystemFontOfSize:LARGE_FONT_SIZE];
	label.adjustsFontSizeToFitWidth = NO;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	// Create a label for the sex
	rect = CGRectMake(LEFT_COLUMN_OFFSET+140, 3, 60, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTSEX_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentRight;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	// Create a label for the age.
	rect = CGRectMake(LEFT_COLUMN_OFFSET+210, 3, 30, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTDOB_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentLeft;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	// Create a label for the id.
	rect = CGRectMake(LEFT_COLUMN_OFFSET, (ROW_HEIGHT - LABEL_HEIGHT) -20, 60, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTID_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentLeft;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	// Create a label for the status.
	rect = CGRectMake(LEFT_COLUMN_OFFSET+70, (ROW_HEIGHT - LABEL_HEIGHT) -20, 200, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTSTATUS_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentLeft;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	
	
	// Create a label for the datetime.
	rect = CGRectMake(LEFT_COLUMN_OFFSET, (ROW_HEIGHT - LABEL_HEIGHT) -1.0, 75, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTDATETIME_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentLeft;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];
	
	// Create a label for the purpose.
	rect = CGRectMake(LEFT_COLUMN_OFFSET+80, (ROW_HEIGHT - LABEL_HEIGHT) -1.0, 200, LABEL_HEIGHT);
	label = [[UILabel alloc] initWithFrame:rect];
	label.tag = PATIENTPURPOSE_TAG;
	label.font = [UIFont systemFontOfSize:SMALL_FONT_SIZE];
	label.textAlignment = UITextAlignmentLeft;
	[cell.contentView addSubview:label];
	label.highlightedTextColor = [UIColor whiteColor];
	[label release];

	return cell;
}


- (void)configureCell:(UITableViewCell *)cell forIndexPath:(NSIndexPath *)indexPath {
	UILabel *label;
//    if (indexPath.section == 0)
//	{
//		
//		label = (UILabel *)[cell viewWithTag:PATIENTHEADER_TAG];
//		label.text = @"Counters will go here";
//	}
//	else {


	//CONSOLE_LOG (@"configure cell section %d row %d",indexPath.section,indexPath.row);
    /*
	 Cache the formatter. Normally you would use one of the date formatter styles (such as NSDateFormatterShortStyle), but here we want a specific format that excludes seconds.
	 */
	static NSDateFormatter *dateFormatter = nil;
	if (dateFormatter == nil) {
		dateFormatter = [[NSDateFormatter alloc] init];
		[dateFormatter setDateFormat:@"h:mm a"];
	}
	
	// Get the time zone from the array associated with the section index in the sections array.
	NSArray *patientsInSection = [sectionsArray objectAtIndex:0];
	// Configure the cell with the time zone's name.
	DashboardPatient *wrapper = [patientsInSection objectAtIndex:indexPath.row];
 
	// Set the locale name.
	label = (UILabel *)[cell viewWithTag:PATIENTNAME_TAG];
	label.text = [[NSString stringWithFormat:@"%@ %@",wrapper.firstName,wrapper.lastName] 
				  stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	if ([wrapper dangerdanger])
		label.textColor = [UIColor redColor];
	else 
		
		label.textColor = [UIColor blackColor];

	// read the associated plist file if there is one
	NSArray *results = [self countsForMcid: wrapper.patientID];
	//NSLog(@"plist count for user %@ are %@", wrapper.patientID, results); 
	

	
	label = (UILabel *)[cell viewWithTag:PATIENTSEX_TAG];
	label.text = wrapper.patientSex;//[dateFormatter stringFromDate:[NSDate date]];
	
	label = (UILabel *)[cell viewWithTag:PATIENTDOB_TAG];
	label.text = wrapper.patientDOB;//[dateFormatter stringFromDate:[NSDate date]];
	
	label = (UILabel *)[cell viewWithTag:PATIENTSTATUS_TAG];
	label.text =[NSString stringWithFormat:@"%@ unsent photos  - %@ unsent SOAP notes", [results objectAtIndex:0],[results objectAtIndex:2]]; //unsentsoap; //// //unsentphotos; ////wrapper.patientStatus;//[dateFormatter stringFromDate:[NSDate date]];
	
	label = (UILabel *)[cell viewWithTag:PATIENTID_TAG];
	label.text = wrapper.patientID;//[dateFormatter stringFromDate:[NSDate date]];
	
	
	label = (UILabel *)[cell viewWithTag:PATIENTPURPOSE_TAG];
	label.text = wrapper.patientPurpose;//[dateFormatter stringFromDate:[NSDate date]];
	
	
	label = (UILabel *)[cell viewWithTag:PATIENTDATETIME_TAG];
	label.text = wrapper.patientDateTime;//[dateFormatter stringFromDate:[NSDate date]];
//
	//
	
	UIView* backgroundView = [ [ [ UIView alloc ] initWithFrame:CGRectZero ] autorelease ];	

	if ([@"QA Red" isEqualToString:(NSString *)[results objectAtIndex:3]])	backgroundView.backgroundColor = [UIColor colorWithRed:.88f green:.0f blue:.0f alpha:.20f];
		 else 
		 if ([@"QA Green" isEqualToString:[results objectAtIndex:3]])	backgroundView.backgroundColor = [UIColor colorWithRed:.0f green:.88f blue:.0f alpha:.20f];
			  else backgroundView.backgroundColor = [UIColor whiteColor];
	cell.backgroundView = backgroundView;
	for ( UIView* view in cell.contentView.subviews ) 
	{
		view.backgroundColor = [ UIColor clearColor ];
	}

	[results release];
	
}

#pragma mark -
-(DashboardTableViewController *) init 
{
	self = [super initWithStyle:UITableViewStylePlain];
	everLoaded = NO;
	mainOuterController = [[DataManager sharedInstance] ffOuterController];
	llc = [[DataManager sharedInstance] ffRESTComms];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	customViews = [[DataManager sharedInstance] ffCustomViews];
	BREADCRUMBS_PUSH;
		
	return self;
}
-(void) pushPatient :(int) row withWrapper:(DashboardPatient *)wrapper
{
	[DataManager sharedInstance].ffPatientWrapper = [wrapper retain]; // stash in global on way in **********this might leak
	NSString *s = [NSString stringWithFormat:@"%d",row];
	[[DataManager sharedInstance] setSelectedPatientIndex:s];
	[[NSUserDefaults standardUserDefaults] setValue:s forKey:@"patientcrumbs"]; // no one set yet
	MCPatientController *p=[[MCPatientController  alloc] init ] ;
	MY_ASSERT(p!=nil);
	[self.navigationController pushViewController:(UIViewController *)p 	animated:YES];
	[p release];
}
#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{	//	if (everLoaded==YES)
		[self.tableView reloadData]; // we hope
//else everLoaded = YES;
	    [super viewWillAppear:animated];
}


- (void)viewDidUnLoad {
	[super viewDidUnload];
}
- (void)viewDidLoad {

	// load or re-load table from internal masterboard
	
	self.patientsArray = [[DataManager sharedInstance] ffMCpatientlist];
	self.myAuth = [[DataManager sharedInstance] ffMCauth];
	self.practiceGroupName = [[DataManager sharedInstance] ffMCpracticename];
	
	
	
	self.tableView.rowHeight = ROW_HEIGHT;
	self.title = [NSString stringWithFormat:@"%@ ",self.practiceGroupName];
	self.navigationItem.leftBarButtonItem =[ [[UIBarButtonItem alloc] initWithTitle:
											  @"More"
																			  style:UIBarButtonItemStylePlain
																			 target:self	action: @selector (more:)] autorelease];
	
	

	
	
	NSString *s = [[[DataManager sharedInstance] ffBreadCrumbs] popRecoveryCrumb];
	if (s)
	{
		// are we trying to recover?
	BREADCRUMBS_LOG(@"dbtbleview restart %@ user last selected %@",s, [[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"]); // no one set yet
		int row = [[[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"] integerValue];
		NSString *s = [NSString stringWithFormat:@"%d",row];
		NSArray *patientsInSection = [sectionsArray objectAtIndex:0];//** tempnewIndexPath.section];
		DashboardPatient *wrapper = [patientsInSection objectAtIndex:row];
		[DataManager sharedInstance].ffPatientWrapper = [wrapper retain]; // stash in global on way in **********this might leak
		[[DataManager sharedInstance] setSelectedPatientIndex:s];
		[[NSUserDefaults standardUserDefaults] setValue:s forKey:@"patientcrumbs"]; // no one set yet
		MCPatientController *p=[[MCPatientController  alloc] init ] ;
		[self.navigationController pushViewController:(UIViewController *)p 	animated:NO]; // no animations during recovery
		[p release];
		return; //when we come back just get out of here
		
	}
	else {
		// we are not pushing in a recovery, should just render straight
		BREADCRUMBS_LOG (@"Not recovering going into main view");
	}

}


#pragma mark Table view data source and delegate methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
	// The number of sections is the same as the number of titles in the collation.
    return 1; //[[collation sectionTitles] count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section 
{	
	NSArray *patientsInSection =[sectionsArray objectAtIndex:0];	
    return [patientsInSection count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    
    static NSString *CellIdentifier = @"patientCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
		cell = [self tableViewCellWithReuseIdentifier:CellIdentifier];
    }
    
	// configureCell:cell forIndexPath: sets the text and image for the cell -- the method is factored out as it's also called during minuted-based updates.
	[self configureCell:cell forIndexPath:indexPath];
	
	[cell setHighlighted:NO animated:NO];
	cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    return cell;
}



-(void) tableView:(UITableView *)tableView           accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *) newIndexPath
{	
	if (newIndexPath.section==0)
	{
	// Get the time zone from the array associated with the section index in the sections array.

		NSArray *patientsInSection = [sectionsArray objectAtIndex:newIndexPath.section];
		DashboardPatient *wrapper = [patientsInSection objectAtIndex:newIndexPath.row];
		int row = newIndexPath.row;
		[self pushPatient:row withWrapper:(DashboardPatient *)wrapper];
		
	}
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	[self tableView:tableView accessoryButtonTappedForRowWithIndexPath:indexPath];
}	

#pragma mark -
#pragma mark Set the data array and configure the section data



- (void)configureSections {
	
	// Get the current collation and keep a reference to it.
	self.collation = [UILocalizedIndexedCollation currentCollation];
	
	NSInteger index, sectionTitlesCount = 1;//[[collation sectionTitles] count];
	
	NSMutableArray *newSectionsArray = [[NSMutableArray alloc] initWithCapacity:sectionTitlesCount];
//	
//	// Set up the sections array: elements are mutable arrays that will contain the time zones for that section.
	for (index = 0; index < sectionTitlesCount; index++) {
	NSMutableArray *array = [[NSMutableArray alloc] init];
		[newSectionsArray addObject:array];
		[array release];
	}
	
	// Segregate the time zones into the appropriate arrays.
	for (DashboardPatient *restaurant in patientsArray) {
		
		// Ask the collation which section number the time zone belongs in, based on its locale name.
		NSInteger sectionNumber = 0;//[collation sectionForObject:restaurant collationStringSelector:@selector(lastName)];
		
		// Get the array for the section.
		NSMutableArray *sectionRestaurants = [newSectionsArray objectAtIndex:sectionNumber];
		
		//  Add the time zone to the section.
		[sectionRestaurants addObject:restaurant];
	}
	
	// Now that all the data's in place, each section array needs to be sorted.
	for (index = 0; index < sectionTitlesCount; index++) {
//		
		NSMutableArray *patientsArrayForSection = [newSectionsArray objectAtIndex:index];
//		
//		// If the table view or its contents were editable, you would make a mutable copy here.
	NSArray *sortedRestaurantsArrayForSection = 
	[collation sortedArrayFromArray:patientsArrayForSection collationStringSelector:@selector(lastName)];
//		
//		// Replace the existing array with the sorted array.
		[newSectionsArray replaceObjectAtIndex:index withObject:sortedRestaurantsArrayForSection];
	}
//	
	self.sectionsArray = newSectionsArray;
	[newSectionsArray release];	
}

- (void)setPatientsArray:(NSMutableArray *)newDataArray {
	if (newDataArray != patientsArray) {
		[patientsArray release];
		patientsArray = [newDataArray retain];
	}
	if (patientsArray == nil) {
		self.sectionsArray = nil;
	}
	else {
		[self configureSections];
	}
}


#pragma mark -




#pragma mark -
#pragma mark Memory management

- (void)dealloc {
	[patientsArray release];
	[sectionsArray release];
	[collation release];
	BREADCRUMBS_POP;
    [super dealloc];
}


@end


