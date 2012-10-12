//
//  SetListViewControllers.m
//  MusicStand
//
//  Created by bill donner on 10/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SetListViewControllers.h"
#import "DataManager.h"



@implementation SetList1ViewController
-(id) init
{
	self = [super initWithList:[DataManager sharedInstance].setListOneItems name:@"Set 1" canEdit:YES canReorder:YES tag:1];

	return self;
}
-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeSetList:1];
}
@end
@implementation SetList2ViewController
-(id) init
{
	self = [super initWithList:[DataManager sharedInstance].setListTwoItems name:@"Set 2" canEdit:YES canReorder:YES tag:2];

	return self;
}
-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeSetList:2];
}
@end
@implementation SetList3ViewController
-(id) init
{
	self = [super initWithList:[DataManager sharedInstance].setListThreeItems name:@"Set 3" canEdit:YES canReorder:YES tag:3];

	return self;
}
-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeSetList:3];
}
@end
@implementation SetList4ViewController
-(id) init
{
	self = [super initWithList:[DataManager sharedInstance].setListFourItems name:@"Set 4" canEdit:YES canReorder:YES tag:4];

	return self;
}
-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeSetList:4];
}
@end
