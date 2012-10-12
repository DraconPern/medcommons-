//
//  OneTuneViewController.m
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "OneTuneViewController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "TitleNode.h"
#import "ViewSourceController.h"
#import "SettingsManager.h"

#import <UIKit/UIKit.h>

#import <MessageUI/MFMailComposeViewController.h>
#import <MediaPlayer/MediaPlayer.h>

#pragma mark Internal Constants

#define CONTENT_VIEW_EDGE_INSET    0.0f
#define DEFAULT_CONTENT_URL_STRING @"http://www.medcommons.net"
#define ANIMATION_DURATION           0.3f

#define NORMAL_EDGE_INSET            20.0f
#define NARROW_EDGE_INSET            8.0f
#define ZERO_EDGE_INSET              0.0f

#define NORMAL_TOOLBAR_HEIGHT        44.0f
#define NARROW_TOOLBAR_HEIGHT        32.0f

// for general screen
#define kLeftMargin				20.0f
#define kTopMargin				20.0f
#define kRightMargin			20.0f
#define kTweenMargin			10.0f
#define kTextFieldHeight		30.0f



@interface OneTuneViewController () 

- (void) startTrackingLoad;

- (NSTimeInterval) stopTrackingLoad;


- (void)createPicker;

-(NSMutableArray *) newSetlistsScan;

-(void) resizeContentFrame;

-(RefNode *) newRefNode;

-(void) pickerSetListChosen;

- (void) setupSegmentedControl: (NSInteger) variantIndex;

-(void) segmentedControlPushedInner:(NSUInteger )i;



-(void) segmentedControlPushed;
@end

@implementation OneTuneViewController
@synthesize mainTitle;
@synthesize footerToolbar      = footerToolbar_;
@synthesize footerView         = footerView_;

@synthesize backgroundView         = backgroundView_;
@dynamic    needsFooterToolbar;

@dynamic    toolbarHeight;


@synthesize contentView                = contentView_;

@synthesize currentPicker;
@synthesize myPickerView ,pickerViewArray, pickerLabel;
#pragma mark screenshot support
-(UIImage *) imageFromView :(UIView *)theView
{
	
	UIGraphicsBeginImageContext(theView.bounds.size);
	[theView drawRect:theView.bounds];
	//CGContextRef context = UIGraphicsGetCurrentContext();
	//[theView.layer renderInContext:context];
	UIImage *theImage = UIGraphicsGetImageFromCurrentImageContext();
	UIGraphicsEndImageContext();
	return theImage;
}


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

-(void) emailTune: (id) sender
{
//	UIImage *img = [self imageFromView:self.view];
	
	controller = [[MFMailComposeViewController alloc] init];
	// I had to introduce this hack which I dont understand
//	CGRect frame = controller.view.frame;
//	frame.origin.y -=44;
//	frame.size.height +=44;
//	controller.view.frame=frame;
	
	controller.mailComposeDelegate = self;
	[controller setSubject:[NSString stringWithFormat:@"Sending Tune -  %@",self->mainTitle]];

	
	//NSError *error;
	//NSStringEncoding encoding;
	NSString *filespec = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],self->path];
	
    SettingsManager *settings = [SettingsManager sharedInstance];
	BOOL pdf = NO;
	NSRange range=[filespec rangeOfString:@".pdf" options:0 ];
	if(range.location<[filespec length])
	{
		pdf = YES;
	}
	NSRange range2=[filespec rangeOfString:@".PDF" options:0 ];
	if(range2.location<[filespec length])
	{
		pdf = YES;
	}
	
	if (pdf == NO)
	{
	//NSString  *filecontents = [NSString stringWithContentsOfFile :filespec usedEncoding:&encoding error: &error];

	
	
	[controller setMessageBody:[NSString stringWithFormat:@"<html><head><title>%@</title></head><body>%@</body></html>",self->mainTitle,
												
								[NSString stringWithFormat: @"<footer>this email was built by %@ %@<br/>visit http://www.gigstand.net for more information</footer>",
								 settings.applicationName,
								 settings.applicationVersion]
								] 
						isHTML:YES]; 
		
	//	[controller addAttachmentData:UIImageJPEGRepresentation(img,1.0f) mimeType:@"image/jpg" fileName:[self->mainTitle stringByAppendingString:@".jpg"]];

	[controller addAttachmentData:[NSData dataWithContentsOfFile:filespec] mimeType:@"text/html" fileName:self->path];
	}
	else {
// PDF
	
	
		
		[controller setMessageBody:[NSString stringWithFormat:@"<html><head><title>%@</title></head><body>Please Share this GigStand Tune With Me<hr/>%@</body></html>",
									self->mainTitle,									
									[NSString stringWithFormat: @"<footer>this email was built by %@ %@<br/>visit http://www.gigstand.net for more information</footer>",
									 settings.applicationName,
									 settings.applicationVersion]
									] 
							isHTML:YES]; 
		[controller addAttachmentData:[NSData dataWithContentsOfFile:filespec] mimeType:@"application/pdf" fileName:self->path];
	
	
	
	}

