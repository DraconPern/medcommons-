
#import "DDLWebController.h"


@implementation DDLWebController

- (id)initWithURL:(NSURL *)aURL{
	
	self = [super initWithWindowNibName:@"DDLWebView"];
	URL = [aURL retain];
	return self;
}

- (void)dealloc{
	[URL release];
	[super dealloc];
}

- (void)windowDidLoad{
	[[webView mainFrame] loadRequest:[NSURLRequest requestWithURL:URL]];
}

@end
