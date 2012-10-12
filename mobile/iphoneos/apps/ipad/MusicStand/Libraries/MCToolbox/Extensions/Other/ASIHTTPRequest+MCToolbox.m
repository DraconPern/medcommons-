//
//  ASIHTTPRequest+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "JSON.h"    // <JSON/JSON.h>

#import "ASIHTTPRequest+MCToolbox.h"
#import "NSString+MCToolbox.h"

@interface NSString (MCToolbox_ASIHTTPRequest)

- (NSDictionary *) dictionaryFromJSONFormat;

@end

@implementation NSString (MCToolbox_ASIHTTPRequest)

- (NSDictionary *) dictionaryFromJSONFormat
{
    SBJsonParser *parser = [[[SBJsonParser alloc] init]
                            autorelease];
    id            obj = [parser objectWithString: self];

    return ([obj isKindOfClass: [NSDictionary class]] ? obj : nil);
}

@end

@implementation ASIHTTPRequest (MCToolbox)

- (NSDictionary *) responseDictionary
{
    NSString *MIMEType = [self responseMIMEType];

    if (!MIMEType)
        return nil;

    BOOL isJSON = ([MIMEType caseInsensitiveCompare: @"application/json"] == NSOrderedSame);

    if (!isJSON)
    {
        NSArray *components = [MIMEType componentsSeparatedByString: @"/"];

        if ([[components objectAtIndex: 0] caseInsensitiveCompare: @"text"] != NSOrderedSame)
            return nil;
    }

    //
    // If here, response is either JSON format or some flavor of text:
    //
    NSString *strValue = [self responseString];

    if (!strValue)
        return nil;

    //
    // Always try parsing string from JSON format first:
    //
    NSDictionary *tmpValue = [strValue dictionaryFromJSONFormat];

    //
    // If it parsed successfully return; otherwise if response MIME type
    // explicitly claims JSON format return anyway:
    //
    if (tmpValue || isJSON)
        return tmpValue;

    //
    // Try parsing string from property list:
    //
    @try
    {
        id tmpObj = [strValue propertyList];

        if ([tmpObj isKindOfClass: [NSDictionary class]])
            return (NSDictionary *) tmpObj;
    }
    @catch (NSException *exc)
    {
        // keep going ...
    }

    //
    // Finally, try parsing string from property list in '.strings' file
    // format:
    //
    @try
    {
        return [strValue propertyListFromStringsFileFormat];
    }
    @catch (NSException *exc)
    {
        return nil;
    }
}

- (NSString *) responseMIMEType
{
    NSString *contentType = [[self responseHeaders] objectForKey: @"Content-Type"];

    if (contentType)
    {
        NSScanner *tmpScanner = [NSScanner scannerWithString: contentType];
        NSString  *MIMEType = nil;

        if ([tmpScanner scanUpToString: @";" intoString: &MIMEType])
            return [MIMEType stringByTrimmingWhitespace];
    }

    return nil;
}

@end
