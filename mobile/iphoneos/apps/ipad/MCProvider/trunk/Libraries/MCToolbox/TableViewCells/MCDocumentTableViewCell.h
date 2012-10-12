//
//  DocumentTableViewCell.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/11/10.
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

#import "MCTableViewCell.h"

#ifndef FILE_SIZE_DEFINED
typedef unsigned long long FileSize;

#define FILE_SIZE_DEFINED 1
#endif

@class MCDocument;

/**
 * The MCDocumentTableViewCell class extends UITableViewCell ...
 */
@interface MCDocumentTableViewCell : MCTableViewCell
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
