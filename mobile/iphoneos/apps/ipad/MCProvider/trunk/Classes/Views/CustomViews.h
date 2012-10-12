//
//  CustomViews.h
//  MCProvider
//
//  Created by Bill Donner on 11/4/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//
// these methods can be implemented multiple times to include pieces of custom code for a particular phone type or customer-specific variant
//
//

#import <UIKit/UIKit.h>

@protocol CustomProtocol

//- (NSDictionary *) customEmailVoucherBodyWithPIN: (NSString *) title
//                                         andName: (NSString *) name
//                                           andID: (NSString *) voucherid
//                                          andPin: (NSString *) pin
//                                         andPath: (NSString *) path;

//- (NSDictionary *) customEmailVoucherBodyWithoutPIN: (NSString *) title
//                                            andName: (NSString *) name
//                                              andID: (NSString *) voucherid
//                                            andPath: (NSString *) path;

- (UIView *) newCustomMainFullPageLabelView;

- (UIImageView *) newCustomMainFullPagePhotoView;

- (void) updateCustomMainFullPageLabels;

- (void) updateCustomMainLandscapeViewLabels;

- (UIView *) newCustomMainLandscapeView;

//- (NSString *) customMainPanningTitle;

//- (void) loadCustomMainPortraitTextFields;

//- (UIView *) newCustomMainPortraitView: (id <UITextFieldDelegate>) textFieldDelegate;

- (void) saveCustomMainPortaitFields: (UITextField *) textField;

- (void) customMainViewConfirmUpload: (UIAlertView *) baseAlert
                            delegate: (id <UIAlertViewDelegate>) alertViewDelegate;

- (void) customMainViewResetPrefs;

//- (NSString *) customMainViewUploadMetaString: (NSTimeInterval) today;

@end


