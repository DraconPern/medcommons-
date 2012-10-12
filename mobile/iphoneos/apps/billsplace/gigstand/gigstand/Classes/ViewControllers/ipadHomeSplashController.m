//
//  ipadHomeSplashController.m
//  gigstand
//
//  Created by bill donner on 4/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//
#import "SettingsManager.h"
#import "SettingsViewController.h"
#import "MenuViewController.h"
#import "ipadHomeSplashController.h"
#import "ModalAlert.h"
#import "DataManager.h"
#import "TunesManager.h"
#import "SplashViewController.h"
#import "GigStandAppDelegate.h"
#import "SnapshotInfo.h"
#import "TuneInfo.h"

// DOES NOT LEAK ON ROTATE :=)
// LEAKS A TINY BIT ON SWIPE

//
// puts up simple local web page to explain collaboration and to accept payment


@implementation ipadHomeSplashController 

-(void) popOff;
{
    [popoverController dismissPopoverAnimated:YES]; 
    //  [popoverController release];
    // popoverController = nil; // prevent troubles
}

-(void) pushToController :(UIViewController *) uivc // called from menuviewcontroller
{
    
    if (popoverController) 
    {
        [popoverController dismissPopoverAnimated:YES]; 
        [popoverController release];
        popoverController = nil; // prevent troubles
    }
    
    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: uivc] autorelease];
    [self.parentViewController presentModalViewController: nav animated:YES];
}
- (void) pushToSettings
{
	
	SettingsViewController *aModalViewController = [[[SettingsViewController alloc] init] autorelease];	// was autorelease
    if (popoverController) 
    {
        
        [popoverController dismissPopoverAnimated:YES]; 
        [popoverController release];
        popoverController = nil; // prevent troubles
    }
    
    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
    popoverController = [[UIPopoverController alloc] initWithContentViewController: nav] ;
    
    popoverController.popoverContentSize = CGSizeMake(320.f,480.f); // same as iphone window
    popoverController.delegate = self;
    nav.navigationItem.title = @"setup";
    
    [popoverController presentPopoverFromBarButtonItem:self.navigationItem.rightBarButtonItem 
                              permittedArrowDirections: UIPopoverArrowDirectionAny
                                              animated: YES];
}

- (void) pushToMenu
{
	
	MenuViewController *aModalViewController = [[[MenuViewController alloc] initWithHomeController:self mode:0] autorelease];	// was autorelease
    
    if (popoverController) 
    {
        
        [popoverController dismissPopoverAnimated:YES]; 
        [popoverController release];
        
        // popoverController = nil; // prevent troubles        
        
    }
    
    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: aModalViewController] autorelease];
    popoverController = [[UIPopoverController alloc] initWithContentViewController: nav] ;
    
    popoverController.popoverContentSize = CGSizeMake(320.f,600.f); // not same as iphone window
    popoverController.delegate = self;
    
    [popoverController presentPopoverFromBarButtonItem:self.navigationItem.leftBarButtonItem 
                              permittedArrowDirections: UIPopoverArrowDirectionAny
                                              animated: YES];
	
}

