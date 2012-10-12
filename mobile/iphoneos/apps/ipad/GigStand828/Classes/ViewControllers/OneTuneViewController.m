//
//  OneTuneViewController.m
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import "VariantChooserControl.h"
#import "OneTuneViewController.h"
#import "DataManager.h"
#import "SetListsManager.h"

#import "DataStore.h"
#import "TuneInfo.h"
#import "TuneInfoController.h"
#import "ArchivesManager.h"

#import "SetListChooserControl.h"
#import "MediaViewController.h"
#import "GigStandAppDelegate.h"
#import <UIKit/UIKit.h>

#import <MessageUI/MFMailComposeViewController.h>
#import <MediaPlayer/MediaPlayer.h>

#import "BonJourManager.h"
#import "InstanceInfo.h"
#import "TunesManager.h"

#pragma mark Internal Constants

#define CONTENT_VIEW_EDGE_INSET    0.0f
#define DEFAULT_CONTENT_URL_STRING @"http://www.medcommons.net"
#define ANIMATION_DURATION           0.3f

#define NORMAL_EDGE_INSET            20.0f
#define NARROW_EDGE_INSET            8.0f
#define ZERO_EDGE_INSET              0.0f

#define NORMAL_TOOLBAR_HEIGHT        32.0f
#define NARROW_TOOLBAR_HEIGHT        32.0f

// for general screen
#define kLeftMargin				20.0f
#define kTopMargin				20.0f
#define kRightMargin			20.0f
#define kTweenMargin			10.0f
#define kTextFieldHeight		30.0f



@interface OneTuneViewController () 



-(void) remakeContentView;
-(void) resizeContentFrame;
-(NSString *) newTuneTitle;

// these routines need to work on the aux screen as well as the main screen
-(void) displayTune: (NSString *)titlex filePath :(NSString * )pathx archive:(NSString *)archive;
-(void) refreshWithHTML : (NSString *) html;
-(void) refreshWithURL : (NSURL *) URL ;
-(void) radioControlVirtuallyPushed:(InstanceInfo *) ii;

// these routines are only for the main device screen 
-(void) toggleFullScreen: (id)abt;

-(void) setupFooterToolbarItems:(NSString *)theLabel label2:(NSUInteger )theLabel2;
-(void) setupRightSideNavItems;

- (CGFloat) toolbarHeight;

@end

@implementation OneTuneViewController


#pragma mark Button Press Actions, however no IB here


-(void) chooserPickedTune: (InstanceInfo *) ii
{
	//NSLog (@"OTV chooserPickedTune ii %@", ii);
	[self radioControlVirtuallyPushed:ii];
}

-(void) upperRightButtonPressed ;
{
	[self->toass showFromBarButtonItem: self.navigationItem.rightBarButtonItem
							  animated: YES];
}

-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}


-(void) setListWasChosen :(NSDictionary *) dict
{
	NSString *list = [dict objectForKey:@"list"];
	NSString *ctune = [dict objectForKey:@"tune"];
	//
	// rewrite entire setlist
	//	
	NSMutableArray *items= [SetListsManager allocListOfTunes:list	]	;
	BOOL found=NO;
	for (NSString *tune in items)
		if ([tune isEqualToString:ctune])// check its not already in there
		{
			found=YES; 
			break;
		}
	if (found==NO) 
	{
		[items addObject: self->currentTuneTitle];
		[SetListsManager rewriteTuneList:items toPropertyList:list];	
	}
	[items release];
}
-(void) presentModalSetListChooser;
{
	self->setListChooserControl = [[SetListChooserControl alloc] initWithTune:self->currentTuneTitle
																	andAction:@selector(setListWasChosen:)
																andController: self];
	[super.view addSubview:self->setListChooserControl];
}
#pragma mark special gesture for full screen toggle

- (void) toggleFullScreen: (id)abt
{
	
	// everylast thing was set up in init
	
	self->fullScreen = ! (self->fullScreen);
    self.navigationController.navigationBarHidden = self->fullScreen; 

	[self remakeContentView];
	self.view = self->backgroundView;
	
}
#pragma mark various footer button pushes end up vectoring thru here
-(void) recordVisited:(InstanceInfo *)ii
{
	ii.lastVisited = [NSDate date];
	TuneInfo *ti = [TunesManager findTune:ii.title];
	ti.lastArchive = ii.archive;
	ti.lastFilePath = ii.filePath;
	
	[[GigStandAppDelegate sharedInstance ]saveContext];
}

