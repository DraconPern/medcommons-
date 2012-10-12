//
//  MCDocument.h
//  MCToolbox
//
//  Created by J. G. Pusey on 6/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#ifndef FILE_SIZE_DEFINED
typedef unsigned long long FileSize;

#define FILE_SIZE_DEFINED 1
#endif

@interface MCDocument : NSObject
{
@private

    NSDictionary *attributes_;
    NSString     *path_;
    NSString     *title_;
    NSString     *UTI_;
}

@property (nonatomic, retain, readonly)  NSDate   *creationDate;
@property (nonatomic, retain, readonly)  NSDate   *modificationDate;
@property (nonatomic, copy,   readonly)  NSString *path;
@property (nonatomic, assign, readonly)  FileSize  size;
@property (nonatomic, copy,   readwrite) NSString *title;
@property (nonatomic, retain, readonly)  NSURL    *URL;
@property (nonatomic, copy,   readwrite) NSString *UTI;

+ (id) documentWithPath: (NSString *) path
             attributes: (NSDictionary *) attrs;

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attrs;

@end