//	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: controller] autorelease];
controller.modalPresentationStyle = UIModalPresentationFormSheet;
	//[self.navigationController pushViewController:controller animated:YES];
	[self presentModalViewController:controller animated:YES];	
}

#pragma mark UIPrintInteractionController support

- (void)printTune:(id)sender {
    UIPrintInteractionController *pcontroller = [UIPrintInteractionController sharedPrintController];
    if(!pcontroller){
        NSLog(@"Couldn't get shared UIPrintInteractionController!");
        return;
    }
    void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
	^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
        if(!completed && error){
            NSLog(@"FAILED! due to error in domain %@ with error code %u",
				  error.domain, error.code);
        }
    };
    UIPrintInfo *printInfo = [UIPrintInfo printInfo];
    printInfo.outputType = UIPrintInfoOutputGeneral;
    printInfo.jobName = self->mainTitle;
    printInfo.duplex = UIPrintInfoDuplexLongEdge;
    pcontroller.printInfo = printInfo;
    pcontroller.showsPageRange = YES;
	
    UIViewPrintFormatter *viewFormatter = [self.contentView viewPrintFormatter];
    viewFormatter.startPage = 0;
    pcontroller.printFormatter = viewFormatter;
	
 //   if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
//        [controller presentFromBarButtonItem:printButton animated:YES completionHandler:completionHandler];
//    }else
        [pcontroller presentAnimated:YES completionHandler:completionHandler];
}




#pragma mark MPMediaQuery to play Audio from iPod
- (void) playMusic
{
	
	
	/// PLAY MUSIC ONLY ON REAL DEVICE
	
	MPMediaQuery *iTunesQuery = [[[MPMediaQuery alloc] init] autorelease];	
	MPMediaPropertyPredicate *p = [MPMediaPropertyPredicate predicateWithValue:self->mainTitle forProperty:MPMediaItemPropertyTitle];
	[iTunesQuery addFilterPredicate:p];
	if ( [ [iTunesQuery items] count] > 0)
	{
		
		appMusicPlayer =
		[MPMusicPlayerController applicationMusicPlayer];
		
		[appMusicPlayer setShuffleMode: MPMusicShuffleModeOff];
		[appMusicPlayer setRepeatMode: MPMusicRepeatModeNone];
		[appMusicPlayer setQueueWithQuery:iTunesQuery];
		
		[appMusicPlayer play];
	}
	
	/// PLAY MUSIC ONLY ON REAL DEVICE		 
	
}

- (void) mediaPickerDidCancel: (MPMediaPickerController *) mediaPicker {
	
	[self dismissModalViewControllerAnimated: YES];
}



-(void) decorateTune: (NSString *)titlex
{
	// put the new tune on display
	
	[self resizeContentFrame]; //adjust based on current bounds
	self->mainTitle = titlex;
	self->canViewSource = YES; //assume true
	
	/// ONLY PLAY ON REAL DEVICE	
	
	MPMediaQuery *iTunesQuery = [[[MPMediaQuery alloc] init] autorelease];	
	MPMediaPropertyPredicate *p = [MPMediaPropertyPredicate predicateWithValue:self->mainTitle forProperty:MPMediaItemPropertyTitle];
	[iTunesQuery addFilterPredicate:p];
	
	self->canPlay = ([ [iTunesQuery items] count] > 0);
	/// 
	
	// add to recents
	
	//	
//	RefNode *tn =  ([RefNode inArray:[DataManager sharedInstance].recentItems findTitle:self->mainTitle]);
//	if (!tn)
	{
		id t = [[self newRefNode] autorelease]; // fill in protonode
		[[DataManager sharedInstance] updateRecents: t];
	}
	
	self.navigationItem.title = titlex;
	// go back to the last place we remember
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if ((tn)&&(tn.lastvariantsegmentindex>=0)) // must be found and must be beyond 
	{
		//NSLog(@"pushing from decorateTune with %d", tn.lastvariantsegmentindex);
		[self segmentedControlPushedInner:tn.lastvariantsegmentindex];
		
	} else 
		
		[self segmentedControlPushedInner:0];
}

-(void) displayTune: (NSString *)titlex
{
	[self decorateTune: titlex];
	// go back to the last place we remember
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if ((tn)&&(tn.lastvariantsegmentindex>=0)) // must be found and must be beyond 
	{
		//NSLog(@"pushing from displayTune with %d", tn.lastvariantsegmentindex);
		[self segmentedControlPushedInner:tn.lastvariantsegmentindex];
		
	} else 
		
	[self segmentedControlPushedInner:0];
}



