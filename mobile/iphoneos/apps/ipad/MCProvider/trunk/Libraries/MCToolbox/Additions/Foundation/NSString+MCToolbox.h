//
//  NSString+MCToolbox.h
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

#import <Foundation/Foundation.h>
#import <UIKit/UIApplication.h>

typedef enum
{
    NSStringTransformFullwidthHalfwidth = 0,
    NSStringTransformHiraganaKatakana,
    NSStringTransformLatinArabic,
    NSStringTransformLatinCyrillic,
    NSStringTransformLatinGreek,
    NSStringTransformLatinHangul,
    NSStringTransformLatinHebrew,
    NSStringTransformLatinHiragana,
    NSStringTransformLatinKatakana,
    NSStringTransformLatinThai,
    NSStringTransformMandarinLatin,
    NSStringTransformStripCombiningMarks,
    NSStringTransformStripDiacritics,
    NSStringTransformToLatin,
    NSStringTransformToUnicodeName,
    NSStringTransformToXMLHex
} NSStringTransform;

FOUNDATION_EXPORT NSString *NSStringFromBOOL (BOOL value);
FOUNDATION_EXPORT NSString *NSStringFromNSTimeInterval (NSTimeInterval value);
FOUNDATION_EXPORT NSString *NSStringFromUIDeviceOrientation (UIDeviceOrientation value);
FOUNDATION_EXPORT NSString *NSStringFromUIInterfaceOrientation (UIInterfaceOrientation value);

/**
 * The MCToolbox library adds programming interfaces to the NSString class of
 * the Foundation framework to ...
 */
@interface NSString (MCToolbox)

+ (NSArray *) allUTIsForMIMEType: (NSString *) MIMEType;

+ (NSArray *) allUTIsForMIMEType: (NSString *) MIMEType
                 conformingToUTI: (NSString *) UTI;

+ (NSArray *) allUTIsForPathExtension: (NSString *) pathExt;

+ (NSArray *) allUTIsForPathExtension: (NSString *) pathExt
                      conformingToUTI: (NSString *) UTI;

+ (NSString *) IANACharSetNameForStringEncoding: (NSStringEncoding) enc;

+ (NSString *) preferredMIMETypeForUTI: (NSString *) UTI;

+ (NSString *) preferredPathExtensionForUTI: (NSString *) UTI;

+ (NSString *) preferredUTIForMIMEType: (NSString *) MIMEType;

+ (NSString *) preferredUTIForMIMEType: (NSString *) MIMEType
                       conformingToUTI: (NSString *) UTI;

+ (NSString *) preferredUTIForPathExtension: (NSString *) pathExt;

+ (NSString *) preferredUTIForPathExtension: (NSString *) pathExt
                            conformingToUTI: (NSString *) UTI;

+ (NSStringEncoding) stringEncodingForIANACharSetName: (NSString *) name;

- (BOOL) conformsToUTI: (NSString *) UTI;

- (BOOL) isEqualToUTI: (NSString *) UTI;

- (NSString *) stringByTransforming: (NSStringTransform) transform;

- (NSString *) stringByTransforming: (NSStringTransform) transform
                              range: (NSRange) range;

- (NSString *) stringByTransforming: (NSStringTransform) transform
                              range: (NSRange) range
                            reverse: (BOOL) reverse;

- (NSString *) stringByTrimmingWhitespace;

- (NSString *) stringByURLDecoding;

- (NSString *) stringByURLEncoding;

- (NSString *) UTIDescription;

@end
