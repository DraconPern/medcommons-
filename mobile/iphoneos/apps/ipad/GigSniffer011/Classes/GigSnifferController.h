//
//  GigSnifferController.h
//  Nayberz
//
//  Created by Joe Conway on 7/27/09.
//  Copyright 2009 Big Nerd Ranch. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface GigSnifferController : UITableViewController 
	<NSNetServiceDelegate, NSNetServiceBrowserDelegate> 
{
    NSMutableArray *netServices; 
    NSNetServiceBrowser *serviceBrowser; 

}

@end