#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//NSLog (@"buttonindex is %d",buttonIndex);
	
	if (actionSheet.tag == 1) // this is invoked from the upper right corner
	{
		
		if (buttonIndex==0)
			
		{
			[self printTune:actionSheet];            
		}
		
		if (buttonIndex==1)
			
		{
			[self emailTune:actionSheet];            
		}
		
		
	//	if (buttonIndex==2)
//		
//		{
//			[self viewSource];            
//		}
		
	}
	else 
		if (actionSheet.tag == 2) // this is invoked from the big plus sign
		{
			{
				if (buttonIndex >=  0)
				[ self pickerSetListChosen ];
			}
		}
	
	
}

-(void) upperRightButtonPressed ;
{
	[self->toass showFromBarButtonItem: self.navigationItem.rightBarButtonItem
							  animated: YES];
}

-(void) donePressed;
{
	//[self.parentViewController dismissModalViewControllerAnimated:YES];
	[self.navigationController popViewControllerAnimated:YES];
}

#pragma mark subs

-(RefNode *) newRefNode;
{
	NSString *archive = [[DataManager sharedInstance].archives objectAtIndex:0];
	RefNode *rn = [[RefNode alloc] initWithTitle:self->mainTitle 
								  andWithArchive:archive ] ;
	return rn;
	
}

-(void) pickerSetListChosen;
{
	NSLog (@"pickerSetListChosen with %@",self->chosenList);
	
	NSMutableArray *items= [[DataManager sharedInstance] allocLoadRefNodeItems:self->chosenList	]	;
	id t = [[self newRefNode] autorelease]; // why are we releasing this>
	[items addObject: t];
	[DataManager writeRefNodeItems:items toPropertyList:self->chosenList];	
	[items release];
	
	//self->pickerActionSheet.delegate=nil;
	//[self->pickerActionSheet release];
}


-(NSMutableArray *) newSetlistsScan;
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	[alllists addObject:@"favorites"];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			if (!([shortie isEqualToString:@"recents"] || [shortie isEqualToString:@"favorites"]))// ||
				  //[shortie isEqualToString:@"alltunes"] ||[shortie isEqualToString:@"archives"]))
				[alllists addObject:shortie];
			//else [shortie release];
		}
	}
	return alllists;
}
-(void) resizeContentFrame;
{
	// Content Window (frame only) is the whole screen minus the toolbar at the bottom
	// Content view:
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	
	tmpFrame.size.height -= self.toolbarHeight; 
	
	CGFloat edgeInset = 5.0f;
	
	//
	// If desired, inset content view frame a bit to help in debugging:
	//
	if (edgeInset > 0.0f)
		tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
										  UIEdgeInsetsMake (edgeInset,
															edgeInset,
															edgeInset,
															edgeInset));
	
	self->contentFrame_ = tmpFrame; // save this for later
}

-(NSString *) displayRect : (CGRect ) r
{
	
	return [NSString stringWithFormat:@"x %4.2f y %4.2f width %4.2f height %4.2f",
			r.origin.x, r.origin.y, r.size.width,r.size.height];
	
}


- (BOOL) needsFooterToolbar
{ 
	return YES;
}

- (void) scrollToSelectedRange
{
	
}


-(void) segmentedControlPushed;
{
	// one of the segmented buttons was pushed
	NSUInteger i = self->segmentedControl_.selectedSegmentIndex;
	//NSLog (@"Segment %d was selected",i);
	[self segmentedControlPushedInner:i];
	
	
	// lets try to flush alltunes to disk and see the time it takes to persist this
	//NSLog(@"beginning rewrite of alltunes in response to button push");
	[DataManager writeAllTunes];
	//NSLog(@"end rewrite of alltunes in response to button push");
	
}

- (void) setupSegmentedControl: (NSInteger) variantIndex;
{
	
	// get all of the nicknames for the archives
	
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if (!tn) {NSLog(@"could not find %@ in segmentedControl search of titlesDictionary", self->mainTitle); return;}
	
	NSUInteger countArchives = [tn.variants count];
	//	NSLog (@"tuneview %@ has %d variants",self->mainTitle, countArchives	);
	
	NSMutableArray *segmentItems = [[[NSMutableArray alloc]
									 initWithCapacity: countArchives]
									autorelease];
	
	
	for (NSString *key in tn.variants) // first make a dummy array
	{ 
		[segmentItems addObject:@"Should Override"];
	}
	for (NSString *key in tn.variants)
	{
		//up to the first slash to get the archive part of the filepath;
		NSString *segmentlabel = [DataManager shortNameFromArchiveName: [DataManager archiveNameFromPath:key]];
		[segmentItems replaceObjectAtIndex:[[tn.variants objectForKey:key] unsignedIntValue] withObject: [NSString stringWithFormat:@"  %@  ",
																										  segmentlabel
																										  ]];
		
	}
	
	self->segmentedControl_ = [[[UISegmentedControl alloc] initWithItems: segmentItems] retain];
	
	self->segmentedControl_.backgroundColor = [UIColor clearColor];
	self->segmentedControl_.momentary = NO;
	self->segmentedControl_.enabled = YES;
	self->segmentedControl_.segmentedControlStyle = UISegmentedControlStyleBar;
	self->segmentedControl_.selectedSegmentIndex = variantIndex;
	self->segmentedControl_.tintColor = [DataManager sharedInstance].appColor;
	
	[self->segmentedControl_ addTarget: self
								action: @selector (segmentedControlPushed)
					  forControlEvents: UIControlEventValueChanged];
	
	
}

