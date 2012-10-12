//
//  WebCaptureController.h
//  iCodeBrowser
//
//  Created by Brandon Trebitowski on 12/19/08.
//  Copyright __MyCompanyName__ 2008. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WebCaptureController : UIViewController<UIWebViewDelegate,UITextFieldDelegate> {
    @private
    UIWebView *webView;
    UITextField *addressBar;
	UIActivityIndicatorView *activityIndicatorView;
	UILabel *instructionsLabel;
	NSString *mcid;
    BOOL addressBarIsImage;
}

@property(nonatomic,retain) UIWebView *webView;
@property(nonatomic,retain) UITextField *addressBar;
@property(nonatomic,retain) UIActivityIndicatorView *activityIndicator;

-(void ) gotoAddress:(id)sender;
-(void ) goBack:(id)sender;
-(void ) goForward:(id)sender;


-(id) initWithMcid: (NSString *) mcid_ ;

@end
