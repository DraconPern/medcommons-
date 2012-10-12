//
//  MCCustomView.m
//  MCProvider
//
//  Created by Bill Donner on 11/11/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "MCCustomView.h"
#import "Member.h"
#import "MemberStore.h"
#import "Session.h"
#import "SessionManager.h"

#define COMMENT_LABEL_TAG                  101
#define COMMENT_TEXT_FIELD_TAG             102
#define SENDER_LABEL_TAG                   103
#define SENDER_TEXT_FIELD_TAG              104
#define SERIES_LABEL_TAG                   105
#define SERIES_TEXT_FIELD_TAG              106
#define SUBJECT_DOB_LABEL_TAG              107
#define SUBJECT_DOB_TEXT_FIELD_TAG         108
#define SUBJECT_FAMILY_NAME_LABEL_TAG      111
#define SUBJECT_FAMILY_NAME_TEXT_FIELD_TAG 112
#define SUBJECT_GIVEN_NAME_LABEL_TAG       109
#define SUBJECT_GIVEN_NAME_TEXT_FIELD_TAG  110

#define COMMENT_PREFS_KEY                  @"comment"
#define SENDER_PREFS_KEY                   @"sender"
#define SERIES_PREFS_KEY                   @"series"
#define SUBJECT_DOB_PREFS_KEY              @"dob"
#define SUBJECT_GIVEN_NAME_PREFS_KEY       @"firstname"
#define SUBJECT_FAMILY_NAME_PREFS_KEY      @"lastname"

#define LABEL_STYLE1_HEIGHT                16.0f
#define LABEL_STYLE2_HEIGHT                17.0f
#define LABEL_STYLE3_HEIGHT                17.0f
#define STATUS_BAR_HEIGHT                  20.0f
#define TEXT_FIELD_HEIGHT                  24.0f

@interface MCCustomView ()

+ (UILabel *) labelStyle1WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag;

+ (UILabel *) labelStyle2WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag;

+ (UILabel *) labelStyle3WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag;

+ (UITextField *) textFieldWithText: (NSString *) text
                        placeholder: (NSString *) placeholder
                           delegate: (id <UITextFieldDelegate>) delegate
                              frame: (CGRect) frame
                                tag: (NSInteger) tag;

@end

@implementation MCCustomView

#pragma mark Private Class Methods

+ (UILabel *) labelStyle1WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag
{
    UILabel *label = [[[UILabel alloc] initWithFrame: frame]
                      autorelease];

    label.backgroundColor = [UIColor lightGrayColor];
    label.font = [UIFont fontWithName: @"Arial"
                                 size: 16.0f];
    label.tag = tag;
    label.text = text;
    label.textAlignment = UITextAlignmentCenter;
    label.textColor = [UIColor blackColor];

    return label;
}

+ (UILabel *) labelStyle2WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag
{
    UILabel *label = [[[UILabel alloc] initWithFrame: frame]
                      autorelease];

    label.backgroundColor = [UIColor lightGrayColor];
    label.font = [UIFont fontWithName: @"Arial"
                                 size: 16.0f];
    label.tag = tag;
    label.text = text;
    label.textAlignment = UITextAlignmentLeft;
    label.textColor = [UIColor whiteColor];

    return label;
}

+ (UILabel *) labelStyle3WithText: (NSString *) text
                            frame: (CGRect) frame
                              tag: (NSInteger) tag
{
    UILabel *label = [[[UILabel alloc] initWithFrame: frame]
                      autorelease];

    label.backgroundColor = [UIColor whiteColor];
    label.font = [UIFont fontWithName: @"Arial"
                                 size: 10.0f];
    label.tag = tag;
    label.text = text;
    label.textAlignment = UITextAlignmentRight;
    label.textColor = [UIColor grayColor];

    return label;
}

+ (UITextField *) textFieldWithText: (NSString *) text
                        placeholder: (NSString *) placeholder
                           delegate: (id <UITextFieldDelegate>) delegate
                              frame: (CGRect) frame
                                tag: (NSInteger) tag
{
    UITextField *textField = [[[UITextField alloc] initWithFrame: frame]
                              autorelease];

    textField.autocorrectionType = UITextAutocorrectionTypeNo;
    textField.backgroundColor = [UIColor groupTableViewBackgroundColor];
    textField.borderStyle = UITextBorderStyleRoundedRect;
    textField.clearButtonMode = UITextFieldViewModeWhileEditing;
    textField.delegate = delegate;
    textField.font = [UIFont systemFontOfSize: 14.0f];
    textField.placeholder = placeholder;
    textField.returnKeyType = UIReturnKeyDone;
    textField.tag = tag;
    textField.text = text;
    textField.textColor = [UIColor darkTextColor];

    return textField;
}

