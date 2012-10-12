//
//  MemberListCell.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "BasicListCell.h"

@class Member;

@interface MemberListCell : BasicListCell
{
@private

    Member *member_;
    //
    // Flags:
    //
    BOOL    showsCustom_;
    BOOL    showsPhoto_;
}

@property (nonatomic, retain, readonly)  UILabel        *commentLabel;
@property (nonatomic, retain, readonly)  UILabel        *customLabel;
@property (nonatomic, retain, readonly)  UILabel        *dateTimeLabel;
@property (nonatomic, retain, readwrite) Member         *member;
@property (nonatomic, retain, readonly)  UILabel        *nameLabel;
@property (nonatomic, retain, readonly)  AsyncImageView *photoImageView;
@property (nonatomic, assign, readwrite) BOOL            showsCustom;
@property (nonatomic, assign, readwrite) BOOL            showsPhoto;

@end
