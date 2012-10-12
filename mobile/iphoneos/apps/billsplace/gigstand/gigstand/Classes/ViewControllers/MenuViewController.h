//
//  MenuViewController.h
//  gigstand
//
//  Created by bill donner on 4/1/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface MenuViewController : UIViewController
{
    @private
        NSMutableArray *listItems;
        NSArray *alistItems;
        UIView *mainTableView;
        UIViewController *homeController;
    NSUInteger dismissalMode;
    
    
    NSUInteger ltunecount;
    NSUInteger licount;
    NSUInteger aicount;
    
        
    }

- (id) initWithHomeController:(UIViewController *) pvc mode:(NSUInteger) mode;
    @end