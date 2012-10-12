//
//  DetailViewController.m
//  MedPad
//
//  Created by bill donner on 2/24/10.
//  Copyright MedCommons Inc 2010. All rights reserved.
//

#import "DetailViewController.h"
#import "RootViewController.h"
#import "AppDelegate.h"
#import "DataManager.h"
#import "SegmentMap.h"
#import "MCShooterController.h"


@interface DetailViewController ()
- (id)configureDetailViewFromSceneBlock;
@end

@implementation DetailViewController

@synthesize navigationBar, popoverController, detailItem;

-(UIButton *) addNativeButton
{
    CGRect buttonframe = CGRectMake (0.f,0.f,30.f,30.f);
    UIButton *cButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
    [cButton setTitle:@"iPhad" forState:UIControlStateNormal];
    [cButton setFrame:buttonframe];
    [cButton addTarget:self action:@selector(goNative) forControlEvents:UIControlEventTouchUpInside];
    return cButton;
}

-(void)toggleLeftPanel
{
    if (    fullScreenWebView ==NO)  // do nothing if not full screen
    {
        CGRect thisFrame = [self.view frame];
        if (thisFrame.size.width ==703.f)
        {
            thisFrame.origin.x  = 0.0f;
            thisFrame.size.width = 1024.;
            navigationBar.topItem.leftBarButtonItem = [DataManager sharedInstance].leftDetailReversedSM.segmentBarItem;
        }
        else
        {
            thisFrame.origin.x  = 321.0f;
            thisFrame.size.width = 703.;
            navigationBar.topItem.leftBarButtonItem = [DataManager sharedInstance].leftDetailSM.segmentBarItem;
        }

        self.view.frame = thisFrame;
    }
}

-(void)resetLeftPanel
{
    if (    fullScreenWebView ==NO)  // do nothing if not full screen
    {
        CGRect thisFrame = [self.view frame];
        if(!((self.interfaceOrientation == UIDeviceOrientationLandscapeLeft) ||
             (self.interfaceOrientation == UIDeviceOrientationLandscapeRight)))

        {
            thisFrame.origin.x  = 0.0f;
            thisFrame.size.width = 1024.;
            navigationBar.topItem.leftBarButtonItem = [DataManager sharedInstance].leftDetailReversedSM.segmentBarItem;
        }
        else
        {
            thisFrame.origin.x  = 321.0f;
            thisFrame.size.width = 703.;
            navigationBar.topItem.leftBarButtonItem = [DataManager sharedInstance].leftDetailSM.segmentBarItem;
        }

        self.view.frame = thisFrame;
    }
}

-(void) displayDetailWebView: (NSString *)urlpart  backcolor: (UIColor *)bc title: (NSString *)titl

{


    NSURL *url = [NSURL URLWithString: [urlpart stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]];
    //  NSLog(@"displayDetailWebView %@ %@ %@",urlpart,[url absoluteURL  ],titl);

    navigationBar.topItem.title = titl;

    CGRect thisFrame = [self.view bounds];
    thisFrame.size.height = thisFrame.size.height-44.0;
    thisFrame.origin.y  = thisFrame.origin.y + 44.0;
    thisFrame.size.width = 768.0f;
    // NSLog (@"webpane webframe %f %f %f %f %@" , thisFrame.origin.x,thisFrame.origin.y,thisFrame.size.width ,thisFrame.size.height, urlpart);
    //CGRect thisFrame = [self.view bounds];

    UIWebView *webView = [[UIWebView alloc] initWithFrame:thisFrame];
    webView.scalesPageToFit=YES;
    webView.backgroundColor =bc;
    webView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;
    webView.delegate = self;

    NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
    [webView loadRequest:requestObj];


    [self.view addSubview:webView];

    [webView release];
    fullScreenWebView = NO;
}

-(CGRect) webFrame
{


    float topdelta = 0.0f;

    if((self.interfaceOrientation == UIDeviceOrientationLandscapeLeft) || (self.interfaceOrientation == UIDeviceOrientationLandscapeRight)){

        return CGRectMake(0.f,topdelta,1024.f,768.f -topdelta);


    } else  if((self.interfaceOrientation == UIDeviceOrientationPortrait) || (self.interfaceOrientation == UIDeviceOrientationPortraitUpsideDown)){

        return CGRectMake(0.f,topdelta,768.f,1024.f -topdelta);

    }

    return  CGRectMake(0.f,0.f,0.f,0.f);
}
-(CGRect) fullFrame
{
    float topdelta = 0.0f;
    if((self.interfaceOrientation == UIDeviceOrientationLandscapeLeft) || (self.interfaceOrientation == UIDeviceOrientationLandscapeRight)){

        return CGRectMake(0.f,0,1024.f,768.f-topdelta);

    } else  if((self.interfaceOrientation == UIDeviceOrientationPortrait) || (self.interfaceOrientation == UIDeviceOrientationPortraitUpsideDown)){

        return CGRectMake(0.f,0.f,768.f,1024.f-topdelta);
    }

    return  CGRectMake(0.f,0.f,0.f,0.f);

}
-(void) displayFullScreenWebView: (NSString *)urlpart  backcolor: (UIColor *)bc title: (NSString *)titl