- (BOOL) askAboutSamples
{
	BOOL beenTouched = [[NSUserDefaults standardUserDefaults] boolForKey:@"BeenTouched"];
	[[NSUserDefaults standardUserDefaults] setBool: YES forKey:@"BeenTouched"]; // now fix this and then synch
	[[NSUserDefaults standardUserDefaults] synchronize];
	if (beenTouched == NO)
	{
		BOOL answer = [ModalAlert ask:@"It looks like your first time in GigStand. Please be sure to only load content that is of your own creation or in the public domain. Would you like to load some public domain samples?"];
		if (answer == YES)
		{
            [self popOff];
			// settings view controller will do all the heavy lifting
			SettingsViewController *aModalViewController = [[[SettingsViewController alloc] initWithAutoStart:YES] autorelease];	// was autorelease
			[self.navigationController pushViewController:aModalViewController animated:YES];
			return YES; // dont want to check inbox
			
		}
		
	}
	return NO;
}
-(void) processInboxViaPushController
{
	// settings view controller will do all the heavy lifting
	SettingsViewController *aModalViewController = [[[SettingsViewController alloc] initWithAutoStart:NO] autorelease];	// was autorelease
	[self.navigationController pushViewController:aModalViewController animated:YES];
}
// do something modal here based on inbox
-(void) checkInbox {
	
    if (checkTimer == nil) return;
	
    
    BOOL didLoadSamples  = [self askAboutSamples]; // can this come back
	
	if (!didLoadSamples)
	{
		NSUInteger zipcount=[DataManager incomingInboxDocsCount];
		if ((zipcount!=0))
		{
			BOOL answer = [ModalAlert ask:@"You have unprocessed items in your iTunes inbox. Would you like to process them now? Please be patient as it can take a while"];
			if (answer == YES)
			{
				[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(processInboxViaPushController) userInfo:nil repeats:NO];
			}
		}
	}
}


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

-(void) viewDidLoad
{
    NSLog(@"IPH view did load");
    [super viewDidLoad];
    
    
}



-(void)loadView
{
    NSLog (@"IPH loadview");
    
}




- (id) init

{
    
    
    self = [super init];
    
    if (self)
    {
        NSArray *infoarray = [[NSBundle mainBundle] loadNibNamed:@"splashprojViewController" owner:self options:nil] ;
        
        // for (id info in infoarray) NSLog (@"%@",info);
        
        landscapeSnapshotView =  [[infoarray objectAtIndex:3] retain];
        portraitSnapshotView = [[infoarray objectAtIndex:2] retain];
        landscapeView =  [[infoarray objectAtIndex:1] retain];
        portraitView = [[infoarray objectAtIndex:0] retain];
        
        NSAssert((portraitView.tag==101),@"landscapeView must have tage 101 from IB");
        NSAssert((landscapeView.tag==102),@"landscapeView must have tage 102 from IB");
        NSAssert((portraitSnapshotView.tag==103),@"landscapeView must have tage 103 from IB");
        NSAssert((landscapeSnapshotView.tag==104),@"landscapeView must have tage 104 from IB");
        
        landscapeView.autoresizesSubviews = NO;
        landscapeSnapshotView.autoresizesSubviews = NO;
        portraitView.autoresizesSubviews = NO;
        portraitSnapshotView.autoresizesSubviews = NO;
        
        first = YES;
    }
    return self;
    
}

- (void) checkBox: (NSTimer *) timer;
{
    
	[self checkInbox]; // do this unconditionally whilst monitoring inbox
	
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.



-(void) invalidateTimer
{
	if (aTimer) 
	{
		[aTimer invalidate];
		//[aTimer release];
		aTimer = nil;
	}
}
-(void) viewWillDisappear:(BOOL)animated
{
    
    NSLog(@"IPH view will disappear");
    [super viewWillDisappear:animated];
    //[self invalidateTimer];
    checkTimer = nil;
}

//////

- (void) viewDidUnload

{
    NSLog(@"IPH view did unload");
	[self invalidateTimer];
    [super viewDidUnload];
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}




#pragma mark Public Instance Methods
//	logoView_.center = CGPointMake (self.view.frame.size.width / 2.0f,
//									self.view.frame.size.height /2.0f);

-(void) pushToTune
{
    NSUInteger page =     self->pageControl.currentPage;
    
    NSArray *snapshots = [TunesManager allSnapshotInfos];  // pretty heavyweight, could just go for particular , bleh....
    
    NSUInteger count = [snapshots count];
    
    NSMutableArray *listitems = [NSMutableArray arrayWithCapacity:[snapshots count]];  //might be leaking, don't release because OTV needs in place
    
    for (SnapshotInfo *si  in snapshots)
        [listitems addObject:si.title];
    
    
    SnapshotInfo *ssi = [snapshots objectAtIndex:count-1-page];
    
    //NSLog (@"will call OTV %@",ssi.title );
    
    TuneInfo *ti = [TunesManager findTune:ssi.title];
    
    NSString *path = [DataManager deriveLongPath:ti.lastFilePath forArchive:ti.lastArchive];
    
    
    UIViewController   *otv  =  [DataManager makeOneTuneViewController:path 
                                                                 title:ssi.title items:listitems]; // without autorelease will leak now
    UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: otv] autorelease];
    
    [self.parentViewController presentModalViewController:nav animated: YES];
    
    
    
    //  [self.navigationController pushViewController:otv animated: YES];
    
    
}

