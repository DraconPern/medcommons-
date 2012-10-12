// this may no longer be needed now that search has been moved to its own tab

//  TuneListViewController.m
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "TuneListViewController.h"
#import "TunesManager.h"
#import "DataManager.h"
#import "DataStore.h"

#import "SetListsManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"


@implementation TuneListViewController

-(id) initWithPlist:(NSString *)path  name:(NSString *) namex edit:(BOOL) editx;
{
	NSMutableArray *locallistItems= [SetListsManager allocListOfTunes:path]; // was complaining of leak here but autorelease seems to crash it
	
	self = [super init];
	if (self)
	{
		self->listItems = [locallistItems retain]; //locallistItems;
		self->name = [namex  retain];
		self->canEdit = editx;
		self->canReorder = YES;
		self->tag = 1;
		self->plist = [path copy];
		
		////
		NSLog(@"TuneList.%@ %@ items %d canEdit %d canReorder %d name %@ ", self,self->plist, [self->listItems count],self->canEdit,self->canReorder,self->name);
	}
	[locallistItems release];
	
	return self;
}





#pragma mark MFMailComposeViewController support
- (void)mailComposeController:(MFMailComposeViewController*)controllerx  
          didFinishWithResult:(MFMailComposeResult)result 
                        error:(NSError*)error;
{
	if (result == MFMailComposeResultSent) {
		
		NSLog(@"TLV mailComposeController");
	}
	[self dismissModalViewControllerAnimated:YES];
	[controller release];
}
-(NSString *) allocSimpleSetListAsText: (NSString *)path ;
{
	
	NSMutableString *sec = [[NSMutableString alloc] init];
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
	NSString *plistPath;
	
	plistPath = [DataStore pathForTuneListWithName:path] ;//stringByAppendingPathComponent:path];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"simpleSetListAsText No plist today for %@:=(", plistPath);
		return sec;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
		return sec;
	}
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		[sec appendFormat:@"%2d.  %@ \r\n",n+1,[titlesx objectAtIndex:n]];
	}
	
	return sec;
}
-(NSString *) allocSimpleSetListAsHTML: (NSString *)path 
{
	
	NSMutableString *sec = [[NSMutableString alloc] init];
	
	NSString *errorDesc = nil;
	NSPropertyListFormat format;
	
	NSString *plistPath;
	
	plistPath = [DataStore pathForTuneListWithName:path] ;//stringByAppendingPathComponent:path];
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"simpleSetListAsText No plist today for %@:=(", plistPath);
		return sec;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
										  propertyListFromData:plistXML
										  mutabilityOption:NSPropertyListMutableContainersAndLeaves
										  format:&format
										  errorDescription:&errorDesc];
	if (!temp) {
		NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
		return sec;
	}
	
	NSString *footer = [NSString stringWithFormat: @"this setlist was built by %@ %@<br/>visit www.gigstand.net for more information",
						[DataManager sharedInstance].applicationName,
						[DataManager sharedInstance].applicationVersion];
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	
	
	[sec appendFormat:@"<html><head><style>body {font-family:Tahoma,Verdana,Arial;} li {font-size:1.6em;}</style></head><body><h1>%@</h1><ul>",self->plist];
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		[sec appendFormat:@"<li>%@</li>",[titlesx objectAtIndex:n]];
	}
	[sec appendFormat:@"</ul><footer><small>%@</small></footer></body></html>",footer];
	return sec;
}

