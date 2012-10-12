//
//  MCPatientID.h
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MCPatientID : NSObject
{
@private

    NSString *dateOfBirth_;
    NSString *firstName_;
    NSString *gender_;
    NSString *lastName_;
    NSString *name_;
}

@property (nonatomic, readonly) NSString *dateOfBirth;
@property (nonatomic, readonly) NSString *firstName;
@property (nonatomic, readonly) NSString *gender;
@property (nonatomic, readonly) NSString *lastName;
@property (nonatomic, readonly) NSString *name;

+ (id) patientIDWithFirstName: (NSString *) firstName
                     lastName: (NSString *) lastName
                       gender: (NSString *) gender
                  dateOfBirth: (NSString *) dateOfBirth;

- (id) initWithFirstName: (NSString *) firstName
                lastName: (NSString *) lastName
                  gender: (NSString *) gender
             dateOfBirth: (NSString *) dateOfBirth;

@end
