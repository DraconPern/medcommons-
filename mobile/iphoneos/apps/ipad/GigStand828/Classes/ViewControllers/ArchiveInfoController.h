//
//  SongsViewController.h
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ArchiveInfoController.h";

@interface ArchiveInfoController : UIViewController {
	NSString *archive;
	NSString *s;
	NSDictionary *attrs;
	UITableView *mainTableView;
	UIWebView *mainWebView;
	UIImageView *mainImgView;
}

-(id) initWithArchive: (NSString *)archive ;
@end