-(void) radioControlVirtuallyPushed:(InstanceInfo *) ii;
{
	[self recordVisited: ii]; // say we got there

	if (self->variantChooser) 
	{	
		
		// take this off the screen
		//[self->variantChooser removeFromSuperview];
		[self->variantChooser release];
		NSLog(@"variantChooser release radioControlVirtuallyPushed");
		
	}
	self->variantItems= [[TunesManager allVariantsFromTitle:ii.title] retain];
	
	self->variantChooserText = [NSString stringWithFormat:@"%@:%@",
								[ArchivesManager shortName:ii.archive],
								[ii.filePath pathExtension]];
	
	self->currentVariantPosition= 0;
	
	for (InstanceInfo *ii2 in self->variantItems)
		if ([ii2.archive isEqualToString:ii.archive] 
			&& [ii2.filePath isEqualToString:ii.filePath]) 
			break;
	else 
		self->currentVariantPosition++;
	
	//NSLog(@"variantChooser alloc radioControlVirtuallyPushed %@",self->variantChooserText);
	
	[self setupFooterToolbarItems:self->variantChooserText label2:[self->variantItems count]] ;	
	
	NSString *filename = [NSString stringWithFormat:@"%@/%@/%@",
														[DataStore pathForSharedDocuments],ii.archive,ii.filePath];
	NSString *ext = [filename pathExtension];
	
	self->contentIsOpaque = [DataManager isOpaque:filename];
	[self setupRightSideNavItems];
	
	BOOL toplay = ([DataManager isFileMusic:ext]||[DataManager isFileVideo:ext]);
	if (toplay)
	{			
		NSURL    *url = [NSURL fileURLWithPath: filename isDirectory: NO];				
		mediaViewController = [[MediaViewController alloc] initWithURL: url andWithTune:ii.title] ; // crashes with autorelease
		[self.navigationController pushViewController:mediaViewController  animated:YES];
	}
	else {	
		// not music
		
		if ((self->contentIsOpaque == NO) 
			)
		{
			// if its any of these, look for a special header in the archiveheaders
			// if no header data then apply defaults
			
			
			NSString *headerdata = [ArchivesManager headerdataFromArchive:ii.archive type:ext];
			
			if (!headerdata)
			{
				float scale=1.1f;
				
				if ([ext isEqualToString:@"htm"])
				{
					scale = 1.3f;
				}
				if ( [ext isEqualToString:@"txt"])
				{
					scale = 1.5f;
				}
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
			// Now build the full deal
			NSString *htmlstring;
		
			{
				
				NSString *bodydata = [NSString stringWithContentsOfFile:filename usedEncoding:&encoding error: &error];
				htmlstring= [NSString stringWithFormat:@"<html>%@<body>%@%@%@</body></html>",headerdata,prebegin,bodydata,preend];
			}
			
			[self refreshWithHTML: htmlstring];
			return;
		}
		else
		{
			// Here for all those tunes that are totally opaque
			NSURL    *docURL = [NSURL fileURLWithPath: filename isDirectory: NO];
			[self refreshWithURL: docURL];
			return;
		}
	}	
	NSLog (@"radioControlVirtuallyPushed no match %@",ii);
}
#pragma mark Set List Support
-(NSString *) goBackOnList
{	
	self->currentListPosition--;
	if (self->currentListPosition<0) self->currentListPosition= [self->listItems count]-1;
	NSString *t = [self->listItems objectAtIndex:self->currentListPosition];
	return t;
}

-(NSString *) goForwardOnList
{
	self->currentListPosition++;
	if (abs(self->currentListPosition)>=[self->listItems count]) self->currentListPosition= 0;
	NSString *t = [self->listItems objectAtIndex:self->currentListPosition];
	return t;
}
-(void) rewindTune
{	
	NSString *ti = [self goBackOnList];
	[self displayTune:ti filePath:nil archive:nil];
}
-(void) fastforwardTune
{
	NSString *ti = [self goForwardOnList];
	[self displayTune:ti filePath:nil archive:nil];
}

-(NSString *) newTuneTitle;
{
	NSString *rn = [[NSString alloc] initWithString :self->currentTuneTitle ] ;
	return rn;
	
}

-(void) nextVariant
{
	NSUInteger c = [self->variantItems count];
	self->currentVariantPosition++;
	if (self->currentVariantPosition>=c) 
		self->currentVariantPosition= 0;
	InstanceInfo *ii = [self->variantItems objectAtIndex:self->currentVariantPosition];

	[self displayTune:ii.title filePath:ii.filePath archive:ii.archive];
	
}
	

#pragma mark setup the whole page

-(void) resizeContentFrame;
{
	// Content Window (frame only) is the whole screen minus the toolbar at the bottom
	// Content view:
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	CGFloat h = [self toolbarHeight];
	tmpFrame.size.height -= h;
	
	CGFloat edgeInset = 0.0f;
	
	//
	// If desired, inset content view frame a bit to help in debugging:
	//
	if (edgeInset > 0.0f)
		tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
										  UIEdgeInsetsMake (edgeInset,
															edgeInset,
															edgeInset,
															edgeInset));
	
	self->contentFrame = tmpFrame; // save this for later
}