-(NSString *) goBackOnList:(NSString *)s
{
	NSArray *a = self->listItems;
	NSUInteger count = [a count];
	for (NSUInteger i=0; i<count; i++)
	{
		RefNode *rn = [a objectAtIndex:i];
		if ([rn.title isEqualToString:s])
		{
			NSUInteger ind;
			// got a match, back it up
			if (i==0) ind = count-1;
			else ind = i-1;
			RefNode *rn2 = [a objectAtIndex:ind];
			return rn2.title;
		}
	}
	// if nothing found just return incoming
	return s;
}
-(NSString *) goForwardOnList:(NSString *)s
{
	
	NSArray *a = self->listItems;
	NSUInteger count = [a count];
	for (NSUInteger i=0; i<count; i++)
	{
		RefNode *rn = [a objectAtIndex:i];
		if ([rn.title isEqualToString:s])
		{
			NSUInteger ind;
			// got a match, back it up
			if (i==(count-1)) ind = 0;
			else ind = i+1;
			RefNode *rn2 = [a objectAtIndex:ind];
			return rn2.title;
		}
	}
	// if nothing found just return incoming
	return s;
}

-(void) rewindTune
{
	// lower rh buttons replacing segmented control
	NSString *tune;	
	if (!self->listItems)
		tune = [DataManager goBack : self->mainTitle];
	else
		tune = [self goBackOnList: self->mainTitle];
	if(![@"" isEqualToString:tune])	
		[self displayTune:tune];
}
-(void) fastforwardTune
{
	// lower rh buttons replacing segmented control
	NSString *tune;	
	if (!self->listItems)
		tune = [DataManager goForward: self->mainTitle];
	else
		tune = [self goForwardOnList: self->mainTitle];
	if(![@"" isEqualToString:tune])	
		[self displayTune:tune];
}

-(void) setupFooterToolbarItems
{
	// rework the toolbar
	
	UIBarButtonItem *flexSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
																				target: nil
																				action: NULL] 
								  autorelease];
	UIBarButtonItem *fixedSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemFixedSpace
																				 target: nil
																				 action: NULL] 
								   autorelease];
	fixedSpace.width = 30.0f;
	
	UIBarButtonItem *segCtlBBI = [[[UIBarButtonItem alloc]
								   initWithCustomView: self->segmentedControl_] autorelease]
	
	;
	
	
	
	UIBarButtonItem *barbutton1 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd 
																				 target:self action:@selector(createPicker)] autorelease];
	//			
	
	UIBarButtonItem *barbutton2 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRewind 
																				 target:self action:@selector(rewindTune)] autorelease];
	
	
	
	UIBarButtonItem *barbutton3 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFastForward 
																				 target:self action:@selector(fastforwardTune)] autorelease];
	
	
	
	
	
	self.footerToolbar.items = [NSArray arrayWithObjects:
								//  flexSpace,
								segCtlBBI,
								flexSpace,
								barbutton1,
								fixedSpace,
								barbutton2,fixedSpace,barbutton3,
								//barbutton4,
								nil];
}


-(void) setupRightSideNavItems
{
	
	if (self ->canViewSource)
	{
		self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Tune Options", @"")
												  delegate:self
										 cancelButtonTitle:nil
									destructiveButtonTitle:nil
										 otherButtonTitles:
					   NSLocalizedString (@"Print", @""),
					   NSLocalizedString (@"Share via Email", @""),
//					   NSLocalizedString (@"View Source", @""), 
					   nil];	
	}
	else {
		self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Tune Options", @"")
												  delegate:self
										 cancelButtonTitle:nil
									destructiveButtonTitle:nil
										 otherButtonTitles:
					   NSLocalizedString (@"Print", @""),
					   NSLocalizedString (@"Share via Email", @""),
					   nil];	
	}
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	
	// create a toolbar where we can place some buttons
	UIToolbar* toolbar;
	if (self->canPlay)
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
	
	if (self->canPlay)
	{
		
		// create a standard play button
		UIBarButtonItem *playButton = [[[UIBarButtonItem alloc]
										initWithBarButtonSystemItem:UIBarButtonSystemItemPlay
										target:self
										action:@selector(playMusic)] autorelease];
		playButton.style = UIBarButtonItemStyleBordered;
		[buttons addObject:playButton];
		// create a spacer between the buttons
		UIBarButtonItem *spacer = [[[UIBarButtonItem alloc]
									initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace
									target:nil
									action:nil] autorelease];
		[buttons addObject:spacer];
	}
	
	// create a standard action
	UIBarButtonItem *saveButton = [[[UIBarButtonItem alloc] 
									initWithBarButtonSystemItem: UIBarButtonSystemItemAction
									target:self 
									action:@selector(upperRightButtonPressed)] autorelease];
	saveButton.style = UIBarButtonItemStyleBordered;
	[buttons addObject:saveButton];
	
	// put the buttons in the toolbar and release them
	[toolbar setItems:buttons animated:NO];
	
	
	// place the toolbar into the navigation bar
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc]
											   initWithCustomView:toolbar] autorelease];
}


