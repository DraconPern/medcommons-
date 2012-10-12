//
//  DetailViewController.h
//  MCProvider
//
//  Created by Bill Donner on 2/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UIViewController <UIWebViewDelegate>
{
@private

    //UILabel    *bottomTextLabel_;
    NSString     *detailItem_;
    //UIView     *faxTitleView_;
    //UIWebView  *faxWebView_;
    //UIView     *fsTitleView_;
    //UIWebView  *fsWebView_;
    //UIView     *fullScreenPlaneView_;
    //BOOL        fullScreenWebView_;
    NSString     *lastURL_;
    //UITextView *textViewBottom_;
    //UITextView *textViewTop_;
    //UILabel    *topTextLabel_;
    //UIView     *traceView_;
    //BOOL        viewInited_;
}

@property (nonatomic, retain, readwrite) NSString *detailItem;

-(void) viewInSafari;

@end