- (void) scrollToSelectedRange
{
	
}
-(void) setupFooterToolbarItems:(NSString *)theLabel label2:(NSUInteger) theLabel2;
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
	fixedSpace.width = 25.0f;
	
	UIBarButtonItem *segCtlBBI1 = [[[UIBarButtonItem alloc] initWithTitle:[NSString stringWithFormat:@"%1d",theLabel2]
																	style:(UIBarButtonItemStyle)UIBarButtonItemStylePlain 
																  target:self action:@selector(nextVariant)] autorelease];
	
	UIBarButtonItem *segCtlBBI2 = [[[UIBarButtonItem alloc] initWithTitle:theLabel style:(UIBarButtonItemStyle)UIBarButtonItemStylePlain 
																  target:self action:@selector(nextVariant)] autorelease];
	segCtlBBI2.width = 160.f;

	
	UIBarButtonItem *barbutton1 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd 
									target:self action:@selector(presentModalSetListChooser)] autorelease];
	
	UIBarButtonItem *barbutton2 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRewind 
									target:self action:@selector(rewindTune)] autorelease];
	
	UIBarButtonItem *barbutton3 = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFastForward 
									target:self action:@selector(fastforwardTune)] autorelease];
	
	
	self->footerToolbar.items = [NSArray arrayWithObjects:
								 //  flexSpace,
								 segCtlBBI1,flexSpace,segCtlBBI2,flexSpace,barbutton1,fixedSpace, barbutton2,fixedSpace,barbutton3,
								 //   barbutton4,
								 nil];
}


-(void) setupRightSideNavItems;
{
	NSString *cancel =  (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"Cancel":nil; // if on the iPhone
	
	if (self->canPlay==NO)
		self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Tune Options", @"")
												  delegate:self
										 cancelButtonTitle:cancel
									destructiveButtonTitle:nil
										 otherButtonTitles:
					   NSLocalizedString (@"Print", @""),
					   NSLocalizedString (@"Email", @""),
					   NSLocalizedString (@"Info", @""),
					   nil];
	
	
	else 
		self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Tune Options", @"")
												  delegate:self
										 cancelButtonTitle:cancel
									destructiveButtonTitle:nil
										 otherButtonTitles:
					   NSLocalizedString (@"Print", @""),
					   NSLocalizedString (@"Email",@""),
					   NSLocalizedString (@"Info", @""),
					   NSLocalizedString (@"Play", @""), 
					   nil];
	
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	// create a standard action
	UIBarButtonItem *saveButton = [[[UIBarButtonItem alloc] 
									initWithBarButtonSystemItem: ((!self->canPlay)? UIBarButtonSystemItemAction:UIBarButtonSystemItemPlay)
									target:self 
									action:@selector(upperRightButtonPressed)] autorelease];
	saveButton.style = UIBarButtonItemStyleBordered;
	
	self.navigationItem.rightBarButtonItem =saveButton;
}


