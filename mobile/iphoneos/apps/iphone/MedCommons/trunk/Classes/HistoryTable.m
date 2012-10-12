//
//  HistoryTable.m
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "HistoryTable.h"

#import "MedCommons.h"
#import "HistoryDetailsViewController.h"

#import "HistoryCase.h"
#import "PatientStore.h"
#import "AsyncImageView.h"
#import "DataManager.h"

@implementation HistoryTable
-(HistoryTable *) init
{
	self = [super initWithStyle: UITableViewStylePlain];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	imageCache = [[DataManager sharedInstance] ffImageCache];	


	BREADCRUMBS_PUSH;
	
	return self;
}
- (void) dealloc
{
	BREADCRUMBS_POP;
	[super dealloc];
}
-(void)  screenUpdate: (id) obj
{
	[self.tableView reloadData]; //should call for refresh
}
-(void) viewWillAppear:(BOOL)animated
{
	//	NSLog (@"History View Controller view will appear");
	[super viewWillAppear:animated];
}

-(void) loadView
{	
	//NSLog (@"History View Controller loadview");
	[super loadView]; // must must
	self.navigationItem.title = @"History";
	self.navigationItem.backBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Back"											  
																			  style:UIBarButtonItemStylePlain
																			 target:nil action:nil] autorelease];

}

// override to allow orientations other than the default portrait orientation
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // return YES for supported orientations
    return UIInterfaceOrientationIsPortrait(interfaceOrientation);
}
#pragma mark UITableViewDataSource Methods

// one section, and only one row for now
-(NSInteger) numberOfSectionsinTableView:(UITableView *) tableView
{
	return 1;
}
- (NSInteger)tableView:(UITableView *)table numberOfRowsInSection:(NSInteger)section
{
	
	int count = [[DataManager sharedInstance]  historyCount]; 
	
	//	NSLog(@" called after update number of rows %d",count);
	return count;
}
//
-(NSString *) makeDate: (NSTimeInterval )x
{
	NSDateFormatter *dateFormatter = [[[NSDateFormatter alloc] init] autorelease];
	[dateFormatter setTimeStyle:NSDateFormatterNoStyle];
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	NSDate *date = [NSDate dateWithTimeIntervalSince1970:x];
	NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
	[dateFormatter setLocale:usLocale];
	[usLocale release];
	return [dateFormatter stringFromDate:date];
	
	// Output:
	// Date for locale en_US: Jan 2, 2001
}

-(UITableViewCell *)tableView: (UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier: @"foo"];
	if (cell == nil) 
	{
		cell = [[[UITableViewCell alloc] initWithFrame: CGRectZero reuseIdentifier:@"foo"] autorelease];
	}
	else {
		AsyncImageView* oldImage = (AsyncImageView*)[cell.contentView viewWithTag:999];
		[oldImage removeFromSuperview];
    }
	HistoryCase *historyCase = [[DataManager sharedInstance] historyCase: [indexPath row]];//[history objectAtIndex:[indexPath row]];	
	
	cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
	//cell.editingStyle = UITableViewCellEditingStyleNone;
	cell.textLabel.font = [UIFont fontWithName:@"Arial" size:14];
	NSTimeInterval x = [[historyCase timestamp] doubleValue];	
	cell.textLabel.text =[NSString stringWithFormat:@"            %@ %@", [self makeDate:x], [historyCase name]];
	
	NSURL *imagesmall;
	if ([historyCase thumbnail])
	{
		imagesmall = [NSURL URLWithString:[historyCase thumbnail]];
		if(imagesmall)
		{
			//	UIImageView  *v = cell.imageView;
			//	v.image = [patientStore getImageFromRemoteURL:imagesmall];
			
			
			[historyCase release];
			
			
			AsyncImageView* asyncImage = [[[AsyncImageView alloc]
										   initWithFrame:CGRectMake (0,0,47,47) andImageCache:imageCache
										   ] autorelease];
			asyncImage.tag = 999;
			
			[asyncImage loadImageFromURL:imagesmall];
			
			[cell.contentView addSubview:asyncImage];
			
			[cell setHighlighted:NO animated:NO];
		}
	}
	return cell;
}
#pragma mark UITableViewDelegate methods

-(void) tableView:(UITableView *)tableView           accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *) newIndexPath
{
	int myrow = [newIndexPath row];
	HistoryCase *historyCase =   [[DataManager sharedInstance] historyCase: myrow]; 
    HistoryDetailsViewController *historyDetailsController;	
	historyDetailsController = 
	(HistoryDetailsViewController *)[[HistoryDetailsViewController alloc] 
																			 initWithCase:historyCase ] ;
	MY_ASSERT(historyDetailsController!=nil);
	[self.navigationController pushViewController:(UIViewController *)historyDetailsController 	animated:YES];
	[historyDetailsController release];	
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	[self tableView:tableView accessoryButtonTappedForRowWithIndexPath:indexPath];
}		
@end