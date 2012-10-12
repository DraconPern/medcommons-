//
//  GroupDetailController.h
//  MCProvider
//
//  Created by Bill Donner on 4/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class InboxGroupListController;

@interface GroupDetailController : UIViewController <UIWebViewDelegate>
{
@private

    InboxGroupListController *iglc_;
    //UILabel    *bottomTextLabel_;
    NSString     *detailItem_;
    //UIView     *faxTitleView_;
    //UIWebView  *faxWebView_;
    //UIView     *fsTitleView_;
    //UIWebView  *fsWebView_;
    //UIView     *fullScreenPlaneView_;
    //BOOL        fullScreenWebView_;
    //NSString   *lastURL_;
    //UITextView *textViewBottom_;
    //UITextView *textViewTop_;
    //UILabel    *topTextLabel_;
    //UIView     *traceView_;
    //BOOL        viewInited;
}

@property (nonatomic, retain, readwrite) NSString *detailItem;

- (void) displayMemberList;

@end
