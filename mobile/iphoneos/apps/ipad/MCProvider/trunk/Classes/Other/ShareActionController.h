//
//  ShareActionController.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/27/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@interface ShareActionController : MCActionController
{
@private

    UIViewController *viewController_;
    UIWebView        *webView_;
}

@property (nonatomic, retain, readwrite) UIViewController *viewController;
@property (nonatomic, retain, readwrite) UIWebView        *webView;

- (id) initWithViewController: (UIViewController *) viewController
                      webView: (UIWebView *) webView;

@end
