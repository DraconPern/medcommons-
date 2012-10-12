//
//  SetListViewController.h
//  MusicStand
//
//  Created by bill donner on 10/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SetListViewController : UIViewController {
	
	NSMutableArray *sections;
	NSString *name;

}

-(id) initWithList: (NSMutableArray *)arr name: (NSString *) name;
@end
