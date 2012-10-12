//
//  LocalWebViewController.h
//  gigstand
//
//  Created by bill donner on 4/28/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface LocalWebViewController : UIViewController {
 
    @private
        
        UIActivityIndicatorView *activityIndicator_;
        NSString                    *html_;
        NSString                *title_;
        UIWebView               *contentView_;
        NSUInteger               depth_;
        BOOL                     sharedDocument_;
        NSTimeInterval           startTime_;
        
        
        
        
        
    }
    
    @property (nonatomic, retain, readonly) UIWebView *contentView;
    


- (id) initWithHTML: (NSString *) html title:(NSString *) title;

    
    - (void) injectJavaScript: (NSString *) jsString;
    
    - (void) refresh;
    
    @end
