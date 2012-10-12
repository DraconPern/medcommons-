    //
//  SongsViewController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.

#import "SmallArchiveViewController.h"
#import "DataManager.h"
#import "ArchiveInfoController.h"
#import "ArchivesManager.h"
#import "GigStandAppDelegate.h"
#import "TunesManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"

@implementation SmallArchiveViewController
-(void) archiveInfoPressed;
{

	ArchiveInfoController *zvc = [[[ArchiveInfoController alloc] initWithArchive:self->archive ]	autorelease];		
		zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
		
		[self presentModalViewController:nav animated: YES];

}
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
    //[self.navigationController popViewControllerAnimated:YES];
}


-(id) initWithArchive: (NSString *)archiv;
{
		NSLog (@"SAC small archive %@ initialization starting",archiv);
	self->archive = [archiv copy];
	NSString *shortie = [ArchivesManager shortName:archiv];
	
	// dynamically build the array of items  of RefNodesin this archiv
	
	NSMutableArray *archiveItems =   [[NSMutableArray alloc]init];
	[archiveItems	addObjectsFromArray:[TunesManager allTitlesFromArchive: archiv]];	
	[archiveItems sortUsingSelector:@selector(compare:)];

	self->listItems = [archiveItems retain];
	self->listName = [[NSString stringWithFormat:@":%@",shortie] retain ];
		NSLog (@"SAC small archive %@ initialization complete",archive);
	return self;
	
	
}
-(id) initWithArchiveReversed: (NSString *)archiv;
{
    NSLog (@"SAC small archive %@ reversed initialization starting",archiv);
	self->archive = [archiv copy];
	NSString *shortie = [ArchivesManager shortName:archiv];
	
	// dynamically build the array of items  of RefNodesin this archiv
	
	NSMutableArray *archiveItems =   [[NSMutableArray alloc]init];
	[archiveItems	addObjectsFromArray:[TunesManager allTitlesFromArchive: archiv]];	
	[archiveItems sortUsingSelector:@selector(reverseCompare:)];
    
	self->listItems = [archiveItems retain];
	self->listName = [[NSString stringWithFormat:@":%@",shortie] retain ];
    NSLog (@"SAC small archive %@ reversed initialization complete",archive);
	return self;
	
	
}
-(UIView *) buildUI
{
	CGRect theframe = self.parentViewController.view.bounds;
	UIView *oview = [[[UIView alloc] initWithFrame: theframe ] autorelease];
	oview.backgroundColor = [DataManager applicationColor];
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
	
	self.navigationItem.titleView = [DataManager makeTitleView:self->listName ];	
		
    [self setColorForNavBar ];
	
	[oview addSubview:tmpView];
	self->tableView = tmpView; // make everyone else happy too!
	return oview;
}

- (void) loadView
{

	NSLog (@"SAC loadView");
	
	self.view = [self buildUI];
	
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
  
    [super viewDidLoad];
    
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"Home" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];

	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											   initWithBarButtonSystemItem: UIBarButtonSystemItemOrganize
											   target:self 
											   action:@selector(archiveInfoPressed)] autorelease];
	
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	
    NSLog (@"SAV didRotateFromInterfaceOrientation %d",(UIInterfaceOrientation) fromOrient);
	
	self.view = [self buildUI];
	
	[self->tableView reloadData];
  
	//[v release];
}

-(void) viewWillAppear:(BOOL)animated
{
    self.view = [self buildUI];
	
	[self->tableView reloadData];
    
}

- (void)didReceiveMemoryWarning {
	
	NSLog (@"SAV didReceiveMemoryWarning");
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
	
	//NSLog (@"SAV viewDidUnload");
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	//NSLog (@"SAV dealloc");	
    
[self->archive release];
   [self->listItems release];
   [self->listName release];
//    [self->tableView release];
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
		 
			NSString *tune = [self->listItems objectAtIndex: row];
			
			cell.textLabel.text = tune;			
			cell.detailTextLabel.text = [DataManager makeBlurb:tune];
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
	
	if (idxPath.row <[self->listItems count])
	{
		NSString *tune = [self->listItems objectAtIndex: idxPath.row];
		TuneInfo  *tn = [TunesManager tuneInfo:tune];
	
		//each dict has different entries, just get the first 
		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title]) // only executed for the first variant			
			{
                
                UIViewController   *otv  =  [DataManager makeOneTuneViewController:
                                              [DataManager deriveLongPath:ii.filePath forArchive:ii.archive]	title:ii.title items:self->listItems] ;
                
                UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: otv] autorelease];
                
                [self presentModalViewController:nav animated: YES];
               	
				break;
			}
	}
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

@end
