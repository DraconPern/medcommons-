//
//  MCDocument.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/23/10.
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

#import "MCDocument.h"
#import "NSString+MCToolbox.h"

#pragma mark -
#pragma mark Public Class MCDocument
#pragma mark -

@interface MCDocument ()

@property (nonatomic, retain, readonly) NSDictionary *attributes;

@end

@implementation MCDocument

@synthesize attributes       = attributes_;
@dynamic    creationDate;
@dynamic    modificationDate;
@synthesize path             = path_;
@dynamic    size;
@synthesize title            = title_;
@dynamic    URL;
@synthesize UTI              = UTI_;

#pragma mark Public Class Methods

+ (id) documentWithPath: (NSString *) path
             attributes: (NSDictionary *) attrs
{
    return [[[self alloc] initWithPath: path
                            attributes: attrs]
            autorelease];
}

#pragma mark Public Instance Methods

- (NSDate *) creationDate
{
    return [self.attributes fileCreationDate];
}

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attrs
{
    NSAssert (path != nil,
              @"Nil path!");

    NSAssert (attrs != nil,
              @"Nil attributes");

    NSAssert ([[attrs fileType] isEqualToString: NSFileTypeRegular],
              @"Not a regular file!");

    self = [super init];

    if (self)
    {
        self->attributes_ = [attrs retain];
        self->path_ = [path copy];
    }

    return self;
}

- (NSDate *) modificationDate
{
    return [self.attributes fileModificationDate];
}

- (FileSize) size
{
    return [self.attributes fileSize];
}

- (NSString *) title
{
    if (!self->title_)
        self->title_ = [[[self.path lastPathComponent]
                         stringByDeletingPathExtension]
                        retain];

    return self->title_;
}

- (NSURL *) URL
{
    return [NSURL fileURLWithPath: self.path
                      isDirectory: NO];
}

- (NSString *) UTI
{
    if (!self->UTI_)
        self->UTI_ = [[NSString preferredUTIForPathExtension: [self.path pathExtension]]
                      retain];

    return self->UTI_;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->attributes_ release];
    [self->path_ release];
    [self->title_ release];
    [self->UTI_ release];

    [super dealloc];
}

@end
