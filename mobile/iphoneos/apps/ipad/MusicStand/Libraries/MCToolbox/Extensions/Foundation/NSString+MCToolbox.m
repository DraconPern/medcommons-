//
//  NSString+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "NSString+MCToolbox.h"

@implementation NSString (MCToolbox)

+ (NSArray *) allUTIsForMIMEType: (NSString *) MIMEType
{
    return [NSString allUTIsForMIMEType: MIMEType
                        conformingToUTI: nil];
}

+ (NSArray *) allUTIsForMIMEType: (NSString *) MIMEType
                 conformingToUTI: (NSString *) UTI
{
    return [(NSArray *) UTTypeCreateAllIdentifiersForTag (kUTTagClassMIMEType,
                                                          (CFStringRef) MIMEType,
                                                          (CFStringRef) UTI)
            autorelease];
}

+ (NSArray *) allUTIsForPathExtension: (NSString *) pathExt
{
    return [NSString allUTIsForPathExtension: pathExt
                             conformingToUTI: nil];
}

+ (NSArray *) allUTIsForPathExtension: (NSString *) pathExt
                      conformingToUTI: (NSString *) UTI
{
    return [(NSArray *) UTTypeCreateAllIdentifiersForTag (kUTTagClassFilenameExtension,
                                                          (CFStringRef) pathExt,
                                                          (CFStringRef) UTI)
            autorelease];
}

+ (NSString *) IANACharSetNameForStringEncoding: (NSStringEncoding) enc
{
    CFStringEncoding cfEnc = CFStringConvertNSStringEncodingToEncoding (enc);

    return (NSString *) CFStringConvertEncodingToIANACharSetName (cfEnc);
}

+ (NSString *) preferredMIMETypeForUTI: (NSString *) UTI
{
    return [(NSString *) UTTypeCopyPreferredTagWithClass ((CFStringRef) UTI,
                                                          kUTTagClassMIMEType)
            autorelease];
}

+ (NSString *) preferredPathExtensionForUTI: (NSString *) UTI
{
    return [(NSString *) UTTypeCopyPreferredTagWithClass ((CFStringRef) UTI,
                                                          kUTTagClassFilenameExtension)
            autorelease];
}

+ (NSString *) preferredUTIForMIMEType: (NSString *) MIMEType
{
    return [NSString preferredUTIForMIMEType: MIMEType
                             conformingToUTI: nil];
}

+ (NSString *) preferredUTIForMIMEType: (NSString *) MIMEType
                       conformingToUTI: (NSString *) UTI
{
    return [(NSString *) UTTypeCreatePreferredIdentifierForTag (kUTTagClassMIMEType,
                                                                (CFStringRef) MIMEType,
                                                                (CFStringRef) UTI)
            autorelease];
}

+ (NSString *) preferredUTIForPathExtension: (NSString *) pathExt
{
    return [NSString preferredUTIForPathExtension: pathExt
                                  conformingToUTI: nil];
}

+ (NSString *) preferredUTIForPathExtension: (NSString *) pathExt
                            conformingToUTI: (NSString *) UTI
{
    return [(NSString *) UTTypeCreatePreferredIdentifierForTag (kUTTagClassFilenameExtension,
                                                                (CFStringRef) pathExt,
                                                                (CFStringRef) UTI)
            autorelease];
}

+ (NSStringEncoding) stringEncodingForIANACharSetName: (NSString *) name
{
    CFStringEncoding cfEnc = CFStringConvertIANACharSetNameToEncoding ((CFStringRef) name);

    return CFStringConvertEncodingToNSStringEncoding (cfEnc);
}

- (BOOL) conformsToUTI: (NSString *) UTI
{
    return UTTypeConformsTo ((CFStringRef) self,
                             (CFStringRef) UTI);
}

- (BOOL) isEqualToUTI: (NSString *) UTI
{
    return UTTypeEqual ((CFStringRef) self,
                        (CFStringRef) UTI);
}

- (NSString *) stringByTrimmingWhitespace
{
    NSMutableString *tmpString = [[self mutableCopy] autorelease];

    CFStringTrimWhitespace ((CFMutableStringRef) tmpString);

    return tmpString;
}

- (NSString *) stringByURLDecoding
{
    //
    // Replace each plus sign ('+') with a space character (' '):
    //
    NSString *tmpString = [self stringByReplacingOccurrencesOfString: @"+"
                                                          withString: @" "];

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

    //
    // Replace each space character (' ') with a plus sign ('+'):
    //
    return [tmpString stringByReplacingOccurrencesOfString: @" "
                                                withString: @"+"];
}

- (NSString *) UTIDescription
{
    return [(NSString *) UTTypeCopyDescription ((CFStringRef) self)
            autorelease];
}

@end
