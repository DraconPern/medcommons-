//
//  ZipViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>
#import "AppDelegate.h"
#import "AsyncImageView.h"
//#import "DictionaryAdditions.h"
#import "MCDocumentTableViewCell.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"
#import "ZipViewController.h"
#import "DataStore.h"
#import "DataManager.h"


@interface SectionLabelWrapper : NSObject {
	NSString *localeName;
	NSString *sectionLabel;
}

@property (nonatomic, copy) NSString *localeName;
@property (nonatomic, retain) NSString *sectionLabel;

- (id)initWithSectionLabel:(NSString *)aLabel nameComponents:(NSArray *)nameComponents ;



@end

@implementation SectionLabelWrapper

@synthesize localeName, sectionLabel ;

- (id)initWithSectionLabel:(NSString *)aLabel nameComponents:(NSArray *)nameComponents {
	
	if ((self = [super init])) {
		
		sectionLabel = [aLabel retain];
		
		NSString *name = nil;
		name = [nameComponents objectAtIndex:0];
		
		
		localeName = [[name stringByReplacingOccurrencesOfString:@"_" withString:@" "] retain];
	}
	return self;
}


- (void)dealloc {
	[localeName release];
	[sectionLabel release];
	
	[super dealloc];
}


@end

#pragma mark -
#pragma mark Public Class ZipViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//


enum
{
    SECTION_COUNT = 27  // MUST be kept in display order ...
	
    //
	
};

@interface ZipViewController () <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem *backButton;
@property (nonatomic, copy,   readonly)  NSString        *base;


-(NSArray *) getSections:(NSString *) str;

@end

@implementation ZipViewController

@synthesize backButton = backButton_;
@synthesize base       = base_;


@synthesize sectionsArray;
@synthesize collation;

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
	
}

- (void) loadView
{
	
//	NSMutableDictionary *dict = [[DataManager sharedInstance] indexSegments];

	//** cache these buggers so when we come back to this archive again we dont have to reread anything
//**	NSArray *x = (NSArray *)[dict objectForKey: self->base_];
//**	if (x) self.sectionsArray = [x retain];
//**	else
	{
//		UIAlertView *alert;
//		
//		[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
//		
//		alert = [[[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"Loading %@\nPlease Wait", self->base_]
//										   message:nil delegate:nil 
//								 cancelButtonTitle:(NSString *)nil 
//								  otherButtonTitles:(NSString *)nil] autorelease];
//		[alert show];
//		UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
//		
//		// Adjust the indicator so it is up a few pixels from the bottom of the alert
//		indicator.center = CGPointMake(alert.bounds.size.width / 2, alert.bounds.size.height - 50);
//		[indicator startAnimating];
//		[alert addSubview:indicator];
//		[indicator release];
		
		// the actual slow part is here
		NSArray *xx= [self getSections: self->base_]; 
//**     	[dict addEntriesFromDictionary:[NSDictionary dictionaryWithObject:xx forKey: self->base_]];
		
		self.sectionsArray =[xx retain];
		// the actual slow part ends here
		
		//
//		[alert dismissWithClickedButtonIndex:0 animated:YES];
//		[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
	}
	
	
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
                                                         style: UITableViewStylePlain]
                            autorelease];
	
    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	
    tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	// autorelease];
	
	
    self.view = tmpView;
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    [(UITableView *) self.view flashScrollIndicators];
}

- (void) viewDidLoad
{
    [super viewDidLoad];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];
	
	
    UITableView *tabView = (UITableView *) self.view;
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];
	
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	
    [self->backButton_ release];
    [self->base_ release];
	[self->collation release];
	[self->sectionsArray release];
	
    [super dealloc];
}




