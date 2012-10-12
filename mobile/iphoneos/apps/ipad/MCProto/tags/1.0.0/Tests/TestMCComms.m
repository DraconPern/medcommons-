//
//  TestMCComms.m
//  MedPad
//
//  Created by bill donner on 4/2/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//
// see webpage http://ci.myhealthespace.com/ordersamples/ for walkthru of MedCommons Services used herein
//
#import "TestMCComms.h"
//#import "TestHarness.h"

#import "MCComms.h"
//#import "AppDelegate.h"
//#import "DetailViewController.h"



@implementation TestMCComms


-(void) testMethod1
{
    TOP_LOG (@"Hit testMethod1 - makeSecureConnectionWith:%@ Email:%@ andPassword %@",
             @"http://portal.medcommons.net/acct/grservice.php",
             @"billdonner@gmail.com",
             @"tester"

             );
    // connect to MedCommons, get back auth and a list of Groups
    //-(NSDictionary *) makeSecureConnectionWith:(NSString *) thisappliance  Email:(NSString *) email andPassword:(NSString *) password


    NSDictionary *ret = [[[MCComms sharedInstance] makeSecureConnectionWithAppliance:@"portal.medcommons.net"
                                                                               email:@"billdonner@gmail.com" password:@"tester"] retain];
    BOTTOM_LOG (@"testMethod1: %@",ret);


}
-(void) testMethod2
{
    TOP_LOG (@"Hit testMethod2 - getGroupPatientList");
    //-(NSDictionary *) getGroupPatientList: (NSString *) groupid
    NSDictionary *ret = [[[MCComms sharedInstance] getGroupPatientList:@"9280310636738829"] retain];
    BOTTOM_LOG (@"testMethod2 returned: %@",ret);

}
-(void) testMethod3
{
    TOP_LOG (@"Hit testMethod3 - getSoloPatientList");
    //-(NSDictionary *) getSoloPatientList: (NSString *) groupid withProviderId: (NSString *) providerid
    NSDictionary *ret = [[[MCComms sharedInstance] getSoloPatientList:@"9280310636738829"  providerID:@"9144955176987013"] retain];
    BOTTOM_LOG (@"testMethod3 returned: %@",ret);


}
-(void) testMethod4
{   TOP_LOG (@"Hit testMethod4 - getBlurts");
    //-(NSDictionary *) getBlurts
    NSDictionary *ret = [[[MCComms sharedInstance] getBlurts] retain];
    BOTTOM_LOG (@"testMethod4 returned: %@",ret);
}
-(void) testMethod5
{
    NSLog (@"Hit testMethod5 - addBlurt");
    //-(NSDictionary *) addBlurt: (NSDictionary *) blurt
    NSDictionary *dict = [[NSDictionary dictionaryWithObjectsAndKeys:@"this is blurtone",@"soapA",
                           @"this is blurttwo",@"soapC",nil] retain];
    NSDictionary *ret = [[[MCComms sharedInstance] addBlurt:dict] retain];
    BOTTOM_LOG (@"testMethod5 returned: %@",ret);
}
-(void) testMethod6
{
    TOP_LOG (@"Hit testMethod6");
}
@end
