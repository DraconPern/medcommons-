//
//  PlayVideoHelperController.m
//  gigstand
//
//  Created by bill donner on 4/27/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "PlayVideoHelperController.h"


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
@implementation PlayVideoHelperController

- (void) presentVideo:(NSString *) url
{
    MPMoviePlayerViewController* theMoviePlayer = [[MPMoviePlayerViewController alloc] initWithContentURL:[NSURL URLWithString: url]]; //leaking i think
    theMoviePlayer.view.frame = self.view.frame;
    [self presentMoviePlayerViewControllerAnimated:theMoviePlayer];
}

-(void) donePressed;
{

        
        [self popOrNot]; // is in GigStandAppDelegate a category on UIViewController
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
		NSString *p = [ [SettingsManager sharedInstance] plistForVideoHelperSet];
        
		NSString *s = p ? p :@"fail";
        
		NSString     *cvcPath = [[NSBundle mainBundle] pathForResource: s ofType: @"plist" ];	
		NSDictionary *cvcDict = [NSDictionary dictionaryWithContentsOfFile: cvcPath];
		
        
		
		self->listNames = [[NSMutableArray alloc] init];
		self->listURLs = [[NSMutableArray alloc] init];
		
		for (NSString *name in [cvcDict objectForKey: @"VideoHelperNames"]) // if any are real archives, take them out of the list
	
				[self->listNames addObject:name];
        
		for (NSString *url in [cvcDict objectForKey: @"VideoHelperURLs"]) // if any are real archives, take them out of the list
            
				[self->listURLs addObject:url];
        
		
		
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
    
	self.navigationItem.titleView = [DataManager makeTitleView:@"Video Tutorials" ];	
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
	
}


- (void)viewDidUnload {
	// dealloc may not get called here


	
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    
    

	[self->listNames release];
	[self->listURLs release];
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
	
	return [self->listNames count];
	
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
    cell.textLabel.text = nil;
	
	cell.accessoryType = UITableViewCellAccessoryNone;;  
	
	if (row < [ self->listNames count])
	{
		
	    NSString *name= [self->listNames objectAtIndex: row];		
	    NSString *url= [self->listURLs objectAtIndex: row];
		
		cell.textLabel.text = name;	
        cell.detailTextLabel.text = url;
		
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
	
	NSString *url = [self->listURLs objectAtIndex:idxPath.row];
    
    [self presentVideo:url];

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

