// this may no longer be needed now that search has been moved to its own tab

//  GeneralSetListViewController.m
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "GeneralSetListViewController.h"

#import "DataManager.h"
#import "DataStore.h"
#import "TitleNode.h"
#import "OneTuneViewController.h"
#import "SettingsManager.h"


@implementation GeneralSetListViewController

#pragma mark MFMailComposeViewController support
- (void)mailComposeController:(MFMailComposeViewController*)controllerx  
          didFinishWithResult:(MFMailComposeResult)result 
                        error:(NSError*)error;
{
	if (result == MFMailComposeResultSent) {
//		NSLog(@"It's away!");
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
		//	NSString *version = [temp objectForKey:@"version"];
		//	NSString *originalList = [temp objectForKey:@"listname"];
		//	NSLog (@"***** Processing list %@ version %@",originalList,version);
		
		NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
		
	//	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
		
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
	//	NSString *version = [temp objectForKey:@"version"];
	//	NSString *originalList = [temp objectForKey:@"listname"];
	//	NSLog (@"***** Processing list %@ version %@",originalList,version);
	
    SettingsManager *settings = [SettingsManager sharedInstance];
	NSString *footer = [NSString stringWithFormat: @"this setlist was built by %@ %@<br/>visit www.gigstand.net for more information",
	 settings.applicationName,
						settings.applicationVersion];
	
	NSMutableArray *titlesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"titles"] ];
	
	//	NSMutableArray *archivesx = [NSMutableArray arrayWithArray:[temp objectForKey:@"archives"]];
	
	[sec appendFormat:@"<html><head><style>body {font-family:Tahoma,Verdana,Arial;} li {font-size:1.6em;}</style></head><body><h1>%@</h1><ul>",self->plist];
	
	for (NSUInteger n=0; n<[titlesx count]; n++)
	{
		
		[sec appendFormat:@"<li>%@</li>",[titlesx objectAtIndex:n]];
	}
	[sec appendFormat:@"</ul><footer><small>%@</small></footer></body></html>",footer];
	return sec;
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
		
		void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
		^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
			if (!completed && error) {
				NSLog(@"Printing could not complete because of error: %@", error);
			}
		};
		//if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
		///	[pic presentFromFromBarButtonItem:sender animated:YES completionHandler:completionHandler];
		//} else {
			[pic presentAnimated:YES completionHandler:completionHandler];
		//}
	[mt release];
	
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
	
	
    SettingsManager *settings = [SettingsManager sharedInstance];
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
									   settings.applicationName,
									   settings.applicationVersion]
								] 
	isHTML:NO]; 
	
	
	
	[controller addAttachmentData:plistXML mimeType:@"gigstand/x-stl" fileName:[self->plist stringByAppendingString:@".stl"]];
	[self presentModalViewController:controller animated:YES];	
	[st release];
}

-(void) donePressed;
{
	[self.navigationController popViewControllerAnimated:YES ];
}

-(void) actionPressed
{
	self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Setlist Options", @"")
											  delegate:self
									 cancelButtonTitle:nil
								destructiveButtonTitle:nil
									 otherButtonTitles:
				   NSLocalizedString (@"Print", @""),
				   NSLocalizedString (@"Email", @""),
				   nil];	
	
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	[self->toass showFromBarButtonItem: self.navigationItem.leftBarButtonItem
							  animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//NSLog (@"buttonindex is %d",buttonIndex);
	
	if (actionSheet.tag == 1) // this is invoked from the upper left corner
	{
		if (buttonIndex==0)
			
		{
			[self printPressed:actionSheet];            
		}
		if (buttonIndex==1)
			
		{
			[self emailPressed];            
		}
		
	}
}
-(void) setupLeftSideNavItems
{
	
	// create a toolbar where we can place some buttons
	UIToolbar* toolbar;
	if ((self->canShare) && (self->canEdit)&& [super itemCount]>0)
	{
		toolbar = [[[UIToolbar alloc]
					initWithFrame:CGRectMake(0, 0, 100, 45)] autorelease];
	}
	else
	{
		toolbar = [[[UIToolbar alloc]
					initWithFrame:CGRectMake(0, 0, 50, 45)] autorelease];
	}
	//[toolbar setBarStyle: UIBarStyleBlackOpaque];
	
	// create an array for the buttons
	NSMutableArray* buttons = [[[NSMutableArray alloc] initWithCapacity:3] autorelease];
	// create a standard done action
	UIBarButtonItem *doneButton = [[[UIBarButtonItem alloc] 
									initWithBarButtonSystemItem: UIBarButtonSystemItemCancel
									target:self 
									action:@selector(donePressed)] autorelease];
	doneButton.style = UIBarButtonItemStyleBordered;
	[buttons addObject:doneButton];
	
	if ((self->canShare)&&(self->canEdit))
	{
		// create a spacer between the buttons
		UIBarButtonItem *spacer = [[[UIBarButtonItem alloc]
									initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace
									target:nil
									action:nil] autorelease];
		[buttons addObject:spacer];
		
		// create a spacer between the buttons
		UIBarButtonItem *spacer2 = [[[UIBarButtonItem alloc]
									initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace
									target:nil
									action:nil] autorelease];
		[buttons addObject:spacer2];
		
		// create a standard action button
		UIBarButtonItem *playButton = [[[UIBarButtonItem alloc]
										initWithBarButtonSystemItem:UIBarButtonSystemItemAction
										target:self
										action:@selector(actionPressed)] autorelease];
		playButton.style = UIBarButtonItemStyleBordered;
		[buttons addObject:playButton];
	

	
	}
	// put the buttons in the toolbar and release them
	[toolbar setItems:buttons animated:NO];
	
	
	// place the toolbar into the navigation bar
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc]
											   initWithCustomView:toolbar] autorelease];
}




