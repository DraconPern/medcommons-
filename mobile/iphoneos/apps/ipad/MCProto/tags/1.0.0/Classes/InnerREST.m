//
//  InnerREST.m
//  MedCommons
//
//  Created by bill donner on 4/1/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "InnerREST.h"
#import "JSON.h"

@implementation InnerREST

- (void) dealloc
{
    [super dealloc];
}

- (id) init
{
    self = [super init];
    return self;
}

- (NSString *) postIt: (NSString *) request
         whichService: (NSString *) whichservice
     withBitsFromPath: (NSString *) uniquePath
{
    LLC_LOG (@"*********HTTP POST application/x-www-form-urlencoded %@%@**",
             whichservice,
             request);

    const char    *utfRequest = [request UTF8String];
    NSError       *error;
    NSMutableData *mdata;
    NSData        *data;

    mdata = [NSMutableData dataWithBytes: utfRequest
                                  length: strlen (utfRequest)];

    if (uniquePath != nil)
    {
        //LLC_LOG (@"*********POSTING TO %@ %@**", whichservice, request);

        data = [NSData dataWithContentsOfFile: uniquePath
                                      options: 0
                                        error: &error];

        if (data == nil)
        {
            // an error occurred
            LLC_LOG (@"Error reading %@ \n  -- not posted %d %@",
                     uniquePath,
                     error,
                     [error localizedFailureReason]);
        }
        else
            [mdata appendData: data];   // add bits to end of it
    }

    NSMutableURLRequest *post = [NSMutableURLRequest requestWithURL: [NSURL URLWithString: whichservice]];

    [post addValue: @"application/x-www-form-urlencoded"
forHTTPHeaderField: @"Content-Type"];
    [post setHTTPMethod: @"POST"];
    [post setHTTPBody: mdata];

    NSURLResponse *response;
    BOOL           old = [UIApplication sharedApplication].networkActivityIndicatorVisible; // turn this on then restore at bottom

    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;

    NSData   *result = [NSURLConnection sendSynchronousRequest: post
                                             returningResponse: &response
                                                         error: &error];
    NSString *rets = [[[NSString alloc] initWithData: result
                                            encoding: NSUTF8StringEncoding]
                      autorelease]; //??

    [UIApplication sharedApplication].networkActivityIndicatorVisible = old;

    NSLog (@"Payload up %d bytes response  %d \n",
           [mdata length],
           [rets length]);

    return rets;
}

- (NSDictionary *) postGenericRequestWithJSONResponse: (NSString *) request
                                            toService: (NSString *) service
{
#if 0
    NSLog (@"*** Posting request to service %@ ****", service);
    NSLog (@"----- BEGIN REQUEST DATA -----");
    NSLog (@"%@", request);
    NSLog (@"------ END REQUEST DATA ------");
#endif

    NSString     *lastPostResponseLine = [self postIt: request
                                         whichService: service
                                     withBitsFromPath: nil];

#if 0
    NSLog (@"*** Received response ****");
    NSLog (@"----- BEGIN RESPONSE DATA -----");
    NSLog (@"%@", lastPostResponseLine);
    NSLog (@"------ END RESPONSE DATA ------");
#endif

    //parse JSON returned from remote server
    SBJSON       *parser = [[SBJSON alloc] init];
    NSDictionary *jdict = [parser objectWithString: lastPostResponseLine
                                             error: nil];
    NSString     *status = [jdict objectForKey: @"status"];

    if (![@"ok" isEqualToString: status])
        LLC_LOG (@"postGenericRequestWithJSONResponse bad status %@ %@",
                 status,
                 request);

    //LLC_LOG (@"--- Dictionary in PostBits %@ ", jdict);

    [parser release];

    return jdict;
}

@end
