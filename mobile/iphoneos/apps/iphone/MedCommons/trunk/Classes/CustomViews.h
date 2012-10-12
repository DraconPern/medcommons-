//
//  customViews.h
//  MedCommons
//
//  Created by bill donner on 11/4/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//
// this class is subclassed to include pieces of custom code for a particular app variant
//
//


@class PatientStore;
//@interface CustomViews : NSObject {
//
//}
//@end

@protocol customProtocol
-(void) customMainPortraitControllerFieldReload;
-(void) customMainViewConfirmUpload:	(UIAlertView *) baseAlert delegate:(id)delegate;
-(NSString *) customMainPanningTitle;
-(void)customMainViewResetPrefs;
-(void) customMainLandscapeLayout;
- (NSString *)customMainViewUploadMetaString: (NSTimeInterval ) today ;
-(UIImageView *)customMainFullPagePhotoView;
-(UIView *)customMainFullPageLabelView;
-(void) customMainFullPageScreenUpdate;
-(UIView *)customHistoryFullPageControllerLabelView;
-(UIImageView *)customHistoryFullPageControllerPhotoView;
- (UIView *) customMainLandscapeLoadView;
-(void) customMainPortaitControllerStoreTextData:(UITextField *)textField;
- (UIView *) customMainPortaitControllerLoadView:(id)delegate;
-(NSDictionary *)customEmailVoucherBodyWithPIN :(NSString *)title andName:(NSString *)name 
										  andID:(NSString *)voucherid andPin:(NSString *)pin andPath:(NSString *)path;
-(NSDictionary *)customEmailVoucherBodyWithoutPIN :(NSString *)title andName:(NSString *)name andID:(NSString *)voucherid
										   andPath:(NSString *)path;
@end


