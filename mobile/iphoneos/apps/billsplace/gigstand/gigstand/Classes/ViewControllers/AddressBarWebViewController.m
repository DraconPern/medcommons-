//
//  AddressBarWebViewController.m
//  iCodeBrowser
//
//  Hacked by BillDonner from Brandon Trebitowski on 12/19/08.
//
#import "GigStandAppDelegate.h"
#import "ArchivesManager.h"
#import "AddressBarWebViewController.h"
#import <QuartzCore/QuartzCore.h>
// larger values of FAcTOR only make the border larger

#define  TITLE_WIDTH 140.0f
#define  STANDARD_NAV_HEIGHT 44.0f
#define  TOP_TOOBAR_HEIGHT 44.0f
#define  ADDRESS_BAR_HEIGHT 44.0f
#define  STATUS_BAR_HEIGHT 20.0f
#define FACTOR 1.0f

@implementation AddressBarWebViewController

@synthesize webView, addressBar, activityIndicator;

-(UIImage *)captureView:(UIView *)view 

{ 
	CGRect tmpFrame = [[UIScreen mainScreen] bounds];
	
	CGRect screenRect = CGRectMake(0,0,FACTOR*tmpFrame.size.width,FACTOR*(tmpFrame.size.height-STATUS_BAR_HEIGHT-STANDARD_NAV_HEIGHT-TOP_TOOBAR_HEIGHT));
	
	UIGraphicsBeginImageContextWithOptions(screenRect.size, NO, 0);//(screenRect.size);
	
	CGContextRef ctx = UIGraphicsGetCurrentContext(); 
    
    [[UIColor blackColor] set]; 
    
    CGContextFillRect(ctx, screenRect);
	
	[view.layer renderInContext:ctx];
	
	UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
	
	UIGraphicsEndImageContext();
	
	//xNSLog (@"captureView returning %@ x %f y %f w %f h %f",newImage,screenRect.origin.x,screenRect.origin.y,screenRect.size.width,screenRect.size.height);
	return newImage; 
}

- (UIImage*)xcaptureView:(UIView *)view  
{
    // Create a graphics context with the target size
    // On iOS 4 and later, use UIGraphicsBeginImageContextWithOptions to take the scale into consideration
    // On iOS prior to 4, fall back to use UIGraphicsBeginImageContext
    
    
	CGRect tmpFrame = [[UIScreen mainScreen] bounds];
	
	CGRect screenRect = CGRectMake(0,0,FACTOR*tmpFrame.size.width,FACTOR*(tmpFrame.size.height-STATUS_BAR_HEIGHT-STANDARD_NAV_HEIGHT-ADDRESS_BAR_HEIGHT));
	
	UIGraphicsBeginImageContextWithOptions(screenRect.size, NO, 0);//(screenRect.size);

    
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Iterate over every window from back to front
    for (UIWindow *window in [[UIApplication sharedApplication] windows]) 
    {
       if ((![window respondsToSelector:@selector(screen)] || [window screen] == [UIScreen mainScreen])
          )// && 
            
           // if (
            //    ([window layer] == [view layer])) // only capture the actual webview
        {
            
            // -renderInContext: renders in the coordinate space of the layer,
            // so we must first apply the layer's geometry to the graphics context
            CGContextSaveGState(context);
            // Center the context around the window's anchor point
            CGContextTranslateCTM(context, [window center].x, [window center].y);
            // Apply the window's transform about the anchor point
            CGContextConcatCTM(context, [window transform]);
            // Offset by the portion of the bounds left of and above the anchor point
            CGContextTranslateCTM(context,
                                  -[window bounds].size.width * [[window layer] anchorPoint].x,
                                  -[window bounds].size.height * [[window layer] anchorPoint].y);
            
            // Render the layer hierarchy to the current context
            [[window layer] renderInContext:context];
            
            // Restore the context
            CGContextRestoreGState(context);
        }
    }
    
    // Retrieve the screenshot image
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return image;
}

- (void) finished: (id) sender
{
    [self dismissModalViewControllerAnimated: YES];
	
}
- (void) screenshot: (id) sender
{	
	UIImage *shot = [self captureView:self.webView];
	[ArchivesManager saveImageToOnTheFlyArchive: shot];
	//[ArchivesManager dump];
    [self dismissModalViewControllerAnimated: YES];	
}


- (void) choosed: (id) sender
{
	[self dismissModalViewControllerAnimated: YES];
	
}
- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
	
    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer
shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return NO;
}

// The designated initializer. Override to perform setup that is required before the view is loaded.
//- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
//	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
//	if (self) {
//        // Custom initialization
//    }
//    return self;
//}

