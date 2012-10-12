//
//  MCSOAPNotesListView.h
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "MCSOAPNotesProvider.h";

@interface MCSOAPNotesListView : UIView
{
@private

    UIView              *backgroundView_;
    UITableView         *contentView_;
    UIView              *footerView_;
    UIView              *headerView_;
}

@property (nonatomic, readonly) UITableView *contentView;

@end