#pragma mark CustomProtocol Methods

//- (NSString *) customMainPanningTitle
//{
//    NSMutableDictionary *settings = self.appDelegate.dataManager.memberStore.settings;
//
//    return  [NSString stringWithFormat: @"%@ %@",
//             [settings objectForKey: SUBJECT_GIVEN_NAME_PREFS_KEY],
//             [settings objectForKey: SUBJECT_FAMILY_NAME_PREFS_KEY]];
//}

//- (NSDictionary *) customEmailVoucherBodyWithoutPIN: (NSString *) title
//                                            andName: (NSString *) name
//                                              andID: (NSString *) voucherid
//                                            andPath: (NSString *) path
//{
//    NSLog (@"*** customEmailVoucherBodyWithoutPIN ***");
//
//    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
//
//    [dict setObject: [NSString stringWithFormat:
//                      @"Forensic Media Set from %@",
//                      title]
//             forKey: @"subject"];
//
//    [dict setObject: [NSString stringWithFormat:
//                      @"<p>I saved a forensic media set re %@ using %@</p><p>You can view this series with</p><p> <code>Voucher ID: %@<br/></code><br/>but first please contact me for the additional PIN Code<br/><a href='%@' >go there now</a></p>",
//                      name,
//                      title,
//                      voucherid,
//                      path]
//             forKey: @"body"];
//
//    [dict setObject: MAIN_LOGO
//             forKey: @"name"];
//
//    [dict setObject: @"image/gif"
//             forKey: @"mimetype"];
//
//    [dict setObject: path
//             forKey: @"path"];
//
//    [dict setObject: [NSData dataWithContentsOfFile: [[NSBundle mainBundle] pathForResource: MAIN_LOGO
//                                                                                     ofType: MAIN_LOGO_TYPE]]
//             forKey: @"picdata"];
//
//    return dict;
//}

//- (NSDictionary *) customEmailVoucherBodyWithPIN: (NSString *) title
//                                         andName: (NSString *) name
//                                           andID: (NSString *) voucherid
//                                          andPin: (NSString *) pin
//                                         andPath: (NSString *) path
//{
//    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
//
//    [dict setObject: [NSString stringWithFormat:
//                      @"Forensic Media Set from %@",
//                      title]
//             forKey: @"subject"];
//
//    [dict setObject: [NSString stringWithFormat:
//                      @"<p>I saved a forensic media set re %@ using %@</p><p>You can view this series with -</p><p> <code>Voucher ID: %@<br/>PIN: %@</code><br/><br/><a href='%@' >go there now</a></p>",
//                      name,
//                      title,
//                      voucherid,
//                      pin,
//                      path]
//             forKey: @"body"];
//
//    [dict setObject: MAIN_LOGO
//             forKey: @"name"];
//
//    [dict setObject: @"image/gif"
//             forKey: @"mimetype"];
//
//    [dict setObject: path
//             forKey: @"path"];
//
//    [dict setObject: [NSData dataWithContentsOfFile: [[NSBundle mainBundle] pathForResource: MAIN_LOGO
//                                                                                     ofType: MAIN_LOGO_TYPE]]
//             forKey: @"picdata"];
//
//    return dict;
//}

- (void) updateCustomMainFullPageLabels
{
    NSLog (@"*** updateCustomMainFullPageLabels ***");

//    SessionManager      *sm = self.appDelegate.sessionManager;
//    NSMutableDictionary *info = sm.loginSession.memberInFocus.store.info;
//    NSString            *name = [NSString stringWithFormat: @"%@ %@ %@",
//                                 [info objectForKey: SUBJECT_GIVEN_NAME_PREFS_KEY],
//                                 [info objectForKey: SUBJECT_FAMILY_NAME_PREFS_KEY],
//                                 [info objectForKey: SUBJECT_DOB_PREFS_KEY]];
//
//    if (pageNumber_ == 0)
//        pageNumberLabel_.text = [NSString stringWithFormat:
//                                 @"%@ - Photo",
//                                 name];
//    else
//        pageNumberLabel_.text = [NSString stringWithFormat:
//                                 @"%@ - Part %d",
//                                 name,
//                                 pageNumber_];
}

- (UIView *) newCustomMainFullPageLabelView
{
    NSLog (@"*** newCustomMainFullPageLabelView ***");

    CGRect appFrame = [[UIScreen mainScreen] applicationFrame];

    appFrame.origin.y = 0; //statusheight;

    UIView *view = [[UIView alloc] initWithFrame: appFrame];

    CGRect pageNumberLabelFrame = CGRectMake (10.0f, 53.0f, 300.0f, LABEL_STYLE1_HEIGHT);

    pageNumberLabel_ = [MCCustomView labelStyle1WithText: nil
                                                   frame: pageNumberLabelFrame
                                                     tag: 0];

    [self updateCustomMainFullPageLabels];

    [view addSubview: pageNumberLabel_];

    return view;
}

