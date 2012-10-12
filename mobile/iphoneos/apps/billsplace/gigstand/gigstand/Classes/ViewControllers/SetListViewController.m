// this may no longer be needed now that search has been moved to its own tab

//  SetListViewController.m
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import "SetListViewController.h"
#import "TunesManager.h"
#import "DataManager.h"
#import "DataStore.h"
#import "GigStandAppDelegate.h"
#import "SetListsManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"


@implementation SetListViewController

-(id) initWithPlist:(NSString *)path  name:(NSString *) namex edit:(BOOL) editx;
{
	NSMutableArray *locallistItems= [SetListsManager listOfTunes:path]; // was complaining of leak here but autorelease seems to crash it
	
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
	// is already autoreleased - [locallistItems release];
	
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
-(NSString *) simpleSetlistAsText: (NSString *)list  ;
{
	
	NSMutableString *sec = [[NSMutableString alloc] init];
	
	
	NSMutableArray *titlesx = [SetListsManager listOfTunes:list ];
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		[sec appendFormat:@"%2d.  %@ \r\n",n+1,[titlesx objectAtIndex:n]];
	}
	
	return [sec autorelease];
}
-(NSString *) simpleSetlistAsHTML: (NSString *)list 
{
	
	NSMutableString *sec = [[NSMutableString alloc] init];
	
    
	
	NSString *footer = [NSString stringWithFormat: @"this setlist was built by %@ %@<br/>visit www.gigstand.net for more information",
						[DataManager sharedInstance].applicationName,
						[DataManager sharedInstance].applicationVersion];
	
	NSMutableArray *titlesx = [SetListsManager listOfTunes:list ];
	
	[sec appendFormat:@"<html><head><style>body {font-family:Tahoma,Verdana,Arial;} li {font-size:1.6em;}</style></head><body><h1>%@</h1><ul>",list];
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		[sec appendFormat:@"<li>%@</li>",[titlesx objectAtIndex:n]];
	}
	[sec appendFormat:@"</ul><footer><small>%@</small></footer></body></html>",footer];
	return [sec autorelease];
}

-(void) emailPressed
{

	controller = [[MFMailComposeViewController alloc] init];
	
	controller.mailComposeDelegate = self;
	[controller setSubject:[NSString stringWithFormat:@"Sending Setlist -  %@",self->plist]];
	// SettingsManager *settings = [SettingsManager sharedInstance];
	NSString *st =  [self simpleSetlistAsText: self->plist];
	//NSData *plistXML = nil; // ***** MUST FIX ******* [[NSFileManager defaultManager] contentsAtPath:plistPath];
	[controller setMessageBody:[NSString stringWithFormat:@"%@\n\r%@\n\r%@%@\n\r%@\n\r\n\r%@\n\r%@",								
								@"Hello there.\r\n\r\nI'm sending you a list from GigStand so that we can play the same tunes.",
								@"\r\nIf you have GigStand, please click on the attachment.",
								@"",
								@"\r\nIf you don't have GigStand yet, please visit the Apple App Store.",
								@"\r\nFor those without an iPad here is the setlist as plain text for use in a standard editor:",
								st ,								
								[NSString stringWithFormat: 
								 @"this email was built by %@ %@ \r\nvisit www.gigstand.net for more information",
								 [DataManager sharedInstance].applicationName,
								 [DataManager sharedInstance].applicationVersion]
								] 
						isHTML:NO]; 
	NSDate *now = [NSDate date];
	NSString *fname = [NSString stringWithFormat:@"%@ from %@.stl",self->plist,[[UIDevice currentDevice] name]];
    NSString *content = [NSString stringWithFormat:@"## %@ \r\n## written by %@ %@ on %@\r\n##\r\n%@",
                         fname, [DataManager sharedInstance].applicationName, [DataManager sharedInstance].applicationVersion,now, st];
	
	[controller addAttachmentData:[content dataUsingEncoding: NSUTF8StringEncoding] mimeType:@"gigstand/x-stl" fileName:fname]; 
	
	//NSLog(@"TLV Presenting Modal Mail");
	[self presentModalViewController:controller animated:YES];	
	//[st release];
}
-(void) printPressed: (id) sender
{		
	UIPrintInteractionController *pic = [UIPrintInteractionController sharedPrintController];
	pic.delegate = self;
	
	UIPrintInfo *printInfo = [UIPrintInfo printInfo];
	printInfo.outputType = UIPrintInfoOutputGeneral;
	printInfo.jobName = self->plist;
	pic.printInfo = printInfo;
	NSString *mt = [self simpleSetlistAsHTML:self->plist];
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
//[mt release];
	
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
									initWithTitle: @"Home" 
                                    style: UIBarButtonItemStyleBordered
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
			[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(leaveEditMode)]autorelease];
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
	//[SetListsManager rewriteTuneList: self->listItems toPropertyList:self->plist];
}