- (void) loadView 
{
	self->canShare =  ([MFMailComposeViewController canSendMail]);

	[super loadView];
	
	[self setupLeftSideNavItems];
	
}

- (void) viewWillAppear: (BOOL) animated
{
	[super viewWillAppear: animated];
	
	
	UITableView *tabView = (UITableView *) self.view;
	
	
	[tabView reloadData];
	
	[self setupLeftSideNavItems];
	
	NSIndexPath *idxPath = [tabView indexPathForSelectedRow];
	
	if (idxPath)
		[tabView deselectRowAtIndexPath: idxPath
							   animated: NO];
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
//- (void)viewDidLoad {
// 
//    [super viewDidLoad];
// //
// self.navigationItem.leftBarButtonItem = 
// [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSearch target:self action:@selector(searchpressed)] autorelease];
//}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

//
//- (void)didReceiveMemoryWarning {
//    // Releases the view if it doesn't have a superview.
//    [super didReceiveMemoryWarning];
//    
//    // Release any cached data, images, etc. that aren't in use.
//}
//
//
//- (void)viewDidUnload {
//    [super viewDidUnload];
//    // Release any retained subviews of the main view.
//    // e.g. self.myOutlet = nil;
//}
//

- (void)dealloc {
    [super dealloc];
	
	[self->toass dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->toass release];//[self->contentURL_];
}

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
		[DataManager writeRefNodeItems:self->listItems toPropertyList:self->plist] ;
		[self->tableView reloadData];
		[super setBarButtonItems];
	}
}


-(void) tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *) oldPath toIndexPath:(NSIndexPath *) newPath
{
	//change the data as we move stuff around, -- thanks Erica Sadun
	
	RefNode *rn= [[self->listItems objectAtIndex:oldPath.row] retain];
	[self->listItems removeObjectAtIndex:oldPath.row];
	[self->listItems insertObject: rn atIndex:newPath.row];
	[rn release];
	[super setBarButtonItems];
	
	
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
	
	
	if (section < [self->listItems count]) // this is always 1
	{
		//MCDocumentTableViewCell *docCell = (MCDocumentTableViewCell *) cell;
		
		cell.accessoryType = UITableViewCellAccessoryNone;; //Indicator;
		
		
		
		RefNode *tn = [ self->listItems objectAtIndex:row];
		
		if (!tn)
		{
			NSLog(@"cant find TitleNode found for item at row %d", tn);
			
			
			cell.textLabel.text = tn.title;			
			cell.detailTextLabel.text = @"NOT FOUND ON THIS IPAD";
			return cell;
		}
		
		else 
		{
			//NSLog (@"found TitleNode for item at row %d dict %@", row, tn);
		}
		
		
		// look it up again
		
		TitleNode *tn2 = [[DataManager sharedInstance].titlesDictionary objectForKey:tn.title ];
		if (!tn2)
		{
			cell.textLabel.text = tn.title;			
			cell.detailTextLabel.text = [NSString stringWithFormat:@"should be in archive %@ but not found on this iPad",tn.archive];
			return cell;
		}
		
		NSEnumerator *enumerator = [tn2.variants keyEnumerator];
		id key;
		//		NSString *path;
		NSMutableString *namex=[NSMutableString stringWithString:@""]; 
		NSUInteger variants = 0;
		while ((key = [enumerator nextObject])) {
			//up to the first slash to get the archive part of the filepath;
			NSString *s = [key stringByDeletingLastPathComponent];
			NSUInteger arcidx=[DataManager indexFromArchiveName:s];
			//NSLog (@"Looked up %@ got %d",s,arcidx);//////////999999//////////////////////////////////////////////
			NSString	*sn = [DataManager shortNameFromArchiveIndex:arcidx];
			namex = (NSMutableString *)[ namex stringByAppendingFormat: @" %@ ",sn];
			variants++;
		}
		//path = [tn2.variants objectForKey:name];
		cell.textLabel.text = tn2.title;			
		cell.detailTextLabel.text = namex;
		//if (variants>1) NSLog (@"multiple versions of single title %@",tn2.title);
		
	}
	else
		cell = nil;
	
	
	return cell;
}



- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	
	// section should always be 1
	
	
	NSUInteger count = [self->listItems  count];
	
	if (idxPath.row < count)
	{
		
		
		RefNode *tn = [ self->listItems  objectAtIndex:idxPath.row];
		if (!tn)
		{
			NSLog(@"no refNodes found for item at row %d", idxPath.row);
			return;
		}
		// look it up again
		
		TitleNode *tn2 = [[DataManager sharedInstance].titlesDictionary objectForKey:tn.title ];
		if (!tn2)
		{
			NSLog(@"cant find TitleNode2for %@", tn.title);
			return;
		}
		
		for (NSString *path in tn2.variants) // only executed for the first variant
			
			if ([[tn2.variants objectForKey:path] unsignedIntValue] == 0) 
				
			{
				
				
				NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],path];
				NSURL    *docURL = [NSURL fileURLWithPath: url
											  isDirectory: NO];
				
			//	NSLog (@"=>recentssview  %@",url);
				
				OneTuneViewController *wvc = [[[OneTuneViewController alloc]
											   initWithURL: docURL andWithTitle: tn.title andWithShortPath: path andWithBackLabel:self->name
											   andWithItems:self->listItems]
											  autorelease];
				wvc.title = tn.title  ;
				
				//UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
//				
//				[self presentModalViewController:nav animated: YES];
//				
				[self.navigationController pushViewController:wvc animated:YES];
				break;
			}
	}
}


@end
