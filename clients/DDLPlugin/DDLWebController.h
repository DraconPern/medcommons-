#import <Cocoa/Cocoa.h>
#import <WebKit/WebKit.h>

@interface DDLWebController : NSWindowController {
	IBOutlet WebView *webView;
	NSURL *URL;
}

- (id)initWithURL:(NSURL *)aURL;

@end
