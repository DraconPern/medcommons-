//
//  CCRToolCell.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AQGridViewCell.h"  // <AQGridView/AQGridViewCell.h>

@class CCRButton;

@interface CCRToolCell : AQGridViewCell
{
@private

    SEL        action_;
    CCRButton *button_;
    id         target_;
}

@property (nonatomic, assign, readwrite) SEL        action;
@property (nonatomic, retain, readonly)  CCRButton *button;
@property (nonatomic, copy,   readonly)  NSString  *identifier;
@property (nonatomic, retain, readwrite) UIImage   *image;       // or imageName ???
@property (nonatomic, copy,   readwrite) NSString  *jsEvent;
@property (nonatomic, copy,   readwrite) NSString  *jsParameter;
@property (nonatomic, assign, readwrite) id         target;

+ (CGSize) preferredSize;

- (id) initWithIdentifier: (NSString *) identifier;

@end