- (CGFloat) toolbarHeight
{
	UIDevice *device = [UIDevice currentDevice];
	
	//
	// If device is in landscape orientation AND device is iPhone, use narrow
	// toolbar height:
	//
	if (UIDeviceOrientationIsLandscape (device.orientation) &&
		(![device respondsToSelector: @selector (userInterfaceIdiom)] ||
		 (device.userInterfaceIdiom == UIUserInterfaceIdiomPhone)))
		return NARROW_TOOLBAR_HEIGHT;
	
	return NORMAL_TOOLBAR_HEIGHT;
}





-(void) segmentedControlPushedInner:(NSUInteger )i;
{
	
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if (!tn) {NSLog(@"could not find %@ in segmentedControl search of titlesDictionary", self->mainTitle); return;}
	for (NSString *key in tn.variants)
		
	{
		NSUInteger value = [[tn.variants objectForKey:key] unsignedIntValue];
		//NSLog(@"key %@ value %d",key,value);
		
		if (value  == i) // found it, change the view
		{
			
			if (self->segmentedControl_) 
			{	// take this off the screen
				[self->segmentedControl_ removeFromSuperview];
				[self->segmentedControl_ release];
			}
			// add the left side segmentcontrol back in
			tn.lastvariantsegmentindex = i; // record where we have been
			[self setupSegmentedControl:i]; 
			
			[self setupFooterToolbarItems];
			
			
			BOOL processAsURL = NO;
			
			NSString *filename = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],key];
			NSString *ext = [filename pathExtension];
			
			
			NSRange range=[filename rangeOfString:@"-opaque" options:0 ];
			if(range.location<[filename length])
			{
				processAsURL = YES;
				self->canViewSource = NO;
			}
			NSRange range2=[filename rangeOfString:@"-asis" options:0 ];
			if(range2.location<[filename length])
			{
				processAsURL = YES;
				self->canViewSource = NO;
			}
			
			
			[self setupRightSideNavItems];
			
			if ((processAsURL == NO) && 
				([ext isEqualToString:@"html"] || [ext isEqualToString:@"txt"] || [ext isEqualToString:@"chords"]|| [ext isEqualToString:@"lyrics"]))
			{
				// if its any of these, look for a special header in the archiveheaders
				// if no header data then apply defaults
				self->canViewSource = YES;
				
				NSString *archivename = [DataManager archiveNameFromPath:key];
				NSUInteger archiveindex = [DataManager indexFromArchiveName:archivename];
				NSMutableDictionary *mdict = [[DataManager sharedInstance].archiveheaders objectAtIndex: archiveindex];
				NSMutableString *headerdata = [mdict valueForKey:ext]; // ok
				if (!headerdata)
				{
					float scale=1.0f;
					
					if ([ext isEqualToString:@"html"] ||  [ext isEqualToString:@"chords"])
					{
						scale = 1.3f;
					}
					if ( [ext isEqualToString:@"txt"] ||  [ext isEqualToString:@"lyrics"])
					{
						scale = 1.5f;
					}
					//,initial-scale=%2.1f
					
					headerdata = [NSString stringWithFormat:
								  @"<head><meta name='viewport' content='width=device-width'/></head>", scale];
					
				}
				NSString *prebegin;
				NSString *preend;
			    if ([ext isEqualToString:@"txt"]) {
					
					prebegin = @"<pre>"; preend = @"</pre>";
					
				}
				else {
					prebegin = preend = @"";
				}
				
				
				NSError *error;
				NSStringEncoding encoding;
				NSString *bodydata = [NSString stringWithContentsOfFile:filename usedEncoding:&encoding error: &error];
				// Now build the full deal
				NSString *html = [NSString stringWithFormat:@"<html>%@<body>%@%@%@</body></html>",headerdata,prebegin,bodydata,preend];
				
				//NSLog (@"Displaying Blended File %@ for ext %@ headerdata %@> ",filename,ext,headerdata);				  
				[self refreshWithHTML: html andWithShortPath: key];
				
				return;
				
				
			}
			
			else
				
			{
				// Here for all those archives that are totally opaque
				self->canViewSource = NO;
				NSURL    *docURL = [NSURL fileURLWithPath: filename isDirectory: NO];
				//NSLog (@"Displaying Direct File %@ with no Header Manipulation > ",filename);
				
				[self refreshWithURL: docURL andWithShortPath: key];
				return;
			}
			
			
		}
	}
}
#pragma mark Private Instance Methods