- (void) quadTap:(UITapGestureRecognizer *)sender {
	int scrollPosition = [[self.webView stringByEvaluatingJavaScriptFromString:@"window.pageYOffset"] intValue];
	CGPoint startPoint = [sender locationInView:self.webView];
	NSString *js = [NSString stringWithFormat:@"document.elementFromPoint(%f, %f).tagName", startPoint.x, startPoint.y+scrollPosition];
	NSString *value = [self.webView stringByEvaluatingJavaScriptFromString:js];
	NSString *imgURL = [NSString stringWithFormat:@"document.elementFromPoint(%f, %f).src", startPoint.x, startPoint.y+scrollPosition];
	NSString *urlToSave = [self.webView stringByEvaluatingJavaScriptFromString:imgURL];
	NSString *blurb;
	
	//Do whatever you want - like a UIAlertView or UIPopover to let the user chose what to do with the image
	if ([value isEqualToString:@"IMG"]) 
	{
		//blurb = @"Saving Image URL as";
		addressBarIsImage = YES;
		addressBar.text = urlToSave;
		[self gotoAddress:sender];
	}
	
	
	else {
		blurb = [NSString stringWithFormat:@"Can not save that page element %@",value];

		UIAlertView *av = [[[UIAlertView alloc] initWithTitle: NSLocalizedString (blurb, @"")
													  message: urlToSave // IMPORTANT
													 delegate: nil
											cancelButtonTitle: @"OK" // NSLocalizedString (@"Cancel", @"")
											otherButtonTitles:  nil] //NSLocalizedString (@"Enter", @""), nil]
						   autorelease];
		
		[av show];
	}
}

-(id) initWithMcid: (NSString *) mcid_ ;
{
	self = [super init];
	if (self)
	{
		addressBarIsImage = NO;
		mcid = [mcid_ copy];
		
	}
	return self;
}

