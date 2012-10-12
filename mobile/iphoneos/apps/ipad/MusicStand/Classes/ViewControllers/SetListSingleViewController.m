    //
//  SetListSingleViewController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SetListSingleViewController.h"
#import "DataManager.h"
#import "DataStore.h"

@implementation SetListSingleViewController

-(id) initWithPlist:(NSString *)path  name:(NSString *) namex;
{	
	NSMutableArray *localitems= [[[DataManager sharedInstance] allocLoadRefNodeItems:path] autorelease] ; // was complaining of leak here but autorelease seems to crash it
	self = [super initWithList:localitems  name:namex plist:path canEdit:YES canReorder:YES tag:1];
	if (self)
	{
	}

	return self;
}
-(id) initWithLockedPlist:(NSString *)path  name:(NSString *) namex;
{	
	NSMutableArray *localitems= [[[DataManager sharedInstance] allocLoadRefNodeItems:path] autorelease]; // was complaining of leak here but autorelease seems to crash it
	self = [super initWithList:localitems  name:namex plist:path canEdit:NO canReorder:NO tag:1];
	if (self)
	{
	}
	
	return self;
}
-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeRefNodeItems: self->listItems toPropertyList:self->plist];
	
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

@end
