//
//  SamplesController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.

#import "TuneInfoController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "SamplesController.h"
#import "ModalAlert.h"
#import "SettingsManager.h"
#import "ArchiveInfo.h"
#import "GigStandAppDelegate.h"
#import "ArchivesManager.h"

@interface SamplesController () <UITableViewDataSource, UITableViewDelegate>


@end
@implementation SamplesController

-(void) donePressed;
{
	NSUInteger j = 0;
	for (NSNumber *num in self->listItemsEnabled)
	{
		BOOL b = [num boolValue];
		if (b) j++;
	}
	
	if (j > 0)
	{
	
		BOOL yesno = [ModalAlert ask :[NSString stringWithFormat:@"Do you want to add selected samples to your Inbox?"]];
		
		if (yesno==YES)
		{
			NSUInteger i = 0;
			for (NSNumber *num in self->listItemsEnabled)
			{
				
				BOOL b = [num boolValue];
				if (b)
				{
					
					NSString *path = [self->listItems objectAtIndex:i];
					// might want spinner here
					NSError *error;
			          [[NSFileManager defaultManager] 
			                copyItemAtPath:[[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:path] 
													toPath:[[DataStore pathForItunesInbox] stringByAppendingPathComponent:path]
					   error:&error];
				}
				i++;
			}
		}

        
        [self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
   }
}
-(void) cancelPressed;
{
	
	[self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
}

-(id) init
{
	self=[super init];
	if (self)
	{
		self->canEdit = NO; // for now
		NSString *p = [ [SettingsManager sharedInstance] plistForFullSampleSet];

		NSString *s = p ? p :@"fail";
		//NSLog (@">>> Samples Plist: %@",s);
		
		NSString     *cvcPath = [[NSBundle mainBundle] pathForResource: s ofType: @"plist" ];	
		NSDictionary *cvcDict = [NSDictionary dictionaryWithContentsOfFile: cvcPath];
		
		// only show those items not in the archive currently
		
		NSMutableArray *l1;
		
		NSMutableArray *l2;
		
		NSMutableArray *l3 = [[NSMutableArray alloc] init];
		
		l1  = [[cvcDict objectForKey: @"SampleArchiveNames"] copy];
		
		l2  = [[cvcDict objectForKey: @"SampleArchiveImages"] copy];
		
		for (NSUInteger i=0; i<[l1 count]; i++) [l3 addObject:[NSNumber numberWithBool:NO]];

		
		self->listItems = [[NSMutableArray alloc] init];
		self->listImages = [[NSMutableArray alloc] init];
		self->listItemsEnabled = [[NSMutableArray alloc] init];
		
		NSUInteger idx=0;
		
		for (NSString *archive in l1) // if any are real archives, take them out of the list
		{
			ArchiveInfo *ai = [ArchivesManager findArchive: archive];
			if (ai==nil) 
			{
				[self->listItems addObject:[l1 objectAtIndex:idx]];
				[self->listImages addObject:[l2 objectAtIndex:idx]];
				[self->listItemsEnabled addObject:[l3 objectAtIndex:idx]];
			}
			idx++;
		}
		
		[l1 release];
		[l2 release];
		[l3 release];
		
		
	}
	return self;
	
}
-(UIView *) buildUI
{
	CGRect theframe = self.parentViewController.view.bounds;//
	//UIView *oview = [[[UIView alloc] initWithFrame: theframe ] autorelease];
	//oview.backgroundColor = [UIColor clearColor];
	float fudge =  [DataManager navBarHeight];
	theframe.origin.y+=fudge;
	theframe.size.height-=fudge;
	
	// outer view installed just to get background colors right
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: theframe
														 style: UITableViewStylePlain]
							autorelease];
	
	tmpView.tableHeaderView = nil;
	tmpView.backgroundColor =  [UIColor clearColor]; 
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;

	self.navigationItem.titleView = [DataManager makeTitleView:@"choose samples and add" ];	
	[self setColorForNavBar ];
	
	//[oview addSubview:tmpView];
	self->tableView = tmpView; // make everyone else happy too!
	return tmpView;
}

- (void) loadView
{
	self.view = [self buildUI];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([DataManager modalPopOversEnabled])
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"cancel" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(cancelPressed)] autorelease];
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"add" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	if ([self->listItems count] == 0)
	{	
		UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"It seems you've already loaded all builtin samples" 
														message:@"Visit www.gistand.net for more information"
													   delegate:self cancelButtonTitle:@"OK" otherButtonTitles: nil];
		[alert show];
		[alert release];
	}
//	
//	unichar backArrowCode = 0x21DA; //BLACK LEFT-POINTING TRIANGLE
//	NSString *backArrowString = [NSString stringWithCharacters:&backArrowCode length:1];
//	
//	UIBarButtonItem *backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backArrowString style:UIBarButtonItemStylePlain target:self action:@selector(donePressed)];
//	
//	self.navigationItem.leftBarButtonItem = backBarButtonItem;
	
	
//	
//	// create button
//	UIButton* backButton = [UIButton buttonWithType:101]; // left-pointing shape!
//	[backButton addTarget:self action:@selector(donePressed) forControlEvents:UIControlEventTouchUpInside];
//	[backButton setTitle:@"Back" forState:UIControlStateNormal];
//	
//	// create button item -- possible because UIButton subclasses UIView!
//	UIBarButtonItem* backItem = [[UIBarButtonItem alloc] initWithCustomView:backButton];
//	
//	self.navigationItem.leftBarButtonItem = backItem;
	
	
	
}
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	NSLog (@"SAM didRotateFromInterfaceOrientation");
	
	[self->tableView reloadData];
	self.view = [self buildUI]; // there must be leakage here
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
	
	[self->listItems release];
	[self->listImages release];
}


- (void)viewDidUnload {
	// dealloc may not get called here
	
    [DataManager worldViewPulse];
	[self->listItems release];
	[self->listImages release];
	
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    

        NSLog (@"samplescontroller dealloc retaincount %d",[self retainCount]);
        
    [DataManager worldViewPulse];
	[self->listItems release];
	[self->listImages release];
	[self->listItemsEnabled release];
   //[self->tableView release];
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
#pragma mark UITableViewDataSource Methods


#pragma mark UITableViewDelegate Methods

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"ZipCell1";
	//	NSUInteger section = idxPath.section;
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
	
	cell.accessoryType = UITableViewCellAccessoryNone;  
	
	if (row < [ self->listItems count])
	{
		
	    NSString *path= [self->listItems objectAtIndex: row];		
	    NSString *image= [self->listImages objectAtIndex: row];
		BOOL enabled  = [[self->listItemsEnabled objectAtIndex:row] boolValue];
		if (enabled) cell.accessoryType = UITableViewCellAccessoryCheckmark;
		
		
		cell.textLabel.text = path;	
		cell.imageView.image = [DataManager makeThumbRS:image size:[DataManager standardThumbSize]] ;
		
	}

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
	//NSError *error;
	///NSStringEncoding encoding;
	//NSString *path= [self->listItems objectAtIndex: idxPath.row];
	
	BOOL enabled = [[self->listItemsEnabled objectAtIndex:idxPath.row] boolValue];
	
	// flip polarity
	
	enabled = ! enabled;
	
	[self->listItemsEnabled replaceObjectAtIndex:idxPath.row withObject:[NSNumber numberWithBool:enabled]];
	
	UITableViewCell *cell = [tabView cellForRowAtIndexPath:idxPath];
	
	if (enabled) 
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	else 
		cell.accessoryType = UITableViewCellAccessoryNone;

	return;
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