-(void) loadView
{
	
	CGRect tmpFrame;
    //
    // Background view:
    //
    tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
	
    UIView *fullview = [[[UIView alloc] initWithFrame: tmpFrame] autorelease];
    fullview.autoresizingMask = (UIViewAutoresizingFlexibleHeight |UIViewAutoresizingFlexibleWidth);
    fullview.backgroundColor = [UIColor clearColor];


	CGRect topToolbarFrame = CGRectMake(0.0f, STANDARD_NAV_HEIGHT+
                                        STATUS_BAR_HEIGHT, 
										tmpFrame.size.width, TOP_TOOBAR_HEIGHT);
	
	CGRect addressBarFrame = CGRectMake(0.0f, 0.0f, 
										tmpFrame.size.width-TITLE_WIDTH, ADDRESS_BAR_HEIGHT); // this is put into a button which is laid into the top toolbar
	
	CGRect activityIndicatorFrame = CGRectMake (tmpFrame.size.width - ADDRESS_BAR_HEIGHT-10.0f,
											   STANDARD_NAV_HEIGHT+5.f, ADDRESS_BAR_HEIGHT, ADDRESS_BAR_HEIGHT); 

	CGRect webViewFrame = CGRectMake (0.0f, TOP_TOOBAR_HEIGHT,//+STANDARD_NAV_HEIGHT, //
									  tmpFrame.size.width ,tmpFrame.size.height-(TOP_TOOBAR_HEIGHT+STATUS_BAR_HEIGHT//+STANDARD_NAV_HEIGHT
                                                                                 ));
	
	UIBarButtonItem *spacer = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil] autorelease];
	spacer.enabled = YES;
	spacer.style = UIBarButtonItemStyleBordered;
	spacer.tag = 0.0f;
	spacer.width = 15.0f;

	UIBarButtonItem *addressButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(gotoAddress:)]autorelease];
	addressButton.enabled = YES;
	addressButton.style = UIBarButtonItemStylePlain;
	addressButton.tag = 0.0f;
	addressButton.title = @"Address";
	addressButton.width = 50.000f;

	UIBarButtonItem *fastForwardButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFastForward target:self action:@selector(goForward:)] autorelease];
	fastForwardButton.enabled = YES;
	fastForwardButton.style = UIBarButtonItemStylePlain;
	fastForwardButton.tag = 0.0f;
	fastForwardButton.width = 0.000f;
	
	UIBarButtonItem *rewindButton = [[[UIBarButtonItem alloc] 
									 initWithBarButtonSystemItem:UIBarButtonSystemItemRewind target:self action:@selector(goBack:)] autorelease];
	rewindButton.enabled = YES;
	rewindButton.style = UIBarButtonItemStylePlain;
	rewindButton.tag = 0.0f;
	rewindButton.width = 0.000f;
	
	///// 
	/////  These objects are explicitly placed on type and orientation
	///// 

	
	
	
	////
	//// these objects are setup as instance variables so they can be manipualted elsewhere
	////

	self.addressBar = [[UITextField alloc] initWithFrame:addressBarFrame];
	addressBar.adjustsFontSizeToFitWidth = YES;
	addressBar.alpha = 1.000f;
	addressBar.autocapitalizationType = UITextAutocapitalizationTypeNone;
	addressBar.autocorrectionType = UITextAutocorrectionTypeDefault;
	addressBar.autoresizesSubviews = YES;
	addressBar.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
	addressBar.borderStyle = UITextBorderStyleRoundedRect;
	addressBar.clearButtonMode = UITextFieldViewModeNever;
	addressBar.clearsContextBeforeDrawing = NO;
	addressBar.clearsOnBeginEditing = YES;
	addressBar.clipsToBounds = NO;
	addressBar.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
	addressBar.contentMode = UIViewContentModeScaleToFill;
	addressBar.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	addressBar.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
	addressBar.enabled = YES;
	addressBar.enablesReturnKeyAutomatically = NO;
	addressBar.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
	addressBar.hidden = NO;
	addressBar.highlighted = NO;
	addressBar.keyboardAppearance = UIKeyboardAppearanceDefault;
	addressBar.keyboardType = UIKeyboardTypeURL;
	addressBar.minimumFontSize = 17.000f;
	addressBar.multipleTouchEnabled = NO;
	addressBar.opaque = NO;
	addressBar.returnKeyType = UIReturnKeyGo;
	addressBar.secureTextEntry = NO;
	addressBar.selected = NO;
	addressBar.tag = 0.0f;
	addressBar.text = @"";
	addressBar.textAlignment = UITextAlignmentLeft;
	addressBar.textColor = [UIColor colorWithWhite:0.000f alpha:1.000f];
	addressBar.userInteractionEnabled = YES;
	addressBar.delegate = self;
	addressBar.placeholder = @"Enter full URL including http://";
	
	
	
	self.webView = [[UIWebView alloc] initWithFrame:webViewFrame]; 
	webView.alpha = 1.000f;
	webView.autoresizesSubviews = YES;
	webView.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
	webView.backgroundColor = [UIColor colorWithRed:1.000f green:1.000f blue:1.000f alpha:1.000f];
	webView.clearsContextBeforeDrawing = YES;
	webView.clipsToBounds = YES;
	webView.contentMode = UIViewContentModeScaleToFill;
	webView.contentStretch = CGRectFromString(@"{{0, 0}, {2, 2}}");
	webView.hidden = NO;
	webView.multipleTouchEnabled = YES;
	webView.opaque = YES;
	webView.scalesPageToFit = YES;
	webView.tag = 0.0f;
	webView.userInteractionEnabled = YES;
	
	
	///
	/// the address bar must be part of the topToolbar, it is forced into a button
	///
	
	UIBarButtonItem *addressBarButton = [[[UIBarButtonItem alloc] initWithCustomView:addressBar] autorelease];
	addressBarButton.enabled = YES;
	addressBarButton.style = UIBarButtonItemStylePlain;
	addressBarButton.tag = 0.0f;
	
	
	///
	/// likewise the activityIndicator is forced into a button sized thing
	
	activityIndicatorView = [[[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite] autorelease];
	activityIndicatorView.frame = activityIndicatorFrame;
	activityIndicatorView.alpha = 1.000f;
	activityIndicatorView.autoresizesSubviews = YES;
	activityIndicatorView.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
	activityIndicatorView.clearsContextBeforeDrawing = NO;
	activityIndicatorView.clipsToBounds = NO;
	activityIndicatorView.contentMode = UIViewContentModeScaleToFill;
	activityIndicatorView.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	activityIndicatorView.hidden = YES;
	activityIndicatorView.hidesWhenStopped = YES;
	activityIndicatorView.multipleTouchEnabled = NO;
	activityIndicatorView.opaque = NO;
	activityIndicatorView.tag = 0.0f;
	activityIndicatorView.userInteractionEnabled = NO;
	[activityIndicatorView stopAnimating];
	
//	UIBarButtonItem *aiv= [[[UIBarButtonItem alloc] initWithCustomView:activityIndicatorView] autorelease];
//	aiv.enabled = YES;
//	aiv.style = UIBarButtonItemStylePlain;
//	aiv.tag = 0.0f;
	
	
	UIToolbar *topToolbar = [[[UIToolbar alloc] initWithFrame:topToolbarFrame] autorelease];
	topToolbar.alpha = 1.000f;
	topToolbar.autoresizesSubviews = YES;
	topToolbar.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
	topToolbar.barStyle = UIBarStyleBlackTranslucent;
	topToolbar.clearsContextBeforeDrawing = NO;
	topToolbar.clipsToBounds = NO;
	topToolbar.contentMode = UIViewContentModeScaleToFill;
	topToolbar.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	topToolbar.hidden = NO;
	topToolbar.multipleTouchEnabled = NO;
	topToolbar.opaque = NO;
	topToolbar.tag = 0.0f;
	topToolbar.userInteractionEnabled = YES;
	topToolbar.items = [NSArray arrayWithObjects:rewindButton,spacer, fastForwardButton,spacer, addressBarButton, addressButton,nil];
	
//	
//	UITapGestureRecognizer *quadTap = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(quadTap:)] autorelease];
//	quadTap.numberOfTouchesRequired = 1;	
//	quadTap.numberOfTapsRequired = 4;
//	
//	[webView addGestureRecognizer:quadTap];
	webView.delegate = self; // be sure to get callbacks
	
	self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone
																						  target:self action:@selector(finished:)];
	
	self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera
																						  target:self action:@selector(screenshot:)];
	
	self.navigationItem.title =@"select a screen to add to OnTheFly >>>>";
	[self setColorForNavBar];
	
	
	[fullview addSubview:activityIndicatorView];
	[fullview addSubview:topToolbar];
	[fullview addSubview:webView];
	[fullview addSubview:activityIndicatorView];
	
	self.view = fullview;
	
}


