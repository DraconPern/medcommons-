//
//  MCTextFieldTableViewCell.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface MCTextFieldTableViewCell : UITableViewCell
{
    UITextField *textField_;
}

@property (nonatomic, retain, readonly) UITextField *textField;

@end
