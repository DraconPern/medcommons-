//
//  MCComms.m
//  MedCommons
//
//  Created by bill donner on 4/1/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "MCComms.h"
#import "NSString+MCShared.h";

@implementation MCComms

static MCComms *_sharedInstance;

- (id) init
{
    if (self = [super init])
    {

        udid_ = [[[UIDevice currentDevice] uniqueIdentifier] retain];
        auth_ = @"0";
        appliance_ = @"";
    }

    return self;
}

+ (MCComms *) sharedInstance
{
    if (!_sharedInstance)
        _sharedInstance = [[MCComms alloc] init];

    return _sharedInstance;
}

- (BOOL) isLoggedIn
{
    return ([appliance_ length] > 0);
}

//
// connect to MedCommons, get back auth and a list of Groups
//
- (NSDictionary *) makeSecureConnectionWithAppliance: (NSString *) appliance
                                               email: (NSString *) email
                                            password: (NSString *) password
{
    //
    // try to log on and then validate the response
    //
    NSString     *request = [NSString stringWithFormat:
                             @"uid=%@&email=%@&password=%@",
                             udid_,
                             [email stringByURLEncoding],
                             [password stringByURLEncoding]];
    NSString     *service = [NSString stringWithFormat:
                             @"http:/%@/%@",
                             appliance,
                             @"acct/grservice.php"];
    NSDictionary *response =  [[MCComms sharedInstance] postGenericRequestWithJSONResponse: request
                                                                                 toService: service];
    NSString *status = [response objectForKey: @"status"];

    if ([@"ok" isEqualToString: status])
    {
        NSDictionary *result = [response objectForKey: @"result"];

        if (![result objectForKey: @"auth"])
            auth_ = @"0";
        else
            auth_ = [[result objectForKey: @"auth"] copy];  // copy this out it will be needed for all the other calls

        appliance_ = [appliance copy];
    }
    else
    {
        auth_ = @"0";
        appliance_ = @"";
    }

    return response;
}

//
// to place a signed order, a string of "&name1=value1&name2=value2...." encodes all the fields
//-(NSDictionary *) addOrder: (NSString *) formfields
//{
//  return nil;
//}

//
// Orders go on to these customized patientlists
//
- (NSDictionary *) getGroupPatientList: (NSString *) groupID
{
    NSString *request = [NSString stringWithFormat:
                         @"auth=%@&uid=%@&groupid=%@",
                         auth_,
                         udid_,
                         [groupID stringByURLEncoding]];
    NSString *service = [NSString stringWithFormat:
                         @"http:/%@/%@",
                         appliance_,
                         @"acct/mfserviceauth.php"];

    return [[MCComms sharedInstance] postGenericRequestWithJSONResponse: request
                                                              toService: service];
}
- (NSDictionary *) getSoloPatientList: (NSString *) groupID
                           providerID: (NSString *) providerID
{
    NSString *request = [NSString stringWithFormat:
                         @"auth=%@&uid=%@&groupid=%@&providerid=%@",
                         auth_,
                         udid_,
                         [groupID stringByURLEncoding],
                         [providerID stringByURLEncoding]];
    NSString *service = [NSString stringWithFormat:
                         @"http:/%@/%@",
                         appliance_,
                         @"acct/mfserviceauth.php"];

    return [[MCComms sharedInstance] postGenericRequestWithJSONResponse: request
                                                              toService: service];
}

//
// blurts are a generalization of SOAP notes, who cares for now where these are actually stored?
//
- (NSDictionary *) getBlurts
{
    NSString *request = [NSString stringWithFormat:
                         @"auth=%@&uid=%@",
                         auth_,
                         udid_];
    NSString *service = [NSString stringWithFormat:
                         @"http:/%@/%@",
                         appliance_,
                         @"acct/getBlurts.php"];

    return [[MCComms sharedInstance] postGenericRequestWithJSONResponse: request
                                                              toService: service];
}

- (NSDictionary *) addBlurt: (NSDictionary *) blurtDict
{
    //
    // the blurt itself is encoded as key value pairs
    //
    NSMutableString *blurt = [NSMutableString stringWithCapacity: 5];

    for (id key in blurtDict)
        [blurt appendFormat:
         @"&%@=%@",
         [(NSString *) key stringByURLEncoding],
         [(NSString *) [blurtDict objectForKey: key] stringByURLEncoding]];

    NSString *request = [NSString stringWithFormat:
                         @"auth=%@&uid=%@&%@",
                         auth_,
                         udid_,
                         blurt];
    NSString *service = [NSString stringWithFormat:
                         @"http:/%@/%@",
                         appliance_,
                         @"acct/newBlurt.php"];

    return [[MCComms sharedInstance] postGenericRequestWithJSONResponse: request
                                                              toService: service];
}

@end
