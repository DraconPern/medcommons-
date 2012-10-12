//
//  DetailViewController.h
//  MedPad
//
//  Created by bill donner on 2/24/10.
//  Copyright Apple Inc 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UIViewController <UIPopoverControllerDelegate,UIWebViewDelegate, UISplitViewControllerDelegate> {

    UIPopoverController *popoverController;
    UINavigationBar *navigationBar;
    NSString  *detailItem;
    UIView *remoteExecutionView;
    UITextView *textViewTop;
    UITextView *textViewBottom;
    UILabel *topTextLabel;
    UILabel *bottomTextLabel;

    BOOL viewInited;
    BOOL fullScreenWebView;
    UIWebView* fswebView ;
}

@property (nonatomic, retain) UIPopoverController *popoverController;
@property (nonatomic, retain) IBOutlet UINavigationBar *navigationBar;
@property (nonatomic, retain) id detailItem;

- (void)showTopDetail:(NSString *) s;
- (void)showBottomDetail:(NSString *) s;


// these are called in response to button presses and possibly gestures
-(void)toggleLeftPanel;
-(void) viewInSafari;
-(void) displayDetailWebView: (NSString *)urlpart  backcolor: (UIColor *)bc title: (NSString *)titl;
-(void) displayFullScreenWebView: (NSString *)urlpart  backcolor: (UIColor *)bc title: (NSString *)titl;


@end