- (UIImageView *) newCustomMainFullPagePhotoView
{
    NSLog (@"*** newCustomMainFullPagePhotoView ***");

    CGRect photoFrame = CGRectMake (0.0f, 42.0f, 320.0f, 320.0f);

    return [[UIImageView alloc] initWithFrame: photoFrame];
}

- (void) updateCustomMainLandscapeViewLabels
{
    NSLog (@"*** updateCustomMainLandscapeViewLabels ***");

//    SessionManager      *sm = self.appDelegate.sessionManager;
//    NSMutableDictionary *info = sm.loginSession.memberInFocus.store.info;
//
//    seriesLabel_.text = [NSString stringWithFormat:
//                         @"series - %@",
//                         [info objectForKey: SERIES_PREFS_KEY]];
//
//    commentLabel_.text = [NSString stringWithFormat:
//                          @"comment - %@",
//                          [info objectForKey: COMMENT_PREFS_KEY]];
//
//    senderLabel_.text = [NSString stringWithFormat:
//                         @"sender - %@",
//                         [info objectForKey: SENDER_PREFS_KEY]];
//
//    subjectFullNameLabel_.text = [NSString stringWithFormat: @"%@ %@ %@",
//                                  [info objectForKey: SUBJECT_GIVEN_NAME_PREFS_KEY],
//                                  [info objectForKey: SUBJECT_FAMILY_NAME_PREFS_KEY],
//                                  [info objectForKey: SUBJECT_DOB_PREFS_KEY]];
}

- (UIView *) newCustomMainLandscapeView
{
    NSLog (@"*** newCustomMainLandscapeView ***");

    CGRect appFrame = [[UIScreen mainScreen] applicationFrame];

    appFrame.origin.y -= STATUS_BAR_HEIGHT;
    appFrame.size.height += STATUS_BAR_HEIGHT;

    //SessionManager      *sm = self.appDelegate.sessionManager;
    NSMutableDictionary *info = nil;    //sm.loginSession.memberInFocus.store.info;
    UIView              *view = [[UIView alloc] initWithFrame: appFrame];

    CGRect subjectFullNameLabelFrame = CGRectMake (110.0f, 24.0f, 265.0f, LABEL_STYLE2_HEIGHT);
    CGRect senderLabelFrame          = CGRectMake (110.0f, 44.0f, 265.0f, LABEL_STYLE2_HEIGHT);
    CGRect commentLabelFrame         = CGRectMake (110.0f, 64.0f, 265.0f, LABEL_STYLE2_HEIGHT);
    CGRect seriesLabelFrame          = CGRectMake (110.0f, 84.0f, 265.0f, LABEL_STYLE2_HEIGHT);

    subjectFullNameLabel_ = [MCCustomView labelStyle2WithText: [NSString stringWithFormat: @"%@ %@ %@",
                                                                [info objectForKey: SUBJECT_GIVEN_NAME_PREFS_KEY],
                                                                [info objectForKey: SUBJECT_FAMILY_NAME_PREFS_KEY],
                                                                [info objectForKey: SUBJECT_DOB_PREFS_KEY]]
                                                        frame: subjectFullNameLabelFrame
                                                          tag: 0];

    senderLabel_ = [MCCustomView labelStyle2WithText: [NSString stringWithFormat:
                                                       @"sender - %@",
                                                       [info objectForKey: SENDER_PREFS_KEY]]
                                               frame: senderLabelFrame
                                                 tag: 0];

    commentLabel_ = [MCCustomView labelStyle2WithText: [NSString stringWithFormat:
                                                        @"comment - %@",
                                                        [info objectForKey: COMMENT_PREFS_KEY]]
                                                frame: commentLabelFrame
                                                  tag: 0];

    seriesLabel_ = [MCCustomView labelStyle2WithText: [NSString stringWithFormat:
                                                       @"series - %@",
                                                       [info objectForKey: SERIES_PREFS_KEY]]
                                               frame: seriesLabelFrame
                                                 tag: 0];

    [view addSubview: subjectFullNameLabel_];
    [view addSubview: senderLabel_];
    [view addSubview: commentLabel_];
    [view addSubview: seriesLabel_];

    return view;
}

