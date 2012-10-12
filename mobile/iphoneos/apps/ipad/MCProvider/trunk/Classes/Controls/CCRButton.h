//
//  CCRButton.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCRButton : UIButton
{
@private

    NSString *identifier_;
    NSString *jsEvent_;
    NSString *jsParameter_;
}

@property (nonatomic, copy, readonly)  NSString *identifier;
@property (nonatomic, copy, readwrite) NSString *jsEvent;
@property (nonatomic, copy, readwrite) NSString *jsParameter;

- (id) initWithIdentifier: (NSString *) identifier;

@end
