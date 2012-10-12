//
//  VariantChooserControl.h
//  GigStand
//
//  Created by bill donner on 3/2/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

// uses same real estate and interface as radiobutton chooser, but is much simpler - it cycles thru labels on each touch


#import <UIKit/UIKit.h>

@class InstanceInfo;
@interface VariantChooserControl : NSObject {
	
	
	NSArray *myvariants; // holds all the tune instances
	
	
	NSUInteger currentVariant; // 
	
	NSString *label;
}
-(NSString *) text;
- (VariantChooserControl *) initWithTitle: (NSString *) title andInstance:(InstanceInfo *)iincoming;
@end
