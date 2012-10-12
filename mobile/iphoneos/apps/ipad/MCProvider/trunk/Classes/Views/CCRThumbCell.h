//
//  CCRThumbCell.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AQGridViewCell.h"  // <AQGridView/AQGridViewCell.h>

@class AsyncImageView;

@interface CCRThumbCell : AQGridViewCell
{
@private

    SEL             action_;
    AsyncImageView *asyncImageView_;
    NSString       *identifier_;
    NSString       *jsEvent_;
    NSString       *jsParameter_;
    UILabel        *subtitleLabel_;
    id              target_;
    UILabel        *titleLabel_;
}

@property (nonatomic, assign, readwrite) SEL       action;
@property (nonatomic, copy,   readonly)  NSString *identifier;
@property (nonatomic, copy,   readwrite) NSString *jsEvent;
@property (nonatomic, copy,   readwrite) NSString *jsParameter;
@property (nonatomic, copy,   readwrite) NSString *subtitle;
@property (nonatomic, assign, readwrite) id        target;
@property (nonatomic, copy,   readwrite) NSString *title;

+ (CGSize) preferredSize;

- (void) activate;

- (void) deactivate;

- (id) initWithIdentifier: (NSString *) identifier;

- (void) loadImageFromURL: (NSURL *) URL;

@end
