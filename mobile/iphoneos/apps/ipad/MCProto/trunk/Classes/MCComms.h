//
//  MCComms.h
//  MedCommons
//
//  Created by bill donner on 4/1/10.
//  Copyright 2010 MEDCOMMONS. All rights reserved.
//


#import "InnerREST.h";

// inherits from low level bit moving library, could possibly get decoupled, do we care?

//
// to make this dirt simple for everyone, all the JSON work is done inside this class
//  every routine returns a dictionary
//
@interface MCComms : InnerREST
{
    NSString *auth_;        // keep this around, if nil then must get a new one from MedCommons
    NSString *appliance_;   // which MedCommons appliance we are talking to is hard to switch
    NSString *udid_;        // known as uid on the server side
}

+ (MCComms *) sharedInstance;

- (BOOL) isLoggedIn;    // make this into a property?

//
// connect to MedCommons, get back auth and a list of Groups
//
- (NSDictionary *) makeSecureConnectionWithAppliance: (NSString *) appliance
                                               email: (NSString *) email
                                            password: (NSString *) password;

//
// Orders go on to these customized patientlists
//
- (NSDictionary *) getGroupPatientList: (NSString *) groupID;

- (NSDictionary *) getSoloPatientList: (NSString *) groupID
                           providerID: (NSString *) providerID;

//
// blurts are a generalization of SOAP notes, who cares for now where these are actually stored?
//
- (NSDictionary *) getBlurts;

- (NSDictionary *) addBlurt: (NSDictionary *) blurt;

@end