-(NSArray *) getSections:(NSString *) str
{
	
	NSString *file;
	NSString *name;
	
	self->alphabetIndex_ = @"ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	
	NSArray *sectionLetters = [NSArray arrayWithArray:[@"A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|#"
													   componentsSeparatedByString:@"|"]];
	
	
	NSMutableArray *sectionLabels =[[ [NSMutableArray alloc] initWithCapacity:[sectionLetters count]] autorelease];
	
	for (NSString *sectionLabel in sectionLetters) {
		
		NSArray *nameComponents = [sectionLabel componentsSeparatedByString:@"/"]; // at this point this should always come back
		// For this example, the time zone itself isn't needed.
		SectionLabelWrapper *sectionlabelWrapper = [[SectionLabelWrapper alloc] initWithSectionLabel:nil 
																					  nameComponents:nameComponents];
		
		[sectionLabels addObject:sectionlabelWrapper];
		[sectionlabelWrapper release];
	}
	
	self.collation = [UILocalizedIndexedCollation currentCollation];
	
	NSInteger index, sectionTitlesCount = [[self.collation  sectionTitles] count];
	
	NSMutableArray *newSectionsArray = [[[NSMutableArray alloc] initWithCapacity:sectionTitlesCount] autorelease];
	
	// Set up the sections array: elements are mutable arrays that will contain the time zones for that section.
	for (index = 0; index < sectionTitlesCount; index++) {
		NSMutableArray *array = [[NSMutableArray alloc] init];
		[newSectionsArray addObject:array];
		[array release];
	}
	
	
    //    NSMutableArray        *docs = [[[NSMutableArray alloc] init]
	//                                       autorelease];
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForExplodedZipFilesForKey: self.base]];
	
	// segregate the files
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		MCDocument *thisdoc = [MCDocument documentWithPath: file attributes: attrs];
		// count total size of all files scanned
		 NSNumber *fs = [attrs objectForKey:@"NSFileSize"];
		NSLog (@"file size %lld",[fs longLongValue]);
		unsigned long long size = [fs longLongValue] +
		[[DataManager sharedInstance].fileSpaceTotal longLongValue];
		
		[DataManager sharedInstance].fileSpaceTotal = [NSNumber numberWithUnsignedLongLong: size];
		
		// put this in the correct bucket in the sections array of arrays
		name  = [[file stringByDeletingPathExtension] lastPathComponent]; //[thisdoc title];!
		//frstLetterIdx =
		NSInteger sectionNumber = [self->alphabetIndex_ rangeOfString:[name substringToIndex:1]].location;
		if ((sectionNumber <0)||(sectionNumber>26)) sectionNumber=26;
		
		//
		//	= [self.collation sectionForObject:name collationStringSelector:@selector(localeName)];
		
		// Get the array for the section.
		NSMutableArray *sectionAlphaLetter = [newSectionsArray objectAtIndex:sectionNumber];
		
		//  Add the label to the section.
		[sectionAlphaLetter addObject:thisdoc];
		//			
		//			
		//			
		//			if (firstLetterIdx == NSNotFound) firstLetterIdx = 26;
		//		    NSLog (@"installing %@ with firstletteridx %d",name,firstLetterIdx);
		//			
		//			[[self->sectionsArray_ objectAtIndex: firstLetterIdx] addObject:name];// [[self->alphabetIndex_ substringFromIndex:firstLetterIdx] substringToIndex:1]];
		////		//	 
		//[docs addObject:thisdoc ];
	}
	
	
	//	
	//	// Now that all the data's in place, each section array needs to be sorted. - does it really
	//	for (index = 0; index < sectionTitlesCount; index++) {
	//		
	//		NSMutableArray *namesArrayForSection = [newSectionsArray objectAtIndex:index];
	//		
	//		// If the table view or its contents were editable, you would make a mutable copy here.
	//		NSArray *sortedNamesArrayForSection = [self.collation sortedArrayFromArray:namesArrayForSection collationStringSelector:@selector(localeName)];
	//		
	//		// Replace the existing array with the sorted array.
	//		[newSectionsArray replaceObjectAtIndex:index withObject:sortedNamesArrayForSection];
	//	
	
	//}
	
	return newSectionsArray;
}


- (id) initWithBase: (NSString *) str
{
	
	self = [super init];
	
    if (!self) return self;
	
	self->base_ = [str copy];	
	
	
    
    return self;
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}



#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles from the collation.
 */

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return [[self.collation sectionTitles] objectAtIndex:section];
}


- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView {
    return [self.collation sectionIndexTitles];
}


- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {
    return [self.collation sectionForSectionIndexTitleAtIndex:index];
}
/*
 - (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index
 {
 NSInteger loc =   [self->alphabetIndex_ rangeOfString:[title substringToIndex:1]].location ;
 NSLog (@"section for section title %@ at Index %d returns %d", title, index, loc);
 return loc;
 
 }*/

//- (NSArray *)sectionIndexTitlesForTableView: (UITableView *) aTableView
//{
//	
//	
//	NSMutableArray *indices = [NSMutableArray arrayWithCapacity:27];
//	
//	for (int i= 0; i<27; i++) 
//		[indices addObject:[[self->alphabetIndex_ substringFromIndex:i] substringToIndex:1]];
////	NSLog (@"section indices set up as %@",indices);
//	return indices;
//}


- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 27;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"ZipCell1";
	NSUInteger section = idxPath.section;
    NSUInteger row = idxPath.row;
	
	
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier1];
	
    if (!cell)
    {
		
		cell = [[[MCDocumentTableViewCell alloc]
				 initWithReuseIdentifier: CellIdentifier1]
				autorelease];
		
    }
	
    //
    // Reset cell properties to default:
    //
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;
	
	
	if (section < [self.sectionsArray count])
	{
		MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;
		
		docCell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		NSArray *documents = [self.sectionsArray objectAtIndex:section];
		
		if (row <[documents count])
			
			docCell.document = [documents objectAtIndex:row]; 
		else cell = nil;
	}
	else
		cell = nil;
	
	
    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
	
   	NSArray *entries = [sectionsArray objectAtIndex:section];
	
    return [entries count];
}


#pragma mark UITableViewDelegate Methods

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return 60.0f;
}


- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	
	NSArray *documents = [self.sectionsArray objectAtIndex:idxPath.section];
	
	if (idxPath.row <[documents count])
	{
		
		MCDocument *doc = [documents objectAtIndex: idxPath.row];
		NSString   *fullPath = [NSString stringWithFormat:@"%@/%@",
								[DataStore pathForExplodedZipFilesForKey: self.base],
								doc.path];
		NSURL    *docURL = [NSURL fileURLWithPath: fullPath
									  isDirectory: NO];
		//NSLog (@"webview is : %@",s);
		WebViewController *wvc = [[[WebViewController alloc]
								   initWithURL: docURL]
								  autorelease];
		
		NSString *name = [NSString stringWithFormat: @"%@:%@",
						  self.base,
						  [doc.path stringByDeletingPathExtension]];
		
		NSArray *parts = [name componentsSeparatedByString:@"-"];
		if ([parts count] !=2 )
		{
			// no hyphen, just make it plain
			wvc.title = name;  
		} 
		else // if ([parts count] ==2)
		{
			// one hyphen , take part to the right make part of type label
			
			wvc.title =  [NSString stringWithFormat:@"%@ %@", [parts objectAtIndex:0], [parts objectAtIndex:1]];
		}
		
		[self.navigationController pushViewController: wvc
											 animated: YES];
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
