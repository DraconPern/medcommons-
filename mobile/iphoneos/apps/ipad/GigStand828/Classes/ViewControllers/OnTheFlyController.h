//
//  OnTheFlyController.h
//  GigStand
//
//  Created by bill donner on 2/17/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "SmallArchiveViewController.h"

@interface OnTheFlyController : SmallArchiveViewController<UIActionSheetDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate> {
	
	UIActionSheet *toass;
	UIPopoverController *popoverController;
}
-(id) init ;
@end
