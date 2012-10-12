//
//  MusicStandController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MusicStandController : UIViewController
{
@private

    NSCalendar      *calendar_;
    NSDateFormatter *dateFormatter_;
    NSMutableArray         *documents_;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;



@end
