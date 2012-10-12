//
//  MCFullPageController.h
//  MCProvider
//
//  Created by Bill Donner on 1/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MemberStore,CustomViews;

@interface FullPageController : UIViewController
{
@private

    MemberStore *memberStore;
    NSString    *pageNumberLabel;
    NSUInteger   pageNumber;
    CGRect       frame;
    CustomViews *customViews;
}

-(void)screenUpdate: (id)o;

- (id)initWithPageNumber:(NSUInteger)page
            andWithFrame:(CGRect )_frame  ;

@end
