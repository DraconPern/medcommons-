//
//  NSString+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

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

- (NSString *) stringByTrimmingWhitespace;

- (NSString *) stringByURLDecoding;

- (NSString *) stringByURLEncoding;

- (NSString *) UTIDescription;

@end