- (CGFloat) toolbarHeight;
{
	UIDevice *device = [UIDevice currentDevice];
	
	//
	// If device is in landscape orientation AND device is iPhone, use narrow
	// toolbar height:
	//
	if (self->fullScreen == YES) return 0.f;
	
	if (UIDeviceOrientationIsLandscape (device.orientation) &&
		(![device respondsToSelector: @selector (userInterfaceIdiom)] ||
		 (device.userInterfaceIdiom == UIUserInterfaceIdiomPhone)))
		return NARROW_TOOLBAR_HEIGHT;
	
	return NORMAL_TOOLBAR_HEIGHT;
}

#pragma mark  tune management

-(void) displayTune: (NSString *)titlex filePath :(NSString * )filePath archive:(NSString *)archive;

{
	// put the new tune on display
	//
	// the title will always be good, if we can find an exact match for the 'path' in variants, then show that

	[TunesManager setLastTitle:titlex];
	[[BonJourManager sharedInstance ] publishTXTFromLastTitle];
	
	self->currentTuneTitle = titlex;
	if (filePath == nil)
	{
		// if coming from nowhere, use the last from the tune record
		
		TuneInfo *ti = [TunesManager findTune:titlex];
		
		self->archivePathForFile = [DataManager newLongPath:ti.lastFilePath forArchive:ti.lastArchive];
		
	}
	else self->archivePathForFile = [DataManager newLongPath:filePath forArchive:archive];
	NSLog (@"OTV %@ - %@",	titlex,self->archivePathForFile);// self->currentArchiveIndex,  self->currentListPosition);
	
	[self resizeContentFrame]; //adjust based on current bound		
	self->canPlay = [DataManager canPlayTune:self->currentTuneTitle];
	/// 
	
	// add to recents
	
	
	[SetListsManager updateRecents: self->currentTuneTitle];
	
	// find the best possible index here 
	
	
	NSArray *parts = [self->archivePathForFile componentsSeparatedByString:@"/"]; // wierd but true
	NSString *parchive = [parts objectAtIndex:0];
	NSString *pfilepath = [parts objectAtIndex:1];
	TuneInfo *tn = [TunesManager tuneInfo: titlex];
	
	for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title])
	{
		
		if ((pfilepath==nil)|| ([pfilepath isEqualToString:ii.filePath]&&[parchive isEqualToString:ii.archive] )) // if passed a nil to begin with then take anything
		{
			self.navigationItem.title = titlex;
			[self radioControlVirtuallyPushed:ii]; // go there now
			return;
		}
	}
	
	NSLog (@"displayTune Cant find variant match  for %@ ",titlex);
	
	
}


-(void) setupTuneView
{
	//
	// Background view:
	//
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	
	self->backgroundView = [[UIView alloc] initWithFrame: tmpFrame];
	
	self->backgroundView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
											 UIViewAutoresizingFlexibleWidth);
	self->backgroundView.backgroundColor = [UIColor lightGrayColor];
	
	[self resizeContentFrame]; // this may actually stay
	
	if (self->fullScreen==NO)
	{
		
		// Stuff at the bottom
		
		//
		// Footer view frame uses coordinates relative to its containing view
		// (i.e., background view):
		//
		
		tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
		tmpFrame.origin.y = (tmpFrame.size.height -  [self toolbarHeight]);
		tmpFrame.size.height = [self toolbarHeight];
		
		//
		// Create footer view:
		//
		self->footerView = [[UIView alloc] initWithFrame: tmpFrame];
		self->footerView.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
											 UIViewAutoresizingFlexibleWidth);
		self->footerView.backgroundColor = [UIColor clearColor];
		
		tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
		tmpFrame.origin.y = (tmpFrame.size.height -  [self toolbarHeight]);
		tmpFrame.size.height = [self toolbarHeight];
		
		
		self->footerToolbar = [[UIToolbar alloc] init];		
		self->footerToolbar.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
												UIViewAutoresizingFlexibleWidth);
		self->footerToolbar.barStyle = UIBarStyleBlack;
		self->footerToolbar.translucent = YES;
		
		// go back to the last place we remember
		TuneInfo *tn = [TunesManager findTuneInfo:self->currentTuneTitle];
		self->variantItems = [[TunesManager allVariantsFromTitle:tn.title] retain];
		NSUInteger pos = 0;
		for (InstanceInfo *ii in self->variantItems)
		{
			if ([tn.lastArchive isEqualToString:ii.archive] && 
				[tn.lastFilePath isEqualToString:ii.filePath])
		
			{
				self->variantChooserText = [NSString stringWithFormat:@"%@:%@",
											[ArchivesManager shortName:ii.archive],
											[ii.filePath pathExtension]];				
				self->currentVariantPosition = pos;
				NSLog(@"variantChooser setupTuneView radioControlVirtuallyPushed %@",self->variantChooserText);
	
				break;
			}
			pos++;
		}
		
		self->footerToolbar.frame = tmpFrame;
		[self setupFooterToolbarItems:self->variantChooserText label2:[self->variantItems count]]; // this adds variant Chooser in 
		
		// add this all into the background in precisely this order otherwise the butons are dead!
		
		[self->backgroundView addSubview:self->footerView];
		[self->backgroundView addSubview:self->footerToolbar];
		
	}
	
	// stuff at the top
	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem:UIBarButtonSystemItemDone 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	[self setupRightSideNavItems];
	
}


