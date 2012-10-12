//
//  DetailViewController+Bill.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DataManager.h"
#import "DetailViewController+Bill.h"
#import "HurlWebViewController.h"
#import "MasterViewController.h"
#import "Member.h"
#import "MemberStore.h"
#import "SegmentedControl.h"
#import "SegmentMap.h"
#import "Session.h"
#import "SessionManager.h"

@implementation DetailViewController (Bill)

- (void) displayFullScreenWebView: (NSString *) urlString
                  backgroundColor: (UIColor *) bc
                            title: (NSString *) titl

{
    lastURL_ = [[urlString stringByTrimmingWhitespace]
                retain];

    //NSLog (@"*** displayFullScreenWebView: %@ ***", lasturl);

    HurlWebViewController *hwvc = [[[HurlWebViewController alloc]
                                    initWithURL: [NSURL URLWithString: urlString]]
                                   autorelease];

    //NSLog (@"*** displayFullScreenWebView: setting hwpc.title to %@ ***", titl);

    hwvc.title = titl;

    [self.navigationController pushViewController: hwvc
                                         animated: YES];
}

- (void) displayDetailWebView: (NSString *) urlString
              backgroundColor: (UIColor *) bc
                        title: (NSString *) titl
{
    //[self displayFullScreenWebView:urlpart backgroundColor:bc title:titl];

    NSURL *url = [NSURL URLWithString: [urlString stringByTrimmingWhitespace]];
    //    //  NSLog(@"displayDetailWebView %@ %@ %@",urlpart,[url absoluteURL  ],titl);
    //
    self.navigationItem.title = titl;
    //
    CGRect thisFrame = [self.view bounds];
    //    //thisFrame.size.height = thisFrame.size.height-44.0;
    //    //thisFrame.origin.y  = thisFrame.origin.y + 44.0;
    thisFrame.size.width = 768.0f;
    //    // NSLog (@"webpane webframe %f %f %f %f %@" , thisFrame.origin.x,thisFrame.origin.y,thisFrame.size.width ,thisFrame.size.height, urlpart);
    //    //CGRect thisFrame = [self.view bounds];
    //
    UIWebView *webView = [[UIWebView alloc] initWithFrame: thisFrame];

    webView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                UIViewAutoresizingFlexibleWidth);
    webView.backgroundColor = bc;
    webView.delegate = self;
    webView.scalesPageToFit = YES;

    NSURLRequest *requestObj = [NSURLRequest requestWithURL: url];

    [webView loadRequest: requestObj];

    [self.view addSubview: webView];

    [webView release];
    //  fullScreenWebView = NO;
    //
}

#pragma mark Handle x-medpad hyperlinks from HTML pages x-medppad://prototyper?scene=2 and x-medpad://viewer?healthURL=1234565

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navigationType
{
    AppDelegate    *appDel = self.appDelegate;
    SessionManager *sm = appDel.sessionManager;
    Session        *session = sm.loginSession;
    DataManager    *dm = appDel.dataManager;

    if ([[request.URL scheme] isEqual: @"x-medpad"])
    {
        //x-medpad:
        NSString *scene = @"missing scene";
        NSString *title = nil;
        NSString *auth = @"noauth";
        NSString *givenName = @"nofn";
        NSString *familyName = @"noln";
        NSString *tmpDate = @"nodate";
        NSString *tmpTime = @"notime";
        NSString *identifier = @"noid";

        // NSLog (@"shouldStartLoadWithRequest %@ type %d",request,navigationType);
        //x-medpad:
        NSString *queryString = [request.URL query];
        NSArray  *queryComponents = [queryString componentsSeparatedByString: @"&"];
        NSString *queryComponent;

        for (queryComponent in queryComponents)
        {
            NSArray *query = [queryComponent componentsSeparatedByString: @"="];

            if ([query count] == 2)
            {
                NSString *key = [query objectAtIndex: 0];
                NSString *value = [query objectAtIndex: 1];

                // NSLog (@"parsing %@ val: %@",key,value);

                if ([key isEqual: @"scene"])
                    scene = value;
                else if ([key isEqual: @"title"])
                    title = value;
                else if ([key isEqual: @"auth"])
                    auth = value;
                else if ([key isEqual: @"fn"])
                    givenName = value;
                else if ([key isEqual: @"ln"])
                    familyName = value;
                else if ([key isEqual: @"date"])
                    tmpDate = value;
                else if ([key isEqual: @"time"])
                    tmpTime = value;
                else if ([key isEqual: @"id"])
                    identifier = value;
                else if ([key isEqual: @"healthURL"])   // new
                {
                    if (session.memberInFocus)
                        [session.memberInFocus dump];

                    session.memberInFocus = [Member memberWithIdentifier: identifier
                                                               givenName: givenName
                                                              familyName: familyName
                                                                dateTime: [NSString stringWithFormat: @"%@ %@",
                                                                           tmpDate,
                                                                           tmpTime]];

                    [self displayFullScreenWebView: [NSString stringWithFormat:
                                                     @"%@?auth=%@",
                                                     value,             //value is healthurl
                                                     auth]
                                   backgroundColor: [UIColor whiteColor]
                                             title: [NSString stringWithFormat:
                                                     @"%@ %@",
                                                     session.memberInFocus.name,
                                                     session.memberInFocus.dateTime]];

                    return YES;
                }
            }
        }

        dm.currentScene = [scene intValue];

        if (title) // only poke this if we actually have a new title here
        {
            UIViewController *vc = appDel.masterNavigationController.topViewController;

            if ([vc isKindOfClass: [MasterViewController class]])
            {
                MasterViewController *mvc = (MasterViewController *) vc;

                [mvc remotePoke: title];
            }
        }
    }

    return YES;
}

@end