{



    NSURL *url = [NSURL URLWithString: [urlpart
                                        stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]];

    if (!fswebView)
    {
        // setup on first time thru
        fswebView = [[UIWebView alloc] initWithFrame:[self webFrame]];
        fswebView.scalesPageToFit=YES;
        fswebView.backgroundColor =bc;
        fswebView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;
        fswebView.delegate = self;

        [fswebView addSubview:[self addNativeButton]];
    }

    NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
    [fswebView loadRequest:requestObj];
    [self.view addSubview:fswebView];



    fullScreenWebView = YES;

    self.view.frame = [self fullFrame];
}
- (void)makeRemoteExecutionWindow
{
    // text window with 2 frames



    CGRect thisFrame = [self.view bounds];
    thisFrame.size.height = thisFrame.size.height-44.0;
    thisFrame.origin.y  = thisFrame.origin.y + 44.0;
    thisFrame.size.width = 768.0f;

    remoteExecutionView =[[UIView alloc] initWithFrame:thisFrame];
    remoteExecutionView.backgroundColor = [UIColor colorWithWhite: 0.96f
                                                            alpha: 1.0f];
    CGRect topTextLabelFrame = CGRectMake(10.f,10.f,680.f,18.f);
    CGRect topTextFrame = CGRectMake(10.f,30.f,680.f,180.f);

    CGRect bottomTextLabelFrame = CGRectMake(10.f,240.f,680.f,18.f);
    CGRect bottomTextFrame = CGRectMake(10.f,260.f,680.f,400.f);

    topTextLabel = [[UILabel alloc] initWithFrame:topTextLabelFrame];
    topTextLabel.text = @"Request to MedCommons:";
    textViewTop = [[UITextView alloc] initWithFrame:topTextFrame];
    textViewTop.text = @"top text not set";
    textViewTop.backgroundColor = [UIColor lightGrayColor];
    textViewTop.editable = NO;

    bottomTextLabel = [[UILabel alloc] initWithFrame:bottomTextLabelFrame];
    bottomTextLabel.text = @"Response from MedCommons:";
    textViewBottom = [[UITextView alloc] initWithFrame:bottomTextFrame];
    textViewBottom.text = @"bottom text not set";
    textViewBottom.editable = NO;
    textViewBottom.backgroundColor =
    [UIColor colorWithWhite: 0.92f
                      alpha: 1.0f];

    [remoteExecutionView addSubview:topTextLabel];
    [remoteExecutionView addSubview:textViewTop];
    [remoteExecutionView addSubview:bottomTextLabel];
    [remoteExecutionView addSubview:textViewBottom];

    [self.view addSubview:remoteExecutionView];
    viewInited = YES;
    fullScreenWebView = NO;

}
- (void)showTopDetail:(NSString *) s
{
    if (viewInited==NO) [self makeRemoteExecutionWindow];
    else [self.view bringSubviewToFront:remoteExecutionView];
    textViewTop.text =s; // set top part
}
- (void)showBottomDetail:(NSString *) s
{
    if (viewInited==NO) [self makeRemoteExecutionWindow];
    else [self.view bringSubviewToFront:remoteExecutionView];
    textViewBottom.text = s; //set bottom part
}



#pragma mark Managing the detail item

/*
 When setting the detail item, update the view and dismiss the popover controller if it's showing.
 */
- (void)setDetailItem:(id)newDetailItem {
    if (detailItem != newDetailItem) {

        [detailItem release];
        detailItem = [newDetailItem retain];
        [[NSUserDefaults standardUserDefaults] setValue:detailItem forKey:@"lastdetail"]; // bill 9 mar 10 - persist this
                                                                                          // Update the view.

        [UIApplication sharedApplication].networkActivityIndicatorVisible=YES;
        [self configureDetailViewFromSceneBlock];

        [UIApplication sharedApplication].networkActivityIndicatorVisible=NO;
    }

    if (popoverController != nil) {
        [popoverController dismissPopoverAnimated:YES];
    }

    fullScreenWebView = NO; // never get full screen this way
}





