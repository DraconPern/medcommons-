//
//  SongsViewController.h
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchableTunesViewController.h"


@interface SongsViewController : SearchableTunesViewController<UIActionSheetDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate> 
{
	UIActionSheet *toass;
	UIPopoverController *popoverController;
}

@end
