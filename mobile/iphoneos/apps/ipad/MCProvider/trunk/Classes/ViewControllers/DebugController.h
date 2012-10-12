//
//  DebugController.h
//  MCProvider
//
//  Created by Bill Donner on 4/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DebugController : UIViewController
{
@private

    UILabel    *bottomLabel_;
    UITextView *bottomTextView_;
    UILabel    *topLabel_;
    UITextView *topTextView_;
    UIView     *traceView_;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@end
