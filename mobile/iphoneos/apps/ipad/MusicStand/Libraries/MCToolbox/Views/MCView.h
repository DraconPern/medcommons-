//
//  MCView.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCView : UIView
{
@private

    id userInfo_;
}

@property (nonatomic, retain, readwrite) id userInfo;

@end
