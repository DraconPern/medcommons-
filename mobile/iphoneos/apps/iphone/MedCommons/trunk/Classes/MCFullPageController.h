//
//  MCFullPageController.h
//  MedCommons
//
//  Created by bill donner on 1/24/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

	@class PatientStore,CustomViews;
	@interface MCFullPageController : UIViewController {
		PatientStore *patientStore;

		NSString *pageNumberLabel;
		NSInteger pageNumber;
		CGRect frame;
		CustomViews *customViews;
	}
	-(void)screenUpdate: (id)o;
	- (id)initWithPageNumber:(int)page   
andWithFrame:(CGRect )_frame  ;
	
	@end