-(UIView *) snapshotsLandscapeView

{
    //  [self->pageControl removeFromSuperview];
    
    [self->scrollView release];
    [self->scrollView removeFromSuperview];
    
    CGRect aframe = CGRectMake(0,0,538.f,642.f);
    self->scrollView = [[UIScrollView alloc] initWithFrame:aframe];
    self->scrollView.contentSize = CGSizeMake([TunesManager snapshotCount] * 538.f,642.f);
    self->scrollView.pagingEnabled = YES;
    self->scrollView.delegate = self;
    
    
    NSArray *snapshots = [TunesManager allSnapshotInfos];
    NSUInteger count = [snapshots count];
    
    for (NSUInteger i = 0; i < count; i++)
    {
        SnapshotInfo *ssi = [snapshots objectAtIndex:count-1-i];
        
        UIImage *img = [UIImage imageWithContentsOfFile:ssi.filePath];
        UIImageView *iv =[[UIImageView alloc] initWithImage:img];
        iv.frame = CGRectMake(i*538.f, 0,538.f,642.f);
        [self->scrollView addSubview:iv];
        [iv release];
    }
    scrollView.center = CGPointMake (1024.f/ 2.0f,
                                     704.f /2.0f + 26.f);
    
    UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self action: @selector (pushToTune)] autorelease];
	tgr.numberOfTapsRequired = 2;
	[self->scrollView addGestureRecognizer: tgr];
    [self->landscapeSnapshotView addSubview:scrollView];
    
    self->pageControl = (UIPageControl *) [self->landscapeSnapshotView viewWithTag:202];
    self->pageControl.numberOfPages = count;
    //    
    //    CGPoint offset = self->scrollView.contentOffset;
    //    self->pageControl.currentPage = offset.x / 538.f;
    
    self->pageControl.currentPage = 0;
    self->pageControl.autoresizesSubviews = NO;
    self->pageControl.autoresizingMask = 0;
    
    return self->landscapeSnapshotView;
}

