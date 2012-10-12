//
//  MCCustomView.h
//  MedCommons
//
//  Created by bill donner on 11/11/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//
#import "CustomViews.h"
	@class PatientStore;

@interface MCCustomViews : NSObject <customProtocol> {

		NSInteger version;
		
		UILabel *labelsubjectLongName;
		UILabel *labelsender;
		UILabel *labelcomment;
		UILabel *labelseries;
		UITextField *textFieldSubjectFirstName,*textFieldSubjectLastName,*textFieldSubjectDOB,*textFieldSender;
		UITextField *textFieldComment,*textFieldSeries; // cant update on modal page
		UILabel *labelSubjectFirstName,*labelSubjectLastName, *labelSubjectDOB,*labelSender,*labelComment,*labelSeries	;
		UILabel *pageNumberLabel;
		int pageNumber;
	}
//	-(void) customMainViewConfirmUpload:	(UIAlertView *) baseAlert withPatientStore: (PatientStore *) patientStore delegate:(id)delegate;
//	-(void) customLoadTestCase:(PatientStore *)patientStore;
//	-(NSString *) customMainPanningTitle;
//	-(void)customMainViewResetPrefs;
//	- (void) customMainLandscapeLayout;
//	- (NSString *)customMainViewUploadMetaString: (NSTimeInterval ) today;
//	-(UIImageView *)customMainFullPagePhotoView;
//	-(UIView *)customMainFullPageLabelView;
//	-(void) customMainFullPageScreenUpdate;
//	-(UIView *)customHistoryFullPageControllerLabelView;
//	-(UIImageView *)customHistoryFullPageControllerPhotoView;
//	- (UIView *) customMainLandscapeLoadView;
//	-(void) customMainPortaitControllerStoreTextData:(UITextField *)textField;
//	- (UIView *) customMainPortaitControllerLoadView:(id)delegate;
//	-(NSDictionary *)customEmailVoucherBodyWithPIN :(NSString *)title andName:(NSString *)name 
//andID:(NSString *)voucherid andPin:(NSString *)pin andPath:(NSString *)path;
//	-(NSDictionary *)customEmailVoucherBodyWithoutPIN :(NSString *)title andName:(NSString *)name andID:(NSString *)voucherid
//andPath:(NSString *)path;
	
	@end
