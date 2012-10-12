//
//  SetListSingleViewController.h
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GeneralSetListViewController.h"


@interface SetListSingleViewController : GeneralSetListViewController {


}

-(id) initWithPlist:(NSString *)path  name:(NSString *) namex;

-(id) initWithLockedPlist:(NSString *)path  name:(NSString *) namex;


@end
