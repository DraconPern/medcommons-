//
//  ViewSourceController.h
//  MusicStand
//
//  Created by bill donner on 10/12/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ViewSourceController : UIViewController {
	@private
	NSString *path_;

}

-(id) initWithPath:(NSString *)path;
@end
