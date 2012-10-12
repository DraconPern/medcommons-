//
//  HistoryFullPageController.h
//  MedCommons
//
//  Created by bill donner on 10/20/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//



@class PatientStore,HistoryCase;
@interface HistoryFullPageController : UIViewController {
	PatientStore *patientStore;
	UILabel *pageNumberLabel;
    int pageNumber;
	HistoryCase *hcase;

	NSString *imageURL;
	CGRect frame;
	NSString *name;
		NSMutableDictionary *imageCache;
}
-(void)screenUpdate: (id)o;
- (id)initWithPageNumber:(int)page andWithImageURL:(NSString *) _ss andWithFrame:(CGRect) _frame andWithName:(NSString *)_name	  andWithHistoryCase:(HistoryCase *)_hcase ;
@end
