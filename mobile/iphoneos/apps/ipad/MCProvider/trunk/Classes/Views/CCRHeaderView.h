//
//  CCRHeaderView.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@class CCRButton;

@interface CCRHeaderView : MCView
{
@private

    CCRButton *documentsButton_;
    CCRButton *episodesButton_;
    UILabel   *subtitleLabel_;
    NSString  *subtitleText_;
    CCRButton *titleButton_;
    NSString  *titleText_;
}

@property (nonatomic, retain, readonly)  CCRButton *documentsButton;
@property (nonatomic, retain, readonly)  CCRButton *episodesButton;
@property (nonatomic, retain, readonly)  UILabel   *subtitleLabel;
@property (nonatomic, retain, readwrite) NSString  *subtitleText;
@property (nonatomic, retain, readonly)  CCRButton *titleButton;
@property (nonatomic, retain, readwrite) NSString  *titleText;

@end