- (void) saveCustomMainPortaitFields: (UITextField *) textField
{
    NSLog (@"*** saveCustomMainPortaitFields ***");

//    SessionManager      *sm = self.appDelegate.sessionManager;
//    MemberStore         *mstore = sm.loginSession.memberInFocus.store;
//    NSMutableDictionary *info = mstore.info;
//
//    [mstore beginUpdates];
//
//    switch (textField.tag)
//    {
//        case COMMENT_TEXT_FIELD_TAG :
//            [info setObject: commentTextField_.text
//                     forKey: COMMENT_PREFS_KEY];
//            break;
//
//        case SENDER_TEXT_FIELD_TAG :
//            [info setObject: senderTextField_.text
//                     forKey: SENDER_PREFS_KEY];
//            break;
//
//        case SERIES_TEXT_FIELD_TAG :
//            [info setObject: seriesTextField_.text
//                     forKey: SERIES_PREFS_KEY];
//            break;
//
//        case SUBJECT_DOB_TEXT_FIELD_TAG :
//            [info setObject: subjectDOBTextField_.text
//                     forKey: SUBJECT_DOB_PREFS_KEY];
//            break;
//
//        case SUBJECT_GIVEN_NAME_TEXT_FIELD_TAG :
//            [info setObject: subjectFirstNameTextField_.text
//                     forKey: SUBJECT_GIVEN_NAME_PREFS_KEY];
//            break;
//
//        case SUBJECT_FAMILY_NAME_TEXT_FIELD_TAG :
//            [info setObject: subjectLastNameTextField_.text
//                     forKey: SUBJECT_FAMILY_NAME_PREFS_KEY];
//            break;
//
//        default :
//            return; // nothing to write out ...
//    }
//
//    [mstore endUpdates];
}

#pragma mark needs adjustment

- (void) customMainViewConfirmUpload: (UIAlertView *) baseAlert
                            delegate: (id <UIAlertViewDelegate>) alertViewDelegate
{
    NSLog (@"*** customMainViewConfirmUpload ***");

//    // we are doing the messaging and testing here,
//    // but the action continues back in the delgate which is always the guy who called us
//
//    SessionManager      *sm = self.appDelegate.sessionManager;
//    MemberStore         *mstore = sm.loginSession.memberInFocus.store;
//    NSMutableDictionary *info = mstore.info;
//    NSString            *message;
//
//    if ((@"" == [info objectForKey: SUBJECT_GIVEN_NAME_PREFS_KEY]) &&
//        (@"" == [info objectForKey: SUBJECT_FAMILY_NAME_PREFS_KEY]))
//    {
//        message = @"Please Enter the Subject Name"; //[self promptForName];
//
//        [baseAlert initWithTitle: @"You can't upload without identifying the subject"
//                         message: message
//                        delegate: alertViewDelegate
//               cancelButtonTitle: @"OK"
//               otherButtonTitles: nil];
//    }
//    else if (![mstore hasSubjectPhoto] && ([mstore numberOfPartPhotos] == 0))
//    {
//        message = @"Please Shoot a Photo"; //[self promptForName];
//
//        [baseAlert initWithTitle: @"You can't upload without any photos or Videos"
//                         message: message
//                        delegate: alertViewDelegate
//               cancelButtonTitle: @"OK"
//               otherButtonTitles: nil];
//    }
//    else
//    {
//        if (@"" == [info objectForKey: SUBJECT_DOB_PREFS_KEY])
//            message = @"Preferably you should enter Date of Birth";
//        else if (![mstore hasPartPhotoAtIndex: 0])
//            message = @"You only have a subject photo";
//        else
//            message = @"Your photo series is complete" ;
//
//        [baseAlert initWithTitle: @"Do you really want to upload?"
//                         message: message
//                        delegate: alertViewDelegate
//               cancelButtonTitle: @"Cancel"
//               otherButtonTitles: @"Upload Now", nil];
//    }
}

- (void) customMainViewResetPrefs
{
    NSLog (@"*** customMainViewResetPrefs ***");

//    SessionManager      *sm = self.appDelegate.sessionManager;
//    MemberStore         *mstore = sm.loginSession.memberInFocus.store;
//    NSMutableDictionary *info = mstore.info;
//
//    commentTextField_.text = @"";
//    senderTextField_.text = @"";
//    seriesTextField_.text = @"";
//    subjectDOBTextField_.text = @"";
//    subjectFirstNameTextField_.text = @"";
//    subjectLastNameTextField_.text = @"";
//
//    [mstore beginUpdates];
//
//    [info setObject: @""
//             forKey: COMMENT_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: SENDER_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: SERIES_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: SUBJECT_DOB_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: SUBJECT_GIVEN_NAME_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: SUBJECT_FAMILY_NAME_PREFS_KEY];
//
//    [info setObject: @""
//             forKey: @"working-mcid"];
//
//    [mstore endUpdates];
}

@end
