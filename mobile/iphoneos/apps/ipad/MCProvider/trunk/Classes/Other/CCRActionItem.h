//
//  CCRActionItem.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CCRActionItem : NSObject
{
@private

    SEL       action_;
    NSString *identifier_;
    NSString *jsEvent_;
    NSString *jsParameter_;
    id        target_;
    NSString *title_;
}

@property (nonatomic, assign, readwrite) SEL       action;
@property (nonatomic, copy,   readonly)  NSString *identifier;
@property (nonatomic, copy,   readwrite) NSString *jsEvent;
@property (nonatomic, copy,   readwrite) NSString *jsParameter;
@property (nonatomic, assign, readwrite) id        target;
@property (nonatomic, copy,   readwrite) NSString *title;

- (id) initWithIdentifier: (NSString *) identifier;

- (void) performAction;

@end
