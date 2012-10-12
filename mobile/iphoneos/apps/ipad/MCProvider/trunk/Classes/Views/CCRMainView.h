//
//  CCRMainView.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCRMainView : UIWebView
{
@private

    UIActivityIndicatorView *activityIndicator_;
}

- (void) updateActivityIndicator;

@end
