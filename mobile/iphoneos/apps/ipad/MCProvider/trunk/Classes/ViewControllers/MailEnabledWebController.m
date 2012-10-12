    //
//  MailEnabledWebController.m
//  MCProvider
//
//  Created by bill donner on 12/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MailEnabledWebController.h"

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@implementation MailEnabledWebController

 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}



#pragma mark UIWebViewDelegate Methods


- (NSDictionary *) handleOpenURL: (NSURL *)URL
{
	NSString *u =  [URL relativeString];
	NSString *q =  [u substringFromIndex:       [ u rangeOfString:@"?"].location+1];
	NSArray *a = [q componentsSeparatedByString: @"&"];
	//	NSLog (@"*** webviewcontroller handleOpenURL: %@ query: %@ a:%@", u,q,a);
	
	//
	// Expect URL with syntax: "mc://viewer?key1=val1&key2=val2&...":
	//
	NSMutableDictionary *params = [NSMutableDictionary dictionary];
	
	for (NSString *queryPair in a)
	{
		NSArray *query = [queryPair componentsSeparatedByString: @"="];
		
		if ([query count] == 2)
		{
			NSString *key = [[query objectAtIndex: 0] stringByURLDecoding];
			NSString *obj = [[query objectAtIndex: 1] stringByURLDecoding];
			
			if (key && obj)
				[params setObject: obj
						   forKey: key];
		}
	}
	
	return params;
}
- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error
{
	NSLog (@"Finishing mail with result %d error %@", result,error);
	[self dismissModalViewControllerAnimated:YES];
	
	// release composer object
	[mailer release];
}

- (BOOL)webView:(UIWebView *)sender shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
	if ([request.URL.scheme isEqualToString:@"mailto"]) {
		// make sure this device is setup to send email
		if ([MFMailComposeViewController canSendMail]) {
			// create mail composer object
			mailer  = [[MFMailComposeViewController alloc] init];
			
			// make this view the delegate
			mailer.mailComposeDelegate = self;
			
			NSDictionary *d = [self handleOpenURL: request.URL];
			//NSLog(@"Got dictionary %@",d);
			
			// set recipient
			[mailer setToRecipients:[NSArray arrayWithObject:@"cmo@medcommons.net"]];
			
			if ([d objectForKey:@"subject"])
			{
				[mailer setSubject:[d objectForKey:@"subject"]];
			}
			
			// generate message body
			
			if ([d objectForKey:@"body"])
			{
				[mailer setMessageBody:[d objectForKey:@"body"] isHTML: NO];
			} else 
				
				// add to users signature
				[mailer setMessageBody:@"" isHTML:NO];
			
			// present user with composer screen
			[self presentModalViewController:mailer animated:YES];
			
		} else {
			// alert to user there is no email support
		}
		
		// don't load url in this webview
		return NO;
	}
	
	return YES;
}


@end