- (void) startTrackingLoad
{
	self->depth_++;
	
	[self->activityIndicator_ startAnimating];
	
	// [self.appDelegate didStartNetworkActivity];
	
	self->startTime_ = [NSDate timeIntervalSinceReferenceDate];
}

- (NSTimeInterval) stopTrackingLoad
{
	NSTimeInterval stopTime = [NSDate timeIntervalSinceReferenceDate];
	
	//  [self.appDelegate didStopNetworkActivity];
	
	[self->activityIndicator_ stopAnimating];
	
	if (self->depth_ > 0)
		self->depth_--;
	
	return stopTime - self->startTime_;
}

#pragma mark Overridden UIViewController Methods



- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
	return YES;
}
-(CGRect) frameContentRect
{
	//float tbheight = 44.0f;//[self toolbarHeight];
	CGRect tmpFrame = self->contentFrame_;
	if (NO==self->first)
	{
		tmpFrame.size.height-= (44.0f+20.0f);
		tmpFrame.origin.y+=44.0f;
	} else {
		tmpFrame.size.height -=44.0f;
		tmpFrame.origin.y+=44.0f;
	}
	return tmpFrame;
	
}

-(void) remakeContentView
{	
	if (self->contentView_) {
		[self->contentView_ removeFromSuperview];
		[self->contentView_ release];
	}
	//NSLog (@"remake contentView contentFrame_ %@",[self displayRect:tmpFrame]);
	
	// and adds it to the background
	self->contentView_ = [[UIWebView alloc] initWithFrame: [self frameContentRect]];
	
	self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
										   UIViewAutoresizingFlexibleWidth);
	self->contentView_.backgroundColor = [UIColor blackColor];
	self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
	self->contentView_.delegate =  nil ; //was just self
	self->contentView_.scalesPageToFit = YES;
	
	
	
	[self->backgroundView_ addSubview: self->contentView_];
	
	//
	// Kick off  load of content view:
	//
	[self->contentView_ loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
}


- (void) refreshWithHTML : (NSString *) html andWithShortPath: (NSString *) shortpath;
{
		self->canViewSource=YES;
	if (self->html_) [self->html_ release];
	self->html_ = [html copy ]; // new URL 
	
	if (self->path) [self->path release];
	self->path = [shortpath copy];
	
	// everylast thing was set up in init
	
	[self resizeContentFrame];
	
	if (self->contentView_) {
		[self->contentView_ removeFromSuperview];
		[self->contentView_ release];
	}
	
	
	self->contentView_ = [[UIWebView alloc] initWithFrame: [self frameContentRect]];
	
	self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
										   UIViewAutoresizingFlexibleWidth);
	self->contentView_.backgroundColor = [UIColor lightGrayColor];
	self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
	self->contentView_.delegate =  nil ; //was just self
	self->contentView_.scalesPageToFit = YES;
	
	[self->backgroundView_ addSubview: self->contentView_];
	
	//
	// Kick off  load of content view:
	//
	[self->contentView_ loadHTMLString:self->html_ baseURL:NULL ];

	self.view = self->backgroundView_;
}

