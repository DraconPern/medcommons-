//
//  GigSnifferController.h
//  Nayberz
//
//  Created by Joe Conway on 7/27/09.
//  Copyright 2009 Big Nerd Ranch. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <MessageUI/MFMailComposeViewController.h>
#import <MessageUI/MessageUI.h>

@interface GigSnifferController : UITableViewController 
	<NSNetServiceDelegate, NSNetServiceBrowserDelegate, UIActionSheetDelegate,MFMailComposeViewControllerDelegate,MFMessageComposeViewControllerDelegate> 
{
    NSMutableArray *netServices; 
    NSNetServiceBrowser *serviceBrowser;
	UIActionSheet *toass;
	MFMailComposeViewController *controller;
	MFMessageComposeViewController *scontroller;

}

@end