-(void) emailPressed
{
	controller = [[MFMailComposeViewController alloc] init];
	controller.mailComposeDelegate = self;
	[controller setSubject:[NSString stringWithFormat:@"Sending List -  %@",self->name]];
	
	
    NSString *plistPath;
	
	plistPath =  [DataStore pathForTuneListWithName:self->plist];
	
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"No plist today for %@:=(", plistPath);
		return;
	}
	
	
	// SettingsManager *settings = [SettingsManager sharedInstance];
	NSString *st =  [self allocSimpleSetListAsText: self->plist];
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	[controller setMessageBody:[NSString stringWithFormat:@"%@\n\r%@\n\r%@%@\n\r%@\n\r\n\r%@\n\r%@",								
								@"Hello there.\r\n\r\nI'm sending you a list from GigStand so that we can play the same tunes.",
								@"\r\nIf you have GigStand, please click on the attachment.",
								@"",
								@"\r\nIf you don't have GigStand yet, please visit the Apple App Store.",
								@"\r\nFor those without an iPad here is the setlist as plain text for use in a standard editor:",
								st ,								
								[NSString stringWithFormat: @"this email was built by %@ %@ \r\nvisit www.gigstand.net for more information",
								 [DataManager sharedInstance].applicationName,
								 [DataManager sharedInstance].applicationVersion]
								] 
						isHTML:NO]; 
	
	
	
	[controller addAttachmentData:plistXML mimeType:@"gigstand/x-stl" fileName:
	 [NSString stringWithFormat:@"%@ from %@.stl",self->plist,[[UIDevice currentDevice] name]]]; 
	
	NSLog(@"TLV Presenting Modal Mail");
	[self presentModalViewController:controller animated:YES];	
	[st release];
}
-(void) printPressed: (id) sender
{		
	UIPrintInteractionController *pic = [UIPrintInteractionController sharedPrintController];
	pic.delegate = self;
	
	UIPrintInfo *printInfo = [UIPrintInfo printInfo];
	printInfo.outputType = UIPrintInfoOutputGeneral;
	printInfo.jobName = self->plist;
	pic.printInfo = printInfo;
	NSString *mt = [self allocSimpleSetListAsHTML:self->plist];
	UIMarkupTextPrintFormatter *htmlFormatter = [[UIMarkupTextPrintFormatter alloc]
												 initWithMarkupText:mt];
	htmlFormatter.startPage = 0;
	htmlFormatter.contentInsets = UIEdgeInsetsMake(72.0f, 72.0f, 72.0f, 72.0f); // 1 inch margins
	htmlFormatter.maximumContentWidth = 6 * 72.0f;
	pic.printFormatter = htmlFormatter;
	[htmlFormatter release];
	pic.showsPageRange = YES;
	
	void (^completionAction)(UIPrintInteractionController *, BOOL, NSError *) =
	^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
		if (!completed && error) {
			NSLog(@"Printing could not complete because of error: %@", error);
		}
	};
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
		[pic presentFromBarButtonItem:self.navigationItem.rightBarButtonItem animated:YES completionHandler:completionAction];
	} 
	else 
	{
		[pic presentAnimated:YES completionHandler:completionAction];
	}
	[mt release];
	
}


-(void) donePressed;
{
	//[self.navigationController popViewControllerAnimated:YES ];
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(void) actionPressed
{
	
	NSString *cancel = (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"Cancel":nil;
	
	self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Setlist Options", @"")
											  delegate:self
									 cancelButtonTitle:cancel
								destructiveButtonTitle:nil
									 otherButtonTitles:nil];
	
	[self->toass addButtonWithTitle:NSLocalizedString (@"Print", @"")];
	
	
	[self->toass addButtonWithTitle:NSLocalizedString (@"Email", @"")];
	
	if (self->canEdit)	
		
		[self->toass addButtonWithTitle:NSLocalizedString (@"Edit", @"")];
	
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	[self->toass showFromBarButtonItem: self.navigationItem.rightBarButtonItem
							  animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//cancel is zero in this scheme
	
	if (actionSheet.tag == 1) // this is invoked from the upper left corner
	{
		// ipad is one lower because of the lack of cancel button on the actionsheet
		if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) buttonIndex++;
		if (buttonIndex==1)
			
		{
			[self printPressed:actionSheet];            
		}
		if (buttonIndex==2)
			
		{
			[self emailPressed];            
		}
		if (buttonIndex==3)
			
		{
			[self enterEditMode];            
		}
		
	}
}
-(void) setupLeftSideNavItems
{
	
	UIBarButtonItem *doneButton = [[[UIBarButtonItem alloc] 
									initWithTitle: @"Home" style: UIBarButtonItemStyleBordered
									target:self 
									action:@selector(donePressed)] autorelease];
	
	
	// place the toolbar into the navigation bar
	self.navigationItem.leftBarButtonItem =doneButton;
}





#pragma mark  stuff inserted from former SUPER

-(NSUInteger ) itemCount;
{
	return [self->listItems count];
}


-(void) reloadListItems : (NSArray *) newItems
{
	[self->listItems release];
	self->listItems = [newItems retain];
	
}




-(void) setBarButtonItems
{
	
	
	if (self->canEdit)
	{
		//when editing, display the done button
		if (self->tableView.isEditing) {
			self.navigationItem.rightBarButtonItem =
			[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(leaveEditMode)];
		}
		else {
			
			// cant edit create a standard action button
			self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc]
													   initWithBarButtonSystemItem:UIBarButtonSystemItemAction
													   target:self
													   action:@selector(actionPressed)] autorelease];
			
			
		}
	}
	else 
	{
	}
	
}
-(void) enterEditMode;
{
	[self->tableView deselectRowAtIndexPath:[self->tableView indexPathForSelectedRow] animated:YES];
	[self->tableView setEditing:YES animated: YES];
	[self setBarButtonItems];
}
-(void) leaveEditMode;
{
	[self->tableView setEditing:NO animated:YES];
	[self setBarButtonItems];
	[SetListsManager rewriteTuneList: self->listItems toPropertyList:self->plist];
}