- (void)viewDidLoad {
    [super viewDidLoad];
	NSString *urlAddress = @"http://s354932748.onlinehome.us/content-sites.html";//@"http://gigstand.net/";
	
	NSURL *url = [NSURL URLWithString:urlAddress];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	
	[webView loadRequest:requestObj];
	[addressBar setText:urlAddress];
	
}

-(void)gotoAddress:(id) sender {
	NSURL *url = [NSURL URLWithString:[addressBar text]];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	
	NSLog (@"gotoAddress url %@", url);
	
	addressBar.text = [NSString stringWithFormat:@"%@",url];
	
	if (addressBarIsImage == YES)
	{
	instructionsLabel.text = @"Hit the Save Button when you have the portrait you want as the only image";
	self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave
																						  target:self action:@selector(choosed:)];
	}
	//Load the request in the UIWebView.
	[webView loadRequest:requestObj];
	
	[addressBar resignFirstResponder];
}

-(void) goBack:(id)sender {
	[webView goBack];
}

-(void) goForward:(id)sender {
	[webView goForward];
}

- (BOOL)webView:(UIWebView*)webView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType {
	//CAPTURE USER LINK-CLICK.
	if (navigationType == UIWebViewNavigationTypeLinkClicked) {
		NSURL *URL = [request URL];	
		if ([[URL scheme] isEqualToString:@"http"]) {
			addressBar.text = [URL absoluteString];
			[self gotoAddress:nil];
		}
		else if  (![URL scheme]|| [[URL scheme] isEqualToString:@""]) {
			addressBar.text = [NSString stringWithFormat:@"http://%@",[URL absoluteString] ];
			[self gotoAddress:nil];
		}
			
		return NO;
	}	
	return YES;   
}
- (BOOL)textFieldShouldReturn:(UITextField *)textField{
	[self gotoAddress:nil];
	
    return YES;
}
- (void)webViewDidStartLoad:(UIWebView *)webView {
	[activityIndicator startAnimating];
}

- (void)webViewDidFinishLoad:(UIWebView *)webViewX {
	[activityIndicator stopAnimating];
	
	[addressBar setText:[NSString stringWithFormat:@"%@",webViewX.request.URL]];
}


/*
 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
 [super viewDidLoad];
 }
 */

-(void) remakeContentView;
{	
	if (self->webView) {
		[self->webView removeFromSuperview];
		[self->webView  release];
	}
    
    if (self->addressBar) {
		[self->addressBar removeFromSuperview];
		[self->addressBar  release];
	}
    
    if (self->activityIndicator ) {
		[self->activityIndicator removeFromSuperview];
		[self->activityIndicator  release];
	}
	
	[self loadView];
    [self viewDidLoad];
    
}
    


- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{	
	// everylast thing was set up in init	
	[self remakeContentView];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning]; // Releases the view if it doesn't have a superview
    // Release anything that's not essential, such as cached data
}


- (void)dealloc {
    [super dealloc];
}

@end