- (void) refreshWithURL : (NSURL *) URL andWithShortPath: (NSString *) shortpath;
{
	
	self->canViewSource=NO;
	if (self->contentURL_) [self->contentURL_ release];
	self->contentURL_ = [URL copy ]; // new URL 
	
	if (self->path) [self->path release];
	self->path = [shortpath copy];
	
	// everylast thing was set up in init
	
	[self resizeContentFrame];
	[self remakeContentView];
	self.view = self->backgroundView_;
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
	[super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}


-(void) setupTuneView
{
	//
	// Background view:
	//
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	
	//	NSLog (@"frame parentViewController_ %@",[self displayRect:tmpFrame]);
	
	self->backgroundView_ = [[UIView alloc] initWithFrame: tmpFrame]
	;
	
	////	NSLog (@"frame backgroundView_ %@",[self displayRect:tmpFrame]);
	self->backgroundView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
											  UIViewAutoresizingFlexibleWidth);
	self->backgroundView_.backgroundColor = [UIColor lightGrayColor];
	
	[self resizeContentFrame];
	
	
	//	NSLog (@"frame contentView_ %@",[self displayRect:tmpFrame]);
	
	// Stuff at the bottom
	
	//
	// Footer view frame uses coordinates relative to its containing view
	// (i.e., background view):
	//
	
	tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	tmpFrame.origin.y = (tmpFrame.size.height -  self.toolbarHeight);
	tmpFrame.size.height = self.toolbarHeight;
	
	//
	// Create footer view:
	//
	//	NSLog (@"frame footerView_ %@",[self displayRect:tmpFrame]);
	self->footerView_ = [[UIView alloc] initWithFrame: tmpFrame];
	
	self.footerView.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
										UIViewAutoresizingFlexibleWidth);
	self.footerView.backgroundColor = [UIColor clearColor];
	
	tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	tmpFrame.origin.y = (tmpFrame.size.height -  self.toolbarHeight);
	tmpFrame.size.height = self.toolbarHeight;
	
	
	self->footerToolbar_ = [[UIToolbar alloc] init];
	
	self.footerToolbar.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
										   UIViewAutoresizingFlexibleWidth);
	self.footerToolbar.barStyle = UIBarStyleBlack;
	self.footerToolbar.translucent = YES;
	
	
	// go back to the last place we remember
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if ((tn)&&(tn.lastvariantsegmentindex>=0)) // must be found and must be beyond 
	{
		NSLog(@"setting up segmentment control with %d", tn.lastvariantsegmentindex);
		[self setupSegmentedControl:tn.lastvariantsegmentindex];
		
	} else 
		
	[self setupSegmentedControl:0];
	
	
	self.footerToolbar.frame = tmpFrame;
	
	
	[self setupFooterToolbarItems]; 
	
	
	//NSLog (@"frame footerToolbar_ %@",[self displayRect:tmpFrame]);
	// add this all into the background in precisely this order otherwise the butons are dead!
	
	[self->backgroundView_ addSubview:self->footerView_];
	[self->backgroundView_ addSubview:self->footerToolbar_];
	
	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] //initWithTitle:self->backLabel style:UIBarButtonItemStyleBordered
											  initWithBarButtonSystemItem:UIBarButtonSystemItemDone 
																			 target:self 
																			 action:@selector(donePressed)] autorelease];
	
	[self setupRightSideNavItems];
	

	
	
	
	
	
	
	
}


#pragma mark - standard bits
//
//-(id) initWithTitle:(NSString *) titl {
//	self = [super init];
//	if (self)
//	{
//		self->mainTitle=[titl copy];
//	}
//	return self;
//}
- (void)createPicker;
{	
	
	if (pickerViewArray) [pickerViewArray release];
	if (myPickerView) 
	{ 
		[myPickerView removeFromSuperview];
		[myPickerView release];
	}
	
	
	// put this in an action sheet
	NSString *title = UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)?
	@"\n\n\n\n\n\n\n\n\n\n\n\nSelect List then Tap to Add":
	@"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nSelect List then Tap to Add"; 
	
	pickerActionSheet = [[UIActionSheet alloc] initWithTitle: title
													delegate:self
										   cancelButtonTitle:nil
									  destructiveButtonTitle:nil
										   otherButtonTitles:[NSString stringWithFormat:@"Add %@",self->mainTitle,nil],nil];
	pickerActionSheet.tag = 2;
	[pickerActionSheet showInView:self.view];								  
	pickerViewArray =[self newSetlistsScan];
	
	CGRect frame = UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)?
	CGRectMake(0, 0, 480, 162.0f):
	CGRectMake(0, 0, 320, 216.0f);
	
	myPickerView = [[UIPickerView alloc] initWithFrame:frame];
	myPickerView.tag = 2;
	
	
	//myPickerView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	myPickerView.showsSelectionIndicator = YES;	// note this is default to NO
	myPickerView.backgroundColor = [UIColor clearColor];
	
	// this view controller is the data source and delegate
	myPickerView.delegate = self;
	myPickerView.dataSource = self;
	[myPickerView selectRow:0 inComponent:0 animated:YES];
	self->chosenList = @"favorites";
	[pickerActionSheet addSubview:myPickerView];
	
}


//- (void) viewSource
//{
//	if (self->canViewSource)
//	{
//		ViewSourceController *vsc = [[[ViewSourceController alloc] initWithPath:self->path] autorelease];
//		
//		
//		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: vsc] autorelease];
//		
//		[self presentModalViewController:nav
//								animated: YES];
//	}
//}

-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex 
					  andWithShortPath: (NSString *) shortpath andWithBackLabel:(NSString *) labelx;

{
	self = [super init];
	
	if (self)
	{
		self->contentURL_ = [URL copy ];
		self->depth_ = 0;
		self->sharedDocument_ = NO; //[URL.scheme isEqualToString: NSURLFileScheme];  // for now ...
		self->mainTitle = [ titlex copy]; 
		self->first = YES;
		self->backLabel = [labelx copy];
		
		self->canPlay = NO;
		
		self->pc = self;
		self->path = [shortpath copy];
		self->listItems = nil; //important
	}
	return self;
}


