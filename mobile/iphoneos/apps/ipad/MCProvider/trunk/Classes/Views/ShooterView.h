//
//  ShooterView.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@interface ShooterView : MCView
{
@private

    UITextField *commentTextField_;
    UIView      *contentView_;
    UIView      *headerView_;
    UILabel     *subjectDOBLabel_;
    UILabel     *subjectNameLabel_;
    UIImageView *subjectThumbImageView_;
}

@property (nonatomic, retain, readonly) UITextField *commentTextField;

- (NSUInteger) indexOfPartThumbAtLocation: (CGPoint) location;

- (BOOL) isSubjectThumbAtLocation: (CGPoint) location;

- (CGRect) rectForPartThumbAtIndex: (NSUInteger) idx;

- (CGRect) rectForSubjectThumb;

- (void) update;

@end