-(void) viewInSafari
{
    NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext];
    if (!scene) return;

    NSDictionary *block = [[DataManager sharedInstance] currentBlock: [detailItem intValue] forScene: scene];
    if(!block) return;

    if (![block objectForKey: @"URL"])
    {
        [[DataManager sharedInstance] dieFromMisconfiguration:@"Missing <url/> in block"];
        return;
    }

    NSString *urlpart = [[block objectForKey: @"URL"]
                         stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

    [[UIApplication sharedApplication] openURL:[[[NSURL alloc] initWithString:urlpart] autorelease]];
}


- (id)configureDetailViewFromSceneBlock {
    // update the detailed pane for the current scene as driven by 'detailItem' and presuming scene and blocks in place

    NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext];
    if (!scene) return nil;

    NSDictionary *block = [[DataManager sharedInstance] currentBlock: [detailItem intValue] forScene: scene];
    if(!block) return nil;

    if (![block objectForKey: @"URL"]) return [[DataManager sharedInstance] dieFromMisconfiguration:[NSString stringWithFormat:@"Missing <URL/> in scene %@ block %d",
                                                                                                     [scene objectForKey:@"name"],
                                                                                                     [detailItem intValue]]];
    NSString *urlpart = [[block objectForKey: @"URL"]
                         stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

    NSString *toptitlepart = [block objectForKey:@"Title"];


    if (!urlpart) return [[DataManager sharedInstance] dieFromMisconfiguration:[NSString stringWithFormat:@"Empty <URL/> in scene %@ block %d",
                                                                                [scene objectForKey:@"name"],
                                                                                [detailItem intValue]]];


    if (!toptitlepart)return [[DataManager sharedInstance] dieFromMisconfiguration:[NSString stringWithFormat:@"Missing <Title/> in scene %@ block %d",
                                                                                    [scene objectForKey:@"name"],
                                                                                    [detailItem intValue]]];
    ;


    [self displayDetailWebView: urlpart backcolor: [UIColor redColor] title:[NSString stringWithFormat:@"%@",toptitlepart]];
    return nil;
}
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200

// Create the iPad view controller
#pragma mark -
#pragma mark webt view support

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navigationType
{
    DataManager *dm = [DataManager sharedInstance];

    if ([[request.URL scheme] isEqual:@"x-medpad"])
    {
        //x-medpad:
        NSString *sceneno = @"missing scene";
        NSString *title = nil;
        int       itscene = -1;

        // NSLog (@"shouldStartLoadWithRequest %@ type %d",request,navigationType);
        //x-medpad:
        NSString *querystring = [request.URL query];
        NSArray *queryComponents = [querystring componentsSeparatedByString:@"&"];
        NSString *queryComponent;

        for (queryComponent in queryComponents)
        {
            NSArray *query = [queryComponent componentsSeparatedByString:@"="];

            if ([query count]==2)
            {
                NSString *key =[query objectAtIndex:0];
                NSString *value = [query objectAtIndex:1];

                NSLog (@"parsing %@ val: %@",key,value);

                if ([key isEqual:@"scene"])
                    sceneno = value;//[value copy];//itscene = [value intValue];
                else if ([key isEqual:@"title"])
                    title = value;//[value copy];

            }
        }

        itscene = [sceneno intValue];

        [dm setScene: itscene];

        if (title) // only poke this if we actually have a new title here
            [dm.rootController remotePoke: title];
    }

    return YES;
}

#pragma mark -
#pragma mark Split view support

- (void) splitViewController: (UISplitViewController *) svc
      willHideViewController: (UIViewController *) vc
           withBarButtonItem: (UIBarButtonItem *) bbi
        forPopoverController: (UIPopoverController *) pc
{
    NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext];

    if (!scene)
        return;

    if (![scene objectForKey: @"barbuttontitle"])
    {
        [[DataManager sharedInstance] dieFromMisconfiguration: @"No barbuttontitle in plist file"];

        return;
    }

    if (fullScreenWebView == NO)
    {
        bbi.title = [scene objectForKey: @"barbuttontitle"];

        [navigationBar.topItem setLeftBarButtonItem: bbi
                                           animated: YES];

        self.popoverController = pc;
    }
    else
    {
        self.popoverController = nil;

    }
}

