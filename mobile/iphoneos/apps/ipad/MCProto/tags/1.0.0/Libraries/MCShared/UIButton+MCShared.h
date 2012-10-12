//
//  UIButton+MCShared.h
//  MCShared
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface UIButton (MCShared)

+ (id) buttonWithTitle: (NSString *) title
                target: (id) target
              selector: (SEL) selector
                 frame: (CGRect) frame
                   tag: (NSInteger) tag;

@end
