//
//  MCCustomView.h
//  MCProvider
//
//  Created by Bill Donner on 11/11/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//
#import "CustomViews.h"

@interface MCCustomView : NSObject <CustomProtocol>
{
@private

    UILabel     *commentLabel_;
//    UITextField *commentTextField_;
//    NSInteger    pageNumber_;
    UILabel     *pageNumberLabel_;
    UILabel     *senderLabel_;
//    UITextField *senderTextField_;
    UILabel     *seriesLabel_;
//    UITextField *seriesTextField_;
//    UILabel     *subjectDOBLabel_;
//    UITextField *subjectDOBTextField_;
//    UILabel     *subjectFirstNameLabel_;
//    UITextField *subjectFirstNameTextField_;
    UILabel     *subjectFullNameLabel_;
//    UILabel     *subjectLastNameLabel_;
//    UITextField *subjectLastNameTextField_;
    //NSInteger    version_;
}

@end
