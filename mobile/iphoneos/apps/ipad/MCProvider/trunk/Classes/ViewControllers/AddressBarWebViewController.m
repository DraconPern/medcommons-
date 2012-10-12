//
//  AddressBarWebViewController.m
//  iCodeBrowser
//
//  Created by Brandon Trebitowski on 12/19/08.
//  Copyright __MyCompanyName__ 2008. All rights reserved.
//www.med

#import "AppDelegate.h"
#import "AddressBarWebViewController.h"
#import "SessionManager.h"

@implementation AddressBarWebViewController

@synthesize webView, addressBar, activityIndicator;

- (void) finished: (id) sender
{
    [self dismissModalViewControllerAnimated: YES];
	
}

- (void) choosed: (id) sender
{
	//- (BOOL) replacePortraitURL: (NSURL *) url mcid: (NSString *) mcid;   
	
	
    SessionManager *sm = self.appDelegate.sessionManager;
	
	[sm replacePortraitURL:addressBar.text mcid:mcid];
	
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
	
    UIView *fullview = [[[UIView alloc] initWithFrame: tmpFrame]
						autorelease];
	
    fullview.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
								 UIViewAutoresizingFlexibleWidth);
    fullview.backgroundColor = [UIColor redColor];
	
	UIBarButtonItem *spacer = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
	//phonyButtonForBottomLabel.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	spacer.enabled = YES;
	spacer.style = UIBarButtonItemStyleBordered;
	spacer.tag = 0.0f;
	spacer.width = 25.0f;
	
	CGRect instructionsframe =  CGRectMake(10.0f, 980.0f, 748.0f, 44.0f);
	instructionsLabel = [[UILabel alloc] initWithFrame:instructionsframe];
    instructionsLabel.text = @"Choose a web-hosted portrait by browsing to a web page and tapping four times on the image" ;
    instructionsLabel.textColor = [UIColor whiteColor];
    instructionsLabel.textAlignment = UITextAlignmentLeft;
    instructionsLabel.font = [UIFont fontWithName:@"Arial" size:14];
    instructionsLabel.backgroundColor = [UIColor clearColor];
	
	UIBarButtonItem	*phonyButtonForBottomLabel = [[UIBarButtonItem alloc] initWithCustomView:instructionsLabel ];
	//phonyButtonForBottomLabel.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	phonyButtonForBottomLabel.enabled = NO;
	phonyButtonForBottomLabel.style = UIBarButtonItemStylePlain;
	phonyButtonForBottomLabel.tag = 0.0f;
	phonyButtonForBottomLabel.width = 748.0f;
	
	UIBarButtonItem *addressButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(gotoAddress:)];
	//addressButton.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	addressButton.enabled = YES;
	addressButton.style = UIBarButtonItemStylePlain;
	addressButton.tag = 0.0f;
	addressButton.title = @"Address";
	addressButton.width = 50.000f;
	
	
	UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
	activityIndicatorView.frame = CGRectMake(735.0f, 29.0f, 20.0f, 20.0f);
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
	
	UIBarButtonItem *fastForwardButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFastForward target:self action:@selector(goForward:)];
	//	fastForwardButton.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	fastForwardButton.enabled = YES;
	fastForwardButton.style = UIBarButtonItemStylePlain;
	fastForwardButton.tag = 0.0f;
	fastForwardButton.width = 0.000f;
	
	UIBarButtonItem *rewindButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRewind target:self action:@selector(goBack:)];
	//	rewindButton.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	rewindButton.enabled = YES;
	rewindButton.style = UIBarButtonItemStylePlain;
	rewindButton.tag = 0.0f;
	rewindButton.width = 0.000f;
	
	
	self.addressBar = [[UITextField alloc] initWithFrame:CGRectMake(103.0f, 7.0f, 580.0f, 31.0f)];
	//addressBar.frame = CGRectMake(103.0f, 7.0f, 626.0f, 31.0f);
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
	
	UIToolbar *topToolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0.0f, 64.0f, 768.0f, 44.0f)];
	//topToolbar.frame = CGRectMake(0.0f, 50.0f, 768.0f, 44.0f);
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
	
	self.webView = [[UIWebView alloc] initWithFrame:CGRectMake(0.0f, 44.0f,768.0f ,  980.0f-108.0f)]; 
	//webView.frame = CGRectMake(-4.0f, 62.0f, 768.0f, 920.0f);
	webView.alpha = 1.000f;
	webView.autoresizesSubviews = YES;
	webView.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
	webView.backgroundColor = [UIColor colorWithRed:1.000f green:1.000f blue:1.000f alpha:1.000f];
	webView.clearsContextBeforeDrawing = YES;
	webView.clipsToBounds = YES;
	webView.contentMode = UIViewContentModeScaleToFill;
	webView.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
	webView.hidden = NO;
	webView.multipleTouchEnabled = YES;
	webView.opaque = YES;
	webView.scalesPageToFit = YES;
	webView.tag = 0.0f;
	webView.userInteractionEnabled = YES;
	
	
	
	UIToolbar *bottomToolBar = [[UIToolbar alloc] initWithFrame:CGRectMake(0.0f, 980.0f, 768.0f, 44.0f)];
	
	bottomToolBar.alpha = 1.000f;
	bottomToolBar.autoresizesSubviews = YES;
	bottomToolBar.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
	bottomToolBar.barStyle = UIBarStyleBlackTranslucent;
	bottomToolBar.clearsContextBeforeDrawing = NO;
	bottomToolBar.clipsToBounds = NO;
	bottomToolBar.contentMode = UIViewContentModeScaleToFill;
	bottomToolBar.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
	bottomToolBar.hidden = NO;
	bottomToolBar.multipleTouchEnabled = NO;
	bottomToolBar.opaque = NO;
	bottomToolBar.tag = 0.0f;
	bottomToolBar.userInteractionEnabled = YES;
	
	UIBarButtonItem *addressBarButton = [[UIBarButtonItem alloc] initWithCustomView:addressBar];//:UIBarButtonSystemItemFixedSpace target:nil action:nil];
	//addressBarButton.frame = CGRectMake(0.0f, 0.0f, 0.0f, 0.0f);
	addressBarButton.enabled = YES;
	addressBarButton.style = UIBarButtonItemStylePlain;
	addressBarButton.tag = 0.0f;
	
	bottomToolBar.items = [NSArray arrayWithObjects:phonyButtonForBottomLabel,nil];
	topToolbar.items = [NSArray arrayWithObjects:rewindButton,spacer, fastForwardButton,spacer, addressBarButton, addressButton, nil];
	addressBar.delegate = self;
	addressBar.placeholder = @"Enter full URL including http://";
    [fullview addSubview:topToolbar];
	[fullview addSubview:bottomToolBar];
	[fullview addSubview:webView];
	[fullview addSubview:activityIndicatorView];
	
	UITapGestureRecognizer *quadTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(quadTap:)];
	quadTap.numberOfTouchesRequired = 1;
	
	quadTap.numberOfTapsRequired = 4;
	
	[webView addGestureRecognizer:quadTap];
	[quadTap release];	
	
	self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
																						  target:self action:@selector(finished:)];
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.view = fullview;
    
    [topToolbar release];//wld 01may11
    
    [bottomToolBar release];//wld 01may11
    
    
    [rewindButton release];//wld 01may11
    
    
    [activityIndicatorView  release];//wld 01may11
    
    [phonyButtonForBottomLabel release];//wld 01may11
    
    [fastForwardButton release];//wld 01may11
    
    [addressButton release];//wld 01may11
    
    [addressBarButton release];//wld 01may11
    
    [spacer release];//wld 01may11
    
	
}


- (void)viewDidLoad {
    [super viewDidLoad];
	NSString *urlAddress = @"http://google.com/images";
	
	NSURL *url = [NSURL URLWithString:urlAddress];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	
	[webView loadRequest:requestObj];
	[addressBar setText:urlAddress];
	
}

-(void)gotoAddress:(id) sender {
	NSURL *url = [NSURL URLWithString:[addressBar text]];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
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

- (void)webViewDidFinishLoad:(UIWebView *)webView {
	[activityIndicator stopAnimating];
}


/*
 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
 [super viewDidLoad];
 }
 */


/*
 // Override to allow orientations other than the default portrait orientation.
 - (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
 // Return YES for supported orientations
 return (interfaceOrientation == UIInterfaceOrientationPortrait);
 }
 */

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning]; // Releases the view if it doesn't have a superview
    // Release anything that's not essential, such as cached data
}


- (void)dealloc {
    [super dealloc];
}

@end
