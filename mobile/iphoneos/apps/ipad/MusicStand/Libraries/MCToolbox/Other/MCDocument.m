//
//  MCDocument.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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

 //   NSAssert ([[attrs fileType] isEqualToString: NSFileTypeRegular],
//

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