#pragma mark paint screen with one tune 


-(CGRect) frameForMainView
{
	CGRect tmpFrame = self->contentFrame;
	
	// on the iphone, the landscape orientations have a thinner upper bar
	float fudge = [DataManager navBarHeight];
	if (self->fullScreen) 
	{
		fudge = 0.f;
	}
	
	
	if (NO==self->first)
	{
		tmpFrame.size.height-= (fudge+[DataManager statusBarHeight]);  // why oh why????
		tmpFrame.origin.y+=fudge;
	} else 
	{
		tmpFrame.size.height -=fudge;
		tmpFrame.origin.y+=fudge;
	}
	return tmpFrame;
	
}

//-(CGRect) frameForTVScreen;-(void)setupTVScreen:(UIView *) viewx;

-(void) remakeContentView;
{	
	if (self->contentView) {
		[self->contentView removeFromSuperview];
		[self->contentView release];
	}
	
	[self resizeContentFrame];
	self->contentView = [[UIWebView alloc] initWithFrame: [self frameForMainView]];
	
	self->contentView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
										  UIViewAutoresizingFlexibleWidth);
	self->contentView.backgroundColor = [UIColor blackColor];
	self->contentView.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
	
	self->contentView.scalesPageToFit = YES;
	
	UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self action: @selector (toggleFullScreen:)] autorelease];
	
	tgr.numberOfTapsRequired = 3;
	
	[self->contentView addGestureRecognizer: tgr];
	
	[self->backgroundView addSubview: self->contentView];
	
	
	//
	// Kick off  load of content view:
	
	if 
		(self->contentIsOpaque ==YES)
		//
	{
		self->contentView.delegate =  self ; 
		[self->contentView loadRequest: [NSURLRequest requestWithURL: self->contentURL]];
	}
	
	else 
	{	self->contentView.delegate =  nil ; //was just self XYZZY
		[self->contentView loadHTMLString:self->html baseURL:NULL ];
	}
}


- (void) refreshWithHTML : (NSString *) htmlstring;
{
	self->contentIsOpaque = NO;
	NSString *nhtml = [htmlstring copy];
	if (self->html) [self->html release];
	self->html = nhtml;
	[self resizeContentFrame];	
	[self remakeContentView];
	
}

- (void) refreshWithURL : (NSURL *) URL ;//andWithShortPath: (NSString *) shortpath;
{	
	self->contentIsOpaque = YES;
	NSURL *nurl = [URL copy];
	if (self->contentURL)
		[self->contentURL release];
	self->contentURL = nurl; 
	[self resizeContentFrame];	
	[self remakeContentView];
	
	//NSLog (@"refreshWithURL %d title %@ archive %d  lispos %d", self->fullScreen, self->currentTuneTitle, self->currentArchiveIndex,  self->currentListPosition);
}

#pragma mark Private Instance Methods

- (void) startTrackingLoad
{
    self->depth++;
	
    [self->activityIndicator startAnimating];
	
    self->startTime = [NSDate timeIntervalSinceReferenceDate];
}

- (NSTimeInterval) stopTrackingLoad
{
    NSTimeInterval stopTime = [NSDate timeIntervalSinceReferenceDate];
	
	//   [self.appDelegate didStopNetworkActivity];
	
    [self->activityIndicator stopAnimating];
	
    if (self->depth > 0)
        self->depth--;
	
    return stopTime - self->startTime;
}
#pragma mark Overridden UIViewController Methods


- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{	
	// everylast thing was set up in init	
	[self remakeContentView];
	self.view = self->backgroundView;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
	return YES;
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
	[super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}



- (void)dealloc {
	NSLog (@"OTV dealloc releases variantChooser");
	[self stopTrackingLoad];
	//[self->currentTuneTitle release];
	[self->mediaViewController release];
	[self->toass dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->toass release];//[self->contentURL_];
	[self->footerToolbar release];
	[self->footerView release];
	[self->variantChooser release];
	[self->setListChooserControl release];
	[self->variantItems release];
	
	
	self->contentView.delegate = nil;    // Apple docs say this is required
	
	[self->contentURL release];
	[self->currentTuneTitle release];
	[self->contentView release];//loadview
	[self->backgroundView release]; //init
	
	[super dealloc];
}

- (void) loadView
{
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
    self.navigationController.navigationBarHidden = self->fullScreen; 	
	
	
	if (self->first)	[self setupTuneView]; // even if we get to loadView again before init, we only call setupTuneView once
	
	NSArray *parts = [self->archivePathForFile componentsSeparatedByString:@"/"];
	NSString *parchive = [parts objectAtIndex:0];
	NSString *pfilepath = [parts objectAtIndex:1];
	
	[self displayTune : self->currentTuneTitle filePath: pfilepath archive:parchive]; // shud get last indexXXXXXXX

	self.view = self->backgroundView;
	self->first = NO;
	
}


-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex 
						  andWithItems:(NSArray *)items andPath: (NSString *) pathx;
{
	
	
	self = [super init];
	
	if (self)
	{
		self->contentURL = [URL copy ];
		self->depth = 0;
		self->sharedDocument = NO; 
		self->currentTuneTitle = [ titlex copy]; 
		self->first = YES;
		self->canPlay = NO;
		self->listItems = [items retain]; //important don't release
		self->fullScreen = NO; // for now
		self->contentIsOpaque = YES; //for now
		self->archivePathForFile = [pathx copy];	
	}
	return self;
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
			   self->depth);
	
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
		
		[self->contentView loadHTMLString: errorString
								  baseURL: nil];
	}
}

- (void) webViewDidFinishLoad: (UIWebView *) webView
{
	// put this outon the tv
	CGRect frame = [[DataManager sharedInstance] frameForTVScreen];
	//	
	//	UITextView *tv = [[[UITextView alloc] initWithFrame: frame] autorelease];
	//	tv.text = s;
	
	UIWebView *tv = [[[UIWebView alloc] initWithFrame:frame] autorelease];
	tv.delegate = nil;
	[tv loadRequest: [NSURLRequest requestWithURL: webView.request.URL]];
	[[DataManager sharedInstance] setupTVScreen: tv ];
	
}

- (void) webViewDidStartLoad: (UIWebView *) webView
{
//	NSLog (@"Loading <%@>, depth: %d",
//		   webView.request.URL,
//		   self->depth);
	
	[self startTrackingLoad];
}

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navType
{    return YES;
}


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
-(void) tuneInfo: (id) sender

{
	TuneInfoController *aModalViewController = [[[TuneInfoController alloc] initWithTune:self->currentTuneTitle] autorelease];	
	
	aModalViewController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal
	;
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
	//	
	[self presentModalViewController:nav animated: YES];
}

#pragma mark MFMailComposeViewController support
- (void)mailComposeController:(MFMailComposeViewController*)controllerx 
          didFinishWithResult:(MFMailComposeResult)result 
                        error:(NSError*)error;
{
	if (result == MFMailComposeResultSent) {
		NSLog(@"OTV MFMailComposeResultSent");
	}
	[self dismissModalViewControllerAnimated:YES];
	[controller release];
}
-(NSString *) emailAttachmentPathForCurrentTune /////////// ************* 
{
	
	TuneInfo *tn = [TunesManager findTuneInfo:self->currentTuneTitle];
	if (tn) 
	{
		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title])
			
		{
			
			NSString *filename = [NSString stringWithFormat:@"%@/%@/%@",[DataStore pathForSharedDocuments],ii.archive,ii.filePath];
			
			return filename;
			
			
		}
	}
	return @"/not found";
}