#pragma mark Overridden UIViewController Methods
-(void) invalidateTimer
{
	if (aTimer) 
	{
		[aTimer invalidate];
		//[aTimer release];
		aTimer = nil;
	}
}


- (void) viewDidAppear: (BOOL) animated
{
	[super viewDidAppear: animated];
	
    //[(UITableView *) self.view flashScrollIndicators];
}

- (void) viewDidLoad
{
	[super viewDidLoad];
    [self setColorForNavBar];
	
}	
- (void) viewDidUnload

{
	//NSLog (@"SVC viewDidUnLoad");
	[self invalidateTimer];
    [super viewDidUnload];
	
	
}
- (void) viewWillAppear: (BOOL) animated
{
	[super viewWillAppear: animated];
	
//	
//	UITableView *tabView = self->tableView; //(UITableView *) self.view;
//	[tabView reloadData];
//	
//	[self setupLeftSideNavItems];
//	[self setBarButtonItems];
//	
//	NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
//	
//	if (idxPath)
//		[tabView deselectRowAtIndexPath: idxPath
//							   animated: NO];
    
    [self loadView]; // will leak
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void) loadView
{
	
	
	self->canShare =  ([MFMailComposeViewController canSendMail]);
    
    
    
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
	
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [UIColor lightGrayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
	
	self.navigationItem.titleView = [DataManager makeTitleView:[NSString stringWithFormat:@"~%@",self->name]] ;
	[self setBarButtonItems];
	
	[self setupLeftSideNavItems];
    [self setColorForNavBar];
    [oview addSubview:tmpView];
	
	self->tableView = tmpView; // make everyone else happy too!
	self.view = oview;
	
}


#pragma mark Overridden NSObject Methods

- (void)dealloc {
	[self->aTimer release];
	[self->toass dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->toass release];	
	[self->plist release];
	[self->name release];
    
    [self->controller release];
    [self->listItems release];
    //[self->tableView release];
	
    [super dealloc];
	
}

- (void) didReceiveMemoryWarning
{	[self invalidateTimer];
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
        NSString *tune = 
		[self->listItems objectAtIndex:indexPath.row];
    
		// make this persistent
       
        
        
        [SetListsManager removeTune:tune list:self->name];
        
        
        
		[self->listItems removeObjectAtIndex:indexPath.row];
        
        
		//[SetListsManager rewriteTuneList:self->listItems toPropertyList:self->plist] ;
		[self->tableView reloadData];
		[self setBarButtonItems];
	}
}


-(void) tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *) oldPath toIndexPath:(NSIndexPath *) newPath
{
    NSUInteger oldrow = oldPath.row;
    NSUInteger newrow = newPath.row;
	//change the data as we move stuff around, -- thanks Erica Sadun
	NSString *oldname = [self->listItems objectAtIndex:oldrow];

    
	NSString *newname = [self->listItems objectAtIndex:newrow];
    
    NSString *rn= [oldname retain];
    
    
	[self->listItems removeObjectAtIndex:oldPath.row];
    
    
	[self->listItems insertObject: rn atIndex:newPath.row];
    
    if (oldrow > newrow)
    {

    NSLog (@"Insert oldrow %d oldname %@ BEFORE newrow %d newname %@",oldrow,oldname,newrow,newname);
    
    [SetListsManager updateTune: oldname before: newname   list: self->name];
    }
    else
        
    {
        
        NSLog (@"Insert oldrow %d oldname %@ AFTER newrow %d newname %@",oldrow,oldname,newrow,newname);
        
        [SetListsManager updateTune: oldname after: newname   list: self->name];
    }
    
    // persist change in core data by tweaking the timestamp
    
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
        cell.detailTextLabel.text = [DataManager makeBlurb:tn.title];
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
        
		TuneInfo  *tn = [TunesManager findTuneInfo:tune];
		if (tn) // item might have disappeared
		{
            
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