#pragma mark Overridden UIViewController Methods



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
	
	
	[tabView reloadData];
	
	[self setupLeftSideNavItems];
	[self setBarButtonItems];
	
	NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
	if (idxPath)
		[tabView deselectRowAtIndexPath: idxPath
							   animated: NO];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void) loadView
{
	
	
	self->canShare =  ([MFMailComposeViewController canSendMail]);
	
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
														 style: UITableViewStylePlain]
							autorelease];
	
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [UIColor lightGrayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	
	self.navigationItem.titleView = [[DataManager allocTitleView:self->name] autorelease];
	[self setBarButtonItems];
	
	[self setupLeftSideNavItems];
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = tmpView;
	
}


#pragma mark Overridden NSObject Methods

- (void)dealloc {
	
	[self->toass dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->toass release];	
	[self->plist release];
	[self->name release];
	
    [super dealloc];
	
}

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}
#pragma mark UITableViewDataSource Methods


/*
 Section-related methods: Retrieve the section titles and section index titles
 */


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView 
		   editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	
	if (self->canEdit)		{
		
		NSString *titlestring = [self->listItems objectAtIndex:indexPath.row];
		if ([@"recents" isEqualToString:titlestring])  return UITableViewCellEditingStyleNone;
		
		
        return UITableViewCellEditingStyleDelete;
		
    } else {
		
        return UITableViewCellEditingStyleNone;
		
    }
	
}

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

// table methods for setlist, these are specialized here
-(void) tableView: (UITableView *)aTableView commitEditingStyle: (UITableViewCellEditingStyle)editingStyle
forRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if (editingStyle == UITableViewCellEditingStyleDelete)
	{
		// DELETE A SINGLE TUNE FROM THE SETLIST
		
		// actually delete this item from the listItems array
		[self->listItems removeObjectAtIndex:indexPath.row];
		// make this persistent
		[SetListsManager rewriteTuneList:self->listItems toPropertyList:self->plist] ;
		[self->tableView reloadData];
		[self setBarButtonItems];
	}
}


-(void) tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *) oldPath toIndexPath:(NSIndexPath *) newPath
{
	//change the data as we move stuff around, -- thanks Erica Sadun
	
	TuneInfo *rn= [[self->listItems objectAtIndex:oldPath.row] retain];
	[self->listItems removeObjectAtIndex:oldPath.row];
	[self->listItems insertObject: rn atIndex:newPath.row];
	[rn release];
	[self setBarButtonItems];
	
	
}


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
	
	//[
	// Reset cell properties to default:
	//
	cell.detailTextLabel.text = nil;
	cell.selectionStyle = UITableViewCellSelectionStyleNone;
	cell.textLabel.text = nil;

		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		
		
		NSString *tune = [ self->listItems objectAtIndex:row];
		TuneInfo  *tn = [TunesManager findTuneInfo:tune];
		if (!tn)
		{
			cell.textLabel.text = tune;			
			cell.detailTextLabel.text = @"--not found on this device--";
			return cell;		

		}
		else 
		{
			cell.textLabel.text = tn.title;			
			cell.detailTextLabel.text = [DataManager newBlurb:tn.title];
		}

	return cell;
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// section should always be 1
	
	
	NSUInteger count = [self->listItems  count];
	
	if (idxPath.row < count)
	{
		
		NSString *tune = [ self->listItems  objectAtIndex:idxPath.row];	
		TuneInfo  *tn = [TunesManager tuneInfo:tune ];

		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title]) // only executed for the first variant	
		{
			[self presentModalViewController:
			 [[DataManager allocOneTuneViewController:
			   [[DataManager newLongPath:ii.filePath forArchive:ii.archive]autorelease]	 title:ii.title items:self->listItems] 
			  autorelease] animated: YES];
			break;
		}
	}
}


- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	return [DataManager standardRowSize];
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