-(NSString *) emailAttachmentNameForCurrentTune; ///////NEEDS UPGRADE _ ALWAYS USES FIRST//// ************* 
{
	
	TuneInfo *tn = [TunesManager findTuneInfo:self->currentTuneTitle];
	if (tn) 
	{
		for (InstanceInfo *ii in [TunesManager allVariantsFromTitle:tn.title])
			
		{	
			NSString *ext = [ii.filePath pathExtension];
			NSString *mypersonalname = [[UIDevice currentDevice] name];
			return [NSString stringWithFormat:@"%@-%@.%@",self->currentTuneTitle,mypersonalname,ext]; 
			
		}
	}
	
	return @"/not found";
}
-(void) emailTune: (id) sender
{
	//	UIImage *img = [self imageFromView:self.view];
	
	controller = [[MFMailComposeViewController alloc] init];
	
	controller.mailComposeDelegate = self;
	[controller setSubject:[NSString stringWithFormat:@"Sending Tune -  %@",self->currentTuneTitle]];
	
	//NSError *error;
	//NSStringEncoding encoding
	NSString *filespec = [self emailAttachmentPathForCurrentTune];
	NSString *attachmentname = [self emailAttachmentNameForCurrentTune];
	
	//NSString *filespec = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],self->archivePathForFile];
	BOOL pdf = [DataManager isPDF:filespec];
	
	if (pdf == NO)
	{
		
		[controller setMessageBody:[NSString stringWithFormat:@"<html><head><title>%@</title></head><body>%@</body></html>",self->currentTuneTitle,
									[NSString stringWithFormat: @"<footer>this email was built by %@ %@<br/>visit http://www.gigstand.net for more information</footer>",
									 [DataManager sharedInstance].applicationName,
									 [DataManager sharedInstance].applicationVersion]
									] 
							isHTML:YES]; 
		[controller addAttachmentData:[NSData dataWithContentsOfFile:filespec] mimeType:@"text/html" fileName:attachmentname];
	}
	else {
		// PDF
		
		[controller setMessageBody:[NSString stringWithFormat:@"<html><head><title>%@</title></head><body>Please Share this GigStand Tune With Me<hr/>%@</body></html>",
									self->currentTuneTitle,									
									[NSString stringWithFormat: @"<footer>this email was built by %@ %@<br/>visit http://www.gigstand.net for more information</footer>",
									 [DataManager sharedInstance].applicationName,
									 [DataManager sharedInstance].applicationVersion]
									] 
							isHTML:YES]; 
		[controller addAttachmentData:[NSData dataWithContentsOfFile:filespec] mimeType:@"application/pdf" 
							 fileName:attachmentname];
	}
	
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
    void (^completionAction)(UIPrintInteractionController *, BOOL, NSError *) =
	^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
        if(!completed && error){
            NSLog(@"FAILED! due to error in domain %@ with error code %u",
				  error.domain, error.code);
        }
    };
    UIPrintInfo *printInfo = [UIPrintInfo printInfo];
    printInfo.outputType = UIPrintInfoOutputGeneral;
    printInfo.jobName = self->currentTuneTitle;
    printInfo.duplex = UIPrintInfoDuplexLongEdge;
    pcontroller.printInfo = printInfo;
    pcontroller.showsPageRange = YES;
	
    UIViewPrintFormatter *viewFormatter = [self->contentView viewPrintFormatter];
    viewFormatter.startPage = 0;
    pcontroller.printFormatter = viewFormatter;
	
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
		[pcontroller presentFromBarButtonItem:self.navigationItem.rightBarButtonItem animated:YES completionHandler:completionAction];
	}
	else
		[pcontroller presentAnimated:YES completionHandler:completionAction];
}




#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//NSLog (@"OTV tag is %d button is %d",actionSheet.tag, buttonIndex);
	
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
		else 
			if (buttonIndex==2)
				
			{
				[self tuneInfo:actionSheet];            
			}
			else 
				
				if (buttonIndex==3)
					
				{
					[DataManager playMusic:self->currentTuneTitle];            
				}
		
	}
	else 
		if (actionSheet.tag == 2) // this is invoked from the big plus sign
		{
			{
				if (buttonIndex >=  0)
					[ self presentModalSetListChooser ];
			}
		}
}
@end
