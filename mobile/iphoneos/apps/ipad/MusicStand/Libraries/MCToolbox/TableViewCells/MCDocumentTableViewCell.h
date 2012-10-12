//
//  DocumentTableViewCell.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

#ifndef FILE_SIZE_DEFINED
typedef unsigned long long FileSize;

#define FILE_SIZE_DEFINED 1
#endif

@class MCDocument;

@interface MCDocumentTableViewCell : UITableViewCell
{
@private

    NSDateFormatter *dateFormatter_;
    UILabel         *dateLabel_;
    MCDocument      *document_;
    UILabel         *nameLabel_;
    UILabel         *sizeLabel_;
    UILabel         *typeLabel_;
    //
    // Flags:
    //
    BOOL             alwaysFormatsSizeInBytes_;
    BOOL             formatsSizeInBinaryUnits_;
    BOOL             usesCreationDate_;
    BOOL             usesPath_;
}

@property (nonatomic, assign, readwrite) BOOL             alwaysFormatsSizeInBytes;
@property (nonatomic, retain, readwrite) NSDateFormatter *dateFormatter;
@property (nonatomic, retain, readonly)  UILabel         *dateLabel;
@property (nonatomic, retain, readwrite) MCDocument      *document;
@property (nonatomic, assign, readwrite) BOOL             formatsSizeInBinaryUnits;
@property (nonatomic, retain, readonly)  UILabel         *nameLabel;
@property (nonatomic, retain, readonly)  UILabel         *sizeLabel;
@property (nonatomic, retain, readonly)  UILabel         *typeLabel;
@property (nonatomic, assign, readwrite) BOOL             usesCreationDate;
@property (nonatomic, assign, readwrite) BOOL             usesPath;

- (NSString *) formatDate: (NSDate *) date;

- (NSString *) formatName: (NSString *) name;

- (NSString *) formatSize: (FileSize) size;

- (NSString *) formatType: (NSString *) type;

- (id) initWithReuseIdentifier: (NSString *) reuseIdentifier;

@end
