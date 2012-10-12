//
//  MCCustomView.h
//  MedCommons
//
//  Created by bill donner on 11/11/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//
#import "CustomViews.h"


@interface MCCustomViews : NSObject <customProtocol> {

        NSInteger version;

        UILabel *labelsubjectLongName;
        UILabel *labelsender;
        UILabel *labelcomment;
    UILabel *labelseries;
        UITextField *textFieldSubjectFirstName,*textFieldSubjectLastName,*textFieldSubjectDOB,*textFieldSender;
        UITextField *textFieldComment,*textFieldSeries; // cant update on modal page
        UILabel *labelSubjectFirstName,*labelSubjectLastName, *labelSubjectDOB,*labelSender,*labelComment,*labelSeries  ;
        UILabel *pageNumberLabel;
        int pageNumber;
    }


    @end
