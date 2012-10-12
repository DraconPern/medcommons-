//
//  NSString+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of MedCommons, Inc. nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#import <CoreFoundation/CoreFoundation.h>
#import <MobileCoreServices/MobileCoreServices.h>

#import "NSString+MCToolbox.h"

NSString *NSStringFromBOOL (BOOL value)
{
    return (value ? @"YES" : @"NO");
}

NSString *NSStringFromNSTimeInterval (NSTimeInterval value)
{
    return [NSString stringWithFormat:
            @"%lg",
            value];
}

NSString *NSStringFromUIDeviceOrientation (UIDeviceOrientation value)
{
    switch (value)
    {
        case UIDeviceOrientationLandscapeLeft :
            return @"UIDeviceOrientationLandscapeLeft";

        case UIDeviceOrientationLandscapeRight :
            return @"UIDeviceOrientationLandscapeRight";

        case UIDeviceOrientationPortrait :
            return @"UIDeviceOrientationPortrait";

        case UIDeviceOrientationPortraitUpsideDown :
            return @"UIDeviceOrientationPortraitUpsideDown";

        default:
            return [NSString stringWithFormat:
                    @"UIDeviceOrientation(%d)",
                    value];
    }
}

NSString *NSStringFromUIInterfaceOrientation (UIInterfaceOrientation value)
{
    switch (value)
    {
        case UIInterfaceOrientationLandscapeLeft :
            return @"UIInterfaceOrientationLandscapeLeft";

        case UIInterfaceOrientationLandscapeRight :
            return @"UIInterfaceOrientationLandscapeRight";

        case UIInterfaceOrientationPortrait :
            return @"UIInterfaceOrientationPortrait";

        case UIInterfaceOrientationPortraitUpsideDown :
            return @"UIInterfaceOrientationPortraitUpsideDown";

        default:
            return [NSString stringWithFormat:
                    @"UIInterfaceOrientation(%d)",
                    value];
    }
}

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

- (NSString *) stringByTransforming: (NSStringTransform) transform
{
    return [self stringByTransforming: transform
                                range: NSMakeRange (0, self.length)
                              reverse: NO];
}

- (NSString *) stringByTransforming: (NSStringTransform) transform
                              range: (NSRange) range
{
    return [self stringByTransforming: transform
                                range: range
                              reverse: NO];
}

- (NSString *) stringByTransforming: (NSStringTransform) transform
                              range: (NSRange) range
                            reverse: (BOOL) reverse
{
    static CFStringRef Transforms [NSStringTransformToXMLHex + 1];

    if (!Transforms [0])
    {
        Transforms [NSStringTransformFullwidthHalfwidth]  = kCFStringTransformFullwidthHalfwidth;
        Transforms [NSStringTransformHiraganaKatakana]    = kCFStringTransformHiraganaKatakana;
        Transforms [NSStringTransformLatinArabic]         = kCFStringTransformLatinArabic;
        Transforms [NSStringTransformLatinCyrillic]       = kCFStringTransformLatinCyrillic;
        Transforms [NSStringTransformLatinGreek]          = kCFStringTransformLatinGreek;
        Transforms [NSStringTransformLatinHangul]         = kCFStringTransformLatinHangul;
        Transforms [NSStringTransformLatinHebrew]         = kCFStringTransformLatinHebrew;
        Transforms [NSStringTransformLatinHiragana]       = kCFStringTransformLatinHiragana;
        Transforms [NSStringTransformLatinKatakana]       = kCFStringTransformLatinKatakana;
        Transforms [NSStringTransformLatinThai]           = kCFStringTransformLatinThai;
        Transforms [NSStringTransformMandarinLatin]       = kCFStringTransformMandarinLatin;
        Transforms [NSStringTransformStripCombiningMarks] = kCFStringTransformStripCombiningMarks;
        Transforms [NSStringTransformStripDiacritics]     = kCFStringTransformStripDiacritics;
        Transforms [NSStringTransformToLatin]             = kCFStringTransformToLatin;
        Transforms [NSStringTransformToUnicodeName]       = kCFStringTransformToUnicodeName;
        Transforms [NSStringTransformToXMLHex]            = kCFStringTransformToXMLHex;
    }

    NSMutableString *tmpString = [[self mutableCopy] autorelease];
    CFRange          tmpRange = CFRangeMake (range.location,
                                             range.length);

    if (CFStringTransform ((CFMutableStringRef) tmpString,
                           &tmpRange,
                           Transforms [transform],
                           reverse))
        return tmpString;

    return self;
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
