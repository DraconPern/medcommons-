//
//  MCSwitchTableViewCell.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/16/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCSwitchTableViewCell : UITableViewCell
{
    UISwitch *switchControl_;
}

@property (nonatomic, retain, readonly) UISwitch *switchControl;

@end
