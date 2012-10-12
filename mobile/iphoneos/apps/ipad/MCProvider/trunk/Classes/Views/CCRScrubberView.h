//
//  CCRScrubberView.h
//  MCProvider
//
//  Created by J. G. Pusey on 9/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@interface CCRScrubberView : MCSliderView
{
@private

    UILabel *label_;
}

- (void) resetValues;

@end
