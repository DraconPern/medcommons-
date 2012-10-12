    //
//  TuneInfoController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.

#import "TuneInfoController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "ArchivesManager.h"
#import "TunesManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"
#import "ArchiveInfo.h"


@implementation TuneInfoController

-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(id) initWithTune : (NSString *) tune;
{
	self=[super init];
	if (self)
	{
		
		self->tuneInfo = [[TunesManager findTune:tune] retain];
	self->listItems = [[TunesManager allVariantsFromTitle:tune] retain];
	self->tuneTitle = [tune copy];
	}
	return self;
	
}


- (void) loadView
{
	CGRect theframe = self.parentViewController.view.bounds;
	UIView *oview = [[[UIView alloc] initWithFrame: theframe ] autorelease];
	oview.backgroundColor = [UIColor lightGrayColor];
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
	
	self.navigationItem.titleView = [[DataManager allocTitleView:self->tuneTitle ]autorelease];	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	
	[oview addSubview:tmpView];
	self->tableView = tmpView; // make everyone else happy too!
	
	
	self.view = oview;
	
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"Done" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	//[self->tableView reloadData];
     [ self loadView] ; // rebuild whole UI
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	
	[self->listItems release];
	[self->tuneInfo release];
    [super dealloc];
}
/*
 Section-related methods: Retrieve the section titles and section index titles
 */



- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	return 1;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	//section should always be 1
	return [self->listItems count];
}


#pragma mark UITableViewDelegate Methods

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"ZipCell1";

    NSUInteger row = idxPath.row;
	
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier1];
	
    if (!cell)
    {
		
		cell = [[[UITableViewCell alloc]
				 initWithStyle: UITableViewCellStyleSubtitle reuseIdentifier: CellIdentifier1]
				autorelease ];
		
    }
	
    //
    // Reset cell properties to default:
    //
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;
	
	
	if (row < [ self->listItems count])
	{
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
	    InstanceInfo *ii= [self->listItems objectAtIndex: row];
		
		NSString *longpath = [DataManager newLongPath:ii.filePath forArchive:ii.archive];
		
		NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForSharedDocuments] 
																					   stringByAppendingPathComponent: longpath]
																			   error: NULL];
		NSDate *date = [attrs objectForKey:NSFileCreationDate];
		NSNumber *size = [attrs objectForKey:NSFileSize];
		unsigned long long ull = ([size unsignedLongLongValue]);
		double mb = (double)ull	; // make this a double
		mb = mb/(1024.f); // Convert to K
		// mark the most recent tune shown
		if ([self->tuneInfo.lastArchive isEqualToString:ii.archive]
			&& [self->tuneInfo.lastFilePath isEqualToString:ii.filePath])	
			cell.accessoryType = UITableViewCellAccessoryCheckmark;
		
		cell.textLabel.text = longpath;	
		cell.textLabel.font = [UIFont boldSystemFontOfSize:14];	
		cell.detailTextLabel.text = [NSString stringWithFormat:@"%.2fKB - visited %@ imported %@",mb,ii.lastVisited,date];
		cell.detailTextLabel.font = [UIFont italicSystemFontOfSize:10];	
		cell.imageView.image =[ArchivesManager newArchiveThumbnail: ii.archive ];
		
		}
		else cell = nil;
	
	
	

	
	
    return cell;
}


- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return [DataManager standardRowSize];
}


- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	InstanceInfo *ii= [self->listItems objectAtIndex: idxPath.row];
	NSString *upath = [NSString stringWithFormat:@"%@/%@",
					   [DataStore pathForSharedDocuments],
					   ii.filePath];
	
    [DataManager docMenuForURL:[NSURL fileURLWithPath:upath isDirectory: NO] 
						inView:[tabView cellForRowAtIndexPath:idxPath].imageView];
	
	return;;
}
- (void) tableView: (UITableView *) tabView
   willDisplayCell: (UITableViewCell *) cell
 forRowAtIndexPath: (NSIndexPath *) idxPath
{
	//
	// Apple docs say to do this here rather than at cell creation time ...
	//
	cell.backgroundColor = [UIColor whiteColor];
}


/////

@end