-(UIView *) snapshotsPortraitView
{
    [self->scrollView removeFromSuperview];
    [self->scrollView release]; //>>????
    
    CGRect aframe = CGRectMake(0,0,538.f,642.f);
    self->scrollView = [[UIScrollView alloc] initWithFrame:aframe];
    self->scrollView.contentSize = CGSizeMake([TunesManager snapshotCount] * 538.f,642.f);
    self->scrollView.pagingEnabled = YES;
    self->scrollView.delegate = self;
    
    
    NSArray *snapshots = [TunesManager allSnapshotInfos];
    NSUInteger count = [snapshots count];
    
    for (NSUInteger i = 0; i < count; i++)
    {
        SnapshotInfo *ssi = [snapshots objectAtIndex:count-1-i];
        
        UIImage *img = [UIImage imageWithContentsOfFile:ssi.filePath];
        UIImageView *iv =[[UIImageView alloc] initWithImage:img];
        iv.frame = CGRectMake(i*538.f, 0,538.f,642.f);
        [self->scrollView addSubview:iv];
        [iv release];
    }
    
    scrollView.center = CGPointMake (768.f / 2.0f,
                                     960.f /2.0f +8.f); //fudge to make it look nice
    
    
    UITapGestureRecognizer *tgr = [[[UITapGestureRecognizer alloc] initWithTarget: self action: @selector (pushToTune)] autorelease];
	tgr.numberOfTapsRequired = 2;
	[self->scrollView addGestureRecognizer: tgr];
    
    [self->portraitSnapshotView addSubview:self->scrollView];
    
    self->pageControl = (UIPageControl *) [self->portraitSnapshotView viewWithTag:201]; //find in the nib base view
    
    self->pageControl.numberOfPages = count;
    //    
    //    CGPoint offset = self->scrollView.contentOffset;
    //    self->pageControl.currentPage = offset.x / 538.f;
    self->pageControl.currentPage = 0;
    self->pageControl.autoresizesSubviews = NO;
    self->pageControl.autoresizingMask = 0;
    
    //    
    
    
    return self->portraitSnapshotView;
}
-(BOOL) snapshotsReady
{
    return ([TunesManager snapshotCount] > [[SettingsManager sharedInstance] galleryTrigger]);
}


-(void) loadStuff
{ 
    if ([self snapshotsReady])
    {
        if ((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)))
        {
            self.view = [self snapshotsLandscapeView];
        }
        else 
        {
            self.view = [self snapshotsPortraitView];
        }
    }
    else
    {
        if ((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)))
        {
            self.view = landscapeView;
        }
        else 
        {
            self.view = portraitView;
        }
        
    }
}

-(void) viewWillAppear:(BOOL)animated
{
    NSLog (@"IPH viewWillAppear");
    [super viewWillAppear:animated];
//    [self loadStuff];
//    aTimer = [NSTimer scheduledTimerWithTimeInterval:.001f target:self selector:@selector(checkBox:) userInfo:nil repeats:NO];
}



#pragma mark Overridden UIViewController Methods

- (void) loadViewX
{
    //    SEL la = @selector(donePressed:); // default in case no left action supplied
    //    id ta = self;
    //    if (self->target) ta = self->target;
    //    if (self->leftaction) la = self->leftaction;
    
    
    [ self setColorForNavBar];
    
    self.navigationItem.leftBarButtonItem =
    [[[UIBarButtonItem alloc] initWithTitle:@"Content" style:UIBarButtonItemStylePlain 
                                     target:self action:@selector (pushToMenu)] autorelease];
    
    self.navigationItem.rightBarButtonItem =
    [[[UIBarButtonItem alloc] initWithTitle:@"Settings" style:UIBarButtonItemStylePlain 
                                     target:self action:@selector (pushToSettings)] autorelease];
    
	self.navigationItem.titleView = [DataManager makeAppTitleView:@""] ;
    
    
    
    
    // puthis on the screen
    [self loadStuff];
}
- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
    
    NSLog(@"IPH viewDidAppear first %d",first);
    
    if (first) [self loadViewX];
    else [self loadStuff] ;// if we re-appear then go for it
    first = NO;
    
    if (checkTimer == nil)
        checkTimer = [NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(checkInbox) 
                                                    userInfo:nil repeats:NO];
}

#pragma mark Overridden NSObject Methods

-(void) dealloc
{
    
    [self->rightbuttonlabel release];
    [self->leftbuttonlabel release];
    [self->titlelabel release];
    [self->landscapeView release];
    [self->portraitView release];
    
    [self->landscapeSnapshotView release];
    [self->portraitSnapshotView release];
    [self->scrollView release];
    
    [self->pageControl release];
    [self->popoverController release];
    [super dealloc];
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{	
    // leaks
    
    [self loadStuff];
}

#pragma mark UIScrollViewDelegate
-(void) scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGPoint offset = self->scrollView.contentOffset;
    self->pageControl.currentPage = offset.x / 538.f;
}
@end
