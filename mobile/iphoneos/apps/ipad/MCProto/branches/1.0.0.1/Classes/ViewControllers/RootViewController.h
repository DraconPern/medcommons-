//
//  RootViewController.h
//  MedPad
//
//  Created by bill donner on 2/24/10.
//  Copyright Apple Inc 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DetailViewController;

@interface RootViewController : UITableViewController
{
    //JGP    DetailViewController *detailViewController;
    IBOutlet UIButton *safariButton;

}

@property (nonatomic, retain) IBOutlet UIButton *safariButton;
//JGP   @property (nonatomic, retain) IBOutlet DetailViewController *detailViewController;

- (void) remotePoke: (NSString *) tit;

@end
