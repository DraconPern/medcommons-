//
//  NSString+MCShared.m
//  MCShared
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <CoreFoundation/CoreFoundation.h>

#import "NSString+MCShared.h"

@implementation NSString (MCShared)

- (NSString *) stringByURLDecoding
{
    //
    // Replace each plus sign ('+') with a space character (' '):
    //
    NSString *tmpString = [self stringByReplacingOccurrencesOfString: @"+"
                                                          withString: @" "];

    assert (tmpString != nil);

    //
    // Replace all percent escape sequence ("%xy") with the matching characters
    // using UTF-8 encoding:
    //
    return [tmpString stringByReplacingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
}

- (NSString *) stringByURLEncoding
{
    //
    // According to the Javadoc for java.net.URLEncoder:
    //
    //  * The alphanumeric characters "a" through "z", "A" through "Z" and "0"
    //    through "9" remain the same.
    //
    //  * The special characters ".", "-", "*", and "_" remain the same.
    //
    //  * The space character " " is converted into a plus sign "+".
    //
    //  * All other characters are unsafe and are first converted into one or
    //    more bytes using some encoding scheme. Then each byte is represented
    //    by the 3-character string "%xy", where xy is the two-digit
    //    hexadecimal representation of the byte. The recommended encoding
    //    scheme to use is UTF-8.
    //
    // By default, it seems that CFURLCreateStringByAddingPercentEscapes does
    // NOT escape several "legal" characters that really should be; in
    // addition, it DOES escape space characters which should be left alone
    // so that they can be converted to plus signs afterward:
    //
    static CFStringRef legalCharsToBeEscaped = CFSTR (",;:!?'()@/&+=$");
    static CFStringRef charsToLeaveUnescaped = CFSTR ("\x20");

    NSString *tmpString = [(NSString *)
                           CFURLCreateStringByAddingPercentEscapes (kCFAllocatorDefault,
                                                                    (CFStringRef) self,
                                                                    charsToLeaveUnescaped,
                                                                    legalCharsToBeEscaped,
                                                                    kCFStringEncodingUTF8)
                           autorelease];

    assert (tmpString != nil);

    //
    // Replace each space character (' ') with a plus sign ('+'):
    //
    return [tmpString stringByReplacingOccurrencesOfString: @" "
                                                withString: @"+"];
}

@end
