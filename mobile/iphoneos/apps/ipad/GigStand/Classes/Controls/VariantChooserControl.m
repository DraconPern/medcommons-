//
//  VariantChooserControl.m
//  GigStand
//
//  Created by bill donner on 3/2/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "VariantChooserControl.h"
#import "DataManager.h"
#import "TunesManager.h"
#import "ArchivesManager.h"
#import "InstanceInfo.h"
#import "TuneInfo.h"

@implementation VariantChooserControl




#pragma mark NSObject overrides
-(NSString *) text
{
	return self->label;
}

- (VariantChooserControl *) initWithTitle: (NSString *) title andInstance:(InstanceInfo *)iincoming;
{
	
	NSLog (@"VCC initWithTitle %@",title);
	
	// this is a subclass of UIView, so lets figure out the correct framesize and number of items we can hold depending on iPod/iPad and Orientation
	CGRect variantLabelFrame = CGRectZero; 
	
	
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) 
		
	{
		if (UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation) )
		{
			// landscape pad
			variantLabelFrame.size.width = 700;
			//maxbuttons = 8;
		}
		else {
			// portrait pad
			
			variantLabelFrame.size.width = 400;
			//maxbuttons = 5;
		}
	}	
	else // pods and phones		
	{
		if (UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation) )
		{
			// landscape pod
			
			variantLabelFrame.size.width = 260;
			//maxbuttons = 3;
		}
		else {
			// portrait pod
			
			variantLabelFrame.size.width = 140;
			//maxbuttons = 2;
		}
	}
	
	variantLabelFrame.size.height = 40;
	self = [super init];
	
	if (self)
	{
		self->currentVariant=0;
		TuneInfo *tn = [TunesManager  tuneInfo:title];
		self->myvariants = [TunesManager allVariantsFromTitle:tn.title];		
		self->label  = [NSString stringWithFormat:@"%d %@/%@",[self->myvariants count],iincoming.archive,iincoming.filePath];

	}
	
	return self;
}

-(void) dealloc
{
		NSLog (@"VCC dealloc %@",self->label);
	[self->myvariants release];
	[self->label release];
	[super dealloc];
	
}

@end