-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex 
					  andWithShortPath: (NSString *) shortpath andWithBackLabel:(NSString *) labelx andWithItems:(NSArray *)items;
{
	self = [super init];
	
	if (self)
	{
		self->contentURL_ = [URL copy ];
		self->depth_ = 0;
		self->sharedDocument_ = NO; //[URL.scheme isEqualToString: NSURLFileScheme];  // for now ...
		self->mainTitle = [ titlex copy]; 
		self->first = YES;
		self->backLabel = [labelx copy];	
		self->canPlay = NO;
		self->pc = self;
		self->path = [shortpath copy];
		self->listItems = items; //important don't release
	}
	return self;
}

- (void)dealloc {
	
	// release all the other objects
	
	[pickerViewArray release];
	[myPickerView release];
	
	
	//self->pickerActionSheet.delegate=nil;
	[self->pickerActionSheet dismissWithClickedButtonIndex:-1 animated:NO];
	[self->pickerActionSheet release];
	
	//self.myPickerView = nil;
	//self.pickerViewArray = nil;
	
	
	
	[ appMusicPlayer stop]; // stop playing
	[self stopTrackingLoad];
	//[self->mainTitle release];
	[self->toass dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->toass release];//[self->contentURL_];
	[self->footerToolbar_ release];
	[self->footerView_ release];
	[self->segmentedControl_ release];
	[self->backLabel release];
	//	[self->listItems release];
	
	self->contentView_.delegate = nil;    // Apple docs say this is required
	
	[self->contentURL_ release];
	[self->mainTitle release];
	[self->contentView_ release];//loadview
	[self->backgroundView_ release]; //init
	
	[super dealloc];
}

- (void) loadView
{

	self.myPickerView = nil;
	self.pickerViewArray = nil;
	self->canViewSource = YES;
	
	NSRange range=[self->path rangeOfString:@"-opaque" options:0 ];
	if(range.location<[self->path length])
	{
		self->canViewSource = NO;
	}
	NSRange range2=[self->path rangeOfString:@"-asis" options:0 ];
	if(range2.location<[self->path length])
	{
		self->canViewSource = NO;
	}
	
	
	if (self->first)	[self setupTuneView]; 
	
	[self decorateTune: self->mainTitle];
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	self.view = self->backgroundView_;
	self->first = NO;

}

#pragma mark UIWebViewDelegate Methods

- (void) webView: (UIWebView *) webView
didFailLoadWithError: (NSError *) error
{
	NSTimeInterval elapsedTime = [self stopTrackingLoad];
	
	if (![@"" isEqual: [webView.request.URL description]])  // avoid spurious errors ...
		NSLog (@"Failed to load <%@>, error code: %d, elapsed time: %.3fs, depth: %d",
			   //self->contentURL_,
			   webView.request.URL,
			   error.code,
			   elapsedTime,
			   self->depth_);
	
	if (error.code != -999) // PDFs return this error code!
	{
		//
		// Report error inside web view:
		//
		NSString *errorString = [NSString stringWithFormat:
								 @"<html><center><font size='+5' color='red'>Failed to load &lt;%@&gt;:<br>%@</font></center></html>",
								 //self->contentURL_,
								 webView.request.URL,
								 error.localizedDescription];
		
		[self.contentView loadHTMLString: errorString
								 baseURL: nil];
	}
}

- (void) webViewDidFinishLoad: (UIWebView *) webView
{
	NSTimeInterval elapsedTime = [self stopTrackingLoad];
	
	NSLog (@"Loaded <%@>, elapsed time: %.3fs, depth: %d",
		   //self->contentURL_,
		   webView.request.URL,
		   elapsedTime,
		   self->depth_);
}

- (void) webViewDidStartLoad: (UIWebView *) webView
{
	//	NSLog (@"Loading <%@>, depth: %d",
	//		   webView.request.URL,
	//		   self->depth_);
	
	[self startTrackingLoad];
}

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navType
{    return YES;
}


#pragma mark -
#pragma mark UIPickerViewDelegate

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	//	if (pickerView == myPickerView)	// don't show selection for the custom picker
	//	{
	//		// report the selection to the UI pickerLabel
	//		pickerLabel.text = [NSString stringWithFormat:@"Click Done to Add This Tune  %@ to %@....",
	//							self->mainTitle,
	//							[pickerViewArray objectAtIndex:[pickerView selectedRowInComponent:0]]
	//							];
	self->chosenList  =  [pickerViewArray objectAtIndex:[pickerView selectedRowInComponent:0]];
	//NSLog (@"pickerview delegate chose %@",self->chosenList);
	//	}
}



- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
	NSString *returnStr = @"";
	
	// note: custom picker doesn't care about titles, it uses custom views
	if (pickerView == myPickerView)
	{
		if (component == 0)
		{
			returnStr = [pickerViewArray objectAtIndex:row];
		}
		else
		{
			returnStr = [[NSNumber numberWithInt:row] stringValue];
		}
	}
	
	return returnStr;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component
{
	CGFloat componentWidth = 768.0f;
	
	return componentWidth;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component
{
	return 60.0f;
}


#pragma mark -
#pragma mark UIPickerViewDataSource

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
	return [pickerViewArray count];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}



@end