// Called when the view is shown again in the split view, invalidating the button and popover controller.
- (void) splitViewController: (UISplitViewController *) svc
      willShowViewController: (UIViewController *) vc
   invalidatingBarButtonItem: (UIBarButtonItem *) bbi
{
    [navigationBar.topItem setLeftBarButtonItem: [DataManager sharedInstance].leftDetailSM.segmentBarItem
                                       animated: YES];

    self.popoverController = nil;
}

#else

// Create the iPhone view controller
#endif
#pragma mark -
#pragma mark Rotation support
- (void)willAnimateSecondHalfOfRotationFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation duration:(NSTimeInterval)duration
{
    //  [self.view sizeToFit]; // say we need this after rotation
    //        [self.view setNeedsLayout];
    //    [self.view setNeedsDisplay];
}


- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation duration:(NSTimeInterval)duration
{
    //    [self.view sizeToFit]; // say we need this after rotation
    //    [self.view setNeedsLayout];
    //    [self.view setNeedsDisplay];
}
-(void) goNative
{
    fullScreenWebView = NO;
    [DataManager sharedInstance].rootController.view.hidden = NO;
    [fswebView removeFromSuperview];
    [self resetLeftPanel];
    [self.view sizeToFit]; // say we need this after rotation
    [self.view setNeedsLayout];
    [self.view setNeedsDisplay];

}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
    if (fullScreenWebView == YES)
    {
        //      CGRect thisFrame = [self.view frame];
        //      if (thisFrame.size.width ==703.f)
        //      {
        //          thisFrame.origin.x  = 0.0f;
        //          thisFrame.size.width = 1024.;
        //
        //
        //          self.view.frame = thisFrame;
        //
        //      }

        self.view.frame = [self fullFrame];

        [DataManager sharedInstance].rootController.view.hidden = YES;
    }
    else
    {
        [DataManager sharedInstance].rootController.view.hidden = NO;

        if (fswebView)
            [fswebView removeFromSuperview];
    }

    [self.view sizeToFit]; // say we need this after rotation
    [self.view setNeedsLayout];
    [self.view setNeedsDisplay];


}

// Ensure that the view controller supports rotation and that the split view can therefore show in both portrait and landscape.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}


#pragma mark -
#pragma mark View lifecycle




#pragma mark -
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    DataManager *dm =   [DataManager sharedInstance];
    dm.detailController = self; // record this for manipulation from above

    NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext]; // deep error checking in here
    if(!scene) return;

    navigationBar.barStyle = UIBarStyleBlack;
    if (![scene objectForKey:@"largetitle"])
        [[DataManager sharedInstance] dieFromMisconfiguration:@"No largetitle for current scene"];

    navigationBar.topItem.title = [scene objectForKey:@"largetitle"];
    navigationBar.topItem.rightBarButtonItem = [DataManager sharedInstance].rightDetailSM.segmentBarItem;

    NSString *onceURL = [[[DataManager sharedInstance] masterPlist] objectForKey:@"once"];
    if (onceURL)
    {
        [UIApplication sharedApplication].networkActivityIndicatorVisible=YES;
        CGRect bounds = [[UIScreen mainScreen] bounds];

        //put up something other than the white background
        UIImageView* imageView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"nAppleIcon_512x512.png"]]  autorelease];
        imageView.center = CGPointMake(bounds.size.width/2.0,bounds.size.height/2.0);
        [self.view addSubview:imageView];
        // just call up this url, dont even display it, so cookies can get set, etc
        NSURL *url = [NSURL URLWithString:[onceURL stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]];

        UIWebView *wv = [[UIWebView alloc] init];

        NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
        [wv loadRequest:requestObj];
        [wv release];

        [UIApplication sharedApplication].networkActivityIndicatorVisible=NO;
    }


    // bill - 9 mar 10 persist this
    NSString *lastdetail = [[NSUserDefaults standardUserDefaults] objectForKey:@"lastdetail"];
    if(lastdetail && [[DataManager sharedInstance] samePlist])
        [self setDetailItem: lastdetail];
    else
        [self setDetailItem : @"0"]; // start with first item on list
                                     // -----
    [super viewDidLoad];
}



- (void)viewDidUnload {
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.popoverController = nil;
}


#pragma mark -
#pragma mark Memory management

/*
 - (void)didReceiveMemoryWarning {
 // Releases the view if it doesn't have a superview.
 [super didReceiveMemoryWarning];

 // Release any cached data, images, etc that aren't in use.
 }
 */

- (void)dealloc {
    [popoverController release];
    [navigationBar release];

    [detailItem release];
    [super dealloc];
}

@end
