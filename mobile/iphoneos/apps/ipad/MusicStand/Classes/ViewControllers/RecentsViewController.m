
#import "RecentsViewController.h"
#import "DataManager.h"



@implementation RecentsViewController


-(id) init
{ 
	self = [super initWithLockedPlist:@"recents" name:@"GigStand: History" ];
	return self;
}


-(void) leaveEditMode;
{
	[super leaveEditMode];
	[DataManager writeRecents];
}

@end
