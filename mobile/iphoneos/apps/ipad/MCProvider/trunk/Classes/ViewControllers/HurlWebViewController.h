//
//  HurlWebViewController.h
//  MCProvider
//
//  Created by Bill Donner on 4/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "WebViewController.h"

@class MCActionController;

@interface HurlWebViewController : WebViewController <UIGestureRecognizerDelegate>
{
@private

    UIBarButtonItem    *actionButton_;
    MCActionController *actionController_;
    UIBarButtonItem    *backButton_;
}

@end
