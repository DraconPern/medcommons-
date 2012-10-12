//
//  RadioButtonChooserControl.m
//  GigStand
//
//  Created by bill donner on 1/2/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "RadioButtonChooserControl.h"
#import "DataManager.h"
#import "TunesManager.h"
#import "ArchivesManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"
@implementation RadioButtonChooserControl


#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)aSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	if (aSheet.tag == 222) // this is invoked from the big plus sign
	{
		{
			if (buttonIndex > 0)
			{
				NSString *longpath = [self->myvariants objectAtIndex:buttonIndex-1];

				[self->viewController performSelector:self->completionAction 
										   withObject:longpath]; 
			}
		}
	}	
}

#pragma mark UISegmentedControl action 
-(void) segmentedControlPushed;
{
	// one of the segmented buttons was pushed
	NSUInteger i = self->segmentedControl.selectedSegmentIndex;
	
	if ((i==self->maxbuttons-1)&&self->actionSheet)
	{
		[self->actionSheet showInView:self->viewController.view];
		return; 
	}
	
	// turn segment index into a proper archive index
	
	NSString *longpath = [self->myvariants objectAtIndex:i];
	[self->viewController performSelector:self->completionAction withObject:longpath]; 
	
}

#pragma mark NSObject overrides

- (id) initWithTitle: (NSString *) title andInstance:(InstanceInfo *)iincoming
		   andAction: (SEL) action andController: (UIViewController *) controller;
{
	// this is a subclass of UIView, so lets figure out the correct framesize and number of items we can hold depending on iPod/iPad and Orientation
	CGRect radiobuttonframe = CGRectZero; 
	
	
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) 
		
	{
		if (UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation) )
		{
			// landscape pad
			radiobuttonframe.size.width = 700;
			maxbuttons = 8;
		}
		else {
			// portrait pad
			
			radiobuttonframe.size.width = 400;
			maxbuttons = 5;
		}
	}	
	else // pods and phones		
	{
		if (UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation) )
		{
			// landscape pod
			
			radiobuttonframe.size.width = 260;
			maxbuttons = 3;
		}
		else {
			// portrait pod
			
			radiobuttonframe.size.width = 140;
			maxbuttons = 2;
		}
	}
	
	radiobuttonframe.size.height = 40;
	
	self=[super initWithFrame:radiobuttonframe];
	if (self)
	{
		self->completionAction = action;
		self->viewController = controller;
		
		TuneInfo *tn = [TunesManager  tuneInfo:title];
		NSArray *variants = [TunesManager allVariantsFromTitle:tn.title];
		//NSLog (@"variants from title %@ are %@",title,variants);
		
		NSUInteger countArchives = [variants count];
		NSUInteger buttoncount = 0;
		self->myvariants = [[NSMutableArray alloc] initWithCapacity:countArchives];
		NSUInteger selectedSegmentIndex =(countArchives<=maxbuttons)? countArchives-1: maxbuttons-1; // assume we are going to hit the max and go to longer list
		NSUInteger upcount = 0;
		for (InstanceInfo *ii in variants) // find ourselves in here
		{ 
			if( ([iincoming.archive isEqualToString:ii.archive]&&[iincoming.filePath isEqualToString:ii.filePath])) //is this us again?
			{
				selectedSegmentIndex = upcount;				
			}
			[self->myvariants addObject: ii];
			upcount++;
		}
		
		NSMutableArray *segmentItems = [[[NSMutableArray alloc]
										 initWithCapacity: maxbuttons]
										autorelease];
		buttoncount = 0;		
		for (InstanceInfo *ii2 in self->myvariants)
		{
			NSString *segmentlabel = [ArchivesManager shortName: ii2.archive];
			[segmentItems addObject	:		 [NSString stringWithFormat:@"  %@  ",
											  segmentlabel
											  ]];
			if(++buttoncount>=maxbuttons) break;
		}
		
		if (countArchives>maxbuttons) 
		{
			NSString *cancelButton =
			(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) ? nil: @"Cancel";
			// if there's too many archives then redo the last button and create an action sheet
			[segmentItems removeLastObject];
			[segmentItems addObject:[NSString stringWithFormat:@" +%d ",countArchives-maxbuttons+1]];
			self->actionSheet = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Choose Tune Variant",@"")
															delegate: self
												   cancelButtonTitle:cancelButton
											  destructiveButtonTitle: nil
												   otherButtonTitles:nil
								 ];
			for (InstanceInfo *ii in variants)
			{
				[self->actionSheet addButtonWithTitle:ii.archive];
			}
			self->actionSheet.tag = 222; // mark this
		}
		
		NSInteger	edgeInset = 6; // make this a little smaller
		
		self->segmentedControl = [[[UISegmentedControl alloc] initWithItems: segmentItems] retain];
		
		CGRect tmpFrame = UIEdgeInsetsInsetRect (radiobuttonframe,
												 UIEdgeInsetsMake (edgeInset,
																   0,
																   edgeInset,
																   0));
		
		NSUInteger maxButtonWidth = 70; // keep this from getting out of control and too wide when we only have a few buttons
		NSUInteger maxFrameWidth = maxButtonWidth * countArchives;
		
		if (tmpFrame.size.width > maxFrameWidth) tmpFrame.size.width = maxFrameWidth; 
		
		self->segmentedControl.frame = tmpFrame; 
		self->segmentedControl.backgroundColor = [UIColor clearColor];
		self->segmentedControl.momentary = YES; // seems broken when set to NO
		self->segmentedControl.enabled = YES;
		self->segmentedControl.segmentedControlStyle = UISegmentedControlStyleBar;
		self->segmentedControl.selectedSegmentIndex = selectedSegmentIndex;
		self->segmentedControl.tintColor = [DataManager applicationColor];
		
		[self->segmentedControl addTarget: self
								   action: @selector (segmentedControlPushed)
						 forControlEvents: UIControlEventValueChanged];
		
		[self addSubview:self->segmentedControl];
	}
	
	return self;
}

-(void) dealloc
{
	[self->myvariants release];
	[self->segmentedControl release];
	if (self->actionSheet) [self->actionSheet release];
	self->actionSheet = nil;
	[super dealloc];
	
}

@end
